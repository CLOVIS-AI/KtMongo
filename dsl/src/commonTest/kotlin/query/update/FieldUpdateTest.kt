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

package opensavvy.ktmongo.dsl.query.update

import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.prepared.runner.kotest.PreparedSpec

class FieldUpdateTest : PreparedSpec({
	suite("Operator $set") {
		test("Single field") {
			update {
				User::age set 18
			} shouldBeBson """
				{
					"$set": {
						"age": 18
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::name set "foo"
			} shouldBeBson """
				{
					"$set": {
						"bestFriend.name": "foo"
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::age set 18
				User::name set "foo"
			} shouldBeBson """
				{
					"$set": {
						"age": 18,
						"name": "foo"
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $setOnInsert") {
		test("Single field") {
			upsert {
				User::age setOnInsert 18
			} shouldBeBson """
				{
					"$setOnInsert": {
						"age": 18
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			upsert {
				User::bestFriend / Friend::name setOnInsert "foo"
			} shouldBeBson """
				{
					"$setOnInsert": {
						"bestFriend.name": "foo"
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			upsert {
				User::age setOnInsert 18
				User::name setOnInsert "foo"
			} shouldBeBson """
				{
					"$setOnInsert": {
						"age": 18,
						"name": "foo"
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $inc") {
		test("Single field") {
			update {
				User::money inc 18.0
			} shouldBeBson """
				{
					"$inc": {
						"money": 18.0
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::money inc -12.9f
			} shouldBeBson """
				{
					"$inc": {
						"bestFriend.money": -12.899999618530273
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::money += 5.2
				User::bestFriend / Friend::money += -5.2f
			} shouldBeBson """
				{
					"$inc": {
						"money": 5.2,
						"bestFriend.money": -5.199999809265137
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $mul") {
		test("Single field") {
			update {
				User::money mul 18.0
			} shouldBeBson """
				{
					"$mul": {
						"money": 18.0
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::money mul -12.9f
			} shouldBeBson """
				{
					"$mul": {
						"bestFriend.money": -12.899999618530273
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::money mul 5.2
				User::bestFriend / Friend::money mul -5.2f
			} shouldBeBson """
				{
					"$mul": {
						"money": 5.2,
						"bestFriend.money": -5.199999809265137
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $unset") {
		test("Single field") {
			update {
				User::money.unset()
			} shouldBeBson """
				{
					"$unset": {
						"money": true
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				(User::bestFriend / Friend::money).unset()
			} shouldBeBson """
				{
					"$unset": {
						"bestFriend.money": true
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::money.unset()
				User::bestFriend.unset()
			} shouldBeBson """
				{
					"$unset": {
						"money": true,
						"bestFriend": true
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $rename") {
		test("Single and nested field") {
			update {
				User::bestFriend / Friend::name renameTo User::name
			} shouldBeBson """
				{
					"$rename": {
						"bestFriend.name": "name"
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::bestFriend / Friend::name renameTo User::name
				User::friends[0] / Friend::name renameTo User::friends[1] / Friend::name
			} shouldBeBson """
				{
					"$rename": {
						"bestFriend.name": "name",
						"friends.0.name": "friends.1.name"
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $min") {
		test("Single field") {
			update {
				User::age min 10
			} shouldBeBson """
				{
					"$min": {
						"age": 10
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::money min 5.0f
			} shouldBeBson """
				{
					"$min": {
						"bestFriend.money": 5.0
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::age min 10
				User::bestFriend / Friend::money min 5.0f
			} shouldBeBson """
				{
					"$min": {
						"age": 10,
						"bestFriend.money": 5.0
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $max") {
		test("Single field") {
			update {
				User::age max 100
			} shouldBeBson """
				{
					"$max": {
						"age": 100
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::money max 1000.0f
			} shouldBeBson """
				{
					"$max": {
						"bestFriend.money": 1000.0
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::age max 100
				User::bestFriend / Friend::money max 1000.0f
			} shouldBeBson """
				{
					"$max": {
						"age": 100,
						"bestFriend.money": 1000.0
					}
				}
			""".trimIndent()
		}
	}
})
