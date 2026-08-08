/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import kotlin.jvm.JvmName
import kotlin.time.Instant

/**
 * Operators to manage date and time values.
 *
 * To learn more about aggregation operators, view [AggregationOperators].
 */
@KtMongoDsl
interface DateTimeValueOperators : ValueOperators {

	// region $year

	/**
	 * Returns the year portion of a date.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthyear: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthyear set User::birthdate.year
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/year/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.year: Value<R, Int>
		get() = UnaryOperator(context, "year", this)

	/**
	 * Returns the year portion of a date.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthyear: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthyear set User::birthdate.year
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/year/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("yearOfObjectId")
	final val <R : Any> Value<R, ObjectId>.year: Value<R, Int>
		get() = UnaryOperator(context, "year", this)

	/**
	 * Returns the year portion of a date.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthyear: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthyear set User::birthdate.year
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/year/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("yearOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.year: Value<R, Int>
		get() = UnaryOperator(context, "year", this)

	// endregion
	// region $week

	/**
	 * Returns the week of the year for a date, ranging from `0` to `53`.
	 *
	 * Weeks begin on Sundays, and week `1` begins with the first Sunday of the year. Days
	 * preceding the first Sunday of the year are in week `0`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthweek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthweek set User::birthdate.week
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/week/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.week: Value<R, Int>
		get() = UnaryOperator(context, "week", this)

	/**
	 * Returns the week of the year for a date, ranging from `0` to `53`.
	 *
	 * Weeks begin on Sundays, and week `1` begins with the first Sunday of the year. Days
	 * preceding the first Sunday of the year are in week `0`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthweek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthweek set User::birthdate.week
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/week/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("weekOfObjectId")
	final val <R : Any> Value<R, ObjectId>.week: Value<R, Int>
		get() = UnaryOperator(context, "week", this)

	/**
	 * Returns the week of the year for a date, ranging from `0` to `53`.
	 *
	 * Weeks begin on Sundays, and week `1` begins with the first Sunday of the year. Days
	 * preceding the first Sunday of the year are in week `0`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthweek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthweek set User::birthdate.week
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/week/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("weekOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.week: Value<R, Int>
		get() = UnaryOperator(context, "week", this)

	// endregion
	// region $second

	/**
	 * Returns the second portion of a date as a number between `0` and `60` (leap seconds).
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthsecond: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthsecond set User::birthdate.second
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/second/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.second: Value<R, Int>
		get() = UnaryOperator(context, "second", this)

	/**
	 * Returns the second portion of a date as a number between `0` and `60` (leap seconds).
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthsecond: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthsecond set User::birthdate.second
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/second/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("secondOfObjectId")
	final val <R : Any> Value<R, ObjectId>.second: Value<R, Int>
		get() = UnaryOperator(context, "second", this)

	/**
	 * Returns the second portion of a date as a number between `0` and `60` (leap seconds).
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthsecond: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthsecond set User::birthdate.second
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/second/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("secondOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.second: Value<R, Int>
		get() = UnaryOperator(context, "second", this)

	// endregion
	// region $month

	/**
	 * Returns the month of a date as a number between `1` and `12`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthmonth: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthmonth set User::birthdate.month
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/month/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.month: Value<R, Int>
		get() = UnaryOperator(context, "month", this)

	/**
	 * Returns the month of a date as a number between `1` and `12`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthmonth: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthmonth set User::birthdate.month
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/month/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("monthOfObjectId")
	final val <R : Any> Value<R, ObjectId>.month: Value<R, Int>
		get() = UnaryOperator(context, "month", this)

	/**
	 * Returns the month of a date as a number between `1` and `12`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthmonth: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthmonth set User::birthdate.month
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/month/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("monthOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.month: Value<R, Int>
		get() = UnaryOperator(context, "month", this)

	// endregion

	@LowLevelApi
	private class UnaryOperator<Root : Any, Input, Output>(
		context: BsonContext,
		private val operator: String,
		private val input: Value<Root, Input>,
	) : AbstractValue<Root, Output>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("$$operator") {
					input.writeTo(this)
				}
			}
		}
	}
}
