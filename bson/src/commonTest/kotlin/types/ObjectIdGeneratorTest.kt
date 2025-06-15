/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class)

package opensavvy.ktmongo.bson.types

import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import opensavvy.ktmongo.bson.types.ObjectId.Companion.COUNTER_BOUND
import opensavvy.ktmongo.bson.types.ObjectId.Companion.PROCESS_ID_BOUND
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.clock
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.time
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.ExperimentalTime

class ObjectIdGeneratorTest : PreparedSpec({

	suite("Hardcoded generator") {

		test("Generate multiple elements") {
			val generator = ObjectIdGenerator.Hardcoded(
				ObjectId.fromHex("68440c96dd675c605ee0e275"),
				ObjectId.fromHex("68440cac8fd4c1c0ed08ebe2"),
				ObjectId.fromHex("68440caf38bc1eeca63ee5a0"),
			)

			check(generator.newId() == ObjectId.fromHex("68440c96dd675c605ee0e275"))
			check(generator.newId() == ObjectId.fromHex("68440cac8fd4c1c0ed08ebe2"))
			check(generator.newId() == ObjectId.fromHex("68440caf38bc1eeca63ee5a0"))

			shouldThrow<NoSuchElementException> { generator.newId() }
		}

	}

	suite("Default generator") {
		test("Hunt for duplicates generated for a given instant") {
			val generator = ObjectIdGenerator.Default(
				clock = time.clock,
				random = random.accessUnsafe(),
			)

			val previous = HashSet<ObjectId>()

			// In theory, we should be able to generate COUNTER_BOUND * PROCESS_ID_BOUND IDs in a single instant.
			// However, that is *way* more than what can fit in RAM via the 'previous' HashSet.
			// Since it's not possible to actually test the algorithm completely, we settle with testing
			// a large number of generations (still in a single instant). Since each run
			// starts from a different offset, hopefully this should be strong enough to detect anomalies.
			repeat(1_000_000) { index ->
				try {
					val id = generator.newId()
					if (id in previous) throw AssertionError("Duplicate ID $id after generating $index IDs (counter bound: $COUNTER_BOUND, process ID bound: $PROCESS_ID_BOUND, ${index * 1.0 / COUNTER_BOUND} rounds)")
					previous += id
				} catch (e: Error) {
					previous.clear() // Free the memory to ensure the next line can succeed
					println("Failed after $index generations (counter bound: $COUNTER_BOUND, process ID bound: $PROCESS_ID_BOUND, ${index * 1.0 / COUNTER_BOUND} rounds)")
					throw e
				}
			}
		}
	}
})
