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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.CompletableBsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.bson.official.types.Jvm
import opensavvy.ktmongo.bson.official.types.KotlinObjectIdCodec
import opensavvy.ktmongo.bson.official.types.toOfficial
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.*
import org.bson.BsonArray
import org.bson.codecs.DecoderContext
import org.bson.codecs.Encoder
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

/**
 * BSON implementation based on the official Java and Kotlin MongoDB drivers.
 */
class JvmBsonContext(
	codecRegistry: CodecRegistry,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
) : BsonContext, ObjectIdGenerator by objectIdGenerator {

	@LowLevelApi
	val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
		codecRegistry,
		CodecRegistries.fromCodecs(
			KotlinBsonCodec(this),
			KotlinBsonArrayCodec(this),
			KotlinObjectIdCodec()
		)
	)

	@LowLevelApi
	override fun buildDocument(block: BsonFieldWriter.() -> Unit): Bson {
		val document = BsonDocument()

		BsonDocumentWriter(document).use { writer ->
			JavaBsonWriter(this, writer).writeDocument {
				block()
			}
		}

		return Bson(document, this)
	}

	@LowLevelApi
	override fun readDocument(bytes: ByteArray): Bson {
		val codec = codecRegistry.get(BsonDocument::class.java)
		val buffer = ByteBuffer.wrap(bytes)
		val document = codec.decode(
			BsonBinaryReader(buffer),
			DecoderContext.builder().build(),
		)
		return Bson(document, this)
	}

	@LowLevelApi
	override fun buildArray(block: BsonValueWriter.() -> Unit): opensavvy.ktmongo.bson.official.BsonArray {
		val nativeArray = BsonArray()

		JavaRootArrayWriter(this, nativeArray).block()

		return BsonArray(nativeArray, this)
	}

	@LowLevelApi
	override fun readArray(bytes: ByteArray): opensavvy.ktmongo.bson.official.BsonArray {
		val codec = codecRegistry.get(BsonArray::class.java)
		val buffer = ByteBuffer.wrap(bytes)
		val document = codec.decode(
			BsonBinaryReader(buffer),
			DecoderContext.builder().build(),
		)
		return BsonArray(document, this)
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openArray(): CompletableBsonValueWriter<opensavvy.ktmongo.bson.official.BsonArray> {
		val nativeArray = BsonArray()
		val writer = JavaRootArrayWriter(this, nativeArray)
		return object: CompletableBsonValueWriter<opensavvy.ktmongo.bson.official.BsonArray>, BsonValueWriter by writer {
			override fun complete(): opensavvy.ktmongo.bson.official.BsonArray {
				return BsonArray(nativeArray, this@JvmBsonContext)
			}
		}
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openDocument(): CompletableBsonFieldWriter<opensavvy.ktmongo.bson.official.Bson>  {
		val document = BsonDocument()
		val officialWriter = BsonDocumentWriter(document)
		val writer = JavaBsonWriter(this, officialWriter)

		return object: CompletableBsonFieldWriter<opensavvy.ktmongo.bson.official.Bson>, BsonFieldWriter by writer {
			override fun complete(): opensavvy.ktmongo.bson.official.Bson {
				officialWriter.close()
				return Bson(document, this@JvmBsonContext)
			}
		}
	}
}

@OptIn(LowLevelApi::class)
private class JavaBsonWriter(
	private val context: JvmBsonContext,
	private val writer: BsonWriter
) : BsonFieldWriter, BsonValueWriter {

	@DangerousMongoApi
	override fun open(name: String): CompletableBsonValueWriter<Unit> {
		writer.writeName(name)
		return object: CompletableBsonValueWriter<Unit>, BsonValueWriter by this {
			override fun complete() {}
		}
	}

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

	override fun writeTimestamp(name: String, value: Timestamp) {
		writer.writeTimestamp(name, value.toOfficial())
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

	override fun writeBinaryData(name: String, type: UByte, data: ByteArray) {
		writer.writeBinaryData(name, BsonBinary(type.toByte(), data))
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

	@DangerousMongoApi
	override fun openDocument(name: String): CompletableBsonFieldWriter<Unit> {
		writer.writeStartDocument(name)
		return object: CompletableBsonFieldWriter<Unit>, BsonFieldWriter by this {
			override fun complete() {
				this@JavaBsonWriter.writer.writeEndDocument()
			}
		}
	}

	@DangerousMongoApi
	override fun openArray(name: String): CompletableBsonValueWriter<Unit> {
		writer.writeStartArray(name)
		return object: CompletableBsonValueWriter<Unit>, BsonValueWriter by this {
			override fun complete() {
				this@JavaBsonWriter.writer.writeEndArray()
			}
		}
	}

	override fun <T> writeObjectSafe(name: String, obj: T) {
		writer.writeName(name)
		writeObjectSafe(obj)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeDBPointer(name: String, namespace: String, id: ByteArray) {
		writer.writeDBPointer(name, BsonDbPointer(namespace, ObjectId(id)))
	}

	override fun writeObjectId(name: String, id: ByteArray) {
		writer.writeObjectId(name, ObjectId(id))
	}

	@ExperimentalTime
	override fun writeObjectId(name: String, id: opensavvy.ktmongo.bson.types.ObjectId) {
		writer.writeObjectId(name, id.toOfficial())
	}

	override fun writeDecimal128(name: String, low: Long, high: Long) {
		writer.writeDecimal128(name, Decimal128.fromIEEE754BIDEncoding(high, low))
	}

	override fun writeMinKey(name: String) {
		writer.writeMinKey(name)
	}

	override fun writeMaxKey(name: String) {
		writer.writeMaxKey(name)
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

	override fun writeTimestamp(value: Timestamp) {
		writer.writeTimestamp(value.toOfficial())
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

	override fun writeBinaryData(type: UByte, data: ByteArray) {
		writer.writeBinaryData(BsonBinary(type.toByte(), data))
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

	@LowLevelApi
	@DangerousMongoApi
	override fun openDocument(): CompletableBsonFieldWriter<Unit> {
		writer.writeStartDocument()
		return object: CompletableBsonFieldWriter<Unit>, BsonFieldWriter by this {
			override fun complete() {
				this@JavaBsonWriter.writer.writeEndDocument()
			}
		}
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openArray(): CompletableBsonValueWriter<Unit> {
		writer.writeStartArray()
		return object: CompletableBsonValueWriter<Unit>, BsonValueWriter by this {
			override fun complete() {
				this@JavaBsonWriter.writer.writeEndArray()
			}
		}
	}

	override fun <T> writeObjectSafe(obj: T) {
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
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		writer.writeDBPointer(BsonDbPointer(namespace, ObjectId(id)))
	}

	override fun writeObjectId(value: ByteArray) {
		writer.writeObjectId(ObjectId(value))
	}

	override fun writeDecimal128(low: Long, high: Long) {
		writer.writeDecimal128(Decimal128.fromIEEE754BIDEncoding(high, low))
	}

	override fun writeMinKey() {
		writer.writeMinKey()
	}

	override fun writeMaxKey() {
		writer.writeMaxKey()
	}
}

@LowLevelApi
private class JavaRootArrayWriter(
	private val context: JvmBsonContext,
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
	override fun writeDecimal128(low: Long, high: Long) {
		array.add(BsonDecimal128(Decimal128.fromIEEE754BIDEncoding(high, low)))
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
	override fun writeObjectId(id: ByteArray) {
		array.add(BsonObjectId(ObjectId(id)))
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
	override fun writeTimestamp(value: Timestamp) {
		array.add(value.toOfficial())
	}

	@LowLevelApi
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(value: String) {
		array.add(BsonSymbol(value))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeUndefined() {
		array.add(BsonUndefined())
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		array.add(BsonDbPointer(namespace, ObjectId(id)))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeJavaScriptWithScope(code: String) {
		array.add(BsonJavaScript(code))
	}

	@LowLevelApi
	override fun writeBinaryData(type: UByte, data: ByteArray) {
		array.add(BsonBinary(type.toByte(), data))
	}

	@LowLevelApi
	override fun writeJavaScript(code: String) {
		array.add(BsonJavaScript(code))
	}

	@LowLevelApi
	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		array.add(context.buildDocument(block).raw)
	}

	@LowLevelApi
	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		array.add(context.buildArray(block).raw)
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openArray(): CompletableBsonValueWriter<Unit> {
		val root = context.openArray()
		return object: CompletableBsonValueWriter<Unit>, BsonValueWriter by root {
			override fun complete(): Unit {
				this@JavaRootArrayWriter.array.add(root.complete().raw)
			}
		}
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openDocument(): CompletableBsonFieldWriter<Unit>  {
		val root = context.openDocument()
		return object: CompletableBsonFieldWriter<Unit>, BsonFieldWriter by root {
			override fun complete(): Unit {
				this@JavaRootArrayWriter.array.add(root.complete().raw)
			}
		}
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(obj: T) {
		val document = BsonDocument()

		BsonDocumentWriter(document).use { writer ->
			JavaBsonWriter(context, writer).writeObjectSafe(obj)
		}

		array.add(document)
	}

	@LowLevelApi
	override fun writeMinKey() {
		array.add(BsonMinKey())
	}

	@LowLevelApi
	override fun writeMaxKey() {
		array.add(BsonMaxKey())
	}

}
