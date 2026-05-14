/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.ktmongo.dsl

import de.infix.testBalloon.framework.shared.TestDisplayName
import de.infix.testBalloon.framework.shared.TestElementName
import de.infix.testBalloon.framework.shared.TestRegistering
import kotlinx.coroutines.currentCoroutineContext
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.PreparedDslMarker
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.config.Context
import opensavvy.prepared.suite.config.TestConfig
import opensavvy.prepared.suite.config.plus
import opensavvy.prepared.suite.prepared
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Parameterizes each test by one of the [testBsonFactory] instances.
 */
@PreparedDslMarker
@TestRegistering
fun multiContextSuite(
	@TestElementName name: String = "",
	@TestDisplayName displayName: String = name,
	config: TestConfig = TestConfig.Empty,
	content: SuiteDsl.() -> Unit,
) = preparedSuite(name, displayName, preparedConfig = config) {
	MultiContextSuite(this).content()
}

private class MultiContextSuite(private val upstream: SuiteDsl) : SuiteDsl {
	override fun suite(name: String, config: TestConfig, block: SuiteDsl.() -> Unit) {
		upstream.suite(name, config) {
			MultiContextSuite(this).block()
		}
	}

	override fun test(name: String, config: TestConfig, block: suspend TestDsl.() -> Unit) {
		for ((factoryName, factory) in testFactories) {
			upstream.test("$name [$factoryName]", config + Context(CurrentBsonFactory(factory)), block)
		}
	}
}

private class CurrentBsonFactory(val factory: () -> BsonFactory) : AbstractCoroutineContextElement(CurrentBsonFactory) {
	companion object : CoroutineContext.Key<CurrentBsonFactory>
}

/**
 * The different [BsonFactory] implementations that should be used for the test.
 *
 * The ksys are a human-readable name of the implementation, appended to the test names.
 *
 * To access the current factory, declare your tests with [multiContextSuite], then access the
 * current factory within a test using [testBsonFactory].
 */
expect val testFactories: Map<String, () -> BsonFactory>

/**
 * The current [BsonFactory].
 *
 * This value is only available within tests declared as [multiContextSuite].
 */
val testBsonFactory by prepared {
	val context = currentCoroutineContext()[CurrentBsonFactory]
		?: error("Could not access this test's BsonFactory. Is it declared with multiContextSuite?")

	context.factory()
}
