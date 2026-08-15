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

import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.multiplatform.wire.Message
import opensavvy.ktmongo.multiplatform.wire.MessageSection
import opensavvy.ktmongo.multiplatform.wire.eager
import opensavvy.ktmongo.multiplatform.wire.fake.FakeServer.Companion.fakeServer
import opensavvy.prepared.runner.testballoon.preparedSuite

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
		val helloMessage = Message.OpMsg(
			MessageSection.Body(
				eager(
					BsonFactory().buildDocument {
						writeInt32("hello", 1)
					}
				)
			)
		)

		val okMessage = Message.OpMsg(
			MessageSection.Body(
				eager(
					BsonFactory().buildDocument {
						writeDouble("ok", 1.0)
					}
				)
			)
		)

		val server = fakeServer {
			expect(helloMessage)
			respond(okMessage)
		}

		val client = server.createClient()

		val response = client.sendSingle(helloMessage)

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
	}

}
