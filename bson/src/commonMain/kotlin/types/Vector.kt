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
}
