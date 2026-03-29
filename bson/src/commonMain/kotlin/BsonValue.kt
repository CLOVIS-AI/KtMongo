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

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.bson.types.Vector
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * An arbitrary BSON value.
 *
 * The BSON specification only allows root [documents][BsonDocument], so it is not possible
 * to instantiate this interface directly with a complex structure. This interface
 * is used to decode the fields of a [BsonDocument] or the items of a [BsonArray].
 *
 * To instantiate a [BsonDocument] or [BsonArray], see [BsonFactory].
 *
 * ### Navigating BSON types
 *
 * This interface is part of the BSON trinity:
 *
 * - [BsonDocument] represents an entire BSON document.
 * - [BsonArray] represents an array of BSON values.
 * - [BsonValue] represents a single value in isolation.
 *
 * ### Usage
 *
 * If this BSON value is the serialized form of a Kotlin DTO, see [decode].
 *
 * This interface provides methods for decoding the different BSON native types, like [decodeInt32],
 * [decodeObjectId] and [decodeInstant].
 *
 * Some BSON types cannot be represented by a single Kotlin type, so multiple methods are provided to decode
 * their components. For example: [decodeRegularExpressionPattern] and [decodeRegularExpressionOptions].
 *
 * ### Equality
 *
 * Different implementations of this interface are considered equal if they represent the same value
 * with the same type. That is, both values would result in the exact same BSON sent over the wire.
 *
 * The methods [BsonValue.Companion.equals] and [BsonValue.Companion.hashCode] are provided
 * as default implementations.
 */
interface BsonValue {

	/**
	 * The native [BSON type][BsonType] of this value.
	 */
	val type: BsonType

	/**
	 * Decodes this value into an instance of the Kotlin type [T].
	 *
	 * ### Serialization configuration
	 *
	 * This method uses the serialization methods configured in the [BsonFactory] that created this instance.
	 *
	 * For example, if you use the official Java or Kotlin MongoDB drivers,
	 * this method will use your configured `CodecRegistry`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * data class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * val factory: BsonFactory = …
	 *
	 * val bson = factory.buildDocument {
	 *     writeDocument("personalInfo") {
	 *         writeString("name", "Bob")
	 *         writeInt32("age", 30)
	 *     }
	 * }
	 *
	 * val user = bson["personalInfo"]!!.decode<User>()
	 *
	 * println(user.name)  // Bob
	 * println(user.age)   // 30
	 * ```
	 *
	 * ### Overloads
	 *
	 * Prefer using the parameter-less overload.
	 *
	 * If [type] doesn't match [T], the behavior is unspecified.
	 *
	 * @throws BsonDecodingException If the value cannot be decoded as an instance of [T].
	 */
	@LowLevelApi
	fun <T> decode(type: KType): T

	/**
	 * Decodes this value as a [Boolean].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Boolean].
	 */
	fun decodeBoolean(): Boolean

	/**
	 * Decodes this value as [Double].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Double].
	 */
	fun decodeDouble(): Double

	/**
	 * Decodes this value as [Int].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Int32].
	 */
	fun decodeInt32(): Int

	/**
	 * Decodes this value as [Long].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Int64].
	 */
	fun decodeInt64(): Long

	/**
	 * Decodes this value as the bytes of a `Decimal128`.
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Decimal128].
	 */
	@LowLevelApi
	fun decodeDecimal128Bytes(): ByteArray

	/**
	 * Decodes this value as a UTC timestamp in milliseconds since the Unix epoch.
	 *
	 * @see decodeInstant Decode the same value as a Kotlin [Instant].
	 * @throws BsonDecodingException If the value is not a [BsonType.Datetime].
	 */
	fun decodeDateTime(): Long

	/**
	 * Decodes this value as a Kotlin [Instant].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Datetime].
	 */
	fun decodeInstant(): Instant =
		Instant.fromEpochMilliseconds(decodeDateTime())

	/**
	 * Decodes this value as a Kotlin `null`.
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Null].
	 */
	fun decodeNull(): Nothing?

	/**
	 * Decodes this value as the bytes of an [ObjectId].
	 *
	 * Prefer using [decodeObjectId], which offers utilities to access its different components, like [ObjectId.timestamp].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.ObjectId].
	 */
	@LowLevelApi
	fun decodeObjectIdBytes(): ByteArray

	/**
	 * Decodes this value as a [ObjectId].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.ObjectId].
	 */
	@OptIn(LowLevelApi::class)
	fun decodeObjectId(): ObjectId =
		ObjectId(decodeObjectIdBytes())

	/**
	 * Decodes this value as the pattern of a regular expression.
	 *
	 * A BSON regular expression is composed of a pattern and options.
	 * To decode the options, see [decodeRegularExpressionOptions].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.RegExp].
	 */
	fun decodeRegularExpressionPattern(): String

	/**
	 * Decodes this value as the options of a regular expression.
	 *
	 * A BSON regular expression is composed of a pattern and options.
	 * To decode the pattern, see [decodeRegularExpressionPattern].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.RegExp].
	 */
	fun decodeRegularExpressionOptions(): String

	/**
	 * Decodes this value as a string.
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.String].
	 */
	fun decodeString(): String

	/**
	 * Decodes this value as a [Timestamp].
	 *
	 * [Timestamp] is a special MongoDB type with a precision of one second, which is mainly used in the oplog.
	 *
	 * For everyday use cases, the Kotlin type [Instant] (see [decodeInstant]) is a better alternative,
	 * with millisecond precision.
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Timestamp].
	 */
	fun decodeTimestamp(): Timestamp

	/**
	 * Decodes this value as a symbol.
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Symbol].
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun decodeSymbol(): String

	/**
	 * Decodes this value as an undefined value.
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Undefined].
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun decodeUndefined()

	/**
	 * Decodes this value as the namespace of a database pointer.
	 *
	 * Database pointers are deprecated values that are composed of the name of a collection, and the
	 * identifier of one of its documents.
	 *
	 * To read the identifier, see [decodeDBPointerId].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.DBPointer].
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun decodeDBPointerNamespace(): String

	/**
	 * Decodes this value as the ID of a database pointer.
	 *
	 * Database pointers are deprecated values that are composed of the name of a collection, and the
	 * identifier of one of its documents.
	 *
	 * To read the namespace, see [decodeDBPointerNamespace].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.DBPointer].
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun decodeDBPointerId(): ObjectId

	/**
	 * Decodes this value as JavaScript code.
	 *
	 * If this value has type [BsonType.JavaScriptWithScope], see [decodeJavaScriptScope].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.JavaScript] or [BsonType.JavaScriptWithScope].
	 */
	fun decodeJavaScript(): String

	/**
	 * Decodes this value as a document describing variables some [JavaScript code][decodeJavaScript] has access to.
	 *
	 * The accompanying JavaScript code is accessed with [decodeJavaScript].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.JavaScriptWithScope].
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun decodeJavaScriptScope(): BsonDocument

	/**
	 * Decodes the type of a binary blob.
	 *
	 * The [BsonType.BinaryData] type stores an additional byte to know what kind of data is stored.
	 * To decode the data itself, see [decodeBinaryData].
	 *
	 * The binary data types `0..127` are reserved.
	 * The data types `128..255` are available for custom use.
	 *
	 * The binary data type `0` is a generic subtype that can be used for any usage.
	 *
	 * Note that the KtMongo library provides utilities for some specific binary types:
	 * - To decode UUIDs, use [decode] with the [kotlin.uuid.Uuid] type.
	 * - To decode vectors, see [decodeVector].
	 *
	 * ### External resources
	 *
	 * - [BSON specification](https://bsonspec.org/spec.html#subtype)
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.BinaryData].
	 */
	fun decodeBinaryDataType(): UByte

	/**
	 * Decodes a binary blob.
	 *
	 * The [BsonType.BinaryData] type stores an additional byte to know what kind of data is stored.
	 * To decode that type, see [decodeBinaryDataType].
	 *
	 * The binary data types `0..127` are reserved.
	 * The data types `128..255` are available for custom use.
	 *
	 * The binary data type `0` is a generic subtype that can be used for any usage.
	 *
	 * Note that the KtMongo library provides utilities for some specific binary types:
	 * - To decode UUIDs, use [decode] with the [kotlin.uuid.Uuid] type.
	 * - To decode vectors, see [decodeVector].
	 *
	 * ### External resources
	 *
	 * - [BSON specification](https://bsonspec.org/spec.html#subtype)
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.BinaryData].
	 */
	fun decodeBinaryData(): ByteArray

	/**
	 * Decodes a binary [Vector].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.BinaryData] with subtype `0x09`.
	 */
	@OptIn(LowLevelApi::class)
	fun decodeVector(): Vector {
		val type = decodeBinaryDataType()
		if (type != 0x09u.toUByte())
			throw BsonDecodingException("Vectors use the BSON binary subtype 0x09, but found: $type")
		return Vector.fromBinaryData(decodeBinaryData())
	}

	/**
	 * Decodes the special BSON value [BsonType.MinKey].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.MinKey].
	 */
	fun decodeMinKey()

	/**
	 * Decodes the special BSON value [BsonType.MaxKey].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.MaxKey].
	 */
	fun decodeMaxKey()

	/**
	 * Decodes this value as a nested [BsonDocument].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Document].
	 */
	fun decodeDocument(): BsonDocument

	/**
	 * Decodes this value as a nested [BsonArray].
	 *
	 * @throws BsonDecodingException If the value is not a [BsonType.Array].
	 */
	fun decodeArray(): BsonArray

	companion object {

		/**
		 * Compares two [BsonValue] to verify whether they are equal according to the rules documented in [BsonValue].
		 *
		 * This method may be used in [BsonValue] implementations as a default implementation.
		 */
		@OptIn(ExperimentalTime::class)
		@LowLevelApi
		fun equals(a: BsonValue, b: BsonValue): Boolean {
			if (a.type != b.type)
				return false

			return when (a.type) {
				BsonType.Double -> a.decodeDouble() == b.decodeDouble()
				BsonType.String -> a.decodeString() == b.decodeString()
				BsonType.Document -> a.decodeDocument() == b.decodeDocument()
				BsonType.Array -> a.decodeArray() == b.decodeArray()
				BsonType.BinaryData -> a.decodeBinaryDataType() == b.decodeBinaryDataType() &&
					a.decodeBinaryData().contentEquals(b.decodeBinaryData())

				BsonType.Undefined -> a.decodeUndefined() == b.decodeUndefined()
				BsonType.ObjectId -> a.decodeObjectId() == b.decodeObjectId()
				BsonType.Boolean -> a.decodeBoolean() == b.decodeBoolean()
				BsonType.Datetime -> a.decodeDateTime() == b.decodeDateTime()
				BsonType.Null ->
					@Suppress("SENSELESS_COMPARISON") // it's not, either could throw
					a.decodeNull() == b.decodeNull()
				BsonType.RegExp -> a.decodeRegularExpressionOptions() == b.decodeRegularExpressionOptions() &&
					a.decodeRegularExpressionPattern() == b.decodeRegularExpressionPattern()

				BsonType.DBPointer -> a.decodeDBPointerNamespace() == b.decodeDBPointerNamespace() &&
					a.decodeDBPointerId() == b.decodeDBPointerId()

				BsonType.JavaScript -> a.decodeJavaScript() == b.decodeJavaScript()
				BsonType.Symbol -> a.decodeSymbol() == b.decodeSymbol()
				BsonType.JavaScriptWithScope -> a.decodeJavaScriptScope() == b.decodeJavaScriptScope()
				BsonType.Int32 -> a.decodeInt32() == b.decodeInt32()
				BsonType.Timestamp -> a.decodeTimestamp() == b.decodeTimestamp()
				BsonType.Int64 -> a.decodeInt64() == b.decodeInt64()
				BsonType.Decimal128 -> a.decodeDecimal128Bytes().contentEquals(b.decodeDecimal128Bytes())
				BsonType.MinKey -> a.decodeMinKey() == b.decodeMinKey()
				BsonType.MaxKey -> a.decodeMaxKey() == b.decodeMaxKey()
			}
		}

		/**
		 * Generates a hash code for a [BsonValue] instance, that respects the equality rules documented in [BsonValue].
		 *
		 * This method may be used in [BsonValue] implementations as a default implementation.
		 */
		@OptIn(ExperimentalTime::class)
		@LowLevelApi
		fun hashCode(a: BsonValue): Int {
			return when (a.type) {
				BsonType.Double -> a.decodeDouble().hashCode()
				BsonType.String -> a.decodeString().hashCode()
				BsonType.Document -> a.decodeDocument().hashCode()
				BsonType.Array -> a.decodeArray().hashCode()
				BsonType.BinaryData -> {
					var hashCode = 1
					hashCode = 31 * hashCode + a.decodeBinaryDataType().toInt()
					for (byte in a.decodeBinaryData()) {
						hashCode = 31 * hashCode + byte.toInt()
					}
					hashCode
				}

				BsonType.Undefined -> a.decodeUndefined().hashCode()
				BsonType.ObjectId -> a.decodeObjectId().hashCode()
				BsonType.Boolean -> a.decodeBoolean().hashCode()
				BsonType.Datetime -> a.decodeDateTime().hashCode()
				BsonType.Null -> a.decodeNull().hashCode()
				BsonType.RegExp -> a.decodeRegularExpressionOptions().hashCode() * 31 + a.decodeRegularExpressionPattern().hashCode()
				BsonType.DBPointer -> a.decodeDBPointerNamespace().hashCode() * 31 + a.decodeDBPointerId().hashCode()
				BsonType.JavaScript -> a.decodeJavaScript().hashCode()
				BsonType.Symbol -> a.decodeSymbol().hashCode()
				BsonType.JavaScriptWithScope -> a.decodeJavaScriptScope().hashCode()
				BsonType.Int32 -> a.decodeInt32().hashCode()
				BsonType.Timestamp -> a.decodeTimestamp().hashCode()
				BsonType.Int64 -> a.decodeInt64().hashCode()
				BsonType.Decimal128 -> {
					var hashCode = 1
					for (byte in a.decodeDecimal128Bytes()) {
						hashCode = 31 * hashCode + byte.toInt()
					}
					hashCode
				}

				BsonType.MinKey -> a.decodeMinKey().hashCode()
				BsonType.MaxKey -> a.decodeMaxKey().hashCode()
			}
		}

	}
}

/**
 * Decodes this value into an instance of the Kotlin type [T].
 *
 * ### Serialization configuration
 *
 * This method uses the serialization methods configured in the [BsonFactory] that created this instance.
 *
 * For example, if you use the official Java or Kotlin MongoDB drivers,
 * this method will use your configured `CodecRegistry`.
 *
 * ### Example
 *
 * ```kotlin
 * data class User(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * val factory: BsonFactory = …
 *
 * val bson = factory.buildDocument {
 *     writeDocument("personalInfo") {
 *         writeString("name", "Bob")
 *         writeInt32("age", 30)
 *     }
 * }
 *
 * val user = bson["personalInfo"]!!.decode<User>()
 *
 * println(user.name)  // Bob
 * println(user.age)   // 30
 * ```
 */
@OptIn(LowLevelApi::class)
@Throws(BsonDecodingException::class)
inline fun <reified T> BsonValue.decode(): T =
	decode(typeOf<T>())

/**
 * Exception thrown when decoding a [BsonValue] fails.
 *
 * This typically happens when the expected type doesn't match the data.
 */
class BsonDecodingException(
	message: String,
	cause: Throwable? = null
) : IllegalStateException(message, cause)
