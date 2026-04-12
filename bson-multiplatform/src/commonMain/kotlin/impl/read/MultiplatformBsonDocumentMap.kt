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

package opensavvy.ktmongo.bson.multiplatform.impl.read

import kotlinx.io.readIntLe
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.*
import opensavvy.ktmongo.dsl.LowLevelApi

internal fun restrictAsDocument(bytes: Bytes): Bytes {
	val size = bytes.reader.readInt32()
	return bytes.subrange(4..<(size - 1)) // remove the initial size header, and remove the final 00 padding
}

@LowLevelApi
internal fun readField(
	bytes: Bytes,
	reader: RawBsonReader,
	type: BsonType,
	factory: BsonFactory,
): BsonValue {
	val fieldStart = reader.readCount

	@Suppress("DEPRECATION")
	val fieldSize = when (type) {
		BsonType.Double -> 8
		BsonType.String -> reader.peek().readIntLe() + 4
		BsonType.Document -> reader.peek().readIntLe()
		BsonType.Array -> reader.peek().readIntLe()
		BsonType.BinaryData -> reader.peek().readIntLe() + 5
		BsonType.Undefined -> 0
		BsonType.ObjectId -> 12
		BsonType.Boolean -> 1
		BsonType.Datetime -> 8
		BsonType.Null -> 0
		BsonType.RegExp -> {
			val peek = reader.peek()
			var byteCount = 0L
			// Read pattern (null-terminated string)
			while (peek.request(1) && peek.readByte() != 0.toByte())
				byteCount++
			byteCount++ // null terminator
			// Read options (null-terminated string)
			while (peek.request(1) && peek.readByte() != 0.toByte())
				byteCount++
			byteCount++ // null terminator
			byteCount.toInt()
		}

		BsonType.DBPointer -> TODO()
		BsonType.JavaScript -> reader.peek().readIntLe() + 4
		BsonType.Symbol -> TODO()
		BsonType.JavaScriptWithScope -> TODO()
		BsonType.Int32 -> 4
		BsonType.Timestamp -> 8
		BsonType.Int64 -> 8
		BsonType.Decimal128 -> 16
		BsonType.MinKey -> 0
		BsonType.MaxKey -> 0
	}

	val fieldRange = fieldStart..<(fieldStart + fieldSize)
	val fieldBytes = bytes.subrange(fieldRange)

	reader.skip(fieldSize)

	return BsonValue(factory, type, fieldBytes)
}

/**
 * Exposes the bytes in a [BsonDocument] as a lazily-populated [List].
 *
 * We take advantage of the linear nature of BSON: we only scan which fields exist when we need them.
 * We do not recursively decode the contents of each field.
 */
@LowLevelApi
internal class MultiplatformBsonDocumentMap(
	private val factory: BsonFactory,
	bytesWithHeader: Bytes,
) : Map<String, BsonValue> {

	private val bytes: Bytes = restrictAsDocument(bytesWithHeader)
	private val reader = this.bytes.reader

	/**
	 * Storage of the reader instances for each field of this class.
	 *
	 * This class is filled lazily by the [scanUntil] method.
	 */
	private val scannedFields = LinkedHashMap<String, BsonValue>()

	private fun scanOne(): String {
		val type = BsonType.fromCode(reader.readSignedByte())
		val name = reader.readCString()
		val field = readField(bytes, reader, type, factory)

		scannedFields[name] = field

		return name
	}

	private fun scanUntil(targetName: String?) {
		while (reader.request(1)) {
			val name = scanOne()

			if (name == targetName) {
				return
			}
		}
	}

	override fun get(key: String): BsonValue? = scannedFields[key]
		?: run {
			scanUntil(key)
			scannedFields[key]
		}

	operator fun iterator(): Iterator<BsonDocument.Field> =
		object : Iterator<BsonDocument.Field> {
			var index: Int = 0

			override fun hasNext(): Boolean =
				scannedFields.size > index || reader.request(1)

			private fun Iterator<*>.skip(n: Int) {
				for (i in 0 until n) {
					if (!hasNext()) return
					val _ = next()
				}
			}

			override fun next(): BsonDocument.Field {
				val iter = scannedFields.iterator()
				iter.skip(index)

				if (iter.hasNext()) {
					val (field, value) = iter.next()
					index++
					return BsonDocument.Field(field, value)
				} else if (reader.request(1)) {
					val field = scanOne()
					index++
					return BsonDocument.Field(field, scannedFields[field]!!)
				} else {
					throw NoSuchElementException("No more elements in this BSON document")
				}
			}
		}

	override val size: Int
		get() {
			scanUntil(null)
			return scannedFields.size
		}

	override fun isEmpty(): Boolean {
		// Fast path: we have already scanned a field
		if (scannedFields.isNotEmpty())
			return false

		// Otherwise: check buffer
		return !reader.request(1)
	}

	override val keys: Set<String>
		get() {
			scanUntil(null)
			return scannedFields.keys
		}

	override val values: Collection<BsonValue>
		get() {
			scanUntil(null)
			return scannedFields.values
		}

	override val entries: Set<Map.Entry<String, BsonValue>>
		get() {
			scanUntil(null)
			return scannedFields.entries
		}

	override fun containsKey(key: String): Boolean =
		get(key) != null

	override fun containsValue(value: BsonValue): Boolean {
		for ((_, found) in iterator())
			if (found == value)
				return true
		return false
	}

	override fun equals(other: Any?): Boolean {
		scanUntil(null)
		return scannedFields == other
	}

	override fun hashCode(): Int {
		scanUntil(null)
		return scannedFields.hashCode()
	}

	override fun toString(): String = buildString {
		append('{')

		var isFirst = true
		for ((key, value) in this@MultiplatformBsonDocumentMap) {
			if (!isFirst)
				append(", ")

			append('"')
			append(key)
			append("\": ")
			append(value)

			isFirst = false
		}

		append('}')
	}
}
