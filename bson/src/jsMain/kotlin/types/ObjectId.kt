/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

private const val BYTE_SIZE = 12
private const val HEX_STRING_SIZE = BYTE_SIZE * 2

private val HEX_CHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

actual class ObjectId actual constructor(private val bytes: ByteArray) : Comparable<ObjectId> {
	init {
		require(bytes.size == BYTE_SIZE) { "An ObjectId must be $BYTE_SIZE bytes long, found ${bytes.size}: $bytes" }
	}

	actual constructor(hexString: String) : this(parseHexString(hexString))

	actual constructor() :
		this(TODO("Generating a randomized ObjectId is not implemented") as ByteArray)

	actual fun toHexString(): String {
		val chars = CharArray(HEX_STRING_SIZE)
		var i = 0
		for (b in toByteArray()) {
			chars[i++] = HEX_CHARS[b.toInt() shr 4 and 0xF]
			chars[i++] = HEX_CHARS[b.toInt() and 0xF]
		}
		return chars.concatToString()
	}

	actual fun toByteArray(): ByteArray =
		bytes.copyOf()

	actual override fun compareTo(other: ObjectId): Int {
		val byteArray = toByteArray()
		val otherByteArray = other.toByteArray()
		for (i in 0 until BYTE_SIZE) {
			if (byteArray[i] != otherByteArray[i]) {
				return if (((byteArray[i].toInt() and 0xff) < (otherByteArray[i].toInt() and 0xff))) -1 else 1
			}
		}
		return 0
	}

	override fun toString(): String = toHexString()

}

private fun parseHexString(hexString: String): ByteArray {
	require(hexString.length == HEX_STRING_SIZE) { "hexString must be $HEX_STRING_SIZE characters long, found ${hexString.length}: '$hexString'" }

	val b = ByteArray(BYTE_SIZE)
	for (i in b.indices) {
		val pos = i shl 1
		val c1: Char = hexString[pos]
		val c2: Char = hexString[pos + 1]
		b[i] = ((hexCharToInt(c1) shl 4) + hexCharToInt(c2)).toByte()
	}
	return b
}

private fun hexCharToInt(c: Char): Int {
	return when (c) {
		in '0'..'9' -> {
			c.code - 48
		}

		in 'a'..'f' -> {
			c.code - 87
		}

		in 'A'..'F' -> {
			c.code - 55
		}

		else -> throw IllegalArgumentException("invalid hexadecimal character: [$c]")
	}
}
