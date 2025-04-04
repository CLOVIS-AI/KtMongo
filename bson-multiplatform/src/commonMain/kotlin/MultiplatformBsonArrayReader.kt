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

import opensavvy.ktmongo.bson.BsonArrayReader
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal class MultiplatformBsonArrayReader(
	bytes: Bytes,
) : BsonArrayReader {

	private val bytes: Bytes = restrictAsDocument(bytes)
	private val reader = this.bytes.reader

	private val fields = ArrayList<MultiplatformBsonValueReader>()

	private fun scanUntil(targetIndex: Int?) {
		println("Scanning until index $targetIndex…") // TODO remove
		while (reader.request(1)) {
			println("Left to read: $reader") // TODO remove
			val type = BsonType.fromCode(reader.readSignedByte())
			reader.skipCString() // We ignore the field name
			val field = readField(bytes, reader, "${fields.lastIndex + 1}", type)

			fields += field

			if (targetIndex != null && fields.lastIndex >= targetIndex) {
				println("Found ${fields.size} elements, giving up") // TODO remove
				return
			}
		}

		println("Reached the end of the array") // TODO remove
	}

	override fun read(index: Int): BsonValueReader? =
		fields.getOrNull(index) ?: run {
			scanUntil(index)
			fields.getOrNull(index)
		}

	override val elements: List<BsonValueReader>
		get() {
			scanUntil(null)
			return fields
		}

	override fun toString(): String = buildString {
		append('[')
		var isFirst = true
		for (element in elements) {
			if (!isFirst)
				append(", ")

			append(element)

			isFirst = false
		}
		append(']')
	}
}
