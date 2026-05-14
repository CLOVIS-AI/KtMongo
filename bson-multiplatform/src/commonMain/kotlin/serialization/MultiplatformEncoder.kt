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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.multiplatform.BsonArray
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.multiplatform.BsonValue
import opensavvy.ktmongo.bson.multiplatform.impl.write.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.CompletableBsonValueWriter
import opensavvy.ktmongo.bson.types.*
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalSerializationApi
@LowLevelApi
private class BsonEncoderTopLevel(
	val factory: BsonFactory,
) : AbstractEncoder() {
	lateinit var out: BsonDocument

	override val serializersModule: SerializersModule
		get() = factory.serializersModule

	override fun encodeNull() {
		throw BsonEncodingException("Cannot encode a null value at the top level of BSON")
	}

	override fun encodeValue(value: Any) {
		throw BsonEncodingException("Cannot encode a value of ${value::class} at the top level of BSON. Top level BSON must always be a class or an array.")
	}

	@OptIn(DangerousMongoApi::class)
	override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
		val out = when (descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> {
				val a = factory.openDocument()
				val doc = object : CompletableBsonFieldWriter by a {
					override fun complete() {
						out = a.build()
					}
				}
				BsonCompositeEncoder(factory, doc)
			}

			else -> TODO("Unsupported structure kind: ${descriptor.kind}")
		}
		return out
	}
}

@OptIn(LowLevelApi::class, DangerousMongoApi::class, ExperimentalTime::class, ExperimentalUuidApi::class)
internal class BsonEncoder(
	val factory: BsonFactory,
	val out: CompletableBsonValueWriter,
) : Encoder {
	override val serializersModule: SerializersModule
		get() = factory.serializersModule

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
			StructureKind.CLASS -> BsonCompositeEncoder(factory, out.openDocument())
			StructureKind.LIST -> BsonCompositeEncoderList(factory, out.openArray())
			StructureKind.OBJECT -> BsonCompositeEncoder(factory, out.openDocument())
			else -> TODO("Unsupported structure kind: ${descriptor.kind}")
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
	private val bsonDocument = BsonDocument.serializer().descriptor
	private val bsonArray = BsonArray.serializer().descriptor
	private val bsonValue = BsonValue.serializer().descriptor
	override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
		when (serializer.descriptor) {
			// Special cases where we provide our own encoder
			bytes -> out.writeBinaryData(0U, (value as ByteArray))
			objectId -> out.writeObjectId(value as ObjectId)
			timestamp -> out.writeTimestamp(value as Timestamp)
			uuid -> out.writeBinaryData(4u, (value as Uuid).toByteArray())
			instant -> out.writeInstant(value as Instant)
			vector, floatVector, booleanVector, byteVector -> out.writeBinaryData(0x09u, (value as Vector).toBinaryData())
			bsonDocument -> out.pipe((value as BsonDocument).asValue())
			bsonArray -> out.pipe((value as BsonArray).asValue())
			bsonValue -> out.pipe(value as BsonValue)

			// General case: do what the serializer says
			else -> serializer.serialize(this, value)
		}
	}
}

@LowLevelApi
@OptIn(DangerousMongoApi::class)
private class BsonCompositeEncoder(
	val factory: BsonFactory,
	val out: CompletableBsonFieldWriter,
) : CompositeEncoder {
	override val serializersModule: SerializersModule
		get() = factory.serializersModule

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
		return BsonEncoder(factory, out.open(descriptor.getElementName(index)))
	}

	override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
		BsonEncoder(factory, out.open(descriptor.getElementName(index))).encodeSerializableValue(serializer, value)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
		BsonEncoder(factory, out.open(descriptor.getElementName(index))).encodeNullableSerializableValue(serializer, value)
	}
}

@LowLevelApi
@OptIn(DangerousMongoApi::class)
private class BsonCompositeEncoderList(
	val factory: BsonFactory,
	val out: CompletableBsonValueWriter,
) : CompositeEncoder {
	override val serializersModule: SerializersModule
		get() = factory.serializersModule

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
		return BsonEncoder(factory, out)
	}

	override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
		BsonEncoder(factory, out).encodeSerializableValue(serializer, value)
	}

	@ExperimentalSerializationApi
	override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
		BsonEncoder(factory, out).encodeNullableSerializableValue(serializer, value)
	}
}

/**
 * Encodes an arbitrary [value] into a [Bson] document.
 *
 * [value] must be serializable using KotlinX.Serialization. For example, using the `@Serializable` annotation.
 */
@ExperimentalSerializationApi
@OptIn(LowLevelApi::class, DangerousMongoApi::class)
fun <T : Any> encodeToBson(factory: BsonFactory, value: T, serializer: SerializationStrategy<T>): BsonDocument {
	val encoder = BsonEncoderTopLevel(factory)
	encoder.encodeSerializableValue(serializer, value)
	return encoder.out
}

/**
 * Encodes an arbitrary [value] into a [Bson] document.
 *
 * [value] must be serializable using KotlinX.Serialization. For example, using the `@Serializable` annotation.
 */
@ExperimentalSerializationApi
inline fun <reified T : Any> encodeToBson(factory: BsonFactory, value: T): BsonDocument =
	encodeToBson(factory, value, serializer())
