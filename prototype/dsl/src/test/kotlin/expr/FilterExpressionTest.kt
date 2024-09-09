package fr.qsh.ktmongo.dsl.expr

import io.kotest.core.spec.style.FunSpec
import org.bson.BsonType

@Suppress("unused")
class FilterExpressionTest : FunSpec({

	class User(
		val id: String,
		val name: String,
		val age: Int?,
	)

	fun <T> filter(block: FilterExpression<T>.() -> Unit): String =
		FilterExpression<T>(testCodec()).apply(block).toString(simplified = true)

	val eq = "\$eq"
	val ne = "\$ne"
	val and = "\$and"
	val or = "\$or"
	val exists = "\$exists"
	val type = "\$type"
	val not = "\$not"
	val isOneOf = "\$in"
	val gt = "\$gt"
	val gte = "\$gte"
	val lt = "\$lt"
	val lte = "\$lte"
	val all = "\$all"

	context("Operator $eq") {
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

	context("Operator $ne") {
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

	context("Operator $isOneOf") {
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

	context("Operator $exists") {
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

	context("Operator $type") {
		test("String") {
			filter {
				User::age hasType BsonType.STRING
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
				User::age hasType BsonType.NULL
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

	context("Operators $and and $or") {
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
			filter<User> {
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
						User::id eq "abc"
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
								"$eq": "abc"
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
			filter<User> {
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

	context("Comparison operators") {
		test("int $gt") {
			filter {
				User::age gt 12
			} shouldBeBson """
				{
					"age": {
						"$gt": 12
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

		test("int $lte") {
			filter {
				User::age lte 12
			} shouldBeBson """
				{
					"age": {
						"$lte": 12
					}
				}
			""".trimIndent()
		}
	}

	context("Array operators") {
		val elemMatch = "\$elemMatch"

		class Pet(
			val name: String,
			val age: Int,
		)

		class User(
			val scores: List<Int>,
			val pets: List<Pet>,
		)

		test("Test on an array element") {
			filter {
				User::scores.any eq 12
			} shouldBeBson """
				{
					"scores": {
						"$eq": 12
					}
				}
			""".trimIndent()
		}

		test("Test on different array elements") {
			filter {
				User::scores.any gt 12
				User::scores.any lte 15
			} shouldBeBson """
				{
					"$and": [
						{
							"scores": {
								"$gt": 12
							}
						},
						{
							"scores": {
								"$lte": 15
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Test on a single array element") {
			filter {
				User::scores.any {
					gt(12)
					lte(15)
				}
			} shouldBeBson """
				{
					"scores": {
						"$elemMatch": {
							"$gt": 12,
							"$lte": 15
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
								"$gt": 15
							}
						},
						{
							"pets.age": {
								"$lte": 18
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
										"$gt": 15
									}
								},
								{
									"age": {
										"$lte": 18
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
								"$gt": 15,
								"$lte": 18
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
							"pets.age": {"$gt": 3}
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
		class User(
			val grades: List<Int>,
		)

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
