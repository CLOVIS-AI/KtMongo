package fr.qsh.ktmongo.dsl.expr

import fr.qsh.ktmongo.dsl.KtMongoDsl
import fr.qsh.ktmongo.dsl.LowLevelApi
import fr.qsh.ktmongo.dsl.expr.common.AbstractExpression
import fr.qsh.ktmongo.dsl.expr.common.AbstractCompoundExpression
import fr.qsh.ktmongo.dsl.expr.common.Expression
import fr.qsh.ktmongo.dsl.expr.common.acceptAll
import fr.qsh.ktmongo.dsl.path.Path
import fr.qsh.ktmongo.dsl.path.PropertySyntaxScope
import fr.qsh.ktmongo.dsl.writeDocument
import fr.qsh.ktmongo.dsl.writeObject
import org.bson.BsonWriter
import org.bson.codecs.configuration.CodecRegistry
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * DSL for MongoDB operators that are used to update fields.
 *
 * For example, these operators are available with `insertOne` or as the update in `updateOne`.
 */
@KtMongoDsl
class UpdateExpression<T>(
	codec: CodecRegistry,
) : AbstractCompoundExpression(codec), PropertySyntaxScope {

	// region Low-level operations

	private class OperatorCombinator<T : AbstractExpression>(
		val type: KClass<T>,
		val combinator: (List<T>, CodecRegistry) -> T
	) {
		@Suppress("UNCHECKED_CAST") // This is a private class, it should not be used incorrectly
		operator fun invoke(sources: List<AbstractExpression>, codec: CodecRegistry) =
			combinator(sources as List<T>, codec)
	}

	@LowLevelApi
	override fun simplify(children: List<Expression>): AbstractExpression? {
		if (children.isEmpty())
			return null

		val simplifiedChildren = combinators.fold(children) { newChildren, combinator ->
			val matching = newChildren.filterIsInstance(combinator.type.java)

			if (matching.size <= 1)
				// At least two elements are required to combine them into a single one!
				return@fold newChildren

			val childrenWithoutMatching = newChildren - matching.toSet()
			childrenWithoutMatching + combinator(matching, codec)
		}

		if (simplifiedChildren != children)
			return UpdateExpression<T>(codec).apply {
				acceptAll(simplifiedChildren)
			}
		return this
	}

	@LowLevelApi
	private sealed class UpdateExpressionNode(codec: CodecRegistry) : AbstractExpression(codec)

	// endregion
	// region $set

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.updateMany {
	 *     User::age set 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 *
	 * @see setOnInsert Only set if a new document is created.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <@OnlyInputTypes V> KProperty1<T, V>.set(value: V) {
		accept(SetExpressionNode(listOf(this.path() to value), codec))
	}

	@LowLevelApi
	private class SetExpressionNode(
		val mappings: List<Pair<Path, *>>,
		codec: CodecRegistry,
	) : UpdateExpressionNode(codec) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonWriter) {
			writer.writeDocument("\$set") {
				for ((field, value) in mappings) {
					writer.writeName(field.toString())
					writer.writeObject(value, codec)
				}
			}
		}
	}

	// endregion
	// region $setOnInsert

	/**
	 * If an upsert operation results in an insert of a document,
	 * then this operator assigns the specified [value] to the field.
	 * If the update operation does not result in an insert, this operator does nothing.
	 *
	 * If used in an update operation that isn't an upsert, no document can be inserted,
	 * and thus this operator never does anything.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.upsertOne {
	 *     User::age setOnInsert 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/setOnInsert/)
	 *
	 * @see set Always set the value.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <@OnlyInputTypes V> KProperty1<T, V>.setOnInsert(value: V) {
		accept(SetOnInsertExpressionNode(listOf(this.path() to value), codec))
	}

	@LowLevelApi
	private class SetOnInsertExpressionNode(
		val mappings: List<Pair<Path, *>>,
		codec: CodecRegistry,
	) : UpdateExpressionNode(codec) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonWriter) {
			writer.writeDocument("\$setOnInsert") {
				for ((field, value) in mappings) {
					writer.writeName(field.toString())
					writer.writeObject(value, codec)
				}
			}
		}
	}

	// endregion
	// region $inc

	/**
	 * Increments a field by the specified [amount].
	 *
	 * [amount] may be negative, in which case the field is decremented.
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of [amount].
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // It's the new year!
	 * collection.updateMany {
	 *     User::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/inc/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <@OnlyInputTypes V : Number> KProperty1<T, V>.inc(amount: V) {
		accept(IncrementExpressionNode(listOf(this.path() to amount), codec))
	}

	@LowLevelApi
	private class IncrementExpressionNode(
		val mappings: List<Pair<Path, Number>>,
		codec: CodecRegistry,
	) : UpdateExpressionNode(codec) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonWriter) {
			writer.writeDocument("\$inc") {
				for ((field, value) in mappings) {
					writer.writeName(field.toString())
					writer.writeObject(value, codec)
				}
			}
		}
	}

	// endregion
	// region $unset

	/**
	 * Deletes a field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val alive: Boolean,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "Luke Skywalker"
	 * }.updateOne {
	 *     User::age.unset()
	 *     User::alive set false
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/unset/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <@OnlyInputTypes V> KProperty1<T, V>.unset() {
		accept(UnsetExpressionNode(listOf(this.path()), codec))
	}

	@LowLevelApi
	private class UnsetExpressionNode(
		val fields: List<Path>,
		codec: CodecRegistry,
	) : UpdateExpressionNode(codec) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { fields.isEmpty() }

		override fun write(writer: BsonWriter) {
			writer.writeDocument("\$unset") {
				for (field in fields) {
					writer.writeName(field.toString())
					writer.writeBoolean(true)
				}
			}
		}
	}

	// endregion
	// region $rename

	/**
	 * Renames a field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val ageOld: Int,
	 * )
	 *
	 * collection.updateMany {
	 *     User::ageOld renameTo User::age
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/rename/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <@OnlyInputTypes V> KProperty1<T, V>.renameTo(newName: KProperty1<T, V>) {
		accept(RenameExpressionNode(listOf(this.path() to newName.path()), codec))
	}

	@LowLevelApi
	private class RenameExpressionNode(
		val fields: List<Pair<Path, Path>>,
		codec: CodecRegistry,
	) : UpdateExpressionNode(codec) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { fields.isEmpty() }

		override fun write(writer: BsonWriter) {
			writer.writeDocument("\$rename") {
				for ((before, after) in fields) {
					writer.writeName(before.toString())
					writer.writeString(after.toString())
				}
			}
		}
	}

	// endregion

	companion object {
		@OptIn(LowLevelApi::class)
		private val combinators = listOf(
			OperatorCombinator(SetExpressionNode::class) { sources, codec ->
				SetExpressionNode(sources.flatMap { it.mappings }, codec)
			},
			OperatorCombinator(SetOnInsertExpressionNode::class) { sources, codec ->
				SetOnInsertExpressionNode(sources.flatMap { it.mappings }, codec)
			},
			OperatorCombinator(IncrementExpressionNode::class) { sources, codec ->
				IncrementExpressionNode(sources.flatMap { it.mappings }, codec)
			},
			OperatorCombinator(UnsetExpressionNode::class) { sources, codec ->
				UnsetExpressionNode(sources.flatMap { it.fields }, codec)
			},
			OperatorCombinator(RenameExpressionNode::class) { sources, codec ->
				RenameExpressionNode(sources.flatMap { it.fields }, codec)
			},
		)
	}
}
