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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.multiplatform.wire.fake

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.multiplatform.wire.Message
import opensavvy.ktmongo.multiplatform.wire.OpMsg
import opensavvy.ktmongo.multiplatform.wire.fake.FakeServer.Companion.fakeServer
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.config.Ignored

val FakeServerTest by preparedSuite {

	test("Create a fake server") {
		val server = fakeServer {}
		println(server)
	}

	test("Create a fake client") {
		val client = fakeServer {}
			.createClient()
		println(client)
	}

	test("Round-trip hello") {
		val helloMessage = OpMsg {
			writeInt32("hello", 1)
		}

		val okMessage = OpMsg {
			writeDouble("ok", 1.0)
		}

		val server = fakeServer {
			expect(helloMessage)
			respond(okMessage)
		}

		val client = server.createClient()

		val response = client.sendSingle(helloMessage)

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
	}

	test("Write a message that fails serialization") {
		val server = fakeServer {
			// After sending the broken message, we'll send a valid one to verify the broken message didn't break the client
			expect(OpMsg { writeInt32("hello", 1) })
			respond(OpMsg { writeDouble("ok", 1.0) })
		}
		val client = server.createClient()

		val incorrect = OpMsg {
			error("Serialization fails!")
		}

		val e = checkThrows<IllegalStateException> {
			client.sendSingle(incorrect)
		}
		check(e.message == "Serialization fails!")

		// Check that the error didn't break the client
		val response = client.sendSingle(OpMsg { writeInt32("hello", 1) })
		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
	}

	test("The caller cancels the message while it's being sent", Ignored) { // TODO
		val server = fakeServer {
			// After sending the canceled message, we'll send a valid one to verify the broken message didn't break the client
			expect(OpMsg { writeInt32("hello", 2) })
			respond(OpMsg { writeDouble("ok", 1.0) })
		}
		val client = server.createClient()

		println("Sending a first message, which will be immediately cancelled")
		coroutineScope {
			val a = launch {
				val _ = client.sendSingle(OpMsg { writeInt32("hello", 1) })
				error("This point should never be reached, since the request will be cancelled before the database answers")
			}

			launch {
				a.cancel("The caller canceled the message")
			}
		}

		println("Sending a second message, which should get an answer normally")
		coroutineScope {
			// Check that the error didn't break the client
			val response = client.sendSingle(OpMsg { writeInt32("hello", 2) })
			check(response is Message.OpMsg)
			check(response.body.document["ok"]?.decodeDouble() == 1.0)
		}
	}

	test("The server dies while a message is being sent", Ignored) { // TODO
		val server = fakeServer {
			expect(byteArrayOf(37, 0, 0, 0, 1, 0))
			die()
		}
		val client = server.createClient()

		val e = checkThrows<RuntimeException> {
			client.sendSingle(OpMsg { writeInt32("hello", 1) })
		}
		check(e.message == "Fake server died according to the scenario")
	}

}
