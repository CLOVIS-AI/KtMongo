package fr.qsh.ktmongo.dsl.expr

import fr.qsh.ktmongo.dsl.LowLevelApi
import fr.qsh.ktmongo.dsl.expr.common.withLoggedContext
import fr.qsh.ktmongo.dsl.writeDocument
import io.kotest.core.spec.style.FunSpec
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.BsonType

@OptIn(LowLevelApi::class)
@Suppress("unused")
class PredicateExpressionTest : FunSpec({

	fun <T> predicate(block: PredicateExpression<T>.() -> Unit): String {
		val document = BsonDocument()

		val writer = BsonDocumentWriter(document)
			.withLoggedContext()

		writer.writeDocument {
			PredicateExpression<T>(testCodec())
				.apply(block)
				.writeTo(writer)
		}

		return document.toString()
	}

	val eq = "\$eq"
	val exists = "\$exists"
	val type = "\$type"
	val not = "\$not"
	val gt = "\$gt"
	val gte = "\$gte"
	val lt = "\$lt"
	val lte = "\$lte"

	context("Operator \$eq") {
		test("Integer") {
			predicate {
				eq(4)
			} shouldBeBson """
				{
					"$eq": 4
				}
			""".trimIndent()
		}

		test("String") {
			predicate {
				eq("foo")
			} shouldBeBson """
				{
					"$eq": "foo"
				}
			""".trimIndent()
		}

		test("Null") {
			predicate {
				eq(null)
			} shouldBeBson """
				{
					"$eq": null
				}
			""".trimIndent()
		}
	}

	context("Operator $exists") {
		test("Does exist") {
			predicate<String> {
				exists()
			} shouldBeBson """
				{
					"$exists": true
				}
			""".trimIndent()
		}

		test("Does not exist") {
			predicate<String> {
				doesNotExist()
			} shouldBeBson """
				{
					"$exists": false
				}
			""".trimIndent()
		}
	}

	context("Operator $type") {
		test("Has a given type") {
			predicate<String> {
				hasType(BsonType.DOUBLE)
			} shouldBeBson """
				{
					"$type": 1
				}
			""".trimIndent()
		}

		test("Is null") {
			predicate<String?> {
				isNull()
			} shouldBeBson """
				{
					"$type": 10
				}
			""".trimIndent()
		}
	}

	context("Operator $not") {
		test("Is not null") {
			predicate<String?> {
				isNotNull()
			} shouldBeBson """
				{
					"$not": {
						"$type": 10
					}
				}
			""".trimIndent()
		}

		test("Empty $not is no-op and thus removed") {
			predicate<String> {
				not {  }
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}
	}

	test("Can specify multiple operators at once") {
		predicate {
			exists()
			gt(15)
			lte(99)
			not {
				isNull()
				eq(17)
			}
		} shouldBeBson """
			{
				"$exists": true,
				"$gt": 15,
				"$lte": 99,
				"$not": {
					"$type": 10,
					"$eq": 17
				}
			}
		""".trimIndent()
	}
})
