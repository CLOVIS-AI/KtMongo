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

package opensavvy.ktmongo.bson.multiplatform.impl.read

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.BsonReaderException
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.multiplatform.Bytes
import opensavvy.ktmongo.bson.multiplatform.RawBsonWriter
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
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@LowLevelApi
internal class MultiplatformBsonValueReader(
	private val factory: BsonFactory,
	override val type: BsonType,
	private val bytes: Bytes,
) : BsonValueReader {

	private fun checkType(expected: BsonType) {
		if (type != expected)
			throw BsonReaderException("Cannot read this field as a $expected, because it is a $type")
	}

	@LowLevelApi
	override fun readBoolean(): Boolean {
		checkType(BsonType.Boolean)
		val byte = bytes.reader.readUnsignedByte()
		return byte == 1.toUByte() // 1 == true, 0 == false, everything else is forbidden so let's say they're false
	}

	@LowLevelApi
	override fun readDouble(): Double {
		checkType(BsonType.Double)
		return bytes.reader.readDouble()
	}

	@LowLevelApi
	override fun readInt32(): Int {
		checkType(BsonType.Int32)
		return bytes.reader.readInt32()
	}

	@LowLevelApi
	override fun readInt64(): Long {
		checkType(BsonType.Int64)
		return bytes.reader.readInt64()
	}

	@LowLevelApi
	override fun readDecimal128(): ByteArray {
		checkType(BsonType.Decimal128)
		TODO("Not yet implemented")
	}

	override fun <T : Any> read(type: KType, klass: KClass<T>): T? {
		val decoder = BsonDecoder(EmptySerializersModule(), this)
		@Suppress("UNCHECKED_CAST")
		return decoder.decodeSerializableValue(serializer(type) as KSerializer<T?>)
	}

	@LowLevelApi
	override fun readDateTime(): Long {
		checkType(BsonType.Datetime)
		return bytes.reader.readInt64()
	}

	@LowLevelApi
	override fun readNull() {
		checkType(BsonType.Null)
	}

	@LowLevelApi
	override fun readObjectIdBytes(): ByteArray {
		checkType(BsonType.ObjectId)
		return bytes.reader.readBytes(12)
	}

	@ExperimentalTime
	@LowLevelApi
	override fun readObjectId(): ObjectId {
		return ObjectId(readObjectIdBytes())
	}

	@LowLevelApi
	override fun readRegularExpressionPattern(): String {
		checkType(BsonType.RegExp)
		return bytes.reader.readCString()
	}

	@LowLevelApi
	override fun readRegularExpressionOptions(): String {
		checkType(BsonType.RegExp)
		val reader = bytes.reader
		reader.readCString() // pattern
		return reader.readCString() // options
	}

	@LowLevelApi
	override fun readString(): String {
		checkType(BsonType.String)
		return bytes.reader.readString()
	}

	@LowLevelApi
	override fun readTimestamp(): Timestamp {
		checkType(BsonType.Timestamp)
		return Timestamp(bytes.reader.readUInt64())
	}

	@LowLevelApi
	override fun readSymbol(): String {
		checkType(BsonType.Symbol)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readUndefined() {
		checkType(BsonType.Undefined)
	}

	@LowLevelApi
	override fun readDBPointerNamespace(): String {
		checkType(BsonType.DBPointer)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readDBPointerId(): ByteArray {
		checkType(BsonType.DBPointer)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readJavaScriptWithScope(): String {
		checkType(BsonType.JavaScriptWithScope)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readBinaryDataType(): UByte {
		checkType(BsonType.BinaryData)
		return bytes.reader
			.apply { skip(4) } // skip the length
			.readUnsignedByte()
	}

	@LowLevelApi
	override fun readBinaryData(): ByteArray {
		checkType(BsonType.BinaryData)
		val reader = bytes.reader
		val entireLength = reader.readInt32()
		val subtype = reader.readUnsignedByte()

		val dataLength = entireLength
			.takeUnless { subtype == 2.toUByte() }
			?: reader.readInt32() // subtype 2 has an additional length field

		return reader.readBytes(dataLength)
	}

	@LowLevelApi
	override fun readJavaScript(): String {
		checkType(BsonType.JavaScript)
		return bytes.reader.readString()
	}

	@LowLevelApi
	override fun readMinKey() {
		checkType(BsonType.MinKey)
	}

	@LowLevelApi
	override fun readMaxKey() {
		checkType(BsonType.MaxKey)
	}

	@LowLevelApi
	override fun readDocument(): MultiplatformDocumentReader {
		checkType(BsonType.Document)
		return MultiplatformDocumentReader(factory, bytes)
	}

	@LowLevelApi
	override fun readArray(): MultiplatformArrayReader {
		checkType(BsonType.Array)
		return MultiplatformArrayReader(factory, bytes)
	}

	@OptIn(DangerousMongoApi::class)
	internal fun writeTo(writer: RawBsonWriter) {
		writer.writeArbitrary(bytes)
	}

	internal fun eager() {
		when (type) {
			BsonType.Document -> readDocument().eager()
			BsonType.Array -> readArray().eager()
			else -> {}
		}
	}

	@OptIn(ExperimentalTime::class)
	@Suppress("DEPRECATION")
	override fun toString(): String = when (type) {
		BsonType.Boolean -> readBoolean().toString()
		BsonType.Int32 -> readInt32().toString()
		BsonType.Int64 -> readInt64().toString()
		BsonType.Double -> commonDoubleToString(readDouble())
		BsonType.String -> '"' + readString() + '"'
		BsonType.Null -> "null"
		BsonType.Undefined -> """{"${'$'}undefined": true}"""
		BsonType.Document -> readDocument().toString()
		BsonType.Array -> readArray().toString()
		BsonType.JavaScript -> """{"${'$'}code": "${readJavaScript()}"}"""
		BsonType.Datetime -> {
			val time = readDateTime()
			if (time in 0..253402300799999) // Start of the year 1970 … End of the year 9999
				"""{"${'$'}date": "${Instant.fromEpochMilliseconds(time)}"}"""
			else
				"""{"${'$'}date": {"${'$'}numberLong": "$time"}}"""
		}

		BsonType.BinaryData -> {
			val subType = readBinaryDataType()
			val data = readBinaryData()

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
			val timestamp = readTimestamp()

			"""{"${'$'}timestamp": {"t": ${timestamp.instant.epochSeconds}, "i": ${timestamp.counter}}}"""
		}

		BsonType.ObjectId -> """{"${'$'}oid": "${readObjectIdBytes().toHexString()}"}"""
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
}
