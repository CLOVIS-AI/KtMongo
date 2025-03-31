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

// https://bsonspec.org/spec.html
internal class RawBsonWriter(
	private val sink: Sink,
) {

	fun writeUnsignedByte(value: UByte) {
		sink.writeUByte(value)
	}

	fun writeSignedByte(value: Byte) {
		sink.writeByte(value)
	}

	fun writeInt32(value: Int) {
		sink.writeIntLe(value)
	}

	fun writeInt64(value: Long) {
		sink.writeLongLe(value)
	}

	fun writeUInt54(value: ULong) {
		sink.writeULongLe(value)
	}

	fun writeDouble(value: Double) {
		sink.writeDouble(value)
	}

	fun writeCString(value: String) {
		val text = value
			.takeUnless { 0.toChar() in it }
			?: value.filterNot { it == 0.toChar() }

		sink.writeString(text)
		writeUnsignedByte(0u)
	}

	fun writeString(value: String) {
		val bytes = value.encodeToByteArray()
		writeInt32(bytes.size + 1)
		sink.write(bytes)
		writeUnsignedByte(0u)
	}

}
