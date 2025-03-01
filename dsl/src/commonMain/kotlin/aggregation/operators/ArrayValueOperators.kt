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
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.ValueDsl
import opensavvy.ktmongo.dsl.path.Field
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

}
