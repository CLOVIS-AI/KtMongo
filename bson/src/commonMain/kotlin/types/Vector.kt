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

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.LowLevelApi
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
interface Vector {

	/**
	 * The type of the elements in the vector (called `dtype` in the specification).
	 *
	 * Currently, the following types are implemented:
	 * - `0x03`: [ByteVector]
	 * - `0x27`: [FloatVector]
	 * - `0x10`: [PackedBitVector]
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
			else -> UnknownVector(content)
		}
	}
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
}
