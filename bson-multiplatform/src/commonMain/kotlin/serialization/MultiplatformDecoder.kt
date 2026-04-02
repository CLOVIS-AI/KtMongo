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

package opensavvy.ktmongo.bson.multiplatform.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.*
import opensavvy.ktmongo.bson.types.*
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalSerializationApi
internal class BsonDecoderTopLevel(
	override val serializersModule: SerializersModule,
	private val factory: BsonFactory,
	val bytesWithHeader: Bytes,
) : AbstractDecoder() {
	var out: Any? = null

	override fun decodeNull(): Nothing {
		throw BsonEncodingException("Cannot encode a null value at the top level of BSON")
	}

	override fun decodeValue() {
		throw BsonEncodingException("Cannot encode a value at the top level of BSON")
	}

	@LowLevelApi
	override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
		return when (descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> BsonCompositeDecoder(serializersModule, BsonDocument(factory, bytesWithHeader))
			StructureKind.LIST -> BsonCompositeListDecoder(serializersModule, BsonArray(factory, bytesWithHeader))
			else -> TODO()
		}
	}

	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		throw BsonEncodingException("Cannot encode a value at the top level of BSON")
	}
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
@LowLevelApi
internal class BsonDecoder(
	override val serializersModule: SerializersModule,
	val source: BsonValue,
) : Decoder {
	@ExperimentalSerializationApi
	override fun decodeNotNullMark(): Boolean {
		return source.type != BsonType.Null
	}

	@ExperimentalSerializationApi
	override fun decodeNull(): Nothing? {
		return source.decodeNull()
	}

	override fun decodeBoolean(): Boolean {
		return source.decodeBoolean()
	}

	override fun decodeByte(): Byte {
		return source.decodeInt32().toByte()
	}

	override fun decodeShort(): Short {
		return source.decodeInt32().toShort()
	}

	override fun decodeChar(): Char {
		return source.decodeString().single()
	}

	override fun decodeInt(): Int {
		return source.decodeInt32()
	}

	override fun decodeLong(): Long {
		return source.decodeInt64()
	}

	override fun decodeFloat(): Float {
		return source.decodeDouble().toFloat()
	}

	override fun decodeDouble(): Double {
		return source.decodeDouble()
	}

	override fun decodeString(): String {
		return source.decodeString()
	}

	override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
		return enumDescriptor.getElementIndex(source.decodeString())
	}

	override fun decodeInline(descriptor: SerialDescriptor): Decoder {
		return this
	}

	override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
		return when (descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> BsonCompositeDecoder(serializersModule, source.decodeDocument())
			StructureKind.LIST -> BsonCompositeListDecoder(serializersModule, source.decodeArray())
			else -> TODO()
		}
	}

	private val bytes = ByteArraySerializer().descriptor
	private val objectId = ObjectId.Serializer.descriptor
	private val timestamp = Timestamp.Serializer.descriptor
	private val uuid = Uuid.serializer().descriptor
	private val instant = Instant.serializer().descriptor
	private val vector = Vector.serializer().descriptor
	private val floatVector = FloatVector.serializer().descriptor
	private val booleanVector = BooleanVector.serializer().descriptor
	private val byteVector = ByteVector.serializer().descriptor
	override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
		@Suppress("UNCHECKED_CAST")
		return when (deserializer.descriptor) {
			// Special cases where we provide our own decoder
			bytes -> source.decodeBinaryData() as T
			objectId -> source.decodeObjectId() as T
			timestamp -> source.decodeTimestamp() as T
			uuid -> {
				val subType = source.decodeBinaryDataType()
				check(subType == 3.toUByte() || subType == 4.toUByte()) { "Uuid should be represented by the binary subtypes 3 (deprecated) or 4, found: $subType" }
				Uuid.fromByteArray(source.decodeBinaryData()) as T
			}
			instant -> source.decodeInstant() as T
			vector, floatVector, booleanVector, byteVector -> Vector.fromBinaryData(source.decodeBinaryData()) as T

			// General case: do what the serializer says
			else -> deserializer.deserialize(this)
		}
	}
}

@LowLevelApi
internal class BsonCompositeDecoder(
	override val serializersModule: SerializersModule,
	source: BsonDocument,
) : CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.iterator()
	lateinit var current: BsonDocument.Field
	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if (!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
		current = iterator.next()
		return descriptor.getElementIndex(current.name)
	}

	override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
		return current.value.decodeBoolean()
	}

	override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
		return current.value.decodeInt32().toByte()
	}

	override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
		return current.value.decodeString().single()
	}

	override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
		return current.value.decodeInt32().toShort()
	}

	override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
		return current.value.decodeInt32()
	}

	override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
		return current.value.decodeInt64()
	}

	override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
		return current.value.decodeDouble().toFloat()
	}

	override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
		return current.value.decodeDouble()
	}

	override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
		return current.value.decodeString()
	}

	override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
		return BsonDecoder(serializersModule, current.value)
	}

	override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T {
		return BsonDecoder(serializersModule, current.value).decodeSerializableValue(deserializer)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? {
		return BsonDecoder(serializersModule, current.value).decodeNullableSerializableValue(deserializer)
	}

}

@LowLevelApi
internal class BsonCompositeListDecoder(
	override val serializersModule: SerializersModule,
	source: BsonArray,
) : CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.iterator()
	lateinit var current: BsonValue
	var index = 0
	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if (!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
		current = iterator.next()
		return index++
	}

	override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
		return current.decodeBoolean()
	}

	override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
		return current.decodeInt32().toByte()
	}

	override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
		return current.decodeString().single()
	}

	override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
		return current.decodeInt32().toShort()
	}

	override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
		return current.decodeInt32()
	}

	override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
		return current.decodeInt64()
	}

	override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
		return current.decodeDouble().toFloat()
	}

	override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
		return current.decodeDouble()
	}

	override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
		return current.decodeString()
	}

	override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
		return BsonDecoder(serializersModule, current)
	}

	override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T {
		return BsonDecoder(serializersModule, current).decodeSerializableValue(deserializer)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? {
		return BsonDecoder(serializersModule, current).decodeNullableSerializableValue(deserializer)
	}
}

@ExperimentalSerializationApi
fun <T : Any> decodeFromBson(factory: BsonFactory, bytes: ByteArray, deserializer: DeserializationStrategy<T>): T {
	val decoder = BsonDecoderTopLevel(EmptySerializersModule(), factory, Bytes(bytes.copyOf()))
	return decoder.decodeSerializableValue(deserializer)
}

@ExperimentalSerializationApi
inline fun <reified T : Any> decodeFromBson(factory: BsonFactory, bytes: ByteArray): T =
	decodeFromBson(factory, bytes, serializer<T>())
