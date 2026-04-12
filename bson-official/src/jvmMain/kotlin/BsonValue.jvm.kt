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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonValue
import opensavvy.ktmongo.bson.official.types.toKtMongo
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Implementation of a KtMongo [opensavvy.ktmongo.bson.BsonValue] that wraps a [org.bson.BsonValue].
 *
 * To create an instance of this class, see [BsonFactory.readValue].
 */
actual class BsonValue internal constructor(
	val raw: org.bson.BsonValue,
	private val factory: BsonFactory,
) : BsonValue {

	override val type: BsonType
		get() = BsonType.fromCode(raw.bsonType.value.toByte())

	private inline fun ensureType(requestedType: BsonType, predicate: () -> Boolean) {
		if (!predicate()) {
			throw BsonDecodingException("Could not read value ‘$raw’ as a $requestedType, because it is a $type")
		}
	}
	
	@LowLevelApi
	override fun <T> decode(type: KType): T {
		val classifier = type.classifier
		require(classifier is KClass<*>) { "The official Java driver only supports types that can be represented as classes\n\tObject: $raw\n\tType: $type" }

		@Suppress("UNCHECKED_CAST")
		classifier as KClass<T & Any>

		// If the underlying BSON value is null, allow returning null for nullable targets
		@Suppress("UNCHECKED_CAST")
		if (raw.isNull && type.isMarkedNullable)
			return null as T

		// If the value is an array but a parameterized Kotlin collection/array is requested,
		// delegate to the array-aware reader to preserve generic element type information.
		if (raw.isArray) {
			return BsonArray(raw.asArray(), factory).decode(type)
		}

		return decodeValue(raw, classifier, factory)
	}

	override fun decodeBoolean(): Boolean {
		ensureType(BsonType.Boolean) { raw.isBoolean }
		return raw.asBoolean().value
	}

	override fun decodeDouble(): Double {
		ensureType(BsonType.Double) { raw.isDouble }
		return raw.asDouble().value
	}

	override fun decodeInt32(): Int {
		ensureType(BsonType.Int32) { raw.isInt32 }
		return raw.asInt32().value
	}

	override fun decodeInt64(): Long {
		ensureType(BsonType.Int64) { raw.isInt64 }
		return raw.asInt64().value
	}

	@LowLevelApi
	override fun decodeDecimal128Bytes(): ByteArray {
		ensureType(BsonType.Decimal128) { raw.isDecimal128 }
		val decimal = raw.asDecimal128().value
		return ByteBuffer.allocate(Long.SIZE_BYTES * 2).apply {
			putLong(decimal.high)
			putLong(decimal.low)
		}.array()
	}

	override fun decodeDateTime(): Long {
		ensureType(BsonType.Datetime) { raw.isDateTime }
		return raw.asDateTime().value
	}

	override fun decodeNull(): Nothing? {
		ensureType(BsonType.Null) { raw.isNull }
		return null
	}

	@LowLevelApi
	override fun decodeObjectIdBytes(): ByteArray {
		ensureType(BsonType.ObjectId) { raw.isObjectId }
		return raw.asObjectId().value.toByteArray()
	}

	override fun decodeRegularExpressionPattern(): String {
		ensureType(BsonType.RegExp) { raw.isRegularExpression }
		return raw.asRegularExpression().pattern
	}

	override fun decodeRegularExpressionOptions(): String {
		ensureType(BsonType.RegExp) { raw.isRegularExpression }
		return raw.asRegularExpression().options
	}

	override fun decodeString(): String {
		ensureType(BsonType.String) { raw.isString }
		return raw.asString().value
	}

	override fun decodeTimestamp(): Timestamp {
		ensureType(BsonType.Timestamp) { raw.isTimestamp }
		return raw.asTimestamp().toKtMongo()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun decodeSymbol(): String {
		ensureType(BsonType.Symbol) { raw.isSymbol }
		return raw.asSymbol().symbol
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun decodeUndefined() {
		ensureType(BsonType.Undefined) { raw.bsonType == org.bson.BsonType.UNDEFINED }
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun decodeDBPointerNamespace(): String {
		ensureType(BsonType.DBPointer) { raw.isDBPointer }
		return raw.asDBPointer().namespace
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun decodeDBPointerId(): ObjectId {
		ensureType(BsonType.DBPointer) { raw.isDBPointer }
		return ObjectId(raw.asDBPointer().id.toByteArray())
	}

	override fun decodeJavaScript(): String {
		return if (raw.isJavaScript) {
			raw.asJavaScript().code
		} else if (raw.isJavaScriptWithScope) {
			raw.asJavaScriptWithScope().code
		} else {
			throw BsonDecodingException("Could not read value '$raw' as JavaScript, because it is a $type")
		}
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun decodeJavaScriptScope(): BsonDocument {
		ensureType(BsonType.JavaScriptWithScope) { raw.isJavaScriptWithScope }
		return BsonDocument(raw.asJavaScriptWithScope().scope, factory)
	}

	override fun decodeBinaryDataType(): UByte {
		ensureType(BsonType.BinaryData) { raw.isBinary }
		return raw.asBinary().type.toUByte()
	}

	override fun decodeBinaryData(): ByteArray {
		ensureType(BsonType.BinaryData) { raw.isBinary }
		return raw.asBinary().data
	}

	override fun decodeMinKey() {
		ensureType(BsonType.MinKey) { raw.bsonType == org.bson.BsonType.MIN_KEY }
	}

	override fun decodeMaxKey() {
		ensureType(BsonType.MaxKey) { raw.bsonType == org.bson.BsonType.MAX_KEY }
	}

	actual override fun decodeDocument(): opensavvy.ktmongo.bson.official.BsonDocument {
		ensureType(BsonType.Document) { raw.isDocument }
		return BsonDocument(raw.asDocument(), factory)
	}

	actual override fun decodeArray(): opensavvy.ktmongo.bson.official.BsonArray {
		ensureType(BsonType.Array) { raw.isArray }
		return BsonArray(raw.asArray(), factory)
	}

	override fun toString(): String {
		// Not as efficient as it could be, but it's the simplest way to guarantee
		// that the representation will be the same as within BSON documents.
		val fakeDoc = org.bson.BsonDocument()
		fakeDoc.append("a", raw)
		return fakeDoc.toJson().removePrefix("{\"a\":").removeSuffix("}").trim()
	}

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		(other is opensavvy.ktmongo.bson.official.BsonValue && raw == other.raw) || (other is BsonValue && BsonValue.equals(this, other))

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int {
		return BsonValue.hashCode(this)
	}
}

internal fun <T> decodeValue(
	value: org.bson.BsonValue,
	kClass: KClass<T & Any>,
	factory: BsonFactory,
): T {
	// At the top-level, only BSON documents exist. Therefore, the Java driver only provides ways
	// to decode BSON documents, and not arrays or other values.
	// In KtMongo, we need to be able to decode arbitrary values, even if they are not top-level.
	// To work around this, we use a temporary BSON document with a single field 'a'.
	val valueHolder = org.bson.BsonDocument("a", value)
	val documentReader: BsonReader = org.bson.BsonDocumentReader(valueHolder)

	// Acquire the codec for the requested type.
	val valueCodec: Codec<T> = factory.codecRegistry.get(unprimitive(kClass).java)

	try {
		// Decode the fake document and extract its only field using a delegating codec.
		val docCodec = FakeDocumentCodec(valueCodec)
		val decoded = docCodec.decode(documentReader, DecoderContext.builder().build())
		return decoded.a
	} catch (e: Exception) {
		throw BsonDecodingException("Could not decode $kClass\n\tfrom value ${factory.readValue(value)}\n\tusing $valueCodec", e)
	}
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
