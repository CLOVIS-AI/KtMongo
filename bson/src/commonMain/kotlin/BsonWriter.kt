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

import opensavvy.ktmongo.dsl.LowLevelApi

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

	@LowLevelApi fun writeBinaryData(type: Byte, data: ByteArray)
	@LowLevelApi fun writeJavaScript(code: String)

	@LowLevelApi fun writeDocument(block: BsonFieldWriter.() -> Unit)
	@LowLevelApi fun writeArray(block: BsonValueWriter.() -> Unit)

	/**
	 * Writes an arbitrary [obj] into a BSON document.
	 *
	 * All nested values are escaped as necessary such that the result is a completely inert BSON document.
	 */
	@LowLevelApi fun <T> writeObjectSafe(obj: T)
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

	@LowLevelApi fun writeBinaryData(name: String, type: Byte, data: ByteArray)
	@LowLevelApi fun writeJavaScript(name: String, code: String)

	@LowLevelApi fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit)
	@LowLevelApi fun writeArray(name: String, block: BsonValueWriter.() -> Unit)

	/**
	 * Writes an arbitrary [obj] into a BSON document.
	 *
	 * All nested values are escaped as necessary such that the result is a completely inert BSON document.
	 */
	@LowLevelApi fun <T> writeObjectSafe(name: String, obj: T)
}
