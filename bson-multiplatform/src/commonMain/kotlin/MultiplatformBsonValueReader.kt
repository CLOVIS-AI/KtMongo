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

package opensavvy.ktmongo.bson.multiplatform

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@LowLevelApi
internal class MultiplatformBsonValueReader(
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

	@LowLevelApi
	override fun readDateTime(): Long {
		checkType(BsonType.Datetime)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readNull() {
		checkType(BsonType.Null)
	}

	@LowLevelApi
	override fun readObjectId(): ByteArray {
		checkType(BsonType.ObjectId)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readRegularExpressionPattern(): String {
		checkType(BsonType.RegExp)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readRegularExpressionOptions(): String {
		checkType(BsonType.RegExp)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readString(): String {
		checkType(BsonType.String)
		return bytes.reader.readString()
	}

	@LowLevelApi
	override fun readTimestamp(): Long {
		checkType(BsonType.Timestamp)
		TODO("Not yet implemented")
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
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readBinaryData(): ByteArray {
		checkType(BsonType.BinaryData)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readJavaScript(): String {
		checkType(BsonType.JavaScript)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readDocument(): BsonDocumentReader {
		checkType(BsonType.Document)
		return MultiplatformBsonDocumentReader(bytes)
	}

	@LowLevelApi
	override fun readArray(): BsonArrayReader {
		checkType(BsonType.Array)
		return MultiplatformBsonArrayReader(bytes)
	}

	override fun toString(): String = when (type) {
		BsonType.Boolean -> readBoolean().toString()
		BsonType.Int32 -> readInt32().toString()
		BsonType.Int64 -> readInt64().toString()
		BsonType.Double -> commonDoubleToString(readDouble())
		BsonType.String -> '"' + readString() + '"'
		BsonType.Null -> "null"
		BsonType.Document -> readDocument().toString()
		BsonType.Array -> readArray().toString()
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
