/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.experimental.or
import kotlin.io.encoding.Base64
import kotlin.math.max
import kotlin.math.min

/**
 * A dense array of numeric values stored in a binary storage efficient for storage and retrieval.
 *
 * Vectors are effectively used to represent data in artificial intelligence, machine learning, semantic search, computer vision, and natural language processing applications.
 *
 * All values within the vector must be of the same [type].
 *
 * ### Comparison with BSON arrays
 *
 * BSON arrays are serialized as objects, where the keys are integers (but still encoded as UTF8 strings).
 * Arrays have a minimum overhead of 3 bytes per stored element.
 *
 * Vectors are serialized contiguously, so there is no overhead per element.
 *
 * However, arrays and vectors are not interchangeable. Most MongoDB operators expect one or the other,
 * but will not work with both.
 *
 * ### External resources
 *
 * - [Atlas Vector Search](https://www.mongodb.com/docs/atlas/atlas-vector-search/vector-search-overview/)
 * - [Specification](https://github.com/mongodb/specifications/blob/master/source/bson-binary-vector/bson-binary-vector.md)
 */
@Serializable(with = Vector.Serializer::class)
interface Vector {

	/**
	 * The type of the elements in the vector (called `dtype` in the specification).
	 *
	 * Currently, the following types are implemented:
	 * - `0x03`: [ByteVector]
	 * - `0x27`: [FloatVector]
	 * - `0x10`: [BooleanVector]
	 *
	 * In most situations, users of this library should use `is` checks with one of the implementing subclasses
	 * rather than attempting to match on this property.
	 */
	@LowLevelApi
	val type: Byte

	/**
	 * The raw data stored in this vector.
	 *
	 * **When reading this property, remember to take into account any declared [padding]!**
	 *
	 * For more information on this field, read [the specification](https://github.com/mongodb/specifications/blob/master/source/bson-binary-vector/bson-binary-vector.md).
	 */
	@LowLevelApi
	val raw: ByteArray

	/**
	 * The number of bits in the final byte of [raw] that are to be ignored.
	 *
	 * This is useful for [types][type] that don't fit in multiples of 8 bits.
	 *
	 * For more information on this field, read [the specification](https://github.com/mongodb/specifications/blob/master/source/bson-binary-vector/bson-binary-vector.md).
	 */
	@LowLevelApi
	val padding: Byte

	/**
	 * Converts this [Vector] into a [ByteArray] that fits into the content of [BsonType.BinaryData].
	 *
	 * Vector is the binary subtype `0x09`.
	 */
	@LowLevelApi
	fun toBinaryData(): ByteArray {
		val r = raw
		return ByteArray(r.size + 2) { index ->
			when (index) {
				0 -> type
				1 -> padding
				else -> r[index - 2]
			}
		}
	}

	companion object {
		@LowLevelApi
		fun fromBinaryData(content: ByteArray): Vector = when (content[0]) {
			0x03.toByte() -> ByteVector(content.sliceArray(2 until content.size), Unit)
			0x27.toByte() -> FloatVector(content.sliceArray(2 until content.size))
			0x10.toByte() -> BooleanVector(content.sliceArray(2 until content.size), content[1])
			else -> UnknownVector(content)
		}
	}

	/**
	 * Default serializer for [Vector].
	 *
	 * When serializing into BSON, this serializer uses the efficient [`Vector`](https://bsonspec.org/spec.html#more-vector) binary subtype.
	 *
	 * When serializing to other formats (e.g. JSON…), this serializer uses a base64-encoded string.
	 *
	 * Avoid interacting with this type directly.
	 */
	@LowLevelApi
	object Serializer : KSerializer<Vector> {
		override val descriptor: SerialDescriptor
			get() = PrimitiveSerialDescriptor("opensavvy.ktmongo.bson.types.Vector", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: Vector) {
			serializeVectorPlatformSpecific(encoder, value)
		}

		override fun deserialize(decoder: Decoder): Vector =
			deserializeVectorPlatformSpecific(decoder)
	}
}

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [serializeVectorAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
internal expect fun serializeVectorPlatformSpecific(encoder: Encoder, value: Vector)

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [deserializeVectorAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
internal expect fun deserializeVectorPlatformSpecific(decoder: Decoder): Vector

@OptIn(LowLevelApi::class)
internal fun serializeVectorAsString(encoder: Encoder, value: Vector) {
	encoder.encodeString(Base64.encode(value.toBinaryData()))
}

@OptIn(LowLevelApi::class)
internal fun deserializeVectorAsString(decoder: Decoder): Vector {
	val binaryData = Base64.decode(decoder.decodeString())
	return Vector.fromBinaryData(binaryData)
}

private class UnknownVector(
	private val binaryData: ByteArray,
) : Vector {
	@LowLevelApi
	override val type: Byte
		get() = binaryData[0]

	@LowLevelApi
	override val raw: ByteArray
		get() = binaryData.copyOfRange(2, binaryData.size)

	@LowLevelApi
	override val padding: Byte
		get() = binaryData[1]

	@LowLevelApi
	override fun toBinaryData(): ByteArray =
		binaryData.copyOf()

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is UnknownVector) return false

		if (!binaryData.contentEquals(other.binaryData)) return false

		return true
	}

	override fun hashCode(): Int {
		return binaryData.contentHashCode()
	}

	@OptIn(LowLevelApi::class)
	override fun toString(): String = buildString {
		append("Vector(type=")
		append(type)
		append(", padding=")
		append(padding)
		append(", content=[")
		for (index in 2..min(34, binaryData.size - 1)) {
			append(binaryData[index])
			append(" ")
		}
		if (binaryData.size > 34)
			append("…")
		else
			deleteAt(this.length - 1)
		append("])")
	}
}

/**
 * A [Vector] of [Byte] elements (BSON's `Int8Vector`).
 *
 * The different bytes can be extracted with [toArray].
 *
 * Alternatively, this class implements [List].
 */
@OptIn(LowLevelApi::class)
@Serializable(with = ByteVector.Serializer::class)
class ByteVector internal constructor(
	/**
	 * The underlying byte storage. **Do not mutate this array!**
	 *
	 * Note that this storage does NOT include the type nor the padding.
	 */
	private val rawUnsafe: ByteArray,
	@Suppress("unused") unused: Unit, // avoid platform declaration clash with the vararg overload
) : Vector, Iterable<Byte>, Collection<Byte>, List<Byte> {

	/**
	 * Constructs a [ByteVector] from a collection of bytes.
	 */
	constructor(bytes: Collection<Byte>) : this(bytes.toByteArray(), Unit)

	/**
	 * Constructs a [ByteVector] from multiple bytes.
	 */
	constructor(vararg bytes: Byte) : this(bytes.asList())

	override val type: Byte
		get() = 0x03

	@LowLevelApi
	override val raw: ByteArray
		get() = rawUnsafe.copyOf()

	@LowLevelApi
	override val padding: Byte
		get() = 0

	override val size: Int
		get() = rawUnsafe.size

	override fun isEmpty(): Boolean =
		rawUnsafe.size == 0

	override fun contains(element: Byte): Boolean =
		rawUnsafe.contains(element)

	override fun containsAll(elements: Collection<Byte>): Boolean =
		elements.all { rawUnsafe.contains(it) }

	override fun get(index: Int): Byte =
		rawUnsafe[index]

	override fun indexOf(element: Byte): Int =
		rawUnsafe.indexOf(element)

	override fun lastIndexOf(element: Byte): Int =
		rawUnsafe.lastIndexOf(element)

	override fun listIterator(): ListIterator<Byte> =
		rawUnsafe.asList().listIterator()

	override fun listIterator(index: Int): ListIterator<Byte> =
		rawUnsafe.asList().listIterator(index)

	override fun subList(fromIndex: Int, toIndex: Int): List<Byte> =
		rawUnsafe.slice(fromIndex until toIndex)

	override fun iterator(): ByteIterator =
		rawUnsafe.iterator()

	fun toArray(): ByteArray =
		raw // 'raw' is cloned on access

	override fun equals(other: Any?): Boolean {
		return when {
			this === other -> true
			other === null -> false
			other is ByteVector -> rawUnsafe.contentEquals(other.rawUnsafe)
			other is List<*> -> {
				if (size != other.size) return false
				for (i in indices) {
					if (rawUnsafe[i] != other[i]) return false
				}
				true
			}

			else -> false
		}
	}

	override fun hashCode(): Int {
		var hashCode = 1
		for (e in this)
			hashCode = 31 * hashCode + e.hashCode()
		return hashCode
	}

	override fun toString(): String =
		joinToString(separator = ", ", prefix = "ByteVector[", postfix = "]")

	@LowLevelApi
	object Serializer : KSerializer<ByteVector> {
		override val descriptor: SerialDescriptor
			get() = Vector.serializer().descriptor

		override fun serialize(encoder: Encoder, value: ByteVector) {
			encoder.encodeSerializableValue(Vector.serializer(), value)
		}

		override fun deserialize(decoder: Decoder): ByteVector =
			decoder.decodeSerializableValue(Vector.serializer()) as ByteVector
	}
}

private fun floatsToBytes(floats: Collection<Float>): ByteArray {
	val array = ByteArray(floats.size * 4)
	floats.forEachIndexed { index, float ->
		val bits = float.toBits()

		// Little-endian byte order (LSB first)
		array[index * 4] = (bits and 0xFF).toByte()
		array[index * 4 + 1] = ((bits shr 8) and 0xFF).toByte()
		array[index * 4 + 2] = ((bits shr 16) and 0xFF).toByte()
		array[index * 4 + 3] = ((bits shr 24) and 0xFF).toByte()
	}
	return array
}

/**
 * A [Vector] of [Float] elements (BSON's `Float32Vector`).
 *
 * The different bytes can be extracted with [toArray].
 *
 * Alternatively, this class implements [List].
 */
@Serializable(with = FloatVector.Serializer::class)
class FloatVector internal constructor(
	/**
	 * The underlying byte storage. **Do not mutate this array!**
	 *
	 * Note that this storage does NOT include the type nor the padding.
	 */
	private val rawUnsafe: ByteArray,
) : Vector, Iterable<Float>, Collection<Float>, List<Float> {

	init {
		require(rawUnsafe.size % 4 == 0) { "Each float takes 4 bytes, so the underlying byte array should have a size divisible by 4, found: ${rawUnsafe.size}" }
	}

	constructor(floats: Collection<Float>) : this(floatsToBytes(floats))

	constructor(vararg floats: Float) : this(floats.asList())

	@LowLevelApi
	override val type: Byte
		get() = 0x27

	@LowLevelApi
	override val raw: ByteArray
		get() = rawUnsafe.copyOf()

	@LowLevelApi
	override val padding: Byte
		get() = 0

	override fun iterator(): Iterator<Float> =
		IteratorImpl()

	private inner class IteratorImpl : Iterator<Float> {
		private var index = 0

		override fun hasNext(): Boolean =
			index < size

		override fun next(): Float =
			get(index++)
	}

	override val size: Int
		get() = rawUnsafe.size / 4

	override fun isEmpty(): Boolean =
		rawUnsafe.isEmpty()

	override fun contains(element: Float): Boolean {
		for (i in indices) {
			if (get(i) == element)
				return true
		}
		return false
	}

	override fun containsAll(elements: Collection<Float>): Boolean =
		elements.all { contains(it) }

	override fun get(index: Int): Float {
		val startIndex = index * 4

		val bits = (rawUnsafe[startIndex].toUByte().toUInt()) or
			(rawUnsafe[startIndex + 1].toUByte().toUInt() shl 8) or
			(rawUnsafe[startIndex + 2].toUByte().toUInt() shl 16) or
			(rawUnsafe[startIndex + 3].toUByte().toUInt() shl 24)

		return Float.fromBits(bits.toInt())
	}

	override fun indexOf(element: Float): Int {
		for (i in indices) {
			if (get(i) == element)
				return i
		}
		return -1
	}

	override fun lastIndexOf(element: Float): Int {
		for (i in size - 1 downTo 0) {
			if (get(i) == element) return i
		}
		return -1
	}

	override fun listIterator(): ListIterator<Float> =
		ListIteratorImpl(0)

	override fun listIterator(index: Int): ListIterator<Float> =
		ListIteratorImpl(index)

	private inner class ListIteratorImpl(
		private var index: Int = 0,
	) : ListIterator<Float> {
		override fun next(): Float =
			get(index++)

		override fun hasNext(): Boolean =
			index < size

		override fun hasPrevious(): Boolean =
			index > 0

		override fun previous(): Float =
			get(--index)

		override fun nextIndex(): Int =
			min(index + 1, size)

		override fun previousIndex(): Int =
			max(index - 1, 0)
	}

	override fun subList(fromIndex: Int, toIndex: Int): List<Float> {
		if (fromIndex < 0)
			throw IndexOutOfBoundsException("fromIndex must be non-negative, found: $fromIndex")

		if (toIndex > size)
			throw IndexOutOfBoundsException("toIndex must be less than size ($size), found: $toIndex")

		if (toIndex < fromIndex)
			throw IllegalArgumentException("toIndex must be greater than or equal to fromIndex, found: toIndex=$toIndex, fromIndex=$fromIndex")

		val list = ArrayList<Float>(toIndex - fromIndex)

		for (i in fromIndex until toIndex) {
			list += get(i)
		}

		return list
	}

	fun toArray(): FloatArray =
		FloatArray(size) { get(it) }

	override fun equals(other: Any?): Boolean {
		return when {
			this === other -> true
			other === null -> false
			other is FloatVector -> rawUnsafe.contentEquals(other.rawUnsafe)
			other is List<*> -> {
				if (size != other.size) return false
				for (i in indices) {
					if (get(i) != other[i]) return false
				}
				true
			}

			else -> false
		}
	}

	override fun hashCode(): Int {
		var hashCode = 1
		for (e in this)
			hashCode = 31 * hashCode + e.hashCode()
		return hashCode
	}

	override fun toString(): String =
		joinToString(separator = ", ", prefix = "FloatVector[", postfix = "]")

	@LowLevelApi
	object Serializer : KSerializer<FloatVector> {
		override val descriptor: SerialDescriptor
			get() = Vector.serializer().descriptor

		override fun serialize(encoder: Encoder, value: FloatVector) {
			encoder.encodeSerializableValue(Vector.serializer(), value)
		}

		override fun deserialize(decoder: Decoder): FloatVector =
			decoder.decodeSerializableValue(Vector.serializer()) as FloatVector
	}
}

private fun booleansToBytes(booleans: Collection<Boolean>): ByteArray {
	val unpaddedSize = booleans.size / 8
	val hasPadding = booleans.size % 8 != 0
	val array = ByteArray(unpaddedSize + if (hasPadding) 1 else 0)
	booleans.forEachIndexed { index, bool ->
		val booleanIndex = index / 8
		val remainder = index % 8
		array[booleanIndex] = array[booleanIndex] or (if (bool) 1 shl remainder else 0).toByte()
	}
	return array
}

/**
 * A [Vector] of [Boolean] elements (BSON's `PackedBitVector`).
 *
 * The different bytes can be extracted with [toArray].
 *
 * Alternatively, this class implements [List].
 */
@Serializable(with = BooleanVector.Serializer::class)
class BooleanVector internal constructor(
	/**
	 * The underlying byte storage. **Do not mutate this array!**
	 *
	 * Note that this storage does NOT include the type nor the padding.
	 */
	private val rawUnsafe: ByteArray,

	@property:LowLevelApi
	override val padding: Byte,

	// Unused parameter, to allow us to have a public constructor with the same signature
	@Suppress("unused") marker: Unit,
) : Vector, Iterable<Boolean>, Collection<Boolean>, List<Boolean> {

	init {
		@OptIn(LowLevelApi::class)
		require(padding in 0..7) { "A vector can only have a maximum padding of 1 byte (8 bits), but found: $padding declared bits" }
	}

	constructor(booleans: Collection<Boolean>) : this(
		rawUnsafe = booleansToBytes(booleans),
		padding = (booleans.size % 8).toByte(),
		marker = Unit,
	)

	constructor(vararg booleans: Boolean) : this(booleans.asList())

	/**
	 * Constructs a [BooleanVector] from the [raw] byte contents with a given [padding].
	 *
	 * Note that [raw] is only the data part of the vector. It is not the entire binary data.
	 * To construct a [BooleanVector] from binary data, see [Vector.fromBinaryData].
	 */
	@LowLevelApi
	constructor(raw: ByteArray, padding: Byte) : this(
		rawUnsafe = raw.copyOf(),
		padding = padding,
		marker = Unit,
	)

	@LowLevelApi
	override val type: Byte
		get() = 0x10

	@LowLevelApi
	override val raw: ByteArray
		get() = rawUnsafe.copyOf()

	override fun iterator(): BooleanIterator =
		IteratorImpl()

	private inner class IteratorImpl : BooleanIterator() {
		private var index = 0

		override fun hasNext(): Boolean =
			index < size

		override fun nextBoolean(): Boolean =
			get(index++)
	}

	@OptIn(LowLevelApi::class)
	override val size: Int =
		rawUnsafe.size * 8 - padding.toInt()

	override fun isEmpty(): Boolean =
		size == 0

	override fun contains(element: Boolean): Boolean {
		for (i in indices) {
			if (get(i) == element)
				return true
		}
		return false
	}

	override fun containsAll(elements: Collection<Boolean>): Boolean =
		elements.all { contains(it) }

	override fun get(index: Int): Boolean {
		if (index < 0)
			throw IndexOutOfBoundsException("Index must be non-negative, found: $index")

		if (index >= size)
			throw IndexOutOfBoundsException("Index must be less than size ($size), found: $index")

		val booleanIndex = index / 8
		val remainder = index % 8
		return rawUnsafe[booleanIndex].toInt() and (1 shl remainder) != 0
	}

	override fun indexOf(element: Boolean): Int {
		for (i in indices) {
			if (get(i) == element)
				return i
		}
		return -1
	}

	override fun lastIndexOf(element: Boolean): Int {
		for (i in size - 1 downTo 0) {
			if (get(i) == element) return i
		}
		return -1
	}

	override fun listIterator(): ListIterator<Boolean> =
		ListIteratorImpl()

	override fun listIterator(index: Int): ListIterator<Boolean> =
		ListIteratorImpl(index)

	private inner class ListIteratorImpl(
		private var index: Int = 0,
	) : ListIterator<Boolean> {
		override fun next(): Boolean =
			get(index++)

		override fun hasNext(): Boolean =
			index < size

		override fun hasPrevious(): Boolean =
			index > 0

		override fun previous(): Boolean =
			get(--index)

		override fun nextIndex(): Int =
			min(index + 1, size)

		override fun previousIndex(): Int =
			max(index - 1, 0)
	}

	override fun subList(fromIndex: Int, toIndex: Int): List<Boolean> {
		if (fromIndex < 0)
			throw IndexOutOfBoundsException("fromIndex must be non-negative, found: $fromIndex")

		if (toIndex > size)
			throw IndexOutOfBoundsException("toIndex must be less than size ($size), found: $toIndex")

		if (toIndex < fromIndex)
			throw IllegalArgumentException("toIndex must be greater than or equal to fromIndex, found: toIndex=$toIndex, fromIndex=$fromIndex")

		val list = ArrayList<Boolean>(toIndex - fromIndex)

		for (i in fromIndex until toIndex) {
			list += get(i)
		}

		return list
	}

	fun toArray(): BooleanArray =
		BooleanArray(size) { get(it) }

	override fun equals(other: Any?): Boolean {
		return when {
			this === other -> true
			other === null -> false
			other is BooleanVector -> rawUnsafe.contentEquals(other.rawUnsafe)
			other is List<*> -> {
				if (size != other.size) return false
				for (i in indices) {
					if (get(i) != other[i]) return false
				}
				true
			}

			else -> false
		}
	}

	override fun hashCode(): Int {
		var hashCode = 1
		for (e in this)
			hashCode = 31 * hashCode + e.hashCode()
		return hashCode
	}

	override fun toString(): String =
		joinToString(separator = ", ", prefix = "BooleanVector[", postfix = "]")

	@LowLevelApi
	object Serializer : KSerializer<BooleanVector> {
		override val descriptor: SerialDescriptor
			get() = Vector.serializer().descriptor

		override fun serialize(encoder: Encoder, value: BooleanVector) {
			encoder.encodeSerializableValue(Vector.serializer(), value)
		}

		override fun deserialize(decoder: Decoder): BooleanVector =
			decoder.decodeSerializableValue(Vector.serializer()) as BooleanVector
	}
}
