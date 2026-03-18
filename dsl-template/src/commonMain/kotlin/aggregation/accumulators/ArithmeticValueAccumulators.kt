/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation.accumulators

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AccumulationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import kotlin.reflect.KProperty1

/**
 * Accumulators to perform arithmetic operations.
 *
 * To learn more about accumulation operators, see [AccumulationOperators].
 */
@KtMongoDsl
interface ArithmeticValueAccumulators<From : Any, Into : Any> : ValueAccumulators<From, Into> {

	// region $sum

	/**
	 * Calculates and returns the collective sum of numeric values.
	 * Non-numeric values are ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val totalBalance: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::totalBalance sum of(User::balance)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sum/#mongodb-group-grp.-sum)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes T : Number> Field<Into, T>.sum(value: Value<From, Number?>) {
		accept(ArithmeticValueAccumulator("\$sum", value, this.path, context))
	}

	/**
	 * Calculates and returns the collective sum of numeric values.
	 * Non-numeric values are ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val totalBalance: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::totalBalance sum of(User::balance)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sum/#mongodb-group-grp.-sum)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes T : Number> KProperty1<Into, T>.sum(value: Value<From, Number?>) {
		this.field.sum(value)
	}

	// endregion
	// region $avg

	/**
	 * Calculates and returns the collective average of numeric values.
	 * Non-numeric values are ignored.
	 *
	 * If all elements are non-numeric, `null` is returned.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val totalBalance: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::totalBalance average of(User::balance)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes T : Number> Field<Into, T>.average(value: Value<From, Number?>) {
		accept(ArithmeticValueAccumulator("\$avg", value, this.path, context))
	}

	/**
	 * Calculates and returns the collective average of numeric values.
	 * Non-numeric values are ignored.
	 *
	 * If all elements are non-numeric, `null` is returned.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val totalBalance: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::totalBalance average of(User::balance)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes T : Number> KProperty1<Into, T>.average(value: Value<From, Number?>) {
		this.field.average(value)
	}

	// endregion
	// region $median

	/**
	 * Returns an approximation of the median, the 50th percentile, as a scalar value.
	 *
	 * The median is computed with the [t-digest algorithm](https://arxiv.org/abs/1902.04023), which computes an approximation.
	 * The result may vary, even on the same dataset.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val medianBalance: Double,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::medianBalance median of(User::balance)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/median/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes T : Number> Field<Into, T>.median(value: Value<From, Number?>) {
		accept(MedianValueAccumulator(value, this.path, context))
	}

	/**
	 * Returns an approximation of the median, the 50th percentile, as a scalar value.
	 *
	 * The median is computed with the [t-digest algorithm](https://arxiv.org/abs/1902.04023), which computes an approximation.
	 * The result may vary, even on the same dataset.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val medianBalance: Double,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::medianBalance median of(User::balance)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/median/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes T : Number> KProperty1<Into, T>.median(value: Value<From, Number?>) {
		this.field median value
	}

	// endregion
	// region $percentile

	/**
	 * Returns an approximation of the specified [percentiles].
	 *
	 * Each percentile is computed with the [t-digest algorithm](https://arxiv.org/abs/1902.04023), which computes an approximation.
	 * The results may vary, even on the same dataset.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val percentiles: List<Double>,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::percentiles.percentiles(of(User::balance), 0.5, 0.75, 0.9, 0.95)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/percentiles/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes T : Number> Field<Into, List<T>>.percentiles(
		value: Value<From, Number?>,
		vararg percentiles: Double,
	) {
		accept(PercentileValueAccumulator(value, this.path, percentiles.asList(), context))
	}

	/**
	 * Returns an approximation of the specified [percentiles].
	 *
	 * Each percentile is computed with the [t-digest algorithm](https://arxiv.org/abs/1902.04023), which computes an approximation.
	 * The results may vary, even on the same dataset.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val balance: Int,
	 * )
	 *
	 * class Result(
	 *     val percentiles: List<Double>,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         Result::percentiles.percentiles(of(User::balance), 0.5, 0.75, 0.9, 0.95)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/percentiles/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes T : Number> KProperty1<Into, List<T>>.percentiles(
		value: Value<From, Number?>,
		vararg percentiles: Double,
	) {
		this.field.percentiles(value, percentiles = percentiles)
	}

	// endregion

	@LowLevelApi
	private class ArithmeticValueAccumulator(
		val operator: String,
		val value: Value<*, *>,
		val into: Path,
		context: BsonContext,
	) : AbstractBsonNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(into.toString()) {
				write(operator) {
					value.writeTo(this)
				}
			}
		}
	}

	@LowLevelApi
	private class MedianValueAccumulator(
		val value: Value<*, *>,
		val into: Path,
		context: BsonContext,
	) : AbstractBsonNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(into.toString()) {
				writeDocument($$"$median") {
					write("input") {
						value.writeTo(this)
					}
					writeString("method", "approximate")
				}
			}
		}
	}

	@LowLevelApi
	private class PercentileValueAccumulator(
		val value: Value<*, *>,
		val into: Path,
		val percentiles: List<Double>,
		context: BsonContext,
	) : AbstractBsonNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(into.toString()) {
				writeDocument($$"$percentile") {
					write("input") {
						value.writeTo(this)
					}
					writeString("method", "approximate")
					writeArray("p") {
						percentiles.forEach { percentile ->
							writeDouble(percentile)
						}
					}
				}
			}
		}
	}
}
