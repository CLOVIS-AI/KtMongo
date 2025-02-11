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
 * Operators to perform trigonometric and geometric operators.
 *
 * To learn more about aggregation operators, see [ValueDsl].
 */
interface TrigonometryValueOperators : ValueOperators {

	// region $acos

	/**
	 * The inverse cosine (arc cosine) of a value, in radians.
	 *
	 * The value must be in the range `-1..1`.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Triangle(
	 *     val name: String,
	 *     val sideA: Double,
	 *     val sideB: Double,
	 *     val hypotenuse: Double,
	 *     val angleA: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Triangle::angleA set acos(of(Triangle::sideB) / of(Triangle::hypotenuse))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/acos/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> acos(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "acos", value)

	// endregion

	@OptIn(LowLevelApi::class)
	private class UnaryTrigonometryOperator<Context : Any, T>(
		context: BsonContext,
		private val operatorName: String,
		private val value: Value<Context, T>
	) : AbstractValue<Context, T>(context) {

		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("$$operatorName") {
					value.writeTo(this)
				}
			}
		}
	}

}
