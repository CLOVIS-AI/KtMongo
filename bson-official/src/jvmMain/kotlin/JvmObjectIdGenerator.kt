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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import kotlin.time.ExperimentalTime

private object JvmObjectIdGenerator : ObjectIdGenerator {
	@ExperimentalTime
	override fun newId(): ObjectId {
		val id = org.bson.types.ObjectId()
		return ObjectId.fromBytes(
			// Yes, this byte array is wasted memory and GC pressure.
			// It is necessary because the Java driver doesn't provide a way to access the nonce
			// more efficiently.
			id.toByteArray()
		)
	}
}

/**
 * An [ObjectIdGenerator] instance that uses the Java driver's [org.bson.types.ObjectId]'s algorithm.
 *
 * This generation algorithm is slightly different from [ObjectIdGenerator.Default].
 * Here are a few differences:
 *
 * |                                       | ObjectIdGenerator.Jvm        | ObjectIdGenerator.Default |
 * |---------------------------------------|------------------------------|----|
 * | Maximum number of ObjectId per second | ≈16 million                  | ≈1 billion billion |
 * | Random source                         | [java.security.SecureRandom] | [kotlin.random.Random] |
 * | Testability                           | None                         | Can inject a clock and a random source to deterministically generate tests |
 */
@Suppress("FunctionName", "GrazieInspection")
fun ObjectIdGenerator.Companion.Jvm(): ObjectIdGenerator =
	JvmObjectIdGenerator
