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

package opensavvy.ktmongo.bson.multiplatform.impl.read

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.BsonArray
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.multiplatform.BsonValue
import opensavvy.ktmongo.bson.multiplatform.Bytes
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Exposes the bytes in a [BsonArray] as a lazily-populated [List].
 *
 * We take advantage of the linear nature of BSON: we only scan which items exist when we need them.
 * We do not recursively decode the contents of each value.
 */
@LowLevelApi
internal class MultiplatformBsonArrayList(
	private val factory: BsonFactory,
	bytesWithHeader: Bytes,
) : List<BsonValue> {

	private val bytes: Bytes = restrictAsDocument(bytesWithHeader)
	private val reader = this.bytes.reader

	private val scannedFields = ArrayList<BsonValue>()

	private fun scanUntil(targetIndex: Int?) {
		while (reader.request(1)) {
			val type = BsonType.fromCode(reader.readSignedByte())
			reader.skipCString() // We ignore the field name
			val field = readField(bytes, reader, type, factory)

			scannedFields += field

			if (targetIndex != null && scannedFields.lastIndex >= targetIndex) {
				return
			}
		}
	}

	override val size: Int
		get() {
			scanUntil(null)
			return scannedFields.size
		}

	override fun isEmpty(): Boolean {
		// Fast path: we have already read at least one field
		if (scannedFields.isNotEmpty())
			return false

		// Slow path: try to read one field
		scanUntil(0)
		return scannedFields.isEmpty()
	}

	override fun contains(element: BsonValue): Boolean {
		scanUntil(null)
		return element in scannedFields
	}

	override fun iterator(): Iterator<BsonValue> =
		BsonArrayIterator()

	inner class BsonArrayIterator : Iterator<BsonValue> {
		var index = 0
			private set

		override fun hasNext(): Boolean {
			if (index < scannedFields.size) {
				// We have already scanned that item
				return true
			}

			scanUntil(index)
			return index < scannedFields.size
		}

		override fun next(): BsonValue {
			if (index < scannedFields.size) {
				return scannedFields[index++]
			} else {
				throw NoSuchElementException("Reached the end of this BSON array: last index is ${scannedFields.lastIndex} but attempted to access index $index")
			}
		}
	}

	override fun containsAll(elements: Collection<BsonValue>): Boolean {
		scanUntil(null)
		return elements.all { it in scannedFields }
	}

	fun getOrNull(index: Int): BsonValue? =
		scannedFields.getOrNull(index) ?: run {
			scanUntil(index)
			scannedFields.getOrNull(index)
		}

	override fun get(index: Int): BsonValue =
		getOrNull(index) ?: throw IndexOutOfBoundsException("Index $index is out of bounds for BSON array of size ${scannedFields.size}")

	override fun indexOf(element: BsonValue): Int {
		val iter = BsonArrayIterator()

		while (iter.hasNext()) {
			if (iter.next() == element) {
				return iter.index - 1
			}
		}

		return -1
	}

	override fun lastIndexOf(element: BsonValue): Int {
		scanUntil(null)
		return scannedFields.lastIndexOf(element)
	}

	override fun listIterator(): ListIterator<BsonValue> {
		scanUntil(null)
		return scannedFields.listIterator()
	}

	override fun listIterator(index: Int): ListIterator<BsonValue> {
		scanUntil(null)
		return scannedFields.listIterator(index)
	}

	override fun subList(fromIndex: Int, toIndex: Int): List<BsonValue> {
		scanUntil(null)
		return scannedFields.subList(fromIndex, toIndex)
	}

	override fun equals(other: Any?): Boolean {
		if (other !is List<*>) return false
		if (size != other.size) return false

		for (index in indices) {
			if (this[index] != other[index]) return false
		}
		return true
	}

	override fun hashCode(): Int {
		var code = 1
		for (element in this) {
			code = code * 31 + element.hashCode()
		}
		return code
	}

	@OptIn(LowLevelApi::class)
	override fun toString(): String = buildString {
		append('[')
		var isFirst = true
		for (element in this@MultiplatformBsonArrayList) {
			if (!isFirst)
				append(", ")

			append(element)

			isFirst = false
		}
		append(']')
	}
}
