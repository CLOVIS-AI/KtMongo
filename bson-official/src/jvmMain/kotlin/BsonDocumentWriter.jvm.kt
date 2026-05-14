/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.BsonEncodingException
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.bson.official.types.toOfficial
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonBinary
import org.bson.BsonDbPointer
import org.bson.BsonRegularExpression
import org.bson.BsonWriter
import org.bson.codecs.EncoderContext
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import kotlin.reflect.KType

@OptIn(LowLevelApi::class)
internal class JavaBsonDocumentWriter(
	private val factory: BsonFactory,
	private val writer: BsonWriter,
) : BsonFieldWriter, BsonValueWriter {

	private var currentlyWritingInChild: JavaBsonDocumentWriter? = null

	fun verifyNesting() {
		if (currentlyWritingInChild != null) {
			throw BsonEncodingException("""
				Cannot write to an outer document while an inner document is open.
				You may have written something like:
					factory.buildDocument {
						val outer = this
						buildDocument("foo") {
							outer.writeString("bar", "baz")
							// ↑ Incorrect writer! Should be:
							this.writeString("bar", "baz")
						}
					}
			""".trimIndent())
		}
	}

	override fun write(name: String, block: BsonValueWriter.() -> Unit) {
		verifyNesting()

		writer.writeName(name)
		block()
	}

	override fun writeBoolean(name: String, value: Boolean) {
		verifyNesting()

		writer.writeBoolean(name, value)
	}

	override fun writeDouble(name: String, value: Double) {
		verifyNesting()

		writer.writeDouble(name, value)
	}

	override fun writeInt32(name: String, value: Int) {
		verifyNesting()

		writer.writeInt32(name, value)
	}

	override fun writeInt64(name: String, value: Long) {
		verifyNesting()

		writer.writeInt64(name, value)
	}

	override fun writeDateTime(name: String, value: Long) {
		verifyNesting()

		writer.writeDateTime(name, value)
	}

	override fun writeNull(name: String) {
		verifyNesting()

		writer.writeNull(name)
	}

	override fun writeRegularExpression(name: String, pattern: String, options: String) {
		verifyNesting()

		writer.writeRegularExpression(name, BsonRegularExpression(pattern, options))
	}

	override fun writeString(name: String, value: String) {
		verifyNesting()

		writer.writeString(name, value)
	}

	override fun writeTimestamp(name: String, value: Timestamp) {
		verifyNesting()

		writer.writeTimestamp(name, value.toOfficial())
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(name: String, value: String) {
		verifyNesting()

		writer.writeSymbol(name, value)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeUndefined(name: String) {
		verifyNesting()

		writer.writeUndefined(name)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeJavaScriptWithScope(name: String, code: String) {
		verifyNesting()

		writer.writeJavaScriptWithScope(name, code)
	}

	override fun writeBinaryData(name: String, type: UByte, data: ByteArray) {
		verifyNesting()

		writer.writeBinaryData(name, BsonBinary(type.toByte(), data))
	}

	override fun writeJavaScript(name: String, code: String) {
		verifyNesting()

		writer.writeJavaScript(name, code)
	}

	override fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit) {
		verifyNesting()

		writer.writeStartDocument(name)
		val child = JavaBsonDocumentWriter(factory, writer)
		currentlyWritingInChild = child
		child.block()
		currentlyWritingInChild = null
		writer.writeEndDocument()
	}

	override fun writeArray(name: String, block: BsonValueWriter.() -> Unit) {
		verifyNesting()

		writer.writeStartArray(name)
		val child = JavaBsonDocumentWriter(factory, writer)
		currentlyWritingInChild = child
		child.block()
		currentlyWritingInChild = null
		writer.writeEndArray()
	}

	override fun <T> writeSafe(name: String, obj: T, type: KType) {
		verifyNesting()

		writer.writeName(name)
		writeSafe(obj, type)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeDBPointer(name: String, namespace: String, id: ByteArray) {
		verifyNesting()

		writer.writeDBPointer(name, BsonDbPointer(namespace, ObjectId(id)))
	}

	override fun writeObjectId(name: String, id: ByteArray) {
		verifyNesting()

		writer.writeObjectId(name, ObjectId(id))
	}

	override fun writeObjectId(name: String, id: opensavvy.ktmongo.bson.types.ObjectId) {
		verifyNesting()

		writer.writeObjectId(name, id.toOfficial())
	}

	override fun writeDecimal128(name: String, low: Long, high: Long) {
		verifyNesting()

		writer.writeDecimal128(name, Decimal128.fromIEEE754BIDEncoding(high, low))
	}

	override fun writeMinKey(name: String) {
		verifyNesting()

		writer.writeMinKey(name)
	}

	override fun writeMaxKey(name: String) {
		verifyNesting()

		writer.writeMaxKey(name)
	}

	override fun writeBoolean(value: Boolean) {
		verifyNesting()

		writer.writeBoolean(value)
	}

	override fun writeDouble(value: Double) {
		verifyNesting()

		writer.writeDouble(value)
	}

	override fun writeInt32(value: Int) {
		verifyNesting()

		writer.writeInt32(value)
	}

	override fun writeInt64(value: Long) {
		verifyNesting()

		writer.writeInt64(value)
	}

	override fun writeDateTime(value: Long) {
		verifyNesting()

		writer.writeDateTime(value)
	}

	override fun writeNull() {
		verifyNesting()

		writer.writeNull()
	}

	override fun writeRegularExpression(pattern: String, options: String) {
		verifyNesting()

		writer.writeRegularExpression(BsonRegularExpression(pattern, options))
	}

	override fun writeString(value: String) {
		verifyNesting()

		writer.writeString(value)
	}

	override fun writeTimestamp(value: Timestamp) {
		verifyNesting()

		writer.writeTimestamp(value.toOfficial())
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(value: String) {
		verifyNesting()

		writer.writeSymbol(value)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeUndefined() {
		verifyNesting()

		writer.writeUndefined()
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeJavaScriptWithScope(code: String) {
		verifyNesting()

		writer.writeJavaScriptWithScope(code)
	}

	override fun writeBinaryData(type: UByte, data: ByteArray) {
		verifyNesting()

		writer.writeBinaryData(BsonBinary(type.toByte(), data))
	}

	override fun writeJavaScript(code: String) {
		verifyNesting()

		writer.writeJavaScript(code)
	}

	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		verifyNesting()

		writer.writeStartDocument()
		val child = JavaBsonDocumentWriter(factory, writer)
		currentlyWritingInChild = child
		child.block()
		currentlyWritingInChild = null
		writer.writeEndDocument()
	}

	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		verifyNesting()

		writer.writeStartArray()
		val child = JavaBsonDocumentWriter(factory, writer)
		currentlyWritingInChild = child
		child.block()
		currentlyWritingInChild = null
		writer.writeEndArray()
	}

	override fun <T> writeSafe(obj: T, type: KType) {
		verifyNesting()

		if (obj == null) {
			writer.writeNull()
		} else {
			@Suppress("UNCHECKED_CAST", "UNNECESSARY_NOT_NULL_ASSERTION")
			val codec = factory.findCodecForType<T>(type)
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
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		verifyNesting()

		writer.writeDBPointer(BsonDbPointer(namespace, ObjectId(id)))
	}

	override fun writeObjectId(id: ByteArray) {
		verifyNesting()

		writer.writeObjectId(ObjectId(id))
	}

	override fun writeDecimal128(low: Long, high: Long) {
		verifyNesting()

		writer.writeDecimal128(Decimal128.fromIEEE754BIDEncoding(high, low))
	}

	override fun writeMinKey() {
		verifyNesting()

		writer.writeMinKey()
	}

	override fun writeMaxKey() {
		verifyNesting()

		writer.writeMaxKey()
	}
}
