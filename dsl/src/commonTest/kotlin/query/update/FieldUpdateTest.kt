/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

@file:OptIn(ExperimentalTime::class, LowLevelApi::class)

package opensavvy.ktmongo.dsl.query.update

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.query.shouldBeBson
import kotlin.time.ExperimentalTime

val FieldUpdateTest by multiContextSuite {
	suite($$"Operator $set") {
		test("Single field") {
			update {
				User::age set 18
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
				{
					"$set": {
						"age": 18,
						"name": "foo"
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $setOnInsert") {
		test("Single field") {
			upsert {
				User::age setOnInsert 18
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
				{
					"$setOnInsert": {
						"age": 18,
						"name": "foo"
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $inc") {
		test("Single field") {
			update {
				User::money inc 18.0
			} shouldBeBson $$"""
				{
					"$inc": {
						"money": 18.0
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::money inc -10.25f
			} shouldBeBson $$"""
				{
					"$inc": {
						"bestFriend.money": -10.25
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::money += 5.2
				User::bestFriend / Friend::money += -1.125f
			} shouldBeBson $$"""
				{
					"$inc": {
						"money": 5.2,
						"bestFriend.money": -1.125
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $mul") {
		test("Single field") {
			update {
				User::money mul 18.0
			} shouldBeBson $$"""
				{
					"$mul": {
						"money": 18.0
					}
				}
			""".trimIndent()
		}

		test("Nested field") {
			update {
				User::bestFriend / Friend::money mul -10.25f
			} shouldBeBson $$"""
				{
					"$mul": {
						"bestFriend.money": -10.25
					}
				}
			""".trimIndent()
		}

		test("Multiple fields") {
			update {
				User::money mul 5.2
				User::bestFriend / Friend::money mul -1.125f
			} shouldBeBson $$"""
				{
					"$mul": {
						"money": 5.2,
						"bestFriend.money": -1.125
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $unset") {
		test("Single field") {
			update {
				User::money.unset()
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
				{
					"$unset": {
						"money": true,
						"bestFriend": true
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $rename") {
		test("Single and nested field") {
			update {
				User::bestFriend / Friend::name renameTo User::name
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
				{
					"$rename": {
						"bestFriend.name": "name",
						"friends.0.name": "friends.1.name"
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $min") {
		test("Single field") {
			update {
				User::age min 10
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
				{
					"$min": {
						"age": 10,
						"bestFriend.money": 5.0
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $max") {
		test("Single field") {
			update {
				User::age max 100
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
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
			} shouldBeBson $$"""
				{
					"$max": {
						"age": 100,
						"bestFriend.money": 1000.0
					}
				}
			""".trimIndent()
		}
	}

	suite("Array operators") {
		suite($$"$addToSet") {
			test("Add a single field") {
				update {
					User::tokens addToSet "123"
				} shouldBeBson $$"""
					{
						"$addToSet": {
							"tokens": "123"
						}
					}
				""".trimIndent()
			}

			test("Add multiple fields") {
				update {
					User::tokens addToSet "123"
					User::scores addToSet 1
				} shouldBeBson $$"""
					{
						"$addToSet": {
							"tokens": "123",
							"scores": 1
						}
					}
				""".trimIndent()
			}

			test("Add multiple values to the same field") {
				update {
					User::tokens addToSet "123"
					User::tokens addToSet "456"
				} shouldBeBson $$"""
					{
						"$addToSet": {
							"tokens": {
								"$each": [
									"123",
									"456"
								]
							}
						}
					}
				""".trimIndent()
			}

			test("Add multiple values to the same field using a list") {
				update {
					User::tokens addEachToSet listOf("123", "456")
				} shouldBeBson $$"""
					{
						"$addToSet": {
							"tokens": {
								"$each": [
									"123",
									"456"
								]
							}
						}
					}
				""".trimIndent()
			}
		}
	}

	suite($$"$push") {
		test("Add a single field") {
			update {
				User::tokens push "123"
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": "123"
					}
				}
			""".trimIndent()
		}

		test("Add multiple fields") {
			update {
				User::tokens push "123"
				User::scores push 1
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": "123",
						"scores": 1
					}
				}
			""".trimIndent()
		}

		test("Add multiple values to the same field") {
			update {
				User::tokens push "123"
				User::tokens push "456"
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": {
							"$each": [
								"123",
								"456"
							]
						}
					}
				}
			""".trimIndent()
		}

		test("Add multiple values to the same field using a list") {
			update {
				User::tokens pushEach listOf("123", "456")
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": {
							"$each": [
								"123",
								"456"
							]
						}
					}
				}
			""".trimIndent()
		}

		test("Add values with slice") {
			update {
				User::tokens push {
					each("123", "456")
					slice(3)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": {
							"$each": [
								"123",
								"456"
							],
							"$slice": 3
						}
					}
				}
			""".trimIndent()
		}

		test("Use slice without each") {
			update {
				User::tokens push {
					slice(3)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": {
							"$each": [],
							"$slice": 3
						}
					}
				}
			""".trimIndent()
		}

		test("Combine both syntaxes together") {
			update {
				User::tokens push "foo"
				User::tokens push {
					each("123")
					slice(5)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": {
							"$each": [
								"foo",
								"123"
							],
							"$slice": 5
						}
					}
				}
			""".trimIndent()
		}

		test("Using the advanced syntax multiple times: each are combined, slice only takes the last one") {
			update {
				User::tokens push {
					each("foo", "bar")
					slice(2)
				}

				User::tokens push {
					each("baz")
					slice(1)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"tokens": {
							"$each": [
								"foo",
								"bar",
								"baz"
							],
							"$slice": 1
						}
					}
				}
			""".trimIndent()
		}

		test("An empty block is removed") {
			update {
				User::tokens push { }
			} shouldBeBson $$"""
				{
				}
			""".trimIndent()
		}

		test("Add values at the beginning with position 0") {
			update {
				User::scores push {
					each(50, 60, 70)
					position(0)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"scores": {
							"$each": [
								50,
								60,
								70
							],
							"$position": 0
						}
					}
				}
			""".trimIndent()
		}

		test("Add values at middle position") {
			update {
				User::scores push {
					each(20, 30)
					position(2)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"scores": {
							"$each": [
								20,
								30
							],
							"$position": 2
						}
					}
				}
			""".trimIndent()
		}

		test("Add values with negative position") {
			update {
				User::scores push {
					each(90, 80)
					position(-2)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"scores": {
							"$each": [
								90,
								80
							],
							"$position": -2
						}
					}
				}
			""".trimIndent()
		}

		test("Add values with position, each, and slice") {
			update {
				User::scores push {
					each(10, 20, 30)
					position(1)
					slice(5)
				}
			} shouldBeBson $$"""
				{
					"$push": {
						"scores": {
							"$each": [
								10,
								20,
								30
							],
							"$slice": 5,
							"$position": 1
						}
					}
				}
			""".trimIndent()
		}

		test("Use position without each") {
			update {
				User::tokens push {
					// 'position' does nothing without 'each', so it should be entirely removed
					position(0)
				}
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}

		test("Sort simple values in ascending order") {
			update {
				User::scores push {
					each(40, 60)
					sort {
						ascending()
					}
				}
			} shouldBeBson $$"""
					{
						"$push": {
							"scores": {
								"$each": [
									40,
									60
								],
								"$sort": 1
							}
						}
					}
				""".trimIndent()
		}

		test("Sort simple values in descending order") {
			update {
				User::scores push {
					each(40, 60)
					sort {
						descending()
					}
				}
			} shouldBeBson $$"""
					{
						"$push": {
							"scores": {
								"$each": [
									40,
									60
								],
								"$sort": -1
							}
						}
					}
				""".trimIndent()
		}

		test("Sort documents by field in ascending order") {
			update {
				User::friends push {
					each(Friend("1", "Alice", 1000.0f), Friend("2", "Bob", 2000.0f))
					sort { ascending(Friend::name) }
				}
			} shouldBeBson $$"""
					{
						"$push": {
							"friends": {
								"$each": [
									{
										"id": "1",
										"name": "Alice",
										"money": 1000.0
									},
									{
										"id": "2",
										"name": "Bob",
										"money": 2000.0
									}
								],
								"$sort": {
									"name": 1
								}
							}
						}
					}
				""".trimIndent()
		}

		test("Sort documents by field in descending order") {
			update {
				User::friends push {
					each(Friend("1", "Alice", 1000.0f), Friend("2", "Bob", 2000.0f))
					sort { descending(Friend::money) }
				}
			} shouldBeBson $$"""
					{
						"$push": {
							"friends": {
								"$each": [
									{
										"id": "1",
										"name": "Alice",
										"money": 1000.0
									},
									{
										"id": "2",
										"name": "Bob",
										"money": 2000.0
									}
								],
								"$sort": {
									"money": -1
								}
							}
						}
					}
				""".trimIndent()
		}

		test("Sort with empty array (sort only)") {
			update {
				User::scores push {
					sort {
						descending()
					}
				}
			} shouldBeBson $$"""
					{
						"$push": {
							"scores": {
								"$each": [],
								"$sort": -1
							}
						}
					}
				""".trimIndent()
		}

		test("Sort combined with slice and position") {
			update {
				User::friends push {
					each(Friend("1", "Alice", 1000.0f), Friend("2", "Bob", 2000.0f), Friend("3", "Charlie", 500.0f))
					sort { descending(Friend::name) }
					slice(2)
					position(0)
				}
			} shouldBeBson $$"""
					{
						"$push": {
							"friends": {
								"$each": [
									{
										"id": "1",
										"name": "Alice",
										"money": 1000.0
									},
									{
										"id": "2",
										"name": "Bob",
										"money": 2000.0
									},
									{
										"id": "3",
										"name": "Charlie",
										"money": 500.0
									}
								],
								"$slice": 2,
								"$position": 0,
								"$sort": {
									"name": -1
								}
							}
						}
					}
				""".trimIndent()
		}
	}

	suite($$"$currentDate") {
		test("Set to instant") {
			update {
				User::creationInstant.setToCurrentDate()
			} shouldBeBson $$"""
				{
					"$currentDate": {
						"creationInstant": true
					}
				}
			""".trimIndent()
		}

		test("Set to timestamp") {
			update {
				User::modificationTimestamp.setToCurrentDate()
			} shouldBeBson $$"""
				{
					"$currentDate": {
						"modificationTimestamp": {
							"$type": "timestamp"
						}
					}
				}
			""".trimIndent()
		}

		test("Multiple usages") {
			update {
				User::creationInstant.setToCurrentDate()
				User::modificationTimestamp.setToCurrentDate()
			} shouldBeBson $$"""
				{
					"$currentDate": {
						"creationInstant": true,
						"modificationTimestamp": {
							"$type": "timestamp"
						}
					}
				}
			""".trimIndent()
		}
	}

	suite("Array filters") {
		test("Explicit array filter") {
			update {
				User::friends.filter("best") / Friend::money += 0.5f
			} shouldBeBson $$"""
				{
					"$inc": {
						"friends.$[best].money": 0.5
					}
				}
			""".trimIndent()
		}
	}
}
