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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import opensavvy.ktmongo.bson.BsonArrayReader
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.CompletableBsonValueWriter
import opensavvy.ktmongo.dsl.LowLevelApi


class BsonDecoderTopLevel(override val serializersModule: SerializersModule, val context: BsonContext, val bytes: ByteArray): AbstractDecoder() {
	var out: Any? = null

	override fun decodeNull(): Nothing? {
		throw BsonEncodingException("Cannot encode a null value at the top level of BSON")
	}

	override fun decodeValue() {
		throw BsonEncodingException("Cannot encode a value at the top level of BSON")
	}

	override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
		return when(descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> BsonCompositeDecoder(serializersModule, context.readDocument(bytes).reader())
			StructureKind.MAP -> BsonCompositeMapDecoder(serializersModule, context.readDocument(bytes).reader())
			StructureKind.LIST -> BsonCompositeListDecoder(serializersModule, context.readArray(bytes).reader())
			else -> TODO()
		}
	}

	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		throw BsonEncodingException("Cannot encode a value at the top level of BSON")
	}
}

class BsonDecoder(override val serializersModule: SerializersModule, val source: BsonValueReader): Decoder {
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
		return when(descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> BsonCompositeDecoder(serializersModule, source.readDocument())
			StructureKind.LIST -> BsonCompositeListDecoder(serializersModule, source.readArray())
			StructureKind.MAP -> BsonCompositeMapDecoder(serializersModule, source.readDocument())
			else -> TODO()
		}
	}

	private val bytes = ByteArraySerializer()
	override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
		@Suppress("UNCHECKED_CAST")
		return when(deserializer.descriptor) {
			bytes.descriptor -> source.readBinaryData() as T
			// TODO: Add more custom types here
			else -> deserializer.deserialize(this)
		}
	}
}

class BsonCompositeDecoder(override val serializersModule: SerializersModule, val source: BsonDocumentReader): CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.entries.entries.iterator()
	lateinit var current: Map.Entry<String, BsonValueReader>
	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if(!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
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

// TODO: Should maps use actual objects in BSON?  String keys are dumb
class BsonCompositeMapDecoder(override val serializersModule: SerializersModule, val source: BsonDocumentReader): CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.entries.entries.iterator()
	lateinit var current: Map.Entry<String, BsonValueReader>
	var index = -1

	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if (++index % 2 == 0) {
			if (!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
			current = iterator.next()
		}
		return index
	}

	fun <T> decodeKey(serializer: DeserializationStrategy<T>): T {
		@Suppress("UNCHECKED_CAST")
		if(serializer.descriptor.serialName  == "kotlin.String") return current.key as T
		return StringFallbackDecoder(serializersModule, current.key).decodeSerializableValue(serializer)
	}

	override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
		return if(index % 2 == 0) decodeKey(Boolean.serializer())
		else current.value.readBoolean()
	}

	override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
		return if(index % 2 == 0) decodeKey(Byte.serializer())
		else current.value.readInt32().toByte()
	}

	override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
		return if(index % 2 == 0) decodeKey(Char.serializer())
		else current.value.readString().single()
	}

	override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
		return if(index % 2 == 0) decodeKey(Short.serializer())
		else current.value.readInt32().toShort()
	}

	override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
		return if(index % 2 == 0) decodeKey(Int.serializer())
		else current.value.readInt32()
	}

	override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
		return if(index % 2 == 0) decodeKey(Long.serializer())
		else current.value.readInt64()
	}

	override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
		return if(index % 2 == 0) decodeKey(Float.serializer())
		else current.value.readDouble().toFloat()
	}

	override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
		return if(index % 2 == 0) decodeKey(Double.serializer())
		else current.value.readDouble()
	}

	override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
		return if(index % 2 == 0) decodeKey(String.serializer())
		else current.value.readString()
	}

	override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
		return if(index % 2 == 0) StringFallbackDecoder(serializersModule, current.key).decodeInline(descriptor.getElementDescriptor(index))
		else BsonDecoder(serializersModule, current.value)
	}

	override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T {
		return if(index % 2 == 0) StringFallbackDecoder(serializersModule, current.key).decodeSerializableValue(deserializer)
		else BsonDecoder(serializersModule, current.value).decodeSerializableValue(deserializer)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? {
		return if(index % 2 == 0) StringFallbackDecoder(serializersModule, current.key).decodeSerializableValue(deserializer)
		else BsonDecoder(serializersModule, current.value).decodeNullableSerializableValue(deserializer)
	}

}

class BsonCompositeListDecoder(override val serializersModule: SerializersModule, val source: BsonArrayReader): CompositeDecoder {
	override fun endStructure(descriptor: SerialDescriptor) {
	}

	val iterator = source.elements.iterator()
	lateinit var current: BsonValueReader
	var index = 0
	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if(!iterator.hasNext()) return CompositeDecoder.DECODE_DONE
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