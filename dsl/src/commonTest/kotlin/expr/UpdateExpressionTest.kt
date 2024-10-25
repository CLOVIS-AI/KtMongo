/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.expr

import opensavvy.prepared.runner.kotest.PreparedSpec

private class Friend(
	val id: String,
	val name: String,
	val money: Float,
)

private class User2(
	val id: String,
	val name: String,
	val age: Int?,
	val money: Double,
	val bestFriend: Friend,
	val friends: List<Friend>,
)

private fun update(block: UpdateExpression<User2>.() -> Unit): String =
	UpdateExpression<User2>(testContext()).apply(block).toString()

private val set = "\$set"
private val setOnInsert = "\$setOnInsert"
private val inc = "\$inc"
private val unset = "\$unset"
private val rename = "\$rename"

class UpdateExpressionTest : PreparedSpec({

	test("Empty update") {
		update { } shouldBeBson "{}"
	}

	suite("Operator $set") {
		test("Single field") {
			update {
				User2::age set 18
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
				User2::bestFriend / Friend::name set "foo"
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
				User2::age set 18
				User2::name set "foo"
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
			update {
				User2::age setOnInsert 18
			} shouldBeBson """
				{
					"$setOnInsert": {
						"age": 18
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User2::bestFriend / Friend::name setOnInsert "foo"
			} shouldBeBson """
				{
					"$setOnInsert": {
						"bestFriend.name": "foo"
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User2::age setOnInsert 18
				User2::name setOnInsert "foo"
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
				User2::money inc 18.0
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
				User2::bestFriend / Friend::money inc -12.9f
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
				User2::money inc 5.2
				User2::bestFriend / Friend::money inc -5.2f
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

	suite("Operator $unset") {
		test("Single field") {
			update {
				User2::money.unset()
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
				(User2::bestFriend / Friend::money).unset()
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
				User2::money.unset()
				User2::bestFriend.unset()
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
				User2::bestFriend / Friend::name renameTo User2::name
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
				User2::bestFriend / Friend::name renameTo User2::name
				User2::friends[0] / Friend::name renameTo User2::friends[1] / Friend::name
			} shouldBeBson """
				{
					"$rename": {
						"bestFriend.name": "name",
						"friends.$0.name": "friends.$1.name"
					}
				}
			""".trimIndent()
		}
	}

})
