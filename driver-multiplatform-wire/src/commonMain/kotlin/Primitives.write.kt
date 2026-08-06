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

package opensavvy.ktmongo.multiplatform.wire

import kotlinx.io.Buffer
import kotlinx.io.writeIntLe
import kotlinx.io.writeString
import kotlinx.io.writeUByte

internal fun writeMessage(message: Message): Buffer {
	val buffer = Buffer()

	// region Message header
	// https://www.mongodb.com/docs/manual/reference/mongodb-wire-protocol/#standard-message-header

	// Writes the complete message to the buffer EXCEPT the first 2 fields:
	// • message length
	// • request ID
	// The writer actor will add these two fields.

	// • response to
	buffer.writeIntLe(0)

	// • opcode
	buffer.writeIntLe(message.opcode)

	// endregion
	// region Message flags
	// https://www.mongodb.com/docs/manual/reference/mongodb-wire-protocol/#flag-bits

	buffer.writeIntLe(0)

	// endregion
	// region Sections

	when (message) {
		is Message.OpMsg -> {
			writeOpMsg(message, buffer)
		}
	}

	// endregion

	return buffer
}

internal fun writeOpMsg(message: Message.OpMsg, buffer: Buffer) {
	// First, write the body (any order is allowed in the spec)

	// • body section kind
	buffer.writeUByte(message.body.kind)

	// • body content
	buffer.write(message.body.document.toByteArray()) // TODO: avoid copy

	// Next, read the sequences, if any
	for (sequence in message.sequences) {
		// • section kind
		buffer.writeUByte(sequence.kind)

		val payload = Buffer()
		payload.writeCString(sequence.id)
		for (document in sequence.documents) {
			payload.write(document.toByteArray()) // TODO: avoid copy
		}

		// • size
		buffer.writeIntLe(payload.size.toInt() + 4)

		// • documents
		buffer.write(payload, payload.size)
	}
}

private fun Buffer.writeCString(value: String) {
	val text = value
		.takeUnless { 0.toChar() in it }
		?: value.filterNot { it == 0.toChar() }

	writeString(text)
	writeUByte(0u)
}
