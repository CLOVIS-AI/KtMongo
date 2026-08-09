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
	// region $minute

	/**
	 * Returns the minute portion of a date as a number between `0` and `59`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthminute: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthminute set User::birthdate.minute
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/minute/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.minute: Value<R, Int>
		get() = UnaryOperator(context, "minute", this)

	/**
	 * Returns the minute portion of a date as a number between `0` and `59`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthminute: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthminute set User::birthdate.minute
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/minute/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("minuteOfObjectId")
	final val <R : Any> Value<R, ObjectId>.minute: Value<R, Int>
		get() = UnaryOperator(context, "minute", this)

	/**
	 * Returns the minute portion of a date as a number between `0` and `59`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthminute: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthminute set User::birthdate.minute
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/minute/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("minuteOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.minute: Value<R, Int>
		get() = UnaryOperator(context, "minute", this)

	// endregion
	// region $millisecond

	/**
	 * Returns the millisecond portion of a date as a number between `0` and `999`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthmillisecond: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthmillisecond set User::birthdate.millisecond
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/millisecond/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.millisecond: Value<R, Int>
		get() = UnaryOperator(context, "millisecond", this)

	/**
	 * Returns the millisecond portion of a date as a number between `0` and `999`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthmillisecond: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthmillisecond set User::birthdate.millisecond
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/millisecond/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("millisecondOfObjectId")
	final val <R : Any> Value<R, ObjectId>.millisecond: Value<R, Int>
		get() = UnaryOperator(context, "millisecond", this)

	/**
	 * Returns the millisecond portion of a date as a number between `0` and `999`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthmillisecond: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthmillisecond set User::birthdate.millisecond
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/millisecond/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("millisecondOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.millisecond: Value<R, Int>
		get() = UnaryOperator(context, "millisecond", this)

	// endregion
	// region $isoWeek

	/**
	 * Returns the week number in ISO 8601 format, ranging from `1` to `53`.
	 *
	 * Week numbers start at `1` with the week (Monday through Sunday) that contains the year's
	 * first Thursday.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthweekIso: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthweekIso set User::birthdate.weekIso
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeek/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.weekIso: Value<R, Int>
		get() = UnaryOperator(context, "isoWeek", this)

	/**
	 * Returns the week number in ISO 8601 format, ranging from `1` to `53`.
	 *
	 * Week numbers start at `1` with the week (Monday through Sunday) that contains the year's
	 * first Thursday.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthweekIso: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthweekIso set User::birthdate.weekIso
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeek/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("weekIsoOfObjectId")
	final val <R : Any> Value<R, ObjectId>.weekIso: Value<R, Int>
		get() = UnaryOperator(context, "isoWeek", this)

	/**
	 * Returns the week number in ISO 8601 format, ranging from `1` to `53`.
	 *
	 * Week numbers start at `1` with the week (Monday through Sunday) that contains the year's
	 * first Thursday.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthweekIso: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthweekIso set User::birthdate.weekIso
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeek/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("weekIsoOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.weekIso: Value<R, Int>
		get() = UnaryOperator(context, "isoWeek", this)

	// endregion
	// region $isoWeekYear

	/**
	 * Returns the year in ISO 8601 format, corresponding to the ISO week number returned by
	 * [weekIso].
	 *
	 * The year always starts on the first day of [weekIso] 1, which must be a Monday.
	 * This means that the year may start anywhere between January 1st and January 7th (if January 1st was a Tuesday).
	 * The year ends on the last synday of the last week, which may be at the start of January for the same reason.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthyearOfIsoWeek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthyearOfIsoWeek set User::birthdate.yearOfIsoWeek
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeekYear/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.yearOfIsoWeek: Value<R, Int>
		get() = UnaryOperator(context, "isoWeekYear", this)

	/**
	 * Returns the year in ISO 8601 format, corresponding to the ISO week number returned by
	 * [weekIso].
	 *
	 * The year always starts on the first day of [weekIso] 1, which must be a Monday.
	 * This means that the year may start anywhere between January 1st and January 7th (if January 1st was a Tuesday).
	 * The year ends on the last synday of the last week, which may be at the start of January for the same reason.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthyearOfIsoWeek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthyearOfIsoWeek set User::birthdate.yearOfIsoWeek
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeekYear/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("yearOfIsoWeekOfObjectId")
	final val <R : Any> Value<R, ObjectId>.yearOfIsoWeek: Value<R, Int>
		get() = UnaryOperator(context, "isoWeekYear", this)

	/**
	 * Returns the year in ISO 8601 format, corresponding to the ISO week number returned by
	 * [weekIso].
	 *
	 * The year always starts on the first day of [weekIso] 1, which must be a Monday.
	 * This means that the year may start anywhere between January 1st and January 7th (if January 1st was a Tuesday).
	 * The year ends on the last synday of the last week, which may be at the start of January for the same reason.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthyearOfIsoWeek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthyearOfIsoWeek set User::birthdate.yearOfIsoWeek
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeekYear/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("yearOfIsoWeekOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.yearOfIsoWeek: Value<R, Int>
		get() = UnaryOperator(context, "isoWeekYear", this)

	// endregion
	// region $isoDayOfWeek

	/**
	 * Returns the day of the week in ISO 8601 format, ranging from `1` (for Monday) to `7` (for Sunday).
	 *
	 * To get the current week number, see [weekIso].
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfWeekIso: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfWeekIso set User::birthdate.dayOfWeekIso
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoDayOfWeek/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.dayOfWeekIso: Value<R, Int>
		get() = UnaryOperator(context, "isoDayOfWeek", this)

	/**
	 * Returns the day of the week in ISO 8601 format, ranging from `1` (for Monday) to `7` (for Sunday).
	 *
	 * To get the current week number, see [weekIso].
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfWeekIso: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfWeekIso set User::birthdate.dayOfWeekIso
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoDayOfWeek/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfWeekIsoOfObjectId")
	final val <R : Any> Value<R, ObjectId>.dayOfWeekIso: Value<R, Int>
		get() = UnaryOperator(context, "isoDayOfWeek", this)

	/**
	 * Returns the day of the week in ISO 8601 format, ranging from `1` (for Monday) to `7` (for Sunday).
	 *
	 * To get the current week number, see [weekIso].
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfWeekIso: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfWeekIso set User::birthdate.dayOfWeekIso
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoDayOfWeek/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfWeekIsoOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.dayOfWeekIso: Value<R, Int>
		get() = UnaryOperator(context, "isoDayOfWeek", this)

	// endregion
	// region $hour

	/**
	 * Returns the hour portion of a date as a number between `0` and `23`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthhour: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthhour set User::birthdate.hour
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/hour/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.hour: Value<R, Int>
		get() = UnaryOperator(context, "hour", this)

	/**
	 * Returns the hour portion of a date as a number between `0` and `23`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthhour: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthhour set User::birthdate.hour
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/hour/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("hourOfObjectId")
	final val <R : Any> Value<R, ObjectId>.hour: Value<R, Int>
		get() = UnaryOperator(context, "hour", this)

	/**
	 * Returns the hour portion of a date as a number between `0` and `23`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthhour: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthhour set User::birthdate.hour
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/hour/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("hourOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.hour: Value<R, Int>
		get() = UnaryOperator(context, "hour", this)

	// endregion
	// region $dayOfMonth

	/**
	 * Returns the day of the month for a date as a number between `1` and `31`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfMonth: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfMonth set User::birthdate.dayOfMonth
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfMonth/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.dayOfMonth: Value<R, Int>
		get() = UnaryOperator(context, "dayOfMonth", this)

	/**
	 * Returns the day of the month for a date as a number between `1` and `31`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfMonth: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfMonth set User::birthdate.dayOfMonth
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfMonth/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfMonthOfObjectId")
	final val <R : Any> Value<R, ObjectId>.dayOfMonth: Value<R, Int>
		get() = UnaryOperator(context, "dayOfMonth", this)

	/**
	 * Returns the day of the month for a date as a number between `1` and `31`.
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfMonth: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfMonth set User::birthdate.dayOfMonth
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfMonth/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfMonthOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.dayOfMonth: Value<R, Int>
		get() = UnaryOperator(context, "dayOfMonth", this)

	// endregion
	// region $dayOfWeek

	/**
	 * Returns the day of the week for a date as a number between `1` (Sunday) and `7` (Saturday).
	 *
	 * To use ISO 8601 day-of-week numbering (`1` for Monday to `7` for Sunday), see [dayOfWeekIso].
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfWeek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfWeek set User::birthdate.dayOfWeek
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfWeek/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.dayOfWeek: Value<R, Int>
		get() = UnaryOperator(context, "dayOfWeek", this)

	/**
	 * Returns the day of the week for a date as a number between `1` (Sunday) and `7` (Saturday).
	 *
	 * To use ISO 8601 day-of-week numbering (`1` for Monday to `7` for Sunday), see [dayOfWeekIso].
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfWeek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfWeek set User::birthdate.dayOfWeek
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfWeek/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfWeekOfObjectId")
	final val <R : Any> Value<R, ObjectId>.dayOfWeek: Value<R, Int>
		get() = UnaryOperator(context, "dayOfWeek", this)

	/**
	 * Returns the day of the week for a date as a number between `1` (Sunday) and `7` (Saturday).
	 *
	 * To use ISO 8601 day-of-week numbering (`1` for Monday to `7` for Sunday), see [dayOfWeekIso].
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfWeek: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfWeek set User::birthdate.dayOfWeek
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfWeek/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfWeekOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.dayOfWeek: Value<R, Int>
		get() = UnaryOperator(context, "dayOfWeek", this)

	// endregion

	// region $dayOfYear

	/**
	 * Returns the day of the year for a date as a number between `1` and `366`.
	 *
	 * The year starts on January 1st, so January 1st is day `1`, and December 31st is day `365` (or `366` in a leap year).
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfYear: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfYear set User::birthdate.dayOfYear
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfYear/)
	 */
	@OptIn(LowLevelApi::class)
	val <R : Any> Value<R, Instant>.dayOfYear: Value<R, Int>
		get() = UnaryOperator(context, "dayOfYear", this)

	/**
	 * Returns the day of the year for a date as a number between `1` and `366`.
	 *
	 * The year starts on January 1st, so January 1st is day `1`, and December 31st is day `365` (or `366` in a leap year).
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfYear: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfYear set User::birthdate.dayOfYear
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfYear/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfYearOfObjectId")
	final val <R : Any> Value<R, ObjectId>.dayOfYear: Value<R, Int>
		get() = UnaryOperator(context, "dayOfYear", this)

	/**
	 * Returns the day of the year for a date as a number between `1` and `366`.
	 *
	 * The year starts on January 1st, so January 1st is day `1`, and December 31st is day `365` (or `366` in a leap year).
	 *
	 * The accepted date types are [Instant], [ObjectId] and [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val birthdate: Instant,
	 *     val birthdayOfYear: Int? = null,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthdayOfYear set User::birthdate.dayOfYear
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfYear/)
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	@get:JvmName("dayOfYearOfTimestamp")
	final val <R : Any> Value<R, Timestamp>.dayOfYear: Value<R, Int>
		get() = UnaryOperator(context, "dayOfYear", this)

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
