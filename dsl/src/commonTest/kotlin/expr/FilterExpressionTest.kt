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

import opensavvy.ktmongo.bson.types.BsonType
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.expr.filter.Pet
import opensavvy.ktmongo.dsl.expr.filter.User
import opensavvy.ktmongo.dsl.expr.filter.filter
import opensavvy.prepared.runner.kotest.PreparedSpec

class FilterExpressionTest : PreparedSpec({

	val eq = "\$eq"
	val ne = "\$ne"
	val and = "\$and"
	val or = "\$or"
	val exists = "\$exists"
	val type = "\$type"
	val not = "\$not"
	val isOneOf = "\$in"
	val gtOp = "\$gt"
	val gte = "\$gte"
	val lt = "\$lt"
	val lteOp = "\$lte"
	val all = "\$all"
	val oid = "\$oid"

	suite("Operator $eq") {
		test("Integer") {
			filter {
				User::age eq 5
			} shouldBeBson """
				{
					"age": {
						"$eq": 5
					}
				}
			""".trimIndent()
		}

		test("Null") {
			filter {
				User::age eq null
			} shouldBeBson """
				{
					"age": {
						"$eq": null
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $ne") {
		test("Integer") {
			filter {
				User::age ne 12
			} shouldBeBson """
				{
					"age": {
						"$ne": 12
					}
				}
			""".trimIndent()
		}

		test("Null") {
			filter {
				User::age ne null
			} shouldBeBson """
				{
					"age": {
						"$ne": null
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $isOneOf") {
		test("With 0 elements") {
			filter {
				User::name.isOneOf()
			} shouldBeBson """
				{
					"name": {
						"$isOneOf": [
						]
					}
				}
			""".trimIndent()
		}

		test("With 1 element") {
			filter {
				User::name.isOneOf("Alfred")
			} shouldBeBson """
				{
					"name": {
						"$isOneOf": [
							"Alfred"
						]
					}
				}
			""".trimIndent()
		}

		test("With multiple elements") {
			filter {
				User::name.isOneOf("Alfred", "Arthur", "Annabelle")
			} shouldBeBson """
				{
					"name": {
						"$isOneOf": [
							"Alfred",
							"Arthur",
							"Annabelle"
						]
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $exists") {
		test("Exists") {
			filter {
				User::age.exists()
			} shouldBeBson """
				{
					"age": {
						"$exists": true
					}
				}
			""".trimIndent()
		}

		test("Does not exist") {
			filter {
				User::age.doesNotExist()
			} shouldBeBson """
				{
					"age": {
						"$exists": false
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $type") {
		test("String") {
			filter {
				User::age hasType BsonType.String
			} shouldBeBson """
				{
					"age": {
						"$type": 2
					}
				}
			""".trimIndent()
		}

		test("Null") {
			filter {
				User::age hasType BsonType.Null
			} shouldBeBson """
				{
					"age": {
						"$type": 10
					}
				}
			""".trimIndent()
		}

		test("Is null") {
			filter {
				User::name.isNull()
			} shouldBeBson """
				{
					"name": {
						"$type": 10
					}
				}
			""".trimIndent()
		}

		test("Is undefined") {
			filter {
				User::name.isUndefined()
			} shouldBeBson """
				{
					"name": {
						"$type": 6
					}
				}
			""".trimIndent()
		}

		test("Is not null") {
			filter {
				User::name.isNotNull()
			} shouldBeBson """
				{
					"name": {
						"$not": {
							"$type": 10
						}
					}
				}
			""".trimIndent()
		}

		test("Is not undefined") {
			filter {
				User::name.isNotUndefined()
			} shouldBeBson """
				{
					"name": {
						"$not": {
							"$type": 6
						}
					}
				}
			""".trimIndent()
		}
	}

	suite("Operators $and and $or") {
		test("And") {
			filter {
				and {
					User::name eq "foo"
					User::age eq null
				}
			} shouldBeBson """
				{
					"$and": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Empty $and") {
			filter {
				and {}
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}

		test("An $and with a single term is removed") {
			filter {
				and {
					User::name eq "foo"
				}
			} shouldBeBson """
				{
					"name": {
						"$eq": "foo"
					}
				}
			""".trimIndent()
		}

		test("Combine nested $and") {
			filter {
				and {
					User::name eq "foo"
					and {
						User::age eq 12
						User::id eq ObjectId("507f1f77bcf86cd799439011")
					}
				}
			} shouldBeBson """
				{
					"$and": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": 12
							}
						},
						{
							"id": {
								"$eq": {
									"$oid": "507f1f77bcf86cd799439011"
								}
							}
						}
					]
				}
			""".trimIndent()
		}

		test("An automatic $and is generated when multiple filters are given") {
			filter { // same example as the previous, but we didn't write the '$and'
				User::name eq "foo"
				User::age eq null
			} shouldBeBson """
				{
					"$and": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Or") {
			filter {
				or {
					User::name eq "foo"
					User::age eq null
				}
			} shouldBeBson """
				{
					"$or": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Empty $or") {
			filter {
				or {}
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}

		test("An $or with a single term is removed") {
			filter {
				or {
					User::name eq "foo"
				}
			} shouldBeBson """
				{
					"name": {
						"$eq": "foo"
					}
				}
			""".trimIndent()
		}
	}

	suite("Comparison operators") {
		test("int $gtOp") {
			filter {
				User::age gt 12
			} shouldBeBson """
				{
					"age": {
						"$gtOp": 12
					}
				}
			""".trimIndent()
		}

		test("int $gte") {
			filter {
				User::age gte 12
			} shouldBeBson """
				{
					"age": {
						"$gte": 12
					}
				}
			""".trimIndent()
		}

		test("int $lt") {
			filter {
				User::age lt 12
			} shouldBeBson """
				{
					"age": {
						"$lt": 12
					}
				}
			""".trimIndent()
		}

		test("int $lteOp") {
			filter {
				User::age lte 12
			} shouldBeBson """
				{
					"age": {
						"$lteOp": 12
					}
				}
			""".trimIndent()
		}
	}

	suite("Array operators") {
		val elemMatch = "\$elemMatch"

		test("Test on an array element") {
			filter {
				User::grades.any eq 12
			} shouldBeBson """
				{
					"grades": {
						"$eq": 12
					}
				}
			""".trimIndent()
		}

		test("Test on different array elements") {
			filter {
				User::grades.any gt 12
				User::grades.any lte 15
			} shouldBeBson """
				{
					"$and": [
						{
							"grades": {
								"$gtOp": 12
							}
						},
						{
							"grades": {
								"$lteOp": 15
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Test on a single array element") {
			filter {
				User::grades.any {
					gt(12)
					lte(15)
				}
			} shouldBeBson """
				{
					"grades": {
						"$elemMatch": {
							"$gtOp": 12,
							"$lteOp": 15
						}
					}
				}
			""".trimIndent()
		}

		test("Test on subfields of different array elements") {
			filter {
				User::pets.any / Pet::age gt 15
				User::pets / Pet::age lte 18  // without 'any', the / does the same thing
			} shouldBeBson """
				{
					"$and": [
						{
							"pets.age": {
								"$gtOp": 15
							}
						},
						{
							"pets.age": {
								"$lteOp": 18
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Test on subfields of a single array element") {
			filter {
				User::pets.anyObject {
					Pet::age gt 15
					Pet::age lte 18
				}
			} shouldBeBson """
				{
					"pets": {
						"$elemMatch": {
							"$and": [
								{
									"age": {
										"$gtOp": 15
									}
								},
								{
									"age": {
										"$lteOp": 18
									}
								}
							]
						}
					}
				}
			""".trimIndent()
		}

		test("Test on a single subfield of a single array element") {
			filter {
				User::pets.anyObject {
					Pet::age {
						gt(15)
						lte(18)
					}
				}
			} shouldBeBson """
				{
					"pets": {
						"$elemMatch": {
							"age": {
								"$gtOp": 15,
								"$lteOp": 18
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Everything combined") {
			filter {
				User::pets / Pet::age gt 3
				User::pets.anyObject {
					Pet::age gte 1
					Pet::name eq "Chocolat"
				}
			} shouldBeBson """
				{
					"$and": [
						{
							"pets.age": {"$gtOp": 3}
						},
						{
							"pets": {
								"$elemMatch": {
									"$and": [
										{
											"age": {"$gte": 1}
										},
										{
											"name": {"$eq": "Chocolat"}
										}
									]
								}
							}
						}
					]
				}
			""".trimIndent()
		}
	}

	test("Operator $all") {
		filter {
			User::grades containsAll listOf(1, 2, 3)
		} shouldBeBson """
			{
				"grades": {
					"$all": [1, 2, 3]
				}
			}
		""".trimIndent()
	}

})
