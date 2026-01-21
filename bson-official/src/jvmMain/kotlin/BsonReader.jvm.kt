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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.official.types.toKtMongo
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonDocument
import org.bson.BsonReader
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.ExperimentalTime
import org.bson.BsonArray as OfficialBsonArray
import org.bson.BsonDocument as OfficialBsonDocument

@LowLevelApi
internal class JavaBsonDocumentReader(
	private val raw: OfficialBsonDocument,
	private val context: JvmBsonFactory,
) : BsonDocumentReader {
	override fun read(name: String): BsonValueReader? {
		return JavaBsonValueReader(raw[name] ?: return null, context)
	}

	override val entries: Map<String, BsonValueReader>
		get() = raw.mapValues { (_, value) -> JavaBsonValueReader(value, context) }

	override fun toBson(): Bson =
		Bson(raw, context)

	override fun asValue(): BsonValueReader =
		JavaBsonValueReader(raw, context)

	override fun <T : Any> read(type: KType, klass: KClass<T>): T? {
		val codec = context.codecRegistry.get(klass.java)
		return codec.decode(
			raw.asBsonReader(),
			DecoderContext.builder().build()
		)
	}

	override fun toString(): String =
		raw.toString()

	override fun equals(other: Any?): Boolean =
		(other is JavaBsonDocumentReader && raw == other.raw) || (other is BsonDocumentReader && BsonDocumentReader.equals(this, other))

	override fun hashCode(): Int =
		BsonDocumentReader.hashCode(this)
}

@LowLevelApi
internal class JavaBsonArrayReader(
	private val raw: OfficialBsonArray,
	private val context: JvmBsonFactory,
) : BsonArrayReader {
	override fun read(index: Int): BsonValueReader? {
		return JavaBsonValueReader(raw.getOrNull(index) ?: return null, context)
	}

	override val elements: List<BsonValueReader>
		get() = raw.map { JavaBsonValueReader(it, context) }

	override fun toBson(): opensavvy.ktmongo.bson.official.BsonArray =
		BsonArray(raw, context)

	override fun asValue(): BsonValueReader =
		JavaBsonValueReader(raw, context)

	override fun <T : Any> read(type: KType, klass: KClass<T>): T? {
		// Special-case Kotlin collections/arrays so we can preserve generic element type information
		// (the Java codec erases generics and would otherwise decode subdocuments as org.bson.Document).
		val typeArg = type.arguments.firstOrNull()?.type
		val elementKClass = typeArg?.classifier as? KClass<*>
		val elementIsNullable = typeArg?.isMarkedNullable == true

		fun decodeElement(element: BsonValue): Any? {
			if (element.isNull) {
				if (elementIsNullable) return null
				else throw BsonReaderException("Cannot decode null element for non-nullable element type in $type")
			}

			return decodeValue(element, elementKClass ?: (klass as KClass<*>), context.codecRegistry)
		}

		@Suppress("UNCHECKED_CAST")
		return when {
			klass == List::class || klass == MutableList::class || klass == Collection::class || klass == MutableCollection::class -> {
				val out = ArrayList<Any?>()
				for (v in raw) out.add(decodeElement(v))
				out as T
			}

			klass == Set::class || klass == MutableSet::class -> {
				val out = LinkedHashSet<Any?>()
				for (v in raw) out.add(decodeElement(v))
				out as T
			}

			klass.java.isArray && typeArg != null && elementKClass != null && !klass.java.componentType.isPrimitive -> {
				val size = raw.size
				val componentClass = elementKClass.java
				val arrayObj = java.lang.reflect.Array.newInstance(componentClass, size)
				for (i in 0 until size) {
					val v = raw[i]
					val decoded = decodeElement(v)
					java.lang.reflect.Array.set(arrayObj, i, decoded)
				}
				arrayObj as T
			}

			else -> decodeValue(raw, klass, context.codecRegistry)
		}
	}

	override fun toString(): String {
		// Yes, this is very ugly, and probably inefficient.
		// The Java library doesn't provide a way to serialize arrays to JSON.
		// https://www.mongodb.com/community/forums/t/how-to-convert-a-single-bsonvalue-such-as-bsonarray-to-json-in-the-java-bson-library

		val document = BsonDocument("a", raw).toJson()

		return document.substring(
			document.indexOf('['),
			document.lastIndexOf(']') + 1
		).trim()
	}

	override fun equals(other: Any?): Boolean =
		(other is JavaBsonArrayReader && raw == other.raw) || (other is BsonArrayReader && BsonArrayReader.equals(this, other))

	override fun hashCode(): Int =
		BsonArrayReader.hashCode(this)
}

@LowLevelApi
private class JavaBsonValueReader(
	private val value: BsonValue,
	private val context: JvmBsonFactory,
) : BsonValueReader {

	override val type: BsonType
		get() = BsonType.fromCode(value.bsonType.value.toByte())

	private inline fun ensureType(requestedType: BsonType, predicate: () -> Boolean) {
		if (!predicate()) {
			throw BsonReaderException("Could not read value ‘$value’ as a $requestedType, because it is a $type")
		}
	}

	@LowLevelApi
	override fun readBoolean(): Boolean {
		ensureType(BsonType.Boolean) { value.isBoolean }
		return value.asBoolean().value
	}

	@LowLevelApi
	override fun readDouble(): Double {
		ensureType(BsonType.Double) { value.isDouble }
		return value.asDouble().value
	}

	@LowLevelApi
	override fun readInt32(): Int {
		ensureType(BsonType.Int32) { value.isInt32 }
		return value.asInt32().value
	}

	@LowLevelApi
	override fun readInt64(): Long {
		ensureType(BsonType.Int64) { value.isInt64 }
		return value.asInt64().value
	}

	@LowLevelApi
	override fun readDecimal128(): ByteArray {
		ensureType(BsonType.Decimal128) { value.isDecimal128 }
		val decimal = value.asDecimal128().value
		return ByteBuffer.allocate(Long.SIZE_BYTES * 2).apply {
			putLong(decimal.high)
			putLong(decimal.low)
		}.array()
	}

	@LowLevelApi
	override fun readDateTime(): Long {
		ensureType(BsonType.Datetime) { value.isDateTime }
		return value.asDateTime().value
	}

	@LowLevelApi
	override fun readNull() {
		ensureType(BsonType.Null) { value.isNull }
	}

	@LowLevelApi
	override fun readObjectIdBytes(): ByteArray {
		ensureType(BsonType.ObjectId) { value.isObjectId }
		return value.asObjectId().value.toByteArray()
	}

	@ExperimentalTime
	@LowLevelApi
	override fun readObjectId(): ObjectId {
		return ObjectId(readObjectIdBytes())
	}

	@LowLevelApi
	override fun readRegularExpressionPattern(): String {
		ensureType(BsonType.RegExp) { value.isRegularExpression }
		return value.asRegularExpression().pattern
	}

	@LowLevelApi
	override fun readRegularExpressionOptions(): String {
		ensureType(BsonType.RegExp) { value.isRegularExpression }
		return value.asRegularExpression().options
	}

	@LowLevelApi
	override fun readString(): String {
		ensureType(BsonType.String) { value.isString }
		return value.asString().value
	}

	@LowLevelApi
	override fun readTimestamp(): Timestamp {
		ensureType(BsonType.Timestamp) { value.isTimestamp }
		return value.asTimestamp().toKtMongo()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readSymbol(): String {
		ensureType(BsonType.Symbol) { value.isSymbol }
		return value.asSymbol().symbol
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readUndefined() {
		ensureType(BsonType.Undefined) { value.bsonType == org.bson.BsonType.UNDEFINED }
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readDBPointerNamespace(): String {
		ensureType(BsonType.DBPointer) { value.isDBPointer }
		return value.asDBPointer().namespace
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readDBPointerId(): ByteArray {
		ensureType(BsonType.DBPointer) { value.isDBPointer }
		return value.asDBPointer().id.toByteArray()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readJavaScriptWithScope(): String {
		ensureType(BsonType.JavaScriptWithScope) { value.isJavaScriptWithScope }
		return value.asJavaScriptWithScope().code
	}

	@LowLevelApi
	override fun readBinaryDataType(): UByte {
		ensureType(BsonType.BinaryData) { value.isBinary }
		return value.asBinary().type.toUByte()
	}

	@LowLevelApi
	override fun readBinaryData(): ByteArray {
		ensureType(BsonType.BinaryData) { value.isBinary }
		return value.asBinary().data
	}

	@LowLevelApi
	override fun readJavaScript(): String {
		ensureType(BsonType.JavaScript) { value.isJavaScript }
		return value.asJavaScript().code
	}

	@LowLevelApi
	override fun readMinKey() {
		ensureType(BsonType.MinKey) { value.bsonType == org.bson.BsonType.MIN_KEY }
	}

	@LowLevelApi
	override fun readMaxKey() {
		ensureType(BsonType.MaxKey) { value.bsonType == org.bson.BsonType.MAX_KEY }
	}

	@LowLevelApi
	override fun readDocument(): BsonDocumentReader {
		ensureType(BsonType.Document) { value.isDocument }
		return JavaBsonDocumentReader(value.asDocument(), context)
	}

	@LowLevelApi
	override fun readArray(): BsonArrayReader {
		ensureType(BsonType.Array) { value.isArray }
		return JavaBsonArrayReader(value.asArray(), context)
	}

	override fun <T : Any> read(type: KType, klass: KClass<T>): T? {
		// If the underlying BSON value is null, allow returning null for nullable targets
		if (value.isNull) {
			return if (type.isMarkedNullable) null else decodeValue(value, klass, context.codecRegistry)
		}

		// If the value is an array but a parameterized Kotlin collection/array is requested,
		// delegate to the array-aware reader to preserve generic element type information.
		if (value.isArray) {
			return JavaBsonArrayReader(value.asArray(), context).read(type, klass)
		}

		return decodeValue(value, klass, context.codecRegistry)
	}

	override fun toString(): String {
		// Not as efficient as it could be, but it's the simplest way to guarantee
		// that the representation will be the same as within BSON documents.
		val fakeDoc = OfficialBsonDocument()
		fakeDoc.append("a", value)
		return fakeDoc.toJson().removePrefix("{\"a\":").removeSuffix("}").trim()
	}

	override fun equals(other: Any?): Boolean =
		(other is JavaBsonValueReader && value == other.value) || (other is BsonValueReader && BsonValueReader.equals(this, other))

	override fun hashCode(): Int =
		BsonValueReader.hashCode(this)
}

private fun <T : Any> decodeValue(
	value: BsonValue,
	kClass: KClass<T>,
	codecRegistry: CodecRegistry,
): T? {
	// At the top-level, only BSON documents exist. Therefore, the Java driver only provides ways
	// to decode BSON documents, and not arrays or other values.
	// In KtMongo, we need to be able to decode arbitrary values, even if they are not top-level.
	// To work around this, we use a temporary BSON document with a single field 'a'.
	val valueHolder = BsonDocument("a", value)
	val documentReader: BsonReader = org.bson.BsonDocumentReader(valueHolder)

	// Acquire the codec for the requested type.
	val valueCodec: Codec<T> = codecRegistry.get(unprimitive(kClass).java)

	// Decode the fake document and extract its only field using a delegating codec.
	val docCodec = FakeDocumentCodec(valueCodec)
	val decoded = docCodec.decode(documentReader, DecoderContext.builder().build())
	@Suppress("UNCHECKED_CAST")
	return decoded.a as T?
}

private class FakeDocument<T>(
	val a: T,
)

private class FakeDocumentCodec<T>(
	private val valueCodec: Codec<T>,
) : Codec<FakeDocument<T>> {
	override fun decode(reader: BsonReader, decoderContext: DecoderContext): FakeDocument<T> {
		reader.readStartDocument()
		reader.readName("a")
		val value = valueCodec.decode(reader, decoderContext)
		reader.readEndDocument()
		return FakeDocument(value)
	}

	override fun encode(writer: BsonWriter, value: FakeDocument<T>, encoderContext: EncoderContext) {
		writer.writeStartDocument()
		writer.writeName("a")
		valueCodec.encode(writer, value.a, encoderContext)
		writer.writeEndDocument()
	}

	override fun getEncoderClass(): Class<FakeDocument<T>> {
		@Suppress("UNCHECKED_CAST")
		return FakeDocument::class.java as Class<FakeDocument<T>>
	}
}

@Suppress("UNCHECKED_CAST") // safe because it's an equality check
private fun <T : Any> unprimitive(kClass: KClass<T>): KClass<T> = when (kClass) {
	Byte::class -> java.lang.Byte::class as KClass<T>
	Short::class -> java.lang.Short::class as KClass<T>
	Int::class -> java.lang.Integer::class as KClass<T>
	Long::class -> java.lang.Long::class as KClass<T>
	Float::class -> java.lang.Float::class as KClass<T>
	Double::class -> java.lang.Double::class as KClass<T>
	Char::class -> java.lang.Character::class as KClass<T>
	Boolean::class -> java.lang.Boolean::class as KClass<T>
	else -> kClass
}
