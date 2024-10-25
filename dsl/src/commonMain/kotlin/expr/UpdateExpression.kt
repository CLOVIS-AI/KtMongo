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

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.expr.common.Expression
import opensavvy.ktmongo.dsl.expr.common.acceptAll
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * DSL for MongoDB operators that are used to update existing values (does *not* include aggregation operators).
 *
 * ### Example
 *
 * This expression type is available on multiple operations, most commonly `update`:
 * ```kotlin
 * class User(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * collection.update(
 *     filter = {
 *         User::name eq "Bob"
 *     },
 *     update = {
 *         User::age set 18
 *     }
 * )
 * ```
 *
 * ### Operators
 *
 * Fields:
 * - [`$inc`][inc]
 * - [`$rename`][renameTo]
 * - [`$set`][set]
 * - [`$setOnInsert`][setOnInsert]
 * - [`$unset`][unset]
 *
 * Arrays:
 * - [`$[]`][FieldDsl.get]
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/)
 *
 * @see FilterExpression Filters
 */
@KtMongoDsl
class UpdateExpression<T>(
	context: BsonContext,
) : AbstractCompoundExpression(context),
	FieldDsl {

	// region Low-level operations

	private class OperatorCombinator<T : AbstractExpression>(
		val type: KClass<T>,
		val combinator: (List<T>, BsonContext) -> T
	) {
		@Suppress("UNCHECKED_CAST") // This is a private class, it should not be used incorrectly
		operator fun invoke(sources: List<AbstractExpression>, context: BsonContext) =
			combinator(sources as List<T>, context)
	}

	@LowLevelApi
	override fun simplify(children: List<Expression>): AbstractExpression? {
		if (children.isEmpty())
			return null

		val simplifiedChildren = combinators.fold(children) { newChildren, combinator ->
			@Suppress("UNCHECKED_CAST") // safe because of the filter
			val matching = newChildren
				.filter { it::class == combinator.type }
				as List<UpdateExpressionNode>

			if (matching.size <= 1)
			// At least two elements are required to combine them into a single one!
				return@fold newChildren

			val childrenWithoutMatching = newChildren - matching.toSet()
			childrenWithoutMatching + combinator(matching, context)
		}

		@OptIn(DangerousMongoApi::class)
		if (simplifiedChildren != children)
			return UpdateExpression<T>(context).apply {
				acceptAll(simplifiedChildren)
			}
		return this
	}

	@LowLevelApi
	private sealed class UpdateExpressionNode(context: BsonContext) : AbstractExpression(context)

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V) {
		accept(SetExpressionNode(listOf(this.path to value), context))
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: V) {
		this.field.set(value)
	}

	@LowLevelApi
	private class SetExpressionNode(
		val mappings: List<Pair<Path, *>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$set") {
				for ((field, value) in mappings) {
					writeObjectSafe(field.toString(), value, context)
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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setOnInsert(value: V) {
		accept(SetOnInsertExpressionNode(listOf(this.path to value), context))
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setOnInsert(value: V) {
		this.field.setOnInsert(value)
	}

	@LowLevelApi
	private class SetOnInsertExpressionNode(
		val mappings: List<Pair<Path, *>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$setOnInsert") {
				for ((field, value) in mappings) {
					writeObjectSafe(field.toString(), value, context)
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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.inc(amount: V) {
		accept(IncrementExpressionNode(listOf(this.path to amount), context))
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V : Number> KProperty1<T, V>.inc(amount: V) {
		this.field.inc(amount)
	}

	@LowLevelApi
	private class IncrementExpressionNode(
		val mappings: List<Pair<Path, Number>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$inc") {
				for ((field, value) in mappings) {
					writeObjectSafe(field.toString(), value, context)
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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.unset() {
		accept(UnsetExpressionNode(listOf(this.path), context))
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.unset() {
		this.field.unset()
	}

	@LowLevelApi
	private class UnsetExpressionNode(
		val fields: List<Path>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { fields.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$unset") {
				for (field in fields) {
					writeBoolean(field.toString(), true)
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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.renameTo(newName: Field<T, V>) {
		accept(RenameExpressionNode(listOf(this.path to newName.path), context))
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.renameTo(newName: Field<T, V>) {
		this.field.renameTo(newName)
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.renameTo(newName: KProperty1<T, V>) {
		this.renameTo(newName.field)
	}

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.renameTo(newName: KProperty1<T, V>) {
		this.renameTo(newName.field)
	}

	@LowLevelApi
	private class RenameExpressionNode(
		val fields: List<Pair<Path, Path>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { fields.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$rename") {
				for ((before, after) in fields) {
					writeString(before.toString(), after.toString())
				}
			}
		}
	}

	// endregion

	companion object {
		@OptIn(LowLevelApi::class)
		private val combinators = listOf(
			OperatorCombinator(SetExpressionNode::class) { sources, context ->
				SetExpressionNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(SetOnInsertExpressionNode::class) { sources, context ->
				SetOnInsertExpressionNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(IncrementExpressionNode::class) { sources, context ->
				IncrementExpressionNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(UnsetExpressionNode::class) { sources, context ->
				UnsetExpressionNode(sources.flatMap { it.fields }, context)
			},
			OperatorCombinator(RenameExpressionNode::class) { sources, context ->
				RenameExpressionNode(sources.flatMap { it.fields }, context)
			},
		)
	}
}
