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
import opensavvy.ktmongo.bson.BsonArrayReader
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.bson.multiplatform.BsonContext
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalSerializationApi
internal class BsonDecoderTopLevel(
	override val serializersModule: SerializersModule,
	val context: BsonContext,
	val bytes: ByteArray,
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
			StructureKind.CLASS, StructureKind.OBJECT -> BsonCompositeDecoder(serializersModule, context.readDocument(bytes).reader())
			StructureKind.LIST -> BsonCompositeListDecoder(serializersModule, context.readArray(bytes).reader())
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
	val source: BsonValueReader,
) : Decoder {
	@ExperimentalSerializationApi
	override fun decodeNotNullMark(): Boolean {
		return source.type != BsonType.Null
	}

	@ExperimentalSerializationApi
	override fun decodeNull(): Nothing? {
		source.readNull()
		return null
	}

	override fun decodeBoolean(): Boolean {
		return source.readBoolean()
	}

	override fun decodeByte(): Byte {
		return source.readInt32().toByte()
	}

	override fun decodeShort(): Short {
		return source.readInt32().toShort()
	}

	override fun decodeChar(): Char {
		return source.readString().single()
	}

	override fun decodeInt(): Int {
		return source.readInt32()
	}

	override fun decodeLong(): Long {
		return source.readInt64()
	}

	override fun decodeFloat(): Float {
		return source.readDouble().toFloat()
	}

	override fun decodeDouble(): Double {
		return source.readDouble()
	}

	override fun decodeString(): String {
		return source.readString()
	}

	override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
		return enumDescriptor.getElementIndex(source.readString())
	}

	override fun decodeInline(descriptor: SerialDescriptor): Decoder {
		return this
	}

	override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
		return when (descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> BsonCompositeDecoder(serializersModule, source.readDocument())
			StructureKind.LIST -> BsonCompositeListDecoder(serializersModule, source.readArray())
			else -> TODO()
		}
	}

	private val bytes = ByteArraySerializer().descriptor
	private val objectId = ObjectId.Serializer().descriptor
	private val timestamp = Timestamp.Serializer().descriptor
	private val uuid = Uuid.serializer().descriptor
	override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
		@Suppress("UNCHECKED_CAST")
		return when (deserializer.descriptor) {
			// Special cases where we provide our own decoder
			bytes -> source.readBinaryData() as T
			objectId -> source.readObjectId() as T
			timestamp -> source.readTimestamp() as T
			uuid -> {
				val subType = source.readBinaryDataType()
				check(subType == 3.toUByte() || subType == 4.toUByte()) { "Uuid should be represented by the binary subtypes 3 (deprecated) or 4, found: $subType" }
				Uuid.fromByteArray(source.readBinaryData()) as T
			}

			// General case: do what the serializer says
			else -> deserializer.deserialize(this)
		}
	}
}

@LowLevelApi
internal class BsonCompositeDecoder(
	override val serializersModule: SerializersModule,
	source: BsonDocumentReader,
) : CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.entries.entries.iterator()
	lateinit var current: Map.Entry<String, BsonValueReader>
	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if (!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
		current = iterator.next()
		return descriptor.getElementIndex(current.key)
	}

	override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
		return current.value.readBoolean()
	}

	override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
		return current.value.readInt32().toByte()
	}

	override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
		return current.value.readString().single()
	}

	override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
		return current.value.readInt32().toShort()
	}

	override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
		return current.value.readInt32()
	}

	override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
		return current.value.readInt64()
	}

	override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
		return current.value.readDouble().toFloat()
	}

	override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
		return current.value.readDouble()
	}

	override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
		return current.value.readString()
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
	source: BsonArrayReader,
) : CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.elements.iterator()
	lateinit var current: BsonValueReader
	var index = 0
	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if (!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
		current = iterator.next()
		return index++
	}

	override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
		return current.readBoolean()
	}

	override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
		return current.readInt32().toByte()
	}

	override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
		return current.readString().single()
	}

	override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
		return current.readInt32().toShort()
	}

	override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
		return current.readInt32()
	}

	override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
		return current.readInt64()
	}

	override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
		return current.readDouble().toFloat()
	}

	override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
		return current.readDouble()
	}

	override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
		return current.readString()
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
fun <T : Any> decodeFromBson(context: BsonContext, bytes: ByteArray, deserializer: DeserializationStrategy<T>): T {
	val decoder = BsonDecoderTopLevel(EmptySerializersModule(), context, bytes)
	return decoder.decodeSerializableValue(deserializer)
}

@ExperimentalSerializationApi
inline fun <reified T : Any> decodeFromBson(context: BsonContext, bytes: ByteArray): T =
	decodeFromBson(context, bytes, serializer<T>())
