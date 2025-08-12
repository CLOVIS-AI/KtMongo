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

@file:OptIn(LowLevelApi::class, DangerousMongoApi::class, ExperimentalSerializationApi::class)

package opensavvy.ktmongo.bson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.CompletableBsonValueWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

class BsonEncodingException(message: String) : Exception(message)
class BsonDecodingException(message: String) : Exception(message)
class BsonUnknownElementException(message: String) : Exception(message)

class BsonEncoderTopLevel(override val serializersModule: SerializersModule, val context: BsonContext): AbstractEncoder() {
	var out: Any? = null

	override fun encodeNull() {
		throw BsonEncodingException("Cannot encode a null value at the top level of BSON")
	}

	override fun encodeValue(value: Any) {
		throw BsonEncodingException("Cannot encode a value at the top level of BSON")
	}

	override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
		val out = when(descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> {
				val a = context.openDocument()
				val doc = object: CompletableBsonFieldWriter<Unit>, BsonFieldWriter by a {
					override fun complete() {
						out = a.complete()
					}
				}
				BsonCompositeEncoder(serializersModule, doc)
			}
			StructureKind.MAP -> {
				val a = context.openDocument()
				val doc = object: CompletableBsonFieldWriter<Unit>, BsonFieldWriter by a {
					override fun complete() {
						out = a.complete()
					}
				}
				BsonCompositeEncoderMap(serializersModule, doc)
			}
			StructureKind.LIST -> {
				val a = context.openArray()
				val doc = object: CompletableBsonValueWriter<Unit>, BsonValueWriter by a {
					override fun complete() {
						out = a.complete()
					}
				}
				BsonCompositeEncoderList(serializersModule, doc)
			}
			else -> TODO()
		}
		return out
	}
}

class BsonEncoder(override val serializersModule: SerializersModule, val out: BsonValueWriter): Encoder {
	@ExperimentalSerializationApi
	override fun encodeNull() {
		out.writeNull()
	}

	override fun encodeBoolean(value: Boolean) {
		out.writeBoolean(value)
	}

	override fun encodeByte(value: Byte) {
		out.writeInt32(value)
	}

	override fun encodeShort(value: Short) {
		out.writeInt32(value)
	}

	override fun encodeChar(value: Char) {
		out.writeString(value.toString())
	}

	override fun encodeInt(value: Int) {
		out.writeInt32(value)
	}

	override fun encodeLong(value: Long) {
		out.writeInt64(value)
	}

	override fun encodeFloat(value: Float) {
		out.writeDouble(value.toDouble())
	}

	override fun encodeDouble(value: Double) {
		out.writeDouble(value)
	}

	override fun encodeString(value: String) {
		out.writeString(value)
	}

	override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
		out.writeString(enumDescriptor.getElementName(index))
	}

	override fun encodeInline(descriptor: SerialDescriptor): Encoder {
		return this
	}

	override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
		return when (descriptor.kind) {
			StructureKind.CLASS -> BsonCompositeEncoder(serializersModule, out.openDocument())
			StructureKind.LIST -> BsonCompositeEncoderList(serializersModule, out.openArray())
			StructureKind.MAP -> BsonCompositeEncoderMap(serializersModule, out.openDocument())
			StructureKind.OBJECT -> BsonCompositeEncoder(serializersModule, out.openDocument())
			else -> throw IllegalArgumentException()
		}
	}

	private val bytes = ByteArraySerializer()
	override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
		when(serializer.descriptor) {
			bytes.descriptor -> out.writeBinaryData(0U, (value as ByteArray))
			// TODO: Add more custom types here
			else -> serializer.serialize(this, value)
		}
	}
}

class BsonCompositeEncoder(override val serializersModule: SerializersModule, val out: CompletableBsonFieldWriter<*>): CompositeEncoder {
	override fun endStructure(descriptor: SerialDescriptor) {
		out.complete()
	}

	override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
		out.writeBoolean(descriptor.getElementName(index), value)
	}

	override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
		out.writeInt32(descriptor.getElementName(index), value)
	}

	override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
		out.writeInt32(descriptor.getElementName(index), value)
	}

	override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
		out.writeString(descriptor.getElementName(index), value.toString())
	}

	override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
		out.writeInt32(descriptor.getElementName(index), value)
	}

	override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
		out.writeInt64(descriptor.getElementName(index), value)
	}

	override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
		out.writeDouble(descriptor.getElementName(index), value.toDouble())
	}

	override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
		out.writeDouble(descriptor.getElementName(index), value)
	}

	override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
		out.writeString(descriptor.getElementName(index), value)
	}

	override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
		return BsonEncoder(serializersModule, out.open(descriptor.getElementName(index)))
	}

	override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
		BsonEncoder(serializersModule, out.open(descriptor.getElementName(index))).encodeSerializableValue(serializer, value)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
		BsonEncoder(serializersModule, out.open(descriptor.getElementName(index))).encodeNullableSerializableValue(serializer, value)
	}
}

// TODO: Should maps use actual objects in BSON?  String keys are dumb
class BsonCompositeEncoderMap(override val serializersModule: SerializersModule, val out: CompletableBsonFieldWriter<*>): CompositeEncoder {
	override fun endStructure(descriptor: SerialDescriptor) {
		out.complete()
	}

	var nextKey: String = ""

	fun <T> setKey(serializer: SerializationStrategy<T>, value: T) {
		if(value is String) nextKey = value
		else nextKey = StringFallbackEncoder(serializersModule).let {
			it.encodeSerializableValue(serializer, value)
			it.out
		}
	}

	override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
		if(index % 2 == 0) setKey(Boolean.serializer(), value)
		else out.writeBoolean(nextKey, value)
	}

	override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
		if(index % 2 == 0) setKey(Byte.serializer(), value)
		else out.writeInt32(nextKey, value)
	}

	override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
		if(index % 2 == 0) setKey(Short.serializer(), value)
		else out.writeInt32(nextKey, value)
	}

	override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
		if(index % 2 == 0) setKey(Char.serializer(), value)
		else out.writeString(nextKey, value.toString())
	}

	override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
		if(index % 2 == 0) setKey(Int.serializer(), value)
		else out.writeInt32(nextKey, value)
	}

	override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
		if(index % 2 == 0) setKey(Long.serializer(), value)
		else out.writeInt64(nextKey, value)
	}

	override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
		if(index % 2 == 0) setKey(Float.serializer(), value)
		else out.writeDouble(nextKey, value.toDouble())
	}

	override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
		if(index % 2 == 0) setKey(Double.serializer(), value)
		else out.writeDouble(nextKey, value)
	}

	override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
		if(index % 2 == 0) setKey(String.serializer(), value)
		else out.writeString(nextKey, value)
	}

	override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
		if(index % 2 == 0) TODO()
		else return BsonEncoder(serializersModule, out.open(nextKey))
	}

	override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
		if(index % 2 == 0) setKey(serializer, value)
		else BsonEncoder(serializersModule, out.open(nextKey)).encodeSerializableValue(serializer, value)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
		@Suppress("UNCHECKED_CAST")
		if(index % 2 == 0) setKey((serializer as KSerializer<Any>).nullable, value)
		else BsonEncoder(serializersModule, out.open(nextKey)).encodeNullableSerializableValue(serializer, value)
	}
}

class BsonCompositeEncoderList(override val serializersModule: SerializersModule, val out: CompletableBsonValueWriter<*>): CompositeEncoder {
	override fun endStructure(descriptor: SerialDescriptor) {
		out.complete()
	}

	override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
		out.writeBoolean(value)
	}

	override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
		out.writeInt32(value)
	}

	override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
		out.writeInt32(value)
	}

	override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
		out.writeString(value.toString())
	}

	override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
		out.writeInt32(value)
	}

	override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
		out.writeInt64(value)
	}

	override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
		out.writeDouble(value.toDouble())
	}

	override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
		out.writeDouble(value)
	}

	override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
		out.writeString(value)
	}

	override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
		return BsonEncoder(serializersModule, out)
	}

	override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
		BsonEncoder(serializersModule, out).encodeSerializableValue(serializer, value)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
		BsonEncoder(serializersModule, out).encodeNullableSerializableValue(serializer, value)
	}
}