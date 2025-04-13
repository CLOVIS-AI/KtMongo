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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.ValueDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import kotlin.reflect.KProperty1

/**
 * Pipeline implementing the `$set` stage.
 */
@KtMongoDsl
interface HasSet<Document : Any> : Pipeline<Document> {

	/**
	 * Adds new fields to documents, or overwrites existing fields.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/set/)
	 */
	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun set(
		block: SetStageOperators<Document>.() -> Unit,
	): Pipeline<Document> =
		withStage(createSetStage(context, block))

}

@OptIn(LowLevelApi::class)
private class SetStage(
	val expression: SetStageOperators<*>,
	context: BsonContext,
) : AbstractBsonNode(context) {
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$set") {
			expression.writeTo(this)
		}
	}
}

internal fun <Document : Any> createSetStage(context: BsonContext, block: SetStageOperators<Document>.() -> Unit): BsonNode =
	SetStage(SetStageBsonNode<Document>(context).apply(block), context)

/**
 * The operators allowed in a [set] stage.
 */
@KtMongoDsl
interface SetStageOperators<T : Any> : CompoundBsonNode, ValueDsl, FieldDsl {

	// region $set

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: Value<T, V>)

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: Value<T, V>) {
		this.field.set(value)
	}

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V) {
		this.set(of(value))
	}

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: V) {
		this.field.set(value)
	}

	// endregion
	// region Conditional $set
	// region setIf

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setIf(condition: Value<T, Boolean>, value: Value<T, V>) =
		this set cond(condition, value, of(this))

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setIf(condition: Value<T, Boolean>, value: Value<T, V>) =
		this.field.setIf(condition, value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setIf(condition: Value<T, Boolean>, value: V) =
		this.setIf(condition, of(value))

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setIf(condition: Value<T, Boolean>, value: V) =
		this.field.setIf(condition, value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setIf(condition: Boolean, value: Value<T, V>) {
		if (condition)
			this.set(value)
	}

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setIf(condition: Boolean, value: Value<T, V>) =
		this.field.setIf(condition, value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setIf(condition: Boolean, value: V) =
		this.setIf(condition, of(value))

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setIf(condition: Boolean, value: V) =
		this.field.setIf(condition, value)

	// endregion
	// region setUnless

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setUnless(condition: Value<T, Boolean>, value: Value<T, V>) =
		this set cond(condition, of(this), value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setUnless(condition: Value<T, Boolean>, value: Value<T, V>) =
		this.field.setUnless(condition, value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setUnless(condition: Value<T, Boolean>, value: V) =
		this.setUnless(condition, of(value))

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setUnless(condition: Value<T, Boolean>, value: V) =
		this.field.setUnless(condition, value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setUnless(condition: Boolean, value: Value<T, V>) {
		if (!condition)
			this.set(value)
	}

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setUnless(condition: Boolean, value: Value<T, V>) =
		this.field.setUnless(condition, value)

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setUnless(condition: Boolean, value: V) =
		this.setUnless(condition, of(value))

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### External resources
	 *
	 * - [`$set`](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 * - [`$cond`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setUnless(condition: Boolean, value: V) =
		this.field.setUnless(condition, value)

	// endregion
	// endregion
}

private class SetStageBsonNode<T : Any>(
	context: BsonContext,
) : AbstractCompoundBsonNode(context), SetStageOperators<T> {

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun <V> Field<T, V>.set(value: Value<T, V>) {
		accept(SetBsonNode(this.path, value, context))
	}

	@LowLevelApi
	private class SetBsonNode(
		val path: Path,
		val value: Value<*, *>,
		context: BsonContext,
	) : AbstractBsonNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			write(path.toString()) {
				value.writeTo(this)
			}
		}
	}
}
