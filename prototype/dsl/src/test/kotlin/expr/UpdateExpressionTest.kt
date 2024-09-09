package fr.qsh.ktmongo.dsl.expr

import fr.qsh.ktmongo.dsl.LowLevelApi
import fr.qsh.ktmongo.dsl.expr.common.withLoggedContext
import fr.qsh.ktmongo.dsl.writeDocument
import io.kotest.core.spec.style.FunSpec
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter

@OptIn(LowLevelApi::class)
class UpdateExpressionTest : FunSpec({

	class Friend(
		val id: String,
		val name: String,
		val money: Float,
	)

	class User(
		val id: String,
		val name: String,
		val age: Int?,
		val money: Double,
		val bestFriend: Friend,
		val friends: List<Friend>,
	)

	fun <T> update(block: UpdateExpression<T>.() -> Unit): String {
		val document = BsonDocument()

		val writer = BsonDocumentWriter(document)
			.withLoggedContext()

		writer.writeDocument {
			UpdateExpression<T>(testCodec())
				.apply(block)
				.writeTo(writer)
		}

		return document.toString()
	}

	val set = "\$set"
	val setOnInsert = "\$setOnInsert"
	val inc = "\$inc"
	val unset = "\$unset"
	val rename = "\$rename"

	test("Empty update") {
		update<User> { } shouldBeBson """{}"""
	}

	context("Operator $set") {
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

	context("Operator $setOnInsert") {
		test("Single field") {
			update {
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
			update {
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
			update {
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

	context("Operator $inc") {
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
				User::money inc 5.2
				User::bestFriend / Friend::money inc -5.2f
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

	context("Operator $unset") {
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

	context("Operator $rename") {
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
						"friends.$0.name": "friends.$1.name"
					}
				}
			""".trimIndent()
		}
	}
})
