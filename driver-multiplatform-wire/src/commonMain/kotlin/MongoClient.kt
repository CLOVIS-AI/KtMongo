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

package opensavvy.ktmongo.multiplatform.wire

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlinx.io.readIntLe
import kotlinx.io.writeIntLe
import kotlinx.io.writeUByte
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.coroutines.CoroutineContext

@LowLevelApi
interface MongoClient : AutoCloseable {

	suspend fun send(
		message: Message,
	): ReceiveChannel<Message>

	companion object
}

@LowLevelApi
private class MultiplatformMongoClient(
	private val socket: Socket,
	private val factory: BsonFactory,
	coroutineScope: CoroutineScope,
) : MongoClient {

	private class Request(
		val data: Buffer,
		val output: Channel<Message>,
	)

	private class Response(
		val length: Int,
		val requestId: Int,
		val responseTo: Int,
		val data: Buffer,
		val output: SendChannel<Message>,
	)

	private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

	private fun log(message: String) {
		println("KtMongo • $message")
	}

	init {
		val sentChannel = Channel<Pair<Int, SendChannel<Message>>>(Channel.BUFFERED)
		val receivedChannel = Channel<Response>(Channel.BUFFERED)

		coroutineScope.launch(CoroutineName("ktmongo-actor-writer")) {
			sendActor(sentChannel)
		}

		coroutineScope.launch(CoroutineName("ktmongo-actor-reader")) {
			readActor(sentChannel, receivedChannel)
		}

		repeat(3) {
			coroutineScope.launch(CoroutineName("ktmongo-actor-parser-$it")) {
				parserActor(receivedChannel)
			}
		}
	}

	private suspend fun sendActor(
		sentChannel: SendChannel<Pair<Int, SendChannel<Message>>>,
	) {
		val writeChannel = socket.openWriteChannel()
		var nextRequestId = 1

		while (currentCoroutineContext().isActive && !writeChannel.isClosedForWrite) {
			val request = requestChannel.receive()
			val requestId = nextRequestId++

			val buffer = Buffer()
			buffer.writeIntLe(request.data.size.toInt() + 8) // + the size itself (4) + the request ID (4)
			buffer.writeIntLe(requestId)
			buffer.write(request.data, request.data.size)
			writeChannel.writeBuffer(buffer)
			writeChannel.flush()

			log("$requestId was sent")
			sentChannel.send(requestId to request.output)
		}
	}

	private suspend fun readActor(
		sentChannel: ReceiveChannel<Pair<Int, SendChannel<Message>>>,
		receivedChannel: SendChannel<Response>,
	) = coroutineScope {
		val readChannel = socket.openReadChannel()
		val waiting = HashMap<Int, SendChannel<Message>>()

		while (currentCoroutineContext().isActive && !readChannel.isClosedForRead) {
			select {
				sentChannel.onReceive { (requestId, output) ->
					log("$requestId expects an answer")
					waiting[requestId] = output
				}

				async {
					readChannel.awaitContent(4 * 4) // standard message header size
				}.onAwait { isActive ->
					if (isActive) {
						log("Received a response from the DB.")
						val messageLength = readChannel.readInt().asLittleEndian()
						val requestId = readChannel.readInt().asLittleEndian()
						val responseTo = readChannel.readInt().asLittleEndian()

						log("Received message $requestId in response to $responseTo, of size $messageLength")

						val data = readChannel.readBuffer(messageLength - (4 * 3)) // don't read the fields we already read

						val handler = waiting[responseTo]
							?: error("Received the message $requestId in response to $responseTo, but no known message with ID $responseTo has been sent by this client.")
						receivedChannel.send(Response(messageLength, requestId, responseTo, data, handler))
					} else {
						log("MongoDB has stopped sending data to us.")
					}
				}
			}
		}
	}

	private suspend fun parserActor(
		receivedChannel: ReceiveChannel<Response>,
	) {
		for (received in receivedChannel) {
			val buffer = received.data

			buffer.readIntLe()
			buffer.readIntLe()

			val sections = ArrayList<MessageSection>()

			while (buffer.canRead()) {
				when (val kind = buffer.readByte()) {
					MessageSection.Body.kind -> {
						val size = buffer.peek().readIntLe()
						sections += MessageSection.Body(factory.readDocument(buffer.readBytes(size)))
					}

					MessageSection.DocumentSequence.kind -> {
						TODO()
					}

					else -> error("Unrecognized section kind $kind in message ${received.requestId} sent as response to ${received.responseTo}")
				}
			}

			log("Received: $sections")
		}
	}

	private fun Int.asLittleEndian(): Int {
		return ((this and 0xFF) shl 24) or
			((this and 0xFF00) shl 8) or
			((this and 0xFF0000) shr 8) or
			((this and 0xFF000000.toInt()) ushr 24)
	}

	private fun writeMessage(message: Message): Buffer {
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

		// Section kind: Body
		buffer.writeUByte(0u)

		buffer.write(message.content.toByteArray()) // TODO: avoid copy

		// endregion

		return buffer
	}

	override suspend fun send(
		message: Message,
	): ReceiveChannel<Message> {
		val output = Channel<Message>()
		log("Preparing to write $message…")
		val buffer = writeMessage(message)
		log("Message to send is: ${message.content}")
		requestChannel.send(Request(buffer, output))
		return output
	}

	override fun close() {
		socket.close()
	}

	override fun toString() = "MongoClient(${socket.remoteAddress})"
}

@LowLevelApi
suspend fun MongoClient(
	hostName: String,
	port: Int,
	factory: BsonFactory = BsonFactory(),
	coroutineContext: CoroutineContext,
): MongoClient {
	val selectorManager = SelectorManager(coroutineContext + Dispatchers.Default + CoroutineName("ktmongo-socket"))
	val socket = aSocket(selectorManager).tcp().connect(hostName, port)

	return MultiplatformMongoClient(socket, factory, CoroutineScope(coroutineContext + CoroutineName("ktmongo-client")))
}
