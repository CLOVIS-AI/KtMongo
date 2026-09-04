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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlinx.io.writeIntLe
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

@LowLevelApi
interface MongoWireClient {

	suspend fun send(
		message: Message,
	): ReceiveChannel<Message>

	/**
	 * Sends a [message] that expects a single response.
	 */
	suspend fun sendSingle(
		message: Message,
	): Message

	/**
	 * Closes the client.
	 *
	 * If there are in-flight requests, they will be allowed to complete.
	 * However, no new requests will be accepted.
	 */
	suspend fun close()

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
	private val inFlightJob = SupervisorJob(actorsJob)

	private sealed class ResponseHandler {
		abstract val isActive: Boolean
		abstract fun completeExceptionally(exception: Throwable)

		data class Single(val result: CompletableDeferred<Message>) : ResponseHandler() {
			override val isActive: Boolean
				get() = result.isActive

			override fun completeExceptionally(exception: Throwable) {
				result.completeExceptionally(exception)
			}
		}

		data class Multiple(val result: SendChannel<Message>) : ResponseHandler() {
			@OptIn(DelicateCoroutinesApi::class) // Only used as a heuristic
			override val isActive: Boolean
				get() = !result.isClosedForSend

			override fun completeExceptionally(exception: Throwable) {
				result.close(exception)
			}
		}
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

	private fun log(actorName: String, message: String) {
		println("» KtMongo +${start.elapsedNow().toString(DurationUnit.MILLISECONDS, decimals = 0)} • $actorName • $message")
	}

	private val sendActor: Job
	private val readActor: Job
	private val triageActor: Job
	private val parserActorSupervisor: Job

	init {
		log("init", "Creating client for socket $socket")

		// Ensure that no resources can leak
		actorsJob.invokeOnCompletion { socket.close() }

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

		sendActor = coroutineScope.launch(CoroutineName("ktmongo-actor-writer")) {
			sendActor(sentChannel)
		}

		readActor = coroutineScope.launch(CoroutineName("ktmongo-actor-reader")) {
			readActor(receivedChannel)
		}

		triageActor = coroutineScope.launch(CoroutineName("ktmongo-actor-triage")) {
			triageActor(sentChannel, receivedChannel, triagedChannel)
		}

		parserActorSupervisor = coroutineScope.launch(CoroutineName("ktmongo-actor-parser-supervisor")) {
			supervisorScope {
				repeat(3) {
					spawnParserActor(this, triagedChannel)
				}
			}
		}
	}

	private fun spawnParserActor(
		coroutineScope: CoroutineScope,
		triagedChannel: ReceiveChannel<ResponseWithHandler>,
	) {
		@OptIn(DelicateCoroutinesApi::class) // we only use 'isClosedForReceive' as a heuristic to avoid creating useless coroutines
		if (coroutineScope.isActive && !triagedChannel.isClosedForReceive) {
			coroutineScope.launch(CoroutineName("ktmongo-actor-parser")) {
				parserActor(triagedChannel)
			}.invokeOnCompletion {
				if (it !is CancellationException && it != null) {
					log("Parser spawner", "Respawning a parser actor because one crashed with $it")
					spawnParserActor(coroutineScope, triagedChannel)
				}
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

		try {
			requestChannel.consumeEach { request ->
				val requestId = nextRequestId++

				if (!request.output.isActive) {
					return@consumeEach // If the output is canceled, give up and don't send the request at all
				}

				try {
					val buffer = Buffer()
					buffer.writeIntLe(request.data.size.toInt() + 8) // + the size itself (4) + the request ID (4)
					buffer.writeIntLe(requestId)
					buffer.write(request.data, request.data.size)
					writeSocket.writeBuffer(buffer)
					writeSocket.flush()

					if (!request.output.isActive) {
						return@consumeEach // If the output is canceled, don't send it to the next actor, the response will arrive in the future but be ignored
					}

					log("Send", "$requestId was sent")
					sentChannel.send(SentMessage(requestId, request.output))
				} catch (e: Exception) {
					request.output.completeExceptionally(e)
					throw e
				}
			}
			log("Send", "Successfully sent all requests, shutting down the send actor")
		} catch (e: Exception) {
			val decorated = RuntimeException("Exception was thrown in the send actor", e)

			log("Send", "Failed with $e")

			log("Send", "Purging not-yet-sent requests")
			runCatching {
				requestChannel.consumeEach { request ->
					request.output.completeExceptionally(decorated)
				}
			}.getOrElse { e.addSuppressed(it) }

			log("Send", "Closing write socket")
			writeSocket.close(decorated)

			sentChannel.close(decorated)
			throw e // rethrow the *original* exception (could be a cancellation)
		} finally {
			sentChannel.close()
			writeSocket.close(null)
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

		try {
			while (readSocket.awaitContent(8)) {
				val response = readSocket.readResponse()

				log("Read", "Received message ${response.requestId} in response to ${response.responseTo}, of size ${response.messageLength}")

				receivedChannel.send(Response(response.requestId, response.responseTo, response.data))
			}
			log("Read", "Successfully read all data in the socket and it was closed, shutting down the read actor")
		} catch (e: Throwable) {
			val decorated = RuntimeException("Exception was thrown in the read actor", e)

			log("Read", "Error reading from socket: $e")
			receivedChannel.close(decorated)
			throw e // rethrow the *original* exception (could be a cancellation)
		} finally {
			receivedChannel.close()
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

		try {
			var unacknowledgedRequests = true
			var unacknowledgedResponses = true
			while (unacknowledgedRequests || unacknowledgedResponses) {
				select {
					/*
					 * Always give priority to the requests sent to ensure we NEVER
					 * read a response before reading its request.
					 */
					if (unacknowledgedRequests) {
						sentChannel.onReceiveCatching { message ->
							if (!message.isClosed) {
								val message = message.getOrThrow()
								log("Triage", "${message.requestId} expects an answer")
								waiting[message.requestId] = message.output
							} else {
								log("Triage", "All incoming requests have been handled")
								unacknowledgedRequests = false
							}
						}
					}

					if (unacknowledgedResponses) {
						receivedChannel.onReceiveCatching { response ->
							if (!response.isClosed) {
								val response = response.getOrThrow()
								log("Triage", "Triaging a response to ${response.responseTo}")
								val handler = waiting[response.responseTo]
									?: error("Received the message ${response.requestId} in response to ${response.responseTo}, but no known message with ID ${response.responseTo} has been sent by this client.\nCurrently in-flight requests: ${waiting.keys.sorted()}")
								triagedChannel.send(ResponseWithHandler(response, handler))
								if (handler is ResponseHandler.Single) {
									waiting.remove(response.responseTo)
								}
							} else {
								log("Triage", "All incoming responses have been handled")
								unacknowledgedResponses = false
							}
						}
					}
				}
			}

			log("Triage", "Successfully triaged all incoming requests and responses, shutting down the triage actor")
		} catch (e: Throwable) {
			val decorated = RuntimeException("Exception was thrown in the triage actor", e)

			log("Triage", "Failed with $e")

			log("Triage", "Purging in-flight requests")
			runCatching {
				for (handler in waiting.values) {
					handler.completeExceptionally(decorated)
				}
			}.getOrElse { e.addSuppressed(it) }

			log("Triage", "Purging sent requests that have been not yet been acknowledged by the triage actor")
			runCatching {
				sentChannel.consumeEach { request ->
					request.output.completeExceptionally(decorated)
				}
			}.getOrElse { e.addSuppressed(it) }

			log("Triage", "Purging responses that have not yet been triaged")
			runCatching {
				receivedChannel.consumeEach {}
			}.getOrElse { e.addSuppressed(decorated) }

			triagedChannel.close(decorated)
			throw e // rethrow the *original* exception (could be a cancellation)
		} finally {
			triagedChannel.close()
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
			try {
				val message = received.response.data.parseMessage(
					factory = factory,
					requestId = received.response.requestId,
					responseTo = received.response.responseTo,
				)

				log("Parser", "Received: $message")

				when (received.output) {
					is ResponseHandler.Single -> received.output.result.complete(message)
					is ResponseHandler.Multiple -> received.output.result.send(message)
				}
			} catch (e: Throwable) {
				received.output.completeExceptionally(e)
			}
		}

		log("Parser", "Successfully parsed all incoming responses, shutting down the parser actor")

		// No exception handling for crashed parser actors: if they die, they'll get replaced by a new one
	}

	override suspend fun send(
		message: Message,
	): ReceiveChannel<Message> {
		val output = Channel<Message>()
		log("send", "Preparing to write $message…")
		val buffer = writeMessage(message)
		requestChannel.send(Request(buffer, ResponseHandler.Multiple(output)))
		return output
	}

	override suspend fun sendSingle(message: Message): Message {
		log("sendSingle", "Preparing to write $message…")
		val buffer = writeMessage(message)

		val output = CompletableDeferred<Message>(inFlightJob)
		try {
			requestChannel.send(Request(buffer, ResponseHandler.Single(output)))
			output.join() // If the caller cancels, we'll throw an exception here
		} catch (e: Throwable) {
			if (!currentCoroutineContext().isActive)
				log("sendSingle", "Detected a cancelled caller for $message")

			// In case 'send' fails
			output.cancel("An exception was thrown while sending $message", e)
			throw e
		}

		return output.await()
	}

	private suspend inline fun Job.tryJoinOrCancel(
		timeout: Duration,
		jobName: String,
	) {
		val job = this

		// Give the job a chance to shut itself down properly
		withTimeoutOrNull(timeout) {
			job.join()
		} ?: run {
			// Otherwise, shut it down forcefully
			job.cancel("${this@SocketWireClient::class}.close() was called but the $jobName did not complete by itself in $timeout")
			job.join()
		}
	}

	override suspend fun close() = withContext(NonCancellable) {
		log("close", ".close() has been called")

		// Allow the supervisor to all in-flight requests to complete
		inFlightJob.complete()

		// Orderly shutdown for the send actor once all requests have been processed
		requestChannel.close()
		sendActor.tryJoinOrCancel(200.milliseconds, "send actor")

		// If the send actor shut down properly, the socket should be shut down
		// and the read actor should be orderly shutting down itself
		readActor.tryJoinOrCancel(500.milliseconds, "read actor")

		// The two actors that access the socket are now dead;
		// In theory the socket should already be dead but just to confirm.
		socket.close()

		// Since there is nothing to triage anymore, the triage actor should be
		// shutting itself down.
		triageActor.tryJoinOrCancel(100.milliseconds, "triage actor")

		// Since there is nothing to triage, the parsers should be shutting down too.
		parserActorSupervisor.tryJoinOrCancel(200.milliseconds, "parser actors")

		// There should not be any children left
		actorsJob.cancel("${this::class}.close() has been called")
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
