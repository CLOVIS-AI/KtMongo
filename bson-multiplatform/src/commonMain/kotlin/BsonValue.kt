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

package opensavvy.ktmongo.bson.multiplatform

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.BsonValue
import opensavvy.ktmongo.bson.multiplatform.serialization.BsonDecoder
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.reflect.KType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Pure Kotlin implementation of [opensavvy.ktmongo.bson.BsonValue].
 *
 * The BSON specification only allows root [documents][BsonDocument], so it is not possible
 * to instantiate this class directly with a complex structure. This class
 * is used to decode the fields of a [BsonDocument] or the items of a [BsonArray].
 *
 * To instantiate a [BsonDocument] or [BsonArray], see [BsonFactory].
 *
 * ### Navigating BSON types
 *
 * This class is part of the BSON trinity:
 *
 * - [BsonDocument][opensavvy.ktmongo.bson.multiplatform.BsonDocument] represents an entire BSON document.
 * - [BsonArray][opensavvy.ktmongo.bson.multiplatform.BsonArray] represents an array of BSON values.
 * - [BsonValue][opensavvy.ktmongo.bson.multiplatform.BsonValue] represents a single value in isolation.
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
 * ### Thread-safety
 *
 * This class is **not thread-safe**.
 * Although it is not possible to mutate its state, this class uses internal mutation to lazily decode the BSON stream.
 */
class BsonValue internal constructor(
	private val factory: BsonFactory,
	override val type: BsonType,
	private val bytes: Bytes,
) : BsonValue {

	private fun checkType(expected: BsonType) {
		if (type != expected)
			throw BsonDecodingException("Cannot read this field as a $expected, because it is a $type")
	}

	@LowLevelApi
	override fun <T> decode(type: KType): T {
		val decoder = BsonDecoder(EmptySerializersModule(), this)
		@Suppress("UNCHECKED_CAST")
		return decoder.decodeSerializableValue(serializer(type) as KSerializer<T>)
	}

	override fun decodeBoolean(): Boolean {
		checkType(BsonType.Boolean)
		val byte = bytes.reader.readUnsignedByte()
		return byte == 1.toUByte() // 1 == true, 0 == false, everything else is forbidden so let's say they're false
	}

	override fun decodeDouble(): Double {
		checkType(BsonType.Double)
		return bytes.reader.readDouble()
	}

	override fun decodeInt32(): Int {
		checkType(BsonType.Int32)
		return bytes.reader.readInt32()
	}

	override fun decodeInt64(): Long {
		checkType(BsonType.Int64)
		return bytes.reader.readInt64()
	}

	@LowLevelApi
	override fun decodeDecimal128Bytes(): ByteArray {
		checkType(BsonType.Decimal128)
		TODO("Not yet implemented")
	}

	override fun decodeDateTime(): Long {
		checkType(BsonType.Datetime)
		return bytes.reader.readInt64()
	}

	override fun decodeNull(): Nothing? {
		checkType(BsonType.Null)
		return null
	}

	@LowLevelApi
	override fun decodeObjectIdBytes(): ByteArray {
		checkType(BsonType.ObjectId)
		return bytes.reader.readBytes(12)
	}

	override fun decodeRegularExpressionPattern(): String {
		checkType(BsonType.RegExp)
		return bytes.reader.readCString()
	}

	override fun decodeRegularExpressionOptions(): String {
		checkType(BsonType.RegExp)
		val reader = bytes.reader
		val _ = reader.readCString() // pattern
		return reader.readCString() // options
	}

	override fun decodeString(): String {
		checkType(BsonType.String)
		return bytes.reader.readString()
	}

	override fun decodeTimestamp(): Timestamp {
		checkType(BsonType.Timestamp)
		return Timestamp(bytes.reader.readUInt64())
	}

	override fun decodeSymbol(): String {
		checkType(BsonType.Symbol)
		TODO("Not yet implemented")
	}

	override fun decodeUndefined() {
		checkType(BsonType.Undefined)
	}

	override fun decodeDBPointerNamespace(): String {
		checkType(BsonType.DBPointer)
		TODO("Not yet implemented")
	}

	override fun decodeDBPointerId(): ObjectId {
		checkType(BsonType.DBPointer)
		TODO("Not yet implemented")
	}

	override fun decodeJavaScript(): String {
		checkType(BsonType.JavaScript)
		return bytes.reader.readString()
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun decodeJavaScriptScope(): BsonDocument {
		checkType(BsonType.JavaScriptWithScope)
		TODO("Not yet implemented")
	}

	override fun decodeBinaryDataType(): UByte {
		checkType(BsonType.BinaryData)
		return bytes.reader
			.apply { skip(4) } // skip the length
			.readUnsignedByte()
	}

	override fun decodeBinaryData(): ByteArray {
		checkType(BsonType.BinaryData)
		val reader = bytes.reader
		val entireLength = reader.readInt32()
		val subtype = reader.readUnsignedByte()

		val dataLength = entireLength
			.takeUnless { subtype == 2.toUByte() }
			?: reader.readInt32() // subtype 2 has an additional length field

		return reader.readBytes(dataLength)
	}

	override fun decodeMinKey() {
		checkType(BsonType.MinKey)
	}

	override fun decodeMaxKey() {
		checkType(BsonType.MaxKey)
	}

	override fun decodeDocument(): BsonDocument {
		checkType(BsonType.Document)
		return BsonDocument(factory, bytes)
	}

	override fun decodeArray(): opensavvy.ktmongo.bson.multiplatform.BsonArray {
		checkType(BsonType.Array)
		return BsonArray(factory, bytes)
	}

	@OptIn(DangerousMongoApi::class)
	internal fun writeTo(writer: RawBsonWriter) {
		writer.writeArbitrary(bytes)
	}

	@OptIn(ExperimentalTime::class)
	@Suppress("DEPRECATION")
	override fun toString(): String = when (type) {
		BsonType.Boolean -> decodeBoolean().toString()
		BsonType.Int32 -> decodeInt32().toString()
		BsonType.Int64 -> decodeInt64().toString()
		BsonType.Double -> commonDoubleToString(decodeDouble())
		BsonType.String -> '"' + decodeString() + '"'
		BsonType.Null -> "null"
		BsonType.Undefined -> """{"${'$'}undefined": true}"""
		BsonType.Document -> decodeDocument().toString()
		BsonType.Array -> decodeArray().toString()
		BsonType.JavaScript -> """{"${'$'}code": "${decodeJavaScript()}"}"""
		BsonType.Datetime -> {
			val time = decodeDateTime()
			if (time in 0..253402300799999) // Start of the year 1970 … End of the year 9999
				"""{"${'$'}date": "${Instant.fromEpochMilliseconds(time)}"}"""
			else
				"""{"${'$'}date": {"${'$'}numberLong": "$time"}}"""
		}

		BsonType.BinaryData -> {
			val subType = decodeBinaryDataType()
			val data = decodeBinaryData()

			@OptIn(ExperimentalEncodingApi::class)
			val base64 = Base64.encode(data)

			"""{"${'$'}binary": {"base64": "$base64", "subType": "${subType.toString(16).padStart(2, '0')}"}}"""
		}

		BsonType.RegExp -> {
			val reader = bytes.reader
			val pattern = reader.readCString()
			val options = reader.readCString()
			val escapedPattern = pattern
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
			"""{"${'$'}regularExpression": {"pattern": "$escapedPattern", "options": "$options"}}"""
		}

		BsonType.Timestamp -> {
			val timestamp = decodeTimestamp()

			"""{"${'$'}timestamp": {"t": ${timestamp.instant.epochSeconds}, "i": ${timestamp.counter}}}"""
		}

		BsonType.ObjectId -> """{"${'$'}oid": "${decodeObjectIdBytes().toHexString()}"}"""
		BsonType.MinKey -> """{"${'$'}minKey": 1}"""
		BsonType.MaxKey -> """{"${'$'}maxKey": 1}"""
		else -> "{$type}: $bytes" // TODO
	}

	private fun commonDoubleToString(value: Double): String {
		// NaN and ±∞ don't exist in JSON, so we have to explicitly specify that we're representing a Double
		if (value.isNaN() || value.isInfinite()) {
			return "{\"\$numberDouble\": \"$value\"}"
		}

		if (abs(value) > 1_000_000.0) {
			val exponent = floor(log10(abs(value))).toInt()
			val scientific = value / 10.0.pow(exponent)
			return """${scientific}E$exponent"""
		}

		var str = value.toString()

		// JS prints decimal numbers as integers when possible, other platforms always have a trailing .0
		if ('.' !in str) {
			str += ".0"
		}

		// JS prints -0.0 as 0.0, other platforms make a difference
		@Suppress("ReplaceCallWithBinaryOperator") // https://youtrack.jetbrains.com/issue/KTIJ-33701
		if (value == 0.0 && value.compareTo(0) < 0 && !str.startsWith('-')) {
			str = "-$str"
		}

		return str
	}

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		(other is opensavvy.ktmongo.bson.multiplatform.BsonValue && bytes == other.bytes) || (other is BsonValue && BsonValue.equals(this, other))

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		BsonValue.hashCode(this)
}
