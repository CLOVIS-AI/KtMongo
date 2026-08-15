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

@file:OptIn(LowLevelApi::class, ExperimentalBsonDiffApi::class)

package opensavvy.ktmongo.multiplatform.wire.fake

import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.Buffer
import kotlinx.io.writeIntLe
import opensavvy.ktmongo.bson.ExperimentalBsonDiffApi
import opensavvy.ktmongo.bson.diff
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.multiplatform.wire.*
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.cleanUp
import opensavvy.prepared.suite.foregroundScope

private fun logFake(message: String) {
	println("» Fake: $message")
}

/**
 * A fake in-memory [MongoSocket], backed by an in-memory pipe instead of a real network connection.
 *
 * Reading from this socket returns the bytes written to its [peer][FakeMongoSocket] socket, and vice-versa.
 */
private class FakeMongoSocket(
	private val readChannel: ByteReadChannel,
	private val writeChannel: ByteWriteChannel,
) : MongoSocket {

	override val isActive: Boolean
		get() = !readChannel.isClosedForRead && !writeChannel.isClosedForWrite

	override fun openReadChannel(): ByteReadChannel = readChannel

	override fun openWriteChannel(): ByteWriteChannel = writeChannel

	override fun close() {
		readChannel.cancel(null)
		writeChannel.cancel(null)
	}

	override fun toString() = "Fake socket"

	companion object {
		/**
		 * Creates a pair of [FakeMongoSocket]s connected to each other, as if by a network connection:
		 * anything written to one of them can be read from the other one.
		 */
		fun createLinked(): Pair<MongoSocket, MongoSocket> {
			val clientToServer = ByteChannel()
			val serverToClient = ByteChannel()

			val client = FakeMongoSocket(readChannel = serverToClient, writeChannel = clientToServer)
			val server = FakeMongoSocket(readChannel = clientToServer, writeChannel = serverToClient)

			return client to server
		}
	}
}

/**
 * Writes a full wire-protocol frame for [message], as a response to the request identified by [responseTo].
 */
private fun writeResponseFrame(message: Message.OpMsg, requestId: Int, responseTo: Int): Buffer {
	val payload = Buffer()
	payload.writeIntLe(responseTo)
	payload.writeIntLe(message.opcode)
	payload.writeIntLe(0) // flag bits
	writeOpMsg(message, payload)

	val frame = Buffer()
	frame.writeIntLe(payload.size.toInt() + 8) // + the size itself (4) + the request ID (4)
	frame.writeIntLe(requestId)
	frame.write(payload, payload.size)
	return frame
}

@DslMarker
annotation class FakeServerDsl

@FakeServerDsl
class FakeServer private constructor(
	private val currentTest: TestDsl,
	private val scenario: FakeServerScenario,
) : AutoCloseable {

	private val sockets = FakeMongoSocket.createLinked()
	private val clientSocket get() = sockets.first
	private val serverSocket get() = sockets.second

	init {
		currentTest.foregroundScope.launch(CoroutineName("fake-server")) {
			run()
		}
	}

	override fun close() {
		serverSocket.close()
		clientSocket.close()
	}

	private suspend fun run() {
		val readChannel = serverSocket.openReadChannel()
		val writeChannel = serverSocket.openWriteChannel()

		var lastRequestId = 0
		var nextResponseId = 1

		for (event in scenario.events) {
			when (event) {
				is FakeServerScenario.Event.Expect -> {
					lastRequestId = verifyExpect(event, readChannel)
				}

				is FakeServerScenario.Event.Respond -> {
					verifyRespond(event, writeChannel, requestId = nextResponseId++, responseTo = lastRequestId)
				}
			}
		}

		logFake("No more events to execute.")
		serverSocket.close()
	}

	private suspend fun verifyExpect(
		event: FakeServerScenario.Event.Expect,
		readChannel: ByteReadChannel,
	): Int {
		logFake("Expecting $event")
		val expected = event.message
		check(expected is Message.OpMsg) { "Other kinds of messages are not supported yet" }

		val responsePayload = readChannel.readResponse()

		val actual = responsePayload.data.parseMessage(
			factory = BsonFactory(),
			requestId = responsePayload.requestId,
			responseTo = responsePayload.responseTo,
		)

		logFake("Received  $actual")

		check(actual is Message.OpMsg) { "Other kinds of messages are not supported yet" }
		check(actual.body.document == expected.body.document) { "The received document doesn't match the expected document:\n${actual.body.document diff expected.body.document}" }

		val expectedSequences = expected.sequences.toList()
			.sortedBy { it.id }
		val actualSequences = actual.sequences.toList()
			.sortedBy { it.id }

		check(expectedSequences.size == actualSequences.size)

		for ((expectedSequence, actualSequence) in expectedSequences.zip(actualSequences)) {
			check(expectedSequence.id == actualSequence.id)

			val expectedDocuments = expectedSequence.documents.toList()
			val actualDocuments = actualSequence.documents.toList()
			check(expectedDocuments.size == actualDocuments.size)

			for ((expectedDocument, actualDocument) in expectedDocuments.zip(actualDocuments)) {
				check(expectedDocument == actualDocument) { "${expectedDocument diff actualDocument}" }
			}
		}

		logFake("Expectation verified")

		return responsePayload.requestId
	}

	private suspend fun verifyRespond(
		event: FakeServerScenario.Event.Respond,
		writeChannel: ByteWriteChannel,
		requestId: Int,
		responseTo: Int,
	) {
		logFake("Responding $event")
		val message = event.message
		check(message is Message.OpMsg) { "Other kinds of messages are not supported yet" }

		val frame = writeResponseFrame(message, requestId, responseTo)
		writeChannel.writeBuffer(frame)
		writeChannel.flush()

		logFake("Response sent")
	}

	suspend fun createClient(): MongoWireClient {
		return MongoWireClient(
			socket = clientSocket,
			coroutineScope = CoroutineScope(currentTest.foregroundScope.coroutineContext + Job(currentTest.foregroundScope.coroutineContext.job)),
		).also {
			currentTest.cleanUp("Fake client") {
				it.close()
			}
		}
	}

	@FakeServerDsl
	companion object {
		suspend fun TestDsl.fakeServer(
			stub: FakeServerScenario.() -> Unit,
		): FakeServer {
			val scenario = FakeServerScenario().apply(stub)

			return FakeServer(this, scenario)
				.also {
					cleanUp("Fake server") {
						it.close()
					}
				}
		}
	}
}

@FakeServerDsl
class FakeServerScenario {
	sealed class Event {
		data class Expect(val message: Message) : Event() {
			override fun toString() = "Expect $message"
		}

		data class Respond(val message: Message) : Event() {
			override fun toString() = "Respond $message"
		}
	}

	val events = ArrayList<Event>()

	fun expect(message: Message) {
		events += Event.Expect(message)
	}

	fun respond(message: Message) {
		events += Event.Respond(message)
	}
}
