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
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value

/**
 * Operators to arithmetically combine two or more values.
 *
 * To learn more about aggregation operators, see [AggregationOperators].
 */
interface ArithmeticValueOperators : ValueOperators {

	// region $abs

	/**
	 * The absolute value of a number.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val name: String,
	 *     val startTemp: Int,
	 *     val endTemp: Int,
	 *     val diffTemp: Int,
	 * )
	 *
	 * collection.updateManyWithPipeline(filter = { Sensor::diffTemp.isNull() }) {
	 *     set {
	 *         Sensor::diffTemp set abs(of(Sensor::startTemp) - of(Sensor::endTemp))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/abs/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <Context : Any, @kotlin.internal.OnlyInputTypes Result : Number?> abs(value: Value<Context, Result>): Value<Context, Result> =
		UnarySameTypeValueOperator(context, "abs", value)

	// endregion
	// region $add

	/**
	 * Sums two aggregation values.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val name: String,
	 *     val price: Int,
	 *     val dailyPriceIncrease: Int,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Product::price set (of(Product::price) + of(Product::dailyPriceIncrease))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/add/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <Context : Any, @kotlin.internal.OnlyInputTypes Result> Value<Context, Result>.plus(other: Value<Context, Result>): Value<Context, Result> =
		AdditionValueOperator(context, listOf(this, other))

	@OptIn(LowLevelApi::class)
	private class AdditionValueOperator<Context : Any, T>(
		context: BsonContext,
		private val operands: List<Value<Context, T>>
	) : AbstractValue<Context, T>(context) {

		override fun simplify(): AbstractValue<Context, T> {
			val flattenedOperands = ArrayList<Value<Context, T>>()

			for (operand in operands) {
				if (operand is AdditionValueOperator) {
					flattenedOperands += operand.operands
				} else {
					flattenedOperands += operand
				}
			}

			return if (flattenedOperands != operands) {
				AdditionValueOperator(context, flattenedOperands)
			} else {
				this
			}
		}

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$add") {
					for (operand in operands) {
						operand.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region $ceil

	/**
	 * The smallest integer greater than or equal to the specified [value].
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val value: Double,
	 *     val maxBound: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Sensor::maxBound set ceil(of(Sensor::value))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/ceil/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <Context : Any, @kotlin.internal.OnlyInputTypes Result : Number?> ceil(value: Value<Context, Result>): Value<Context, Result> =
		UnarySameTypeValueOperator(context, "ceil", value)

	// endregion
	// region $concat

	/**
	 * Concatenates two strings together.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val firstName: String,
	 *     val lastName: String,
	 *     val fullName: String,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         User::fullName set (of(User::firstName) concat of(" ") concat of(User::lastName))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/concat/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any> Value<Context, String>.concat(other: Value<Context, String>): Value<Context, String> =
		ConcatValueOperator(context, listOf(this, other))

	@OptIn(LowLevelApi::class)
	private class ConcatValueOperator<Context : Any>(
		context: BsonContext,
		private val operands: List<Value<Context, String>>
	) : AbstractValue<Context, String>(context) {

		override fun simplify(): AbstractValue<Context, String> {
			val flattenedOperands = ArrayList<Value<Context, String>>()

			for (operand in operands) {
				if (operand is ConcatValueOperator) {
					flattenedOperands += operand.operands
				} else {
					flattenedOperands += operand
				}
			}

			return if (flattenedOperands != operands) {
				ConcatValueOperator(context, flattenedOperands)
			} else {
				this
			}
		}

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$concat") {
					for (operand in operands) {
						operand.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region $multiply

	/**
	 * Multiplies two or more aggregation values.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sale(
	 *     val price: Double,
	 *     val quantity: Int,
	 *     val total: Double,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Sale::total set (of(Sale::price) * of(Sale::quantity))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/multiply/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <Context : Any, @kotlin.internal.OnlyInputTypes Result> Value<Context, Result>.times(other: Value<Context, Result>): Value<Context, Result> =
		MultiplicationValueOperator(context, listOf(this, other))

	@OptIn(LowLevelApi::class)
	private class MultiplicationValueOperator<Context : Any, T>(
		context: BsonContext,
		private val operands: List<Value<Context, T>>,
	) : AbstractValue<Context, T>(context) {

		override fun simplify(): AbstractValue<Context, T> {
			val flattenedOperands = ArrayList<Value<Context, T>>()

			for (operand in operands) {
				if (operand is MultiplicationValueOperator) {
					flattenedOperands += operand.operands
				} else {
					flattenedOperands += operand
				}
			}

			return if (flattenedOperands != operands) {
				MultiplicationValueOperator(context, flattenedOperands)
			} else {
				this
			}
		}

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$multiply") {
					for (operand in operands) {
						operand.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region $divide

	/**
	 * Divides one aggregation value by another.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class ConferencePlanning(
	 *     val hours: Int,
	 *     val workdays: Double,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         ConferencePlanning::workdays set (of(ConferencePlanning::hours) / of(8))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/divide/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <Context : Any, @kotlin.internal.OnlyInputTypes Result> Value<Context, Result>.div(other: Value<Context, Result>): Value<Context, Result> =
		DivisionValueOperator(context, this, other)

	@OptIn(LowLevelApi::class)
	private class DivisionValueOperator<Context : Any, T>(
		context: BsonContext,
		private val dividend: Value<Context, T>,
		private val divisor: Value<Context, T>,
	) : AbstractValue<Context, T>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$divide") {
					dividend.writeTo(this)
					divisor.writeTo(this)
				}
			}
		}
	}

	// endregion
	// region $subtract

	/**
	 * Subtracts one aggregation value from another.
	 *
	 * The second argument is subtracted from the first argument.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sale(
	 *     val price: Int,
	 *     val fee: Int,
	 *     val discount: Int,
	 *     val total: Int,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Sale::total set (of(Sale::price) + of(Sale::fee) - of(Sale::discount))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/subtract/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <Context : Any, @kotlin.internal.OnlyInputTypes Result> Value<Context, Result>.minus(other: Value<Context, Result>): Value<Context, Result> =
		SubtractionValueOperator(context, this, other)

	@OptIn(LowLevelApi::class)
	private class SubtractionValueOperator<Context : Any, T>(
		context: BsonContext,
		private val minuend: Value<Context, T>,
		private val subtrahend: Value<Context, T>,
	) : AbstractValue<Context, T>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$subtract") {
					minuend.writeTo(this)
					subtrahend.writeTo(this)
				}
			}
		}
	}

	// endregion
	// region $floor

	/**
	 * The largest integer less than or equal to the specified [value].
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Sensor(
	 *     val value: Double,
	 *     val minBound: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Sensor::minBound set floor(of(Sensor::value))
	 *     }.toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/floor/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <Context : Any, @kotlin.internal.OnlyInputTypes Result : Number?> floor(value: Value<Context, Result>): Value<Context, Result> =
		UnarySameTypeValueOperator(context, "floor", value)

	// endregion

	@LowLevelApi
	private class UnarySameTypeValueOperator<Context : Any, T>(
		context: BsonContext,
		private val operator: String,
		private val value: Value<Context, T>,
	) : AbstractValue<Context, T>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("$$operator") {
					value.writeTo(this)
				}
			}
		}
	}
}
