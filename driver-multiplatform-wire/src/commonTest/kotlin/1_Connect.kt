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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.multiplatform.wire

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite

val ConnectTest by preparedSuite {

	test("Connect to the database") {
		val client = MongoClient()
		println("Connected!")

		println("Disconnecting…")
		client.close()
		println("Disconnected.")
	}

	test("Send a find on a collection that doesn't exist") {
		val client = MongoClient()

		val output = client.send(Message.Find())

		println("Awaiting response…")
		val response = output.receive()

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
		check(response.body.document["cursor"]?.decodeDocument()?.get("ns")?.decodeString() == "test-basic.test-basic")
	}

	test("Insert an element") {
		val client = MongoClient()

		val output = client.send(Message.Insert())

		println("Awaiting response…")
		val response = output.receive()

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
		check(response.body.document["writeErrors"] == null)
	}
}
