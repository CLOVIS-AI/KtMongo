/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.options.options

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.FindOptions
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.kotest.PreparedSpec
import org.bson.types.ObjectId

@LowLevelApi
class SortTest : PreparedSpec({

	class Target(
		val _id: ObjectId,
		val name: String,
		val age: Int,
	)

	test("Sort by ID (ascending)") {
		val options = FindOptions<Target>(testContext())

		options.sort {
			ascending(Target::_id)
		}

		options.toString() shouldBeBson """
			{
				"sort": {
					"_id": 1
				}
			}
		""".trimIndent()
	}

	test("Sort by ID (descending)") {
		val options = FindOptions<Target>(testContext())

		options.sort {
			descending(Target::_id)
		}

		options.toString() shouldBeBson """
			{
				"sort": {
					"_id": -1
				}
			}
		""".trimIndent()
	}

	test("Sort by multiple fields") {
		val options = FindOptions<Target>(testContext())

		options.sort {
			ascending(Target::name)
			descending(Target::_id)
			ascending(Target::age)
		}

		// It's important that the order is the same
		options.toString() shouldBeBson """
			{
				"sort": {
					"name": 1,
					"_id": -1,
					"age": 1
				}
			}
		""".trimIndent()
	}

	test("Empty sort is simplified away") {
		val options = FindOptions<Target>(testContext())

		options.sort {}

		options.toString() shouldBeBson """
			{
			}
		""".trimIndent()
	}

})
