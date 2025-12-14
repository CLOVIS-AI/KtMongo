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

import kotlinx.io.readIntLe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.bson.multiplatform.Bson
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.multiplatform.Bytes
import opensavvy.ktmongo.bson.multiplatform.RawBsonReader
import opensavvy.ktmongo.bson.multiplatform.serialization.BsonDecoder
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal fun restrictAsDocument(bytes: Bytes): Bytes {
	println("Creating a document from:      $bytes") // TODO remove
	val size = bytes.reader.readInt32()
	return bytes.subrange(4..<(size - 1)) // remove the initial size header, and remove the final 00 padding
		.also { println("Detected document payload:     $it") } // TODO remove
}

@LowLevelApi
internal fun readField(
	bytes: Bytes,
	reader: RawBsonReader,
	name: String,
	type: BsonType,
	factory: BsonFactory,
): MultiplatformBsonValueReader {
	val fieldStart = reader.readCount

	println("Found field '$name' of type $type, starting at index $fieldStart")

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

	println("Found field '$name' in range $fieldRange: $fieldBytes")

	return MultiplatformBsonValueReader(factory, type, fieldBytes)
}

@LowLevelApi
internal class MultiplatformDocumentReader(
	private val factory: BsonFactory,
	private val bytesWithHeader: Bytes,
) : BsonDocumentReader {

	private val bytes: Bytes = restrictAsDocument(bytesWithHeader)
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
			val field = readField(bytes, reader, name, type, factory)

			fields[name] = field

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

	override fun toBson(): Bson =
		Bson(factory, bytesWithHeader)

	override fun asValue(): BsonValueReader =
		MultiplatformBsonValueReader(factory, BsonType.Document, bytesWithHeader)

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> read(type: KType, klass: KClass<T>): T? {
		val decoder = BsonDecoder(EmptySerializersModule(), this.asValue())
		return decoder.decodeSerializableValue(serializer(type) as KSerializer<T?>)
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
