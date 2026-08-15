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

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readIntLe
import kotlinx.io.readString
import kotlinx.io.readUByte
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory

internal class ResponsePayload(
	val messageLength: Int,
	val requestId: Int,
	val responseTo: Int,
	val data: Buffer,
)

internal suspend fun ByteReadChannel.readResponse(): ResponsePayload {
	val messageLength = readInt().asLittleEndian()
	val requestId = readInt().asLittleEndian()
	val responseTo = readInt().asLittleEndian()

	val data = readBuffer(messageLength - (4 * 3)) // don't read the fields we already read

	return ResponsePayload(messageLength, requestId, responseTo, data)
}

internal fun Buffer.parseMessage(
	factory: BsonFactory,
	requestId: Int,
	responseTo: Int,
): Message {
	val buffer = this

	val opcode = buffer.readIntLe()
	check(opcode == 2013) { "Currently, only OP_MSG is supported, but found opcode $opcode" }

	buffer.readIntLe() // flag bits

	val sections = ArrayList<MessageSection>()

	while (buffer.canRead()) {
		when (val kind = buffer.readUByte()) {
			MessageSection.Body.kind -> {
				val size = buffer.peek().readIntLe()
				sections += MessageSection.Body(eager(factory.readDocument(buffer.readBytes(size)))) // TODO: avoid copy
			}

			MessageSection.DocumentSequence.kind -> {
				// • section size
				val size = buffer.readIntLe() - 4
				var read = 4

				// • section id
				val id = buffer.readCString()
				read += id.length
				read += 1 // null terminator

				// • section documents
				val documents = ArrayList<Lazy<BsonDocument>>()
				while (read < size) {
					val documentSize = buffer.peek().readIntLe()
					documents += eager(factory.readDocument(buffer.readBytes(documentSize))) // TODO: avoid copy
				}

				sections += MessageSection.DocumentSequence(id, documents)
			}

			else -> error("Unrecognized section kind $kind in message $requestId sent as response to $responseTo")
		}
	}

	val body = sections.singleOrNull { it is MessageSection.Body } as? MessageSection.Body
		?: error("An OP_MSG message must have a single body section, found: $sections")

	val response = Message.OpMsg(
		body,
		sections.asSequence()
			.filterIsInstance<MessageSection.DocumentSequence>(),
	)

	return response
}

private fun Int.asLittleEndian(): Int {
	return ((this and 0xFF) shl 24) or
		((this and 0xFF00) shl 8) or
		((this and 0xFF0000) shr 8) or
		((this and 0xFF000000.toInt()) ushr 24)
}

private fun Buffer.readCString(): String {
	val peek = peek()
	var byteCount = 0L
	while (peek.request(1) && peek.readByte() != 0.toByte())
		byteCount++

	return readString(byteCount)
		.also { skip(1) } // null-terminator
}
