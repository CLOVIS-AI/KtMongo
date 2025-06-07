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

@file:OptIn(ExperimentalTime::class)

package opensavvy.ktmongo.bson.types

import io.kotest.assertions.throwables.shouldThrow
import opensavvy.prepared.runner.kotest.PreparedSpec
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

})
