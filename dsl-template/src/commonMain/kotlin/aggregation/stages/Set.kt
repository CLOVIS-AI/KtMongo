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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import kotlin.jvm.JvmName

/**
 * Pipeline implementing the `$set` stage.
 */
@KtMongoDsl
interface HasSet<Document : Any> : Pipeline<Document> {

	/**
	 * Adds new fields to documents, or overwrites existing fields.
	 *
	 * See [$project][HasProject.project] to learn more about their differences.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val age: Int,
	 *     val isAdult: Boolean? = null,
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         User::isAdult set (User::age gte 18)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/set/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun <Out : Any> set(
		block: SetStageOperators<Document, Out>.() -> Unit,
	): Pipeline<Out> =
		withStage(createSetStage(context, block))
			.reinterpret()

}

@OptIn(LowLevelApi::class)
private class SetStage(
	val expression: SetStageOperators<*, *>,
	context: BsonContext,
) : AbstractBsonNode(context) {
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$set") {
			expression.writeTo(this)
		}
	}
}

internal fun <In : Any, Out : Any> createSetStage(context: BsonContext, block: SetStageOperators<In, Out>.() -> Unit): BsonNode =
	SetStage(SetStageBsonNode<In, Out>(context).apply(block), context)

/**
 * The operators allowed in a [set] stage.
 */
@KtMongoDsl
interface SetStageOperators<In : Any, Out : Any> : CompoundBsonNode, AggregationOperators, FieldDsl {

	// region $set

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val age: Int,
	 *     val isAdult: Boolean? = null,
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         User::isAdult set (User::age gte 18)
	 *     }
	 * ```
	 *
	 * If you want to create a temporary field that is only used within the aggregation and not deserialized,
	 * see [Field.unsafe].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	infix fun <@kotlin.internal.Exact V> Field<Out, V>.set(value: Value<In, V>) {
		accept(SetBsonNode(this.path, value, context))
	}

	/**
	 * Replaces the value of an array with the specified list of [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val age: Int,
	 *     val maths: Score,
	 *     val physics: Score,
	 *     val scores: List<Score>,
	 * )
	 *
	 * class Score(
	 *     val subject: String,
	 *     val value: Double,
	 *     val max: Double,
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         User::scores set listOf(
	 *             of(User::maths),
	 *             of(User::physics),
	 *         )
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	infix fun <@kotlin.internal.Exact V> Field<Out, Collection<V>>.set(values: Collection<Value<In, V>>) {
		accept(SetArrayBsonNode(this.path, values, context))
	}

	/**
	 * Replaces the value of an array with the specified list of [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val age: Int,
	 *     val maths: Score,
	 *     val physics: Score,
	 *     val scores: List<Score>,
	 * )
	 *
	 * class Score(
	 *     val subject: String,
	 *     val value: Double,
	 *     val max: Double,
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         User::scores set listOf(
	 *             of(User::maths),
	 *             of(User::physics),
	 *         )
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@JvmName("setNullable")
	final infix fun <@kotlin.internal.Exact V> Field<Out, Collection<V>?>.set(values: Collection<Value<In, V>?>) {
		accept(SetArrayBsonNode(this.path, values, context))
	}

	// endregion
	// region Conditional $set

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
	@Suppress("INVISIBLE_REFERENCE", "UNCHECKED_CAST")
	fun <@kotlin.internal.OnlyInputTypes V> Field<Out, V>.setIf(condition: Value<In, Boolean>, value: Value<In, V>) =
		this set cond(condition, value, of(this as Field<In, V>))

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
	@Suppress("INVISIBLE_REFERENCE", "UNCHECKED_CAST")
	fun <@kotlin.internal.OnlyInputTypes V> Field<Out, V>.setUnless(condition: Value<In, Boolean>, value: Value<In, V>) =
		this set cond(condition, of(this as Field<In, V>), value)

	// endregion
}

private class SetStageBsonNode<In : Any, Out : Any>(
	context: BsonContext,
) : AbstractCompoundBsonNode(context), SetStageOperators<In, Out>

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

@LowLevelApi
private class SetArrayBsonNode(
	val path: Path,
	val values: Collection<Value<*, *>?>,
	context: BsonContext,
) : AbstractBsonNode(context) {

	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray(path.toString()) {
			for (value in values) {
				if (value != null)
					value.writeTo(this)
				else
					writeNull()
			}
		}
	}
}
