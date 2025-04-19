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

package opensavvy.ktmongo.bson.multiplatform

import kotlinx.io.*

/**
 * Represents an area within a [ByteArray].
 */
internal class Bytes(
	private val data: ByteArray,
	private val range: IntRange,
) {

	constructor(data: ByteArray) : this(data, 0..data.lastIndex)

	val size get() = range.endInclusive - range.start + 1

	val rawSource: RawSource get() = BytesRawSource(data, range)

	val source: Source get() = rawSource.buffered()

	val reader: RawBsonReader get() = RawBsonReader(source)

	fun subrange(subrange: IntRange): Bytes {
		require(subrange.start >= 0) { "Cannot create a subrange that starts at a negative index, found: $subrange" }
		require(subrange.endInclusive - subrange.start < size) { "Cannot create a subrange that ends after the end of the data, there are $size bytes, found: $subrange" }
		return Bytes(data, (range.start + subrange.start)..(range.start + subrange.endInclusive))
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

private class BytesRawSource(
	private val data: ByteArray,
	private var start: Int,
	private val endInclusive: Int,
) : RawSource {

	init {
		require(start <= data.lastIndex) { "Cannot start reading a ByteArray of size ${data.size} from index $start" }
		require(endInclusive <= data.lastIndex) { "Cannot read a ByteArray of size ${data.size} until the index $endInclusive (inclusive)" }
	}

	private var isClosed = false

	constructor(data: ByteArray, range: IntRange) : this(data, range.start, range.endInclusive)

	override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
		check(!isClosed) { "This source has been closed, reading it is now forbidden" }

		if (start > endInclusive) {
			return -1
		}

		for (i in 0..byteCount) {
			if (start > endInclusive) {
				return i
			}

			sink.writeByte(data[start])
			start++
		}

		return byteCount
	}

	override fun close() {
		isClosed = true
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
