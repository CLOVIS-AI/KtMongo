/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.ValueDsl
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.options.common.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.Path
import kotlin.reflect.KProperty1

/**
 * Operators to manipulate arrays.
 *
 * To learn more about aggregation operators, see [opensavvy.ktmongo.dsl.aggregation.ValueDsl].
 */
interface ArrayValueOperators : ValueOperators {

	// region $filter

	/**
	 * Selects a subset of an array to return based on the specified [predicate], similarly to [Kotlin's `filter`][kotlin.collections.filter].
	 *
	 * The returned elements are in the original order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val measurements: List<Int>,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Sensor::measurements set (Sensor::measurements).filter { it gte of(0) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/filter/)
	 *
	 * @param limit If set, specifies a maximum number of elements returned:
	 * only the first [limit] matching elements are returned, even if there are more matching elements.
	 * Must be greater or equal to `1`, or be `null`.
	 *
	 * @param variableName The name of the temporary variable passed to the [predicate] lambda, which represents the
	 * current element being iterated over. By default, `"this"`. Setting this parameter is only useful when using
	 * nested [filter] or other similar calls, which could otherwise conflict.
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <Context : Any, T> Value<Context, Collection<T>>.filter(
		limit: Value<Context, Number>? = null,
		variableName: String = "this",
		predicate: ValueDsl.(Value<Any, T>) -> Value<T & Any, Boolean>,
	): Value<Context, List<T>> =
		FilterValueOperator(
			input = this,
			predicate = PredicateEvaluator(context).predicate(ThisValue(variableName, context)),
			limit = limit,
			variableName = variableName,
			context = context,
		)

	/**
	 * Selects a subset of an array to return based on the specified [predicate], similarly to [Kotlin's `filter`][kotlin.collections.filter].
	 *
	 * The returned elements are in the original order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val measurements: List<Int>,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Sensor::measurements set (Sensor::measurements).filter { it gte of(0) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/filter/)
	 *
	 * @param limit If set, specifies a maximum number of elements returned:
	 * only the first [limit] matching elements are returned, even if there are more matching elements.
	 * Must be greater or equal to `1`, or be `null`.
	 *
	 * @param variableName The name of the temporary variable passed to the [predicate] lambda, which represents the
	 * current element being iterated over. By default, `"this"`. Setting this parameter is only useful when using
	 * nested [filter] or other similar calls, which could otherwise conflict.
	 */
	@KtMongoDsl
	fun <Context : Any, T> Field<Context, Collection<T>>.filter(
		limit: Value<Context, Number>? = null,
		variableName: String = "this",
		predicate: ValueDsl.(Value<Any, T>) -> Value<T & Any, Boolean>,
	): Value<Context, List<T>> =
		of(this).filter(limit, variableName, predicate)

	/**
	 * Selects a subset of an array to return based on the specified [predicate], similarly to [Kotlin's `filter`][kotlin.collections.filter].
	 *
	 * The returned elements are in the original order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val measurements: List<Int>,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Sensor::measurements set (Sensor::measurements).filter { it gte of(0) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/filter/)
	 *
	 * @param limit If set, specifies a maximum number of elements returned:
	 * only the first [limit] matching elements are returned, even if there are more matching elements.
	 * Must be greater or equal to `1`, or be `null`.
	 *
	 * @param variableName The name of the temporary variable passed to the [predicate] lambda, which represents the
	 * current element being iterated over. By default, `"this"`. Setting this parameter is only useful when using
	 * nested [filter] or other similar calls, which could otherwise conflict.
	 */
	@KtMongoDsl
	fun <Context : Any, T> KProperty1<Context, Collection<T>>.filter(
		limit: Value<Context, Number>? = null,
		variableName: String = "this",
		predicate: ValueDsl.(Value<Any, T>) -> Value<T & Any, Boolean>,
	): Value<Context, List<T>> =
		of(this).filter(limit, variableName, predicate)

	/**
	 * Selects a subset of an array to return based on the specified [predicate], similarly to [Kotlin's `filter`][kotlin.collections.filter].
	 *
	 * The returned elements are in the original order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val measurements: List<Int>,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Sensor::measurements set (Sensor::measurements).filter { it gte of(0) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/filter/)
	 *
	 * @param limit If set, specifies a maximum number of elements returned:
	 * only the first [limit] matching elements are returned, even if there are more matching elements.
	 * Must be greater or equal to `1`, or be `null`.
	 *
	 * @param variableName The name of the temporary variable passed to the [predicate] lambda, which represents the
	 * current element being iterated over. By default, `"this"`. Setting this parameter is only useful when using
	 * nested [filter] or other similar calls, which could otherwise conflict.
	 */
	@KtMongoDsl
	fun <Context : Any, T> Collection<T>.filter(
		limit: Value<Context, Number>? = null,
		variableName: String = "this",
		predicate: ValueDsl.(Value<Any, T>) -> Value<T & Any, Boolean>,
	): Value<Context, List<T>> =
		of(this).filter(limit, variableName, predicate)

	@LowLevelApi
	private class PredicateEvaluator(override val context: BsonContext) : ValueDsl

	@LowLevelApi
	private class ThisValue(
		private val variableName: String,
		context: BsonContext,
	) : AbstractValue<Any, Nothing>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeString("$$$variableName")
		}
	}

	@LowLevelApi
	private class FilterValueOperator<Context : Any, T>(
		private val input: Value<Context, Collection<T>>,
		private val predicate: Value<T & Any, Boolean>,
		private val variableName: String,
		private val limit: Value<Context, Number>?,
		context: BsonContext,
	) : AbstractValue<Context, List<T>>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument("\$filter") {
					write("input") {
						input.writeTo(this)
					}

					writeString("as", variableName)

					write("cond") {
						predicate.writeTo(this)
					}

					if (limit != null) {
						write("limit") {
							limit.writeTo(this)
						}
					}
				}
			}
		}
	}

	// endregion
	// region $firstN

	/**
	 * Returns the first [limit] elements in an array, similar to [kotlin.collections.take].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val firstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::firstScores set Player::scores.take(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/#array-operator)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Value<Context, Collection<T>>.take(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		TakeValueOperator(
			input = this,
			limit = limit,
			context = context,
		)

	/**
	 * Returns the first [limit] elements in an array, similar to [kotlin.collections.take].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val firstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::firstScores set Player::scores.take(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/#array-operator)
	 */
	@KtMongoDsl
	fun <Context : Any, T> Field<Context, Collection<T>>.take(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		of(this).take(limit)

	/**
	 * Returns the first [limit] elements in an array, similar to [kotlin.collections.take].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val firstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::firstScores set Player::scores.take(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/#array-operator)
	 */
	@KtMongoDsl
	fun <Context : Any, T> KProperty1<Context, Collection<T>>.take(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		of(this).take(limit)

	/**
	 * Returns the first [limit] elements in an array, similar to [kotlin.collections.take].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val firstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::firstScores set Player::scores.take(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/#array-operator)
	 */
	@KtMongoDsl
	fun <Context : Any, T> Collection<T>.take(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		of(this).take(limit)

	@LowLevelApi
	private class TakeValueOperator<Context : Any, T>(
		private val input: Value<Context, Collection<T>>,
		private val limit: Value<Context, Number>,
		context: BsonContext,
	) : AbstractValue<Context, List<T>>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument("\$firstN") {
					write("input") {
						input.writeTo(this)
					}

					write("n") {
						limit.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region $lastN

	/**
	 * Returns the last [limit] elements in an array, similar to [kotlin.collections.takeLast].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val lastScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::lastScores set Player::scores.takeLast(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/#array-operator)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Value<Context, Collection<T>>.takeLast(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		TakeLastValueOperator(
			input = this,
			limit = limit,
			context = context,
		)

	/**
	 * Returns the last [limit] elements in an array, similar to [kotlin.collections.takeLast].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val lastScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::lastScores set Player::scores.takeLast(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/#array-operator)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Field<Context, Collection<T>>.takeLast(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		of(this).takeLast(limit)

	/**
	 * Returns the last [limit] elements in an array, similar to [kotlin.collections.takeLast].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val lastScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::lastScores set Player::scores.takeLast(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/#array-operator)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> KProperty1<Context, Collection<T>>.takeLast(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		of(this).takeLast(limit)

	/**
	 * Returns the last [limit] elements in an array, similar to [kotlin.collections.takeLast].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val lastScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::lastScores set Player::scores.takeLast(3)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/#array-operator)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Collection<T>.takeLast(
		limit: Value<Context, Number>,
	): Value<Context, List<T>> =
		of(this).takeLast(limit)

	@LowLevelApi
	private class TakeLastValueOperator<Context : Any, T>(
		private val input: Value<Context, Collection<T>>,
		private val limit: Value<Context, Number>,
		context: BsonContext,
	) : AbstractValue<Context, List<T>>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument("\$lastN") {
					write("input") {
						input.writeTo(this)
					}

					write("n") {
						limit.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region $sortArray

	/**
	 * Sorts an array based on fields of its elements.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Score(
	 *     val value: Int,
	 * )
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Score>,
	 *     val bestScores: List<Score>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedBy { ascending(Score::value) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sorted Sort by the elements themselves (ascending order).
	 * @see sortedDescending Sort by the elements themselves (descending order).
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Value<Context, Collection<T>>.sortedBy(
		order: SortOptionDsl<T & Any>.() -> Unit,
	): Value<Context, List<T>> =
		SortValueOperator(
			input = this,
			sortOrder = SortOptionDslExpression<T & Any>(context).apply { order() }.toValue(),
			context = context,
		)

	/**
	 * Sorts an array based on fields of its elements.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Score(
	 *     val value: Int,
	 * )
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Score>,
	 *     val bestScores: List<Score>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedBy { ascending(Score::value) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sorted Sort by the elements themselves (ascending order).
	 * @see sortedDescending Sort by the elements themselves (descending order).
	 */
	@KtMongoDsl
	fun <Context : Any, T> Field<Context, Collection<T>>.sortedBy(
		order: SortOptionDsl<T & Any>.() -> Unit,
	): Value<Context, List<T>> =
		of(this).sortedBy(order)

	/**
	 * Sorts an array based on fields of its elements.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Score(
	 *     val value: Int,
	 * )
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Score>,
	 *     val bestScores: List<Score>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedBy { ascending(Score::value) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sorted Sort by the elements themselves (ascending order).
	 * @see sortedDescending Sort by the elements themselves (descending order).
	 */
	@KtMongoDsl
	fun <Context : Any, T> KProperty1<Context, Collection<T>>.sortedBy(
		order: SortOptionDsl<T & Any>.() -> Unit,
	): Value<Context, List<T>> =
		of(this).sortedBy(order)

	/**
	 * Sorts an array based on fields of its elements.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Score(
	 *     val value: Int,
	 * )
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Score>,
	 *     val bestScores: List<Score>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedBy { ascending(Score::value) }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sorted Sort by the elements themselves (ascending order).
	 * @see sortedDescending Sort by the elements themselves (descending order).
	 */
	@KtMongoDsl
	fun <Context : Any, T> Collection<T>.sortedBy(
		order: SortOptionDsl<T & Any>.() -> Unit,
	): Value<Context, List<T>> =
		of(this).sortedBy(order)

	@LowLevelApi
	private class SortOptionDslExpression<Context : Any>(
		context: BsonContext,
	) : AbstractCompoundExpression(context), SortOptionDsl<Context> {

		@OptIn(DangerousMongoApi::class)
		override fun ascending(field: Field<Context, *>) {
			accept(SortExpression(field.path, 1, context))
		}

		@OptIn(DangerousMongoApi::class)
		override fun descending(field: Field<Context, *>) {
			accept(SortExpression(field.path, -1, context))
		}

		@LowLevelApi
		private class SortExpression(
			val path: Path,
			val value: Int,
			context: BsonContext,
		) : AbstractExpression(context) {

			override fun write(writer: BsonFieldWriter) = with(writer) {
				writeInt32(path.toString(), value)
			}
		}

		fun toValue() = SortOptionDslValue(context)

		@LowLevelApi
		private inner class SortOptionDslValue(
			context: BsonContext,
		) : AbstractValue<Context, Nothing>(context) {

			init {
				this@SortOptionDslExpression.freeze()
			}

			@LowLevelApi
			override fun write(writer: BsonValueWriter) = with(writer) {
				writeDocument {
					this@SortOptionDslExpression.writeTo(this)
				}
			}
		}
	}

	/**
	 * Sorts an array based on its elements, in ascending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val worstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sorted()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by elements in descending order.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Value<Context, Collection<T>>.sorted(): Value<Context, List<T>> =
		SortValueOperator(
			input = this,
			sortOrder = SortSelfValueOperator(order = 1, context),
			context = context,
		)

	/**
	 * Sorts an array based on its elements, in ascending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val worstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sorted()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by elements in descending order.
	 */
	@KtMongoDsl
	fun <Context : Any, T> Field<Context, Collection<T>>.sorted(): Value<Context, List<T>> =
		of(this).sorted()

	/**
	 * Sorts an array based on its elements, in ascending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val worstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sorted()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by elements in descending order.
	 */
	@KtMongoDsl
	fun <Context : Any, T> KProperty1<Context, Collection<T>>.sorted(): Value<Context, List<T>> =
		of(this).sorted()

	/**
	 * Sorts an array based on its elements, in ascending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val worstScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sorted()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by elements in descending order.
	 */
	@KtMongoDsl
	fun <Context : Any, T> Collection<T>.sorted(): Value<Context, List<T>> =
		of(this).sorted()

	/**
	 * Sorts an array based on its elements, in descending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val bestScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedDescending()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by fields of elements.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, T> Value<Context, Collection<T>>.sortedDescending(): Value<Context, List<T>> =
		SortValueOperator(
			input = this,
			sortOrder = SortSelfValueOperator(order = -1, context),
			context = context,
		)

	/**
	 * Sorts an array based on its elements, in descending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val bestScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedDescending()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by fields of elements.
	 */
	@KtMongoDsl
	fun <Context : Any, T> Field<Context, Collection<T>>.sortedDescending(): Value<Context, List<T>> =
		of(this).sortedDescending()

	/**
	 * Sorts an array based on its elements, in descending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val bestScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedDescending()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by fields of elements.
	 */
	@KtMongoDsl
	fun <Context : Any, T> KProperty1<Context, Collection<T>>.sortedDescending(): Value<Context, List<T>> =
		of(this).sortedDescending()

	/**
	 * Sorts an array based on its elements, in descending order.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 *
	 * class Player(
	 *     val _id: ObjectId,
	 *     val scores: List<Int>,
	 *     val bestScores: List<Int>,
	 * )
	 *
	 * players.updateManyWithPipeline {
	 *     set {
	 *         Player::bestScores set Player::scores
	 *             .sortedDescending()
	 *             .take(5)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/)
	 *
	 * @see sortedBy Sort by fields of elements.
	 * @see sortedDescending Sort by fields of elements.
	 */
	@KtMongoDsl
	fun <Context : Any, T> Collection<T>.sortedDescending(): Value<Context, List<T>> =
		of(this).sortedDescending()

	@LowLevelApi
	private class SortSelfValueOperator(
		private val order: Int,
		context: BsonContext,
	) : AbstractValue<Any, Nothing>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeInt32(order)
		}
	}

	@LowLevelApi
	private class SortValueOperator<Context : Any, T>(
		private val input: Value<Context, Collection<T>>,
		private val sortOrder: Value<T & Any, Nothing>,
		context: BsonContext
	) : AbstractValue<Context, List<T>>(context) {

		init {
			sortOrder.freeze()
		}

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument("\$sortArray") {
					write("input") {
						input.writeTo(this)
					}

					write("sortBy") {
						sortOrder.writeTo(this)
					}
				}
			}
		}
	}

	// endregion

}
