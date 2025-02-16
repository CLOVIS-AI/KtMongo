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

/**
 * Operators to arithmetically combine two or more values.
 *
 * To learn more about aggregation operators, see [ValueDsl].
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
		AbsoluteValueOperator(context, value)

	@OptIn(LowLevelApi::class)
	private class AbsoluteValueOperator<Context : Any, T>(
		context: BsonContext,
		private val value: Value<Context, T>
	) : AbstractValue<Context, T>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$abs") {
					value.writeTo(this)
				}
			}
		}
	}

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

}
