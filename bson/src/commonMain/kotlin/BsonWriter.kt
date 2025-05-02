/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.experimental.and

/**
 * Annotation to mark types that are part of the BSON writer DSL.
 *
 * To learn more, see [AnyBsonWriter].
 */
@DslMarker
annotation class BsonWriterDsl

/**
 * Parent interface for type parameters that can accept either [BsonValueWriter] or [BsonFieldWriter].
 */
@LowLevelApi
@BsonWriterDsl
sealed interface AnyBsonWriter

/**
 * Generator of BSON values.
 *
 * This interface is used to write a generic BSON value.
 * For example, the root BSON value, or a specific value of a field.
 *
 * To write fields in a BSON document, see [BsonFieldWriter].
 *
 * Instances of this interface are commonly obtained by calling the [BsonContext.buildArray] function.
 */
@LowLevelApi
@BsonWriterDsl
interface BsonValueWriter : AnyBsonWriter {
	@LowLevelApi fun writeBoolean(value: Boolean)
	@LowLevelApi fun writeDouble(value: Double)
	@LowLevelApi fun writeInt32(value: Int)
	@LowLevelApi fun writeInt32(value: Short) = writeInt32(value.toInt())
	@LowLevelApi fun writeInt32(value: Byte) = writeInt32(value.toInt())
	@LowLevelApi fun writeInt64(value: Long)
	@LowLevelApi fun writeDecimal128(low: Long, high: Long)
	@LowLevelApi fun writeDateTime(value: Long)
	@LowLevelApi fun writeNull()
	@LowLevelApi fun writeObjectId(id: ByteArray)
	@LowLevelApi fun writeRegularExpression(pattern: String, options: String)
	@LowLevelApi fun writeString(value: String)
	@LowLevelApi fun writeTimestamp(value: Long)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeSymbol(value: String)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeUndefined()

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeDBPointer(namespace: String, id: ByteArray)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeJavaScriptWithScope(code: String)

	@LowLevelApi
	fun writeBinaryData(type: UByte, data: ByteArray)
	@LowLevelApi fun writeJavaScript(code: String)
	@LowLevelApi
	fun writeMinKey()
	@LowLevelApi
	fun writeMaxKey()

	@LowLevelApi fun writeDocument(block: BsonFieldWriter.() -> Unit)
	@LowLevelApi fun writeArray(block: BsonValueWriter.() -> Unit)

	/**
	 * Writes an arbitrary [obj] into a BSON document.
	 *
	 * All nested values are escaped as necessary such that the result is a completely inert BSON document.
	 */
	@LowLevelApi fun <T> writeObjectSafe(obj: T)

	/**
	 * Writes the arbitrary [obj] into this writer.
	 *
	 * Note that the object will be written as-is, with no safety checks whatsoever.
	 * Only use this method if you are absolutely sure attackers cannot control the contents of [obj].
	 *
	 * If in doubt, prefer using [writeObjectSafe].
	 */
	@LowLevelApi
	@DangerousMongoApi
	fun pipe(obj: BsonValueReader) {
		@Suppress("DEPRECATION")
		when (obj.type) {
			BsonType.Double -> writeDouble(obj.readDouble())
			BsonType.String -> writeString(obj.readString())
			BsonType.Document -> writeDocument {
				for ((name, value) in obj.readDocument().entries) {
					write(name) {
						pipe(value)
					}
				}
			}

			BsonType.Array -> writeArray {
				for (value in obj.readArray().elements) {
					pipe(value)
				}
			}

			BsonType.BinaryData -> writeBinaryData(obj.readBinaryDataType(), obj.readBinaryData())
			BsonType.Undefined -> writeUndefined()
			BsonType.ObjectId -> writeObjectId(obj.readObjectId())
			BsonType.Boolean -> writeBoolean(obj.readBoolean())
			BsonType.Datetime -> writeDateTime(obj.readDateTime())
			BsonType.Null -> writeNull()
			BsonType.RegExp -> writeRegularExpression(obj.readRegularExpressionPattern(), obj.readRegularExpressionOptions())
			BsonType.DBPointer -> writeDBPointer(obj.readDBPointerNamespace(), obj.readDBPointerId())
			BsonType.JavaScript -> writeJavaScript(obj.readJavaScript())
			BsonType.Symbol -> writeSymbol(obj.readSymbol())
			BsonType.JavaScriptWithScope -> writeJavaScriptWithScope(obj.readJavaScriptWithScope())
			BsonType.Int32 -> writeInt32(obj.readInt32())
			BsonType.Timestamp -> writeTimestamp(obj.readTimestamp())
			BsonType.Int64 -> writeInt64(obj.readInt64())
			BsonType.Decimal128 -> {
				val bytes = obj.readDecimal128()
				writeDecimal128(bytes.readLong(0), bytes.readLong(1))
			}

			BsonType.MinKey -> writeMinKey()
			BsonType.MaxKey -> writeMaxKey()
		}
	}
}

private fun ByteArray.readLong(index: Int): Long {
	var value = 0L

	for (i in (index * 8)..<(index * 8 + 8)) {
		value = (value shl 8) + (this[i] and 0xFF.toByte())
	}

	return value
}

/**
 * Generator of BSON document fields.
 *
 * This interface is used to write fields in a BSON document.
 *
 * To write generic values, see [BsonValueWriter].
 *
 * Instances of this interface are commonly obtained by calling the [BsonContext.buildDocument] function.
 */
@LowLevelApi
@BsonWriterDsl
interface BsonFieldWriter : AnyBsonWriter {
	@LowLevelApi fun write(name: String, block: BsonValueWriter.() -> Unit)

	@LowLevelApi fun writeBoolean(name: String, value: Boolean)
	@LowLevelApi fun writeDouble(name: String, value: Double)
	@LowLevelApi fun writeInt32(name: String, value: Int)
	@LowLevelApi fun writeInt32(name: String, value: Short) = writeInt32(name, value.toInt())
	@LowLevelApi fun writeInt32(name: String, value: Byte) = writeInt32(name, value.toInt())
	@LowLevelApi fun writeInt64(name: String, value: Long)
	@LowLevelApi fun writeDecimal128(name: String, low: Long, high: Long)
	@LowLevelApi fun writeDateTime(name: String, value: Long)
	@LowLevelApi fun writeNull(name: String)
	@LowLevelApi fun writeObjectId(name: String, id: ByteArray)
	@LowLevelApi fun writeRegularExpression(name: String, pattern: String, options: String)
	@LowLevelApi fun writeString(name: String, value: String)
	@LowLevelApi fun writeTimestamp(name: String, value: Long)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeSymbol(name: String, value: String)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeUndefined(name: String)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeDBPointer(name: String, namespace: String, id: ByteArray)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi fun writeJavaScriptWithScope(name: String, code: String)

	@LowLevelApi
	fun writeBinaryData(name: String, type: UByte, data: ByteArray)
	@LowLevelApi fun writeJavaScript(name: String, code: String)
	@LowLevelApi
	fun writeMinKey(name: String)
	@LowLevelApi
	fun writeMaxKey(name: String)

	@LowLevelApi fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit)
	@LowLevelApi fun writeArray(name: String, block: BsonValueWriter.() -> Unit)

	/**
	 * Writes an arbitrary [obj] into a BSON document.
	 *
	 * All nested values are escaped as necessary such that the result is a completely inert BSON document.
	 */
	@LowLevelApi fun <T> writeObjectSafe(name: String, obj: T)

	// No 'pipe' overload because it would encourage people to use it.
	// If you really must use 'pipe', use 'write("name") { pipe(…) }'
}
