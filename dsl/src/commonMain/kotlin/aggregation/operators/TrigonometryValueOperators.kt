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

	// region cos/sin/tan

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

	/**
	 * The inverse hyperbolic cosine (hyperbolic arc cosine) of a value, in radians.
	 *
	 * The value must be in the range `1..∞`.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val name: String,
	 *     val x: Double,
	 *     val y: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Trigonometry::y set acosh(of(Trigonometry::x))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/acosh/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> acosh(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "acosh", value)

	/**
	 * The cosine of a value that is measured in radians.
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
	 *         Triangle::sideB set (of(cos(Triangle::angleA) * of(Triangle::hypotenuse)))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cos/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> cos(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "cos", value)

	/**
	 * The hyperbolic cosine of a value that is measured in radians.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val name: String,
	 *     val angle: Double,
	 *     val cosh: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Trigonometry::cosh set cosh(of(Trigonometry::angle))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cosh/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> cosh(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "cosh", value)

	/**
	 * The inverse sine (arc sine) of a value, in radians.
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
	 *         Triangle::angleA set asin(of(Triangle::sideA) / of(Triangle::hypotenuse))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/asin/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> asin(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "asin", value)

	/**
	 * The inverse hyperbolic sine (hyperbolic arc sine) of a value, in radians.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val name: String,
	 *     val x: Double,
	 *     val y: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Trigonometry::y set asinh(of(Trigonometry::x))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/asinh/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> asinh(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "asinh", value)

	/**
	 * The sine of a value that is measured in radians.
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
	 *         Triangle::sideB set (sin(of(Trigonometry::angleA)) * of(Trigonometry::hypotenuse))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sin/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> sin(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "sin", value)

	/**
	 * The hyperbolic sine of a value that is measured in radians.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val name: String,
	 *     val x: Double,
	 *     val y: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Trigonometry::y set sinh(of(Trigonometry::x))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sinh/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> sinh(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "sinh", value)

	/**
	 * The inverse tangent (arc tangent) of a value, in radians.
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
	 *         Triangle::angleA set atan(of(Triangle::sideB) / of(Triangle::sideA))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/atan/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> atan(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "atan", value)

	/**
	 * The inverse hyperbolic tangent (hyperbolic arc tangent) of a value, in radians.
	 *
	 * The value must be in the range `-1..1`.
	 *
	 * If the value is `null` or `NaN`, it is returned unchanged.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val name: String,
	 *     val x: Double,
	 *     val y: Double,
	 * )
	 *
	 * collection.aggregate()
	 *     .set {
	 *         Trigonometry::y set atanh(of(Trigonometry::x))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/atanh/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any> atanh(value: Value<Context, Double?>): Value<Context, Double?> =
		UnaryTrigonometryOperator(context, "atanh", value)

	// endregion
	// region °/radians

	/**
	 * Converts an angle in degrees to an angle in radians.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val angleADeg: Double,
	 *     val angleARad: Double,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Trigonometry::angleARad set of(Trigonometry::angleADeg).toRadians()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/degreesToRadians/)
	 *
	 * @see toDegrees Opposite operation
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, Double> Value<Context, Double>.toRadians(): Value<Context, Double> =
		UnaryTrigonometryOperator(context, "degreesToRadians", this)

	/**
	 * Converts an angle in radians to an angle in degrees.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Trigonometry(
	 *     val angleADeg: Double,
	 *     val angleARad: Double,
	 * )
	 *
	 * collection.updateManyWithPipeline {
	 *     set {
	 *         Trigonometry::angleADeg set of(Trigonometry::angleARad).toDegrees()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/radiansToDegrees/)
	 *
	 * @see toRadians Opposite operation
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <Context : Any, Double> Value<Context, Double>.toDegrees(): Value<Context, Double> =
		UnaryTrigonometryOperator(context, "radiansToDegrees", this)

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
