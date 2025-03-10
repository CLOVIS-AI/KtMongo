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

import opensavvy.ktmongo.bson.types.Decimal128
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.*
import org.bson.BsonArray
import org.bson.codecs.Encoder
import org.bson.codecs.EncoderContext

@OptIn(LowLevelApi::class)
private class JavaBsonWriter(
	private val writer: BsonWriter
) : BsonFieldWriter, BsonValueWriter {
	override fun write(name: String, block: BsonValueWriter.() -> Unit) {
		writer.writeName(name)
		block()
	}

	override fun writeBoolean(name: String, value: Boolean) {
		writer.writeBoolean(name, value)
	}

	override fun writeDouble(name: String, value: Double) {
		writer.writeDouble(name, value)
	}

	override fun writeInt32(name: String, value: Int) {
		writer.writeInt32(name, value)
	}

	override fun writeInt64(name: String, value: Long) {
		writer.writeInt64(name, value)
	}

	override fun writeDateTime(name: String, value: Long) {
		writer.writeDateTime(name, value)
	}

	override fun writeNull(name: String) {
		writer.writeNull(name)
	}

	override fun writeRegularExpression(name: String, pattern: String, options: String) {
		writer.writeRegularExpression(name, BsonRegularExpression(pattern, options))
	}

	override fun writeString(name: String, value: String) {
		writer.writeString(name, value)
	}

	override fun writeTimestamp(name: String, value: Long) {
		writer.writeTimestamp(name, BsonTimestamp(value))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(name: String, value: String) {
		writer.writeSymbol(name, value)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeUndefined(name: String) {
		writer.writeUndefined(name)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeJavaScriptWithScope(name: String, code: String) {
		writer.writeJavaScriptWithScope(name, code)
	}

	override fun writeBinaryData(name: String, type: Byte, data: ByteArray) {
		writer.writeBinaryData(name, BsonBinary(type, data))
	}

	override fun writeJavaScript(name: String, code: String) {
		writer.writeJavaScript(name, code)
	}

	override fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit) {
		writer.writeStartDocument(name)
		block()
		writer.writeEndDocument()
	}

	override fun writeArray(name: String, block: BsonValueWriter.() -> Unit) {
		writer.writeStartArray(name)
		block()
		writer.writeEndArray()
	}

	override fun <T> writeObjectSafe(name: String, obj: T, context: BsonContext) {
		writer.writeName(name)
		writeObjectSafe(obj, context)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeDBPointer(name: String, namespace: String, id: ObjectId) {
		writer.writeDBPointer(name, BsonDbPointer(namespace, id))
	}

	override fun writeObjectId(name: String, value: ObjectId) {
		writer.writeObjectId(name, value)
	}

	override fun writeDecimal128(name: String, value: Decimal128) {
		writer.writeDecimal128(name, value)
	}

	override fun writeBoolean(value: Boolean) {
		writer.writeBoolean(value)
	}

	override fun writeDouble(value: Double) {
		writer.writeDouble(value)
	}

	override fun writeInt32(value: Int) {
		writer.writeInt32(value)
	}

	override fun writeInt64(value: Long) {
		writer.writeInt64(value)
	}

	override fun writeDateTime(value: Long) {
		writer.writeDateTime(value)
	}

	override fun writeNull() {
		writer.writeNull()
	}

	override fun writeRegularExpression(pattern: String, options: String) {
		writer.writeRegularExpression(BsonRegularExpression(pattern, options))
	}

	override fun writeString(value: String) {
		writer.writeString(value)
	}

	override fun writeTimestamp(value: Long) {
		writer.writeTimestamp(BsonTimestamp(value))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(value: String) {
		writer.writeSymbol(value)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeUndefined() {
		writer.writeUndefined()
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeJavaScriptWithScope(code: String) {
		writer.writeJavaScriptWithScope(code)
	}

	override fun writeBinaryData(type: Byte, data: ByteArray) {
		writer.writeBinaryData(BsonBinary(type, data))
	}

	override fun writeJavaScript(code: String) {
		writer.writeJavaScript(code)
	}

	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		writer.writeStartDocument()
		block()
		writer.writeEndDocument()
	}

	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		writer.writeStartArray()
		block()
		writer.writeEndArray()
	}

	override fun <T> writeObjectSafe(obj: T, context: BsonContext) {
		if (obj == null) {
			writer.writeNull()
		} else {
			@Suppress("UNCHECKED_CAST", "UNNECESSARY_NOT_NULL_ASSERTION")
			val codec = context.codecRegistry.get(obj!!::class.java) as Encoder<T>
			codec.encode(
				writer,
				obj,
				EncoderContext.builder()
					.isEncodingCollectibleDocument(true)
					.build()
			)
		}
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeDBPointer(namespace: String, id: ObjectId) {
		writer.writeDBPointer(BsonDbPointer(namespace, id))
	}

	override fun writeObjectId(value: ObjectId) {
		writer.writeObjectId(value)
	}

	override fun writeDecimal128(value: Decimal128) {
		writer.writeDecimal128(value)
	}

}

/**
 * Creates a BSON document with the contents defined in [block].
 *
 * The returned type is from the official MongoDB BSON library.
 */
@LowLevelApi
actual fun buildBsonDocument(block: BsonFieldWriter.() -> Unit): Bson {
	val document = BsonDocument()

	BsonDocumentWriter(document).use { writer ->
		JavaBsonWriter(writer).writeDocument {
			block()
		}
	}

	return document
}

@LowLevelApi
private class JavaRootArrayWriter(
	private val array: BsonArray,
) : BsonValueWriter {
	@LowLevelApi
	override fun writeBoolean(value: Boolean) {
		array.add(BsonBoolean(value))
	}

	@LowLevelApi
	override fun writeDouble(value: Double) {
		array.add(BsonDouble(value))
	}

	@LowLevelApi
	override fun writeInt32(value: Int) {
		array.add(BsonInt32(value))
	}

	@LowLevelApi
	override fun writeInt64(value: Long) {
		array.add(BsonInt64(value))
	}

	@LowLevelApi
	override fun writeDecimal128(value: Decimal128) {
		array.add(BsonDecimal128(value))
	}

	@LowLevelApi
	override fun writeDateTime(value: Long) {
		array.add(BsonDateTime(value))
	}

	@LowLevelApi
	override fun writeNull() {
		array.add(BsonNull())
	}

	@LowLevelApi
	override fun writeObjectId(value: ObjectId) {
		array.add(BsonObjectId(value))
	}

	@LowLevelApi
	override fun writeRegularExpression(pattern: String, options: String) {
		array.add(BsonRegularExpression(pattern, options))
	}

	@LowLevelApi
	override fun writeString(value: String) {
		array.add(BsonString(value))
	}

	@LowLevelApi
	override fun writeTimestamp(value: Long) {
		array.add(BsonTimestamp(value))
	}

	@LowLevelApi
	override fun writeSymbol(value: String) {
		array.add(BsonSymbol(value))
	}

	@LowLevelApi
	override fun writeUndefined() {
		array.add(BsonUndefined())
	}

	@LowLevelApi
	override fun writeDBPointer(namespace: String, id: ObjectId) {
		array.add(BsonDbPointer(namespace, id))
	}

	@LowLevelApi
	override fun writeJavaScriptWithScope(code: String) {
		array.add(BsonJavaScript(code))
	}

	@LowLevelApi
	override fun writeBinaryData(type: Byte, data: ByteArray) {
		array.add(BsonBinary(type, data))
	}

	@LowLevelApi
	override fun writeJavaScript(code: String) {
		array.add(BsonJavaScript(code))
	}

	@LowLevelApi
	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		array.add(buildBsonDocument(block))
	}

	@LowLevelApi
	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		array.add(buildBsonArray(block).raw)
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(obj: T, context: BsonContext) {
		val document = BsonDocument()

		BsonDocumentWriter(document).use { writer ->
			JavaBsonWriter(writer).writeObjectSafe(obj, context)
		}

		array.add(document)
	}

}

/**
 * Creates a BSON document with the contents defined in [block].
 *
 * The returned type is from the official MongoDB BSON library.
 */
@LowLevelApi
actual fun buildBsonArray(block: BsonValueWriter.() -> Unit): opensavvy.ktmongo.bson.BsonArray {
	val nativeArray = BsonArray()
	val array = opensavvy.ktmongo.bson.BsonArray(nativeArray)

	JavaRootArrayWriter(nativeArray).block()

	return array
}
