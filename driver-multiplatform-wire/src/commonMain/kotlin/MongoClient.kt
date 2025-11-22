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

/**
 * MongoDB client based on a [socket].
 *
 * ### Implementation
 *
 * 1. The user calls [send].
 * 2. The request is serialized to binary and added to [requestChannel].
 * 3. The [sendActor] sends it into the socket and tells the [triageActor].
 * 4. When a response arrives, it is read by the [readActor].
 * The entire response is extracted from the socket and sent to the [triageActor].
 * 5. The [triageActor] matches the response with the initial request, then passes the result to [parserActor]s.
 * 6. The [parserActor]s deserialize the request and give it back to the original [send] to be returned to the user.
 */
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

	private class SentMessage(
		val requestId: Int,
		val output: Channel<Message>,
	)

	private class Response(
		val requestId: Int,
		val responseTo: Int,
		val data: Buffer,
	)

	private class ResponseWithHandler(
		val response: Response,
		val output: SendChannel<Message>,
	)

	/**
	 * When a client calls [send], the request is serialized then is added to this channel.
	 *
	 * The [triageActor] reads from this channel.
	 */
	private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

	private fun log(message: String) {
		println("KtMongo • $message")
	}

	init {
		/**
		 * When the [sendActor] has sent a message into the socket, it adds a message in here.
		 *
		 * The [triageActor] reads from this channel.
		 */
		val sentChannel = Channel<SentMessage>(Channel.BUFFERED)

		/**
		 * When the [readActor] has found a message in the socket, it adds it here.
		 *
		 * The [triageActor] reads from this channel.
		 */
		val receivedChannel = Channel<Response>(Channel.BUFFERED)

		/**
		 * When the [triageActor] has combined a response with its request handler, it adds it here.
		 *
		 * The [parserActor]s read from this channel.
		 */
		val triagedChannel = Channel<ResponseWithHandler>(Channel.BUFFERED)

		coroutineScope.launch(CoroutineName("ktmongo-actor-writer")) {
			sendActor(sentChannel)
		}

		coroutineScope.launch(CoroutineName("ktmongo-actor-reader")) {
			readActor(receivedChannel)
		}

		coroutineScope.launch(CoroutineName("ktmongo-actor-triage")) {
			triageActor(sentChannel, receivedChannel, triagedChannel)
		}

		repeat(3) {
			coroutineScope.launch(CoroutineName("ktmongo-actor-parser-$it")) {
				parserActor(triagedChannel)
			}
		}
	}

	/**
	 * The [sendActor]:
	 * 1. Reads from [requestChannel].
	 * 2. Writes into the socket.
	 * 3. Tells the [triageActor] about the request through [sentChannel].
	 */
	private suspend fun sendActor(
		sentChannel: SendChannel<SentMessage>,
	) {
		val writeSocket = socket.openWriteChannel()
		var nextRequestId = 1

		while (currentCoroutineContext().isActive && !writeSocket.isClosedForWrite) {
			val request = requestChannel.receive()
			val requestId = nextRequestId++

			val buffer = Buffer()
			buffer.writeIntLe(request.data.size.toInt() + 8) // + the size itself (4) + the request ID (4)
			buffer.writeIntLe(requestId)
			buffer.write(request.data, request.data.size)
			writeSocket.writeBuffer(buffer)
			writeSocket.flush()

			log("$requestId was sent")
			sentChannel.send(SentMessage(requestId, request.output))
		}
	}

	/**
	 * The [readActor]:
	 * 1. Reads from the socket.
	 * 2. Sends each response to the [triageActor] through [receivedChannel].
	 */
	private suspend fun readActor(
		receivedChannel: SendChannel<Response>,
	) {
		val readSocket = socket.openReadChannel()

		while (currentCoroutineContext().isActive && !readSocket.isClosedForRead) {
			val messageLength = readSocket.readInt().asLittleEndian()
			val requestId = readSocket.readInt().asLittleEndian()
			val responseTo = readSocket.readInt().asLittleEndian()

			log("Received message $requestId in response to $responseTo, of size $messageLength")

			val data = readSocket.readBuffer(messageLength - (4 * 3)) // don't read the fields we already read
			receivedChannel.send(Response(requestId, responseTo, data))
		}
	}

	/**
	 * The [triageActor]:
	 * 1. Reads all the requests that have been sent by the [sendActor] through [sentChannel].
	 * 2. Reads all the responses that have been received by the [readActor] through [receivedChannel].
	 * 3. For each response, matches it with its initial request, and send them to the [parserActor]s through [triagedChannel].
	 */
	private suspend fun triageActor(
		sentChannel: ReceiveChannel<SentMessage>,
		receivedChannel: ReceiveChannel<Response>,
		triagedChannel: SendChannel<ResponseWithHandler>,
	) {
		val waiting = HashMap<Int, SendChannel<Message>>()

		while (currentCoroutineContext().isActive && socket.isActive) {
			select {
				/*
				 * Always give priority to the requests sent to ensure we NEVER
				 * read a response before reading its request.
				 */
				sentChannel.onReceive { message ->
					log("${message.requestId} expects an answer")
					waiting[message.requestId] = message.output
				}

				receivedChannel.onReceive { response ->
					val handler = waiting[response.responseTo]
						?: error("Received the message ${response.requestId} in response to ${response.responseTo}, but no known message with ID ${response.responseTo} has been sent by this client.\nCurrently in-flight requests: ${waiting.keys.sorted()}")
					triagedChannel.send(ResponseWithHandler(response, handler))
				}
			}
		}
	}

	/**
	 * The [parserActor]s:
	 * 1. Receives triaged responses from the [triageActor] through [receivedChannel].
	 * 2. Deserializes each response.
	 * 3. Sends it back to [send] using the response's [ResponseWithHandler.output].
	 */
	private suspend fun parserActor(
		receivedChannel: ReceiveChannel<ResponseWithHandler>,
	) {
		for (received in receivedChannel) {
			val buffer = received.response.data

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

					else -> error("Unrecognized section kind $kind in message ${received.response.requestId} sent as response to ${received.response.responseTo}")
				}
			}

			log("Received: $sections")

			// TODO: send it back to the sender
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
