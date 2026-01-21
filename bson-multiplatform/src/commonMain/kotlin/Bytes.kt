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

package opensavvy.ktmongo.bson.multiplatform

import kotlinx.io.*
import kotlinx.io.unsafe.UnsafeBufferOperations

/**
 * Represents an area within a [ByteArray].
 */
internal class Bytes(
	private val data: ByteArray,
	private val range: IntRange,
) {

	constructor(data: ByteArray) : this(data, 0..data.lastIndex)

	val size get() = range.last - range.first + 1

	@OptIn(UnsafeIoApi::class) // Safe because 'data' is effectively immutable • https://github.com/Kotlin/kotlinx-io/issues/444
	val source: Source
		get() {
			val buffer = Buffer()
			UnsafeBufferOperations.moveToTail(
				buffer,
				data,
				startIndex = range.first,
				endIndex = range.last + 1, // We should never have a ByteArray large enough for this to overflow
			)
			return buffer
		}

	val reader: RawBsonReader get() = RawBsonReader(source)

	fun subrange(subrange: IntRange): Bytes {
		require(subrange.first >= 0) { "Cannot create a subrange that starts at a negative index, found: $subrange" }
		require(subrange.last - subrange.first < size) { "Cannot create a subrange that ends after the end of the data, there are $size bytes, found: $subrange" }
		return Bytes(data, (range.first + subrange.first)..(range.first + subrange.last))
	}

	fun toByteArray(): ByteArray {
		val array = ByteArray(size)
		for ((index, realIndex) in range.withIndex()) {
			array[index] = data[realIndex]
		}
		return array
	}

	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String = buildString {
		append('[')
		var isFirst = true
		for (index in range) {
			if (!isFirst)
				append(' ')

			append(data[index].toHexString(HexFormat.UpperCase))

			isFirst = false
		}
		append("] [size $size]")
	}
}

// https://bsonspec.org/spec.html
internal class RawBsonReader(
	private val source: Source,
) {

	var readCount: Int = 0
		private set

	fun request(n: Int) = source.request(n.toLong())
	fun skip(n: Int) {
		source.skip(n.toLong())
		readCount += n
	}

	fun peek(): Source = source.peek()

	fun readUnsignedByte(): UByte =
		source.readUByte()
			.also { readCount += 1 }

	fun readSignedByte(): Byte =
		source.readByte()
			.also { readCount += 1 }

	fun readInt32(): Int =
		source.readIntLe()
			.also { readCount += 4 }

	fun readInt64(): Long =
		source.readLongLe()
			.also { readCount += 8 }

	fun readUInt64(): ULong =
		source.readULongLe()
			.also { readCount += 8 }

	fun readDouble(): Double =
		source.readDoubleLe()
			.also { readCount += 8 }

	fun readCString(): String {
		val peek = source.peek()
		var byteCount = 0L
		while (peek.request(1) && peek.readByte() != 0.toByte())
			byteCount++

		return source.readString(byteCount)
			.also { source.skip(1) } // null-terminator
			.also { readCount += byteCount.toInt() + 1 } // null-terminator
	}

	fun skipCString() {
		val peek = source.peek()
		var byteCount = 0L
		while (peek.request(1) && peek.readByte() != 0.toByte())
			byteCount++

		source.skip(byteCount + 1) // null-terminator
			.also { readCount += byteCount.toInt() + 1 }
	}

	fun readString(): String {
		val byteCount = readInt32() // includes the null-terminator

		return source.readString(byteCount.toLong() - 1)
			.also { source.skip(1) } // null-terminator
			.also { readCount += byteCount }
	}

	fun readBytes(length: Int): ByteArray {
		val data = ByteArray(length)
		source.readTo(data, 0, length)
		return data
	}

	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String = buildString {
		append('[')
		val peek = source.peek()
		var isFirst = true
		while (peek.request(1)) {
			if (!isFirst)
				append(' ')

			append(peek.readByte().toHexString(HexFormat.UpperCase))

			isFirst = false
		}
		append(']')
	}
}
