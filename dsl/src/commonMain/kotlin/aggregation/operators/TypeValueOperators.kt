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

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Operators to interact with type information.
 *
 * To learn more about aggregation operators, view [AggregationOperators].
 */
@KtMongoDsl
interface TypeValueOperators : ValueOperators {

	/**
	 * Gets the [BsonType] of the current value.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.aggregate()
	 *     .project {
	 *         Field.unsafe<Boolean>("nameIsString") set (of(User::name).type eq of(BsonType.String))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/type/)
	 *
	 * @see BsonType List of possible types.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <R : Any> Value<R, *>.type: Value<R, BsonType>
		get() = TypeValue(context, this)

	@OptIn(LowLevelApi::class)
	private class TypeValue<Root : Any>(
		context: BsonContext,
		private val value: Value<Root, *>,
	) : Value<Root, BsonType>, AbstractValue<Root, BsonType>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$type") {
					value.writeTo(this)
				}
			}
		}
	}

	/**
	 * Determines if this value is an array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val data: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .project {
	 *         Field.unsafe<Boolean>("dataIsArray") set of(User::data).isArray
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isArray/)
	 *
	 * @see type Get a value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <R : Any> Value<R, *>.isArray: Value<R, Boolean>
		get() = IsArrayValue(context, this)

	@LowLevelApi
	private class IsArrayValue<Root : Any>(
		context: BsonContext,
		private val value: Value<Root, *>,
	) : Value<Root, Boolean>, AbstractValue<Root, Boolean>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("\$isArray") {
					value.writeTo(this)
				}
			}
		}
	}

	/**
	 * Determines if this value is a number.
	 *
	 * The following types are considered numbers:
	 * - [BsonType.Int32]
	 * - [BsonType.Int64]
	 * - [BsonType.Double]
	 * - [BsonType.Decimal128]
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val data: String,
	 * )
	 *
	 * collection.aggregate()
	 *     .project {
	 *         Field.unsafe<Boolean>("dataIsNumber") set of(User::data).isNumber
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/isNumber/)
	 *
	 * @see type Get a value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <R : Any> Value<R, *>.isNumber: Value<R, Boolean>
		get() = IsNumberValue(context, this)

	@LowLevelApi
	private class IsNumberValue<Root : Any>(
		context: BsonContext,
		private val value: Value<Root, *>,
	) : Value<Root, Boolean>, AbstractValue<Root, Boolean>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$isNumber") {
					value.writeTo(this)
				}
			}
		}
	}

	/**
	 * Converts this value to a [BsonType.Boolean].
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.Boolean] is returned as itself.
	 *
	 * Numeric types ([BsonType.Int32], [BsonType.Int64], [BsonType.Double] and [BsonType.Decimal128]) consider that
	 * 0 is `false` and all other values are `true`.
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * All other types always return `true`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val foo: String
	 * )
	 *
	 * users.aggregate()
	 *     .project {
	 *         Field.unsafe<Boolean>("asBoolean") set of(User::foo).toBoolean()
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toBool)
	 *
	 * @see type Get the value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toBoolean(): Value<R, Boolean> =
		ConvertToValue(context, this, "Bool")

	/**
	 * Converts this value to an [Instant] ([BsonType.Datetime]).
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.Datetime] is returned as itself.
	 *
	 * [BsonType.Int64], [BsonType.Double] and [BsonType.Decimal128] are interpreted as a timestamp from the UNIX epoch
	 * in milliseconds: positive values happen after the epoch, negative values happen before the epoch.
	 *
	 * [BsonType.String] is parsed using the ISO timestamp formats, for example:
	 * - `"2018-03-20"`
	 * - `"2018-03-20T12:00:00Z"`
	 * - `"2018-03-20T12:00:00+0500"`
	 *
	 * [BsonType.ObjectId] and [BsonType.Timestamp] are represented by extracting their timestamp component
	 * (both have a precision of one second).
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * Other types throw an exception.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val modificationDate: Instant
	 * )
	 *
	 * // Update old data
	 * users.updateManyWithPipeline(
	 *     filter = {
	 *         User::modificationType {
	 *             not {
	 *                 hasType(BsonType.String)
	 *             }
	 *         }
	 *     }
	 * ) {
	 *    set {
	 *        User::name set of(User::name).toInstant()
	 *    }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/todate/)
	 *
	 * @see type Get the value's type.
	 */
	@ExperimentalTime
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toInstant(): Value<R, Instant> =
		ConvertToValue(context, this, "Date")

	/**
	 * Converts this value to a [BsonType.Double].
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.Double] is returned as itself.
	 *
	 * [BsonType.Int32] and [BsonType.Int64] are extended to a double.
	 *
	 * [BsonType.Boolean] becomes 1 if `true` and 0 if `false`.
	 *
	 * [BsonType.Decimal128] is converted if it is within the possible range of a double value.
	 * Otherwise, an exception is thrown.
	 *
	 * [BsonType.String] is parsed as a double.
	 * Only base 10 numbers can be parsed.
	 * If the value is outside the range of a double, an exception is thrown.
	 * `"-5.5"` and `"123456"` are two valid examples.
	 *
	 * [BsonType.Datetime] returns the number of milliseconds since the epoch.
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * Other types throw an exception.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Metric(
	 *     val instant: Instant,
	 *     val millis: Double
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         User::millis set of(Metric::instant).toDouble()
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/todouble)
	 *
	 * @see type Get the value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toDouble(): Value<R, Double> =
		ConvertToValue(context, this, "Double")

	/**
	 * Converts this value to an [Int] ([BsonType.Int32]).
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.Int32] is returned as itself.
	 *
	 * [BsonType.Int64] is converted to an Int if it fits into the range.
	 * Otherwise, an exception is thrown.
	 *
	 * [BsonType.Boolean] becomes 1 if `true` and 0 if `false`.
	 *
	 * [BsonType.Double] and [BsonType.Decimal128] are truncated to an integer value.
	 * If this value does not fall in the valid range for an int, an exception is thrown.
	 *
	 * [BsonType.String] is parsed as an int.
	 * Only base 10 numbers can be parsed.
	 * If the value is outside the range of a double, an exception is thrown.
	 * `"-5"` and `"123456"` are two valid examples.
	 * Floating-point numbers are not supported (use [toDouble]).
	 *
	 * [BsonType.Datetime] returns the number of milliseconds since the epoch.
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * Other types throw an exception.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val quantity: Double,
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         Field.unsafe<Int>("quantity") set of(Product::quantity).toInt()
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toint)
	 *
	 * @see type Get the value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toInt(): Value<R, Int> =
		ConvertToValue(context, this, "Int")

	/**
	 * Converts this value to an [Long] ([BsonType.Int64]).
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.Int64] is returned as itself.
	 *
	 * [BsonType.Int32] is extended to a Long.
	 *
	 * [BsonType.Boolean] becomes 1 if `true` and 0 if `false`.
	 *
	 * [BsonType.Double] and [BsonType.Decimal128] are truncated to an integer value.
	 * If this value does not fall in the valid range for a long, an exception is thrown.
	 *
	 * [BsonType.String] is parsed as a double.
	 * Only base 10 numbers can be parsed.
	 * If the value is outside the range of a double, an exception is thrown.
	 * `"-5"` and `"123456"` are two valid examples.
	 * Floating-point numbers are not supported (use [toDouble]).
	 *
	 * [BsonType.Datetime] returns the number of milliseconds since the epoch.
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * Other types throw an exception.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val quantity: Double,
	 * )
	 *
	 * users.aggregate()
	 *     .set {
	 *         Field.unsafe<Long>("quantity") set of(Product::quantity).toLong()
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/tolong)
	 *
	 * @see type Get the value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toLong(): Value<R, Long> =
		ConvertToValue(context, this, "Long")

	/**
	 * Converts this value to an [ObjectId].
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.ObjectId] is returned as itself.
	 *
	 * [BsonType.String] is parsed as an [ObjectId] as a 24-character hexadecimal representation,
	 * for example `"5ab9cbfa31c2ab715d42129e"`.
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * Other types throw an exception.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val _id: ObjectId,
	 * )
	 *
	 * // Migrate ids which were accidentally written as strings:
	 * products.updateManyWithPipeline {
	 *     _id set of(Product::_id).toObjectId()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toObjectId)
	 *
	 * @see type Get the value's type.
	 */
	@ExperimentalTime
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toObjectId(): Value<R, ObjectId> =
		ConvertToValue(context, this, "ObjectId")

	/**
	 * Converts this value to a [String].
	 *
	 * Note: the MongoDB operator is called `toString`, but that name would be ambiguous in Kotlin because of [Any.toString].
	 *
	 * ### Conversion algorithm
	 *
	 * [BsonType.String] is always returned as itself.
	 *
	 * [BsonType.Int32], [BsonType.Int64], [BsonType.Double], [BsonType.Decimal128], [BsonType.Boolean] and
	 * [BsonType.BinaryData] are converted to a string.
	 *
	 * [BsonType.ObjectId] returns its hexadecimal representation.
	 *
	 * [BsonType.Datetime] is formatted in ISO.
	 *
	 * [BsonType.Null] always returns `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val _id: ObjectId,
	 *     val age: Double,
	 * )
	 *
	 * // Migrate ids which were accidentally written as strings:
	 * products.updateManyWithPipeline {
	 *     _id set of(Product::age).toText()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toString)
	 *
	 * @see type Get the value's type.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toText(): Value<R, String> =
		ConvertToValue(context, this, "String")

	/**
	 * Converts a string value to a [Uuid] ([BsonType.BinaryData]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val eventId: Uuid,
	 * )
	 *
	 * // Convert old 'eventId' data which was incorrectly created as strings
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::eventId set of(User::eventId).toUuid()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/toUUID/)
	 *
	 * @see type Get the value's type.
	 */
	@ExperimentalUuidApi
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any> Value<R, *>.toUuid(): Value<R, Uuid> =
		ConvertToValue(context, this, "UUID")

	@LowLevelApi
	private class ConvertToValue<Root : Any, Type : Any>(
		context: BsonContext,
		private val value: Value<Root, *>,
		private val typeName: String,
	) : Value<Root, Type>, AbstractValue<Root, Type>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$to$typeName") {
					value.writeTo(this)
				}
			}
		}
	}
}
