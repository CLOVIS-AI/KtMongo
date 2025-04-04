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

import kotlinx.io.readIntLe
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal class MultiplatformBsonDocumentReader(
	bytes: Bytes,
) : BsonDocumentReader {

	init {
		println("Creating a document from:      $bytes") // TODO remove
	}

	private val size: Int =
		bytes.reader.readInt32()

	private val bytes: Bytes =
		bytes.subrange(4..<(size - 1)) // remove the initial size header, and remove the final 00 padding

	init {
		println("Detected document payload:     ${this.bytes}") // TODO remove
	}

	private val reader = this.bytes.reader

	/**
	 * Storage of the reader instances for each field of this class.
	 *
	 * This class is filled lazily by the [scanUntil] method.
	 */
	private val fields = LinkedHashMap<String, MultiplatformBsonValueReader>()

	private fun scanUntil(targetName: String?) {
		while (reader.request(1)) {
			println("Left to read: $reader")
			val type = BsonType.fromCode(reader.readSignedByte())
			val name = reader.readCString()
			val fieldStart = reader.readCount

			println("Found field '$name' of type $type, starting at index $fieldStart")

			@Suppress("DEPRECATION")
			val fieldSize = when (type) {
				BsonType.Double -> 8
				BsonType.String -> reader.peek().readIntLe() + 4
				BsonType.Document -> reader.peek().readIntLe()
				BsonType.Array -> TODO()
				BsonType.BinaryData -> TODO()
				BsonType.Undefined -> 0
				BsonType.ObjectId -> 12
				BsonType.Boolean -> 1
				BsonType.Datetime -> 8
				BsonType.Null -> 0
				BsonType.RegExp -> TODO()
				BsonType.DBPointer -> TODO()
				BsonType.JavaScript -> TODO()
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

			println("Found field '$name' in range $fieldRange: $fieldBytes")

			fields[name] = MultiplatformBsonValueReader(type, fieldBytes)

			reader.skip(fieldSize)

			if (name == targetName) {
				return
			}
		}

		println("Reached the end of the document")
	}

	override fun read(name: String): BsonValueReader? = fields[name]
		?: run {
			scanUntil(name)
			fields[name]
		}

	override val entries: Map<String, BsonValueReader>
		get() {
			scanUntil(null)
			return fields
		}

	override fun toString(): String = buildString {
		append('{')

		var isFirst = true
		for ((key, value) in entries) {
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
