/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

@file:Suppress("DeprecatedCallableAddReplaceWith")

package opensavvy.ktmongo.bson

import org.bson.*
import org.bson.BsonWriter

internal class JavaBsonPrimitiveWriter(
	private val writer: BsonWriter
) : BsonPrimitiveWriter {
	override fun flush() {
		writer.flush()
	}

	override fun writeName(name: String) {
		writer.writeName(name)
	}

	override fun writeBinaryData(type: Byte, data: ByteArray) {
		writer.writeBinaryData(BsonBinary(type, data))
	}

	override fun writeJavaScript(code: String) {
		writer.writeJavaScript(code)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeJavaScriptWithScope(code: String) {
		writer.writeJavaScriptWithScope(code)
	}

	override fun writeStartDocument() {
		writer.writeStartDocument()
	}

	override fun writeEndDocument() {
		writer.writeEndDocument()
	}

	override fun writeStartArray() {
		writer.writeStartArray()
	}

	override fun writeEndArray() {
		writer.writeEndArray()
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeDBPointer(namespace: String, id: ObjectId) {
		writer.writeDBPointer(BsonDbPointer(namespace, id))
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

	override fun writeDecimal128(value: Decimal128) {
		writer.writeDecimal128(value)
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

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(value: String) {
		writer.writeSymbol(value)
	}

	override fun writeTimestamp(value: Long) {
		writer.writeTimestamp(BsonTimestamp(value))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeUndefined() {
		writer.writeUndefined()
	}

	override fun writeObjectId(value: ObjectId) {
		writer.writeObjectId(value)
	}

}

// region JVM-specific extensions

fun BsonPrimitiveWriter.writeBinaryData(
	data: BsonBinary
) {
	writeBinaryData(data.type, data.data)
}

fun BsonPrimitiveWriter.writeBinaryData(
	name: String,
	data: BsonBinary
) {
	writeName(name)
	writeBinaryData(data)
}

@Suppress("DEPRECATION")
@Deprecated(DEPRECATED_IN_BSON_SPEC)
fun BsonPrimitiveWriter.writeDBPointer(
	pointer: BsonDbPointer
) {
	writeDBPointer(pointer.namespace, pointer.id)
}

@Suppress("DEPRECATION")
@Deprecated(DEPRECATED_IN_BSON_SPEC)
fun BsonPrimitiveWriter.writeDBPointer(
	name: String,
	pointer: BsonDbPointer
) {
	writeName(name)
	writeDBPointer(pointer)
}

fun BsonPrimitiveWriter.writeRegularExpression(
	expression: BsonRegularExpression
) {
	writeRegularExpression(expression.pattern, expression.options)
}

fun BsonPrimitiveWriter.writeRegularExpression(
	name: String,
	expression: BsonRegularExpression
) {
	writeName(name)
	writeRegularExpression(expression)
}

// endregion
