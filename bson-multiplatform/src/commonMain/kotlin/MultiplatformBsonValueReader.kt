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
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readInt32(): Int {
		checkType(BsonType.Int32)
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readInt64(): Long {
		checkType(BsonType.Int64)
		TODO("Not yet implemented")
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
		TODO("Not yet implemented")
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
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun readArray(): BsonArrayReader {
		checkType(BsonType.Array)
		TODO("Not yet implemented")
	}

	override fun toString(): String = when (type) {
		BsonType.Boolean -> readBoolean().toString()
		else -> "{$type}: $bytes" // TODO
	}
}
