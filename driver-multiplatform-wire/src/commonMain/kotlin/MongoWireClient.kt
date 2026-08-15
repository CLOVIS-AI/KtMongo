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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlinx.io.writeIntLe
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

@LowLevelApi
interface MongoWireClient : AutoCloseable {

	suspend fun send(
		message: Message,
	): ReceiveChannel<Message>

	/**
	 * Sends a [message] that expects a single response.
	 */
	suspend fun sendSingle(
		message: Message,
	): Message

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
private class SocketWireClient(
	private val socket: MongoSocket,
	private val factory: BsonFactory,
	coroutineScope: CoroutineScope, // Should contain a Job dedicated to this client
) : MongoWireClient {

	private val actorsJob = coroutineScope.coroutineContext.job

	private sealed class ResponseHandler {
		data class Single(val result: CompletableDeferred<Message>) : ResponseHandler()
		data class Multiple(val result: SendChannel<Message>) : ResponseHandler()
	}

	private class Request(
		val data: Buffer,
		val output: ResponseHandler,
	)

	private class SentMessage(
		val requestId: Int,
		val output: ResponseHandler,
	)

	private class Response(
		val requestId: Int,
		val responseTo: Int,
		val data: Buffer,
	)

	private class ResponseWithHandler(
		val response: Response,
		val output: ResponseHandler,
	)

	/**
	 * When a client calls [send], the request is serialized then is added to this channel.
	 *
	 * The [triageActor] reads from this channel.
	 */
	private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

	// Helps debugging time-sensitive operations for now. Will need to be removed when stabilizing, and be replaced by a proper observability framework.
	private val start = TimeSource.Monotonic.markNow()

	private fun log(message: String) {
		println("» KtMongo +${start.elapsedNow().toString(DurationUnit.MILLISECONDS, decimals = 0)} • $message")
	}

	init {
		log("Creating client for socket $socket")

		// Ensure that no resources can leak
		actorsJob.invokeOnCompletion { close() }

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
			val response = readSocket.readResponse()

			log("Received message ${response.requestId} in response to ${response.responseTo}, of size ${response.messageLength}")

			receivedChannel.send(Response(response.requestId, response.responseTo, response.data))
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
		val waiting = HashMap<Int, ResponseHandler>()

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
					if (handler is ResponseHandler.Single) {
						waiting.remove(response.responseTo)
					}
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
			val message = received.response.data.parseMessage(
				factory = factory,
				requestId = received.response.requestId,
				responseTo = received.response.responseTo,
			)

			log("Received: $message")

			when (received.output) {
				is ResponseHandler.Single -> received.output.result.complete(message)
				is ResponseHandler.Multiple -> received.output.result.send(message)
			}
		}
	}

	override suspend fun send(
		message: Message,
	): ReceiveChannel<Message> {
		val output = Channel<Message>()
		log("Preparing to write $message…")
		val buffer = writeMessage(message)
		requestChannel.send(Request(buffer, ResponseHandler.Multiple(output)))
		return output
	}

	override suspend fun sendSingle(message: Message): Message {
		val output = CompletableDeferred<Message>()
		log("Preparing to write $message…")
		val buffer = writeMessage(message)
		requestChannel.send(Request(buffer, ResponseHandler.Single(output)))
		val message = output.await()
		return message
	}

	override fun close() {
		actorsJob.cancel("${this::class}.close() has been called")
		socket.close()
	}

	override fun toString() = "MongoWireClient($socket)"
}

/**
 * Creates a [MongoWireClient] wrapping an existing [socket].
 *
 * Used by tests to inject fake sockets instead of connecting to a real server.
 */
@LowLevelApi
internal fun MongoWireClient(
	socket: MongoSocket,
	factory: BsonFactory = BsonFactory(),
	coroutineScope: CoroutineScope,
): MongoWireClient =
	SocketWireClient(
		socket = socket,
		factory = factory,
		coroutineScope = coroutineScope,
	)

@LowLevelApi
suspend fun MongoWireClient(
	hostName: String,
	port: Int,
	factory: BsonFactory = BsonFactory(),
	coroutineContext: CoroutineContext,
): MongoWireClient {
	val innerJob = Job(coroutineContext.job)

	val selectorManager = SelectorManager(coroutineContext + innerJob + Dispatchers.Default + CoroutineName("ktmongo-socket"))
	val socket = aSocket(selectorManager).tcp().connect(hostName, port) {
		keepAlive = true
	}

	return SocketWireClient(
		socket = MongoSocket(socket, selectorManager),
		factory = factory,
		coroutineScope = CoroutineScope(coroutineContext + innerJob + CoroutineName("ktmongo-client"))
	)
}
