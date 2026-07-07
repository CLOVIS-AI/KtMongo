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

@file:OptIn(LowLevelApi::class, ExperimentalBsonPathApi::class, ExperimentalAtomicApi::class)

package opensavvy.ktmongo.multiplatform.wire

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.ExperimentalBsonPathApi
import opensavvy.ktmongo.bson.encode
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.selectFirst
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator.Default
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.multiplatform.wire.Message.OpMsg
import opensavvy.ktmongo.multiplatform.wire.MessageSection.Body
import opensavvy.ktmongo.multiplatform.wire.MessageSection.DocumentSequence
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Serializable
private data class DataTest(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

val ConnectTest by preparedSuite {

	test("Connect to the database") {
		val client = MongoWireClient()
		println("Connected!")

		println("Disconnecting…")
		client.close()
		println("Disconnected.")
	}

	test("Send a find on a collection that doesn't exist") {
		val client = MongoWireClient()

		val output = client.send(
			OpMsg(
				Body(
					eager(
						BsonFactory().buildDocument {
							writeString("find", "test-basic-1")
							writeDocument("filter") {}
							writeString("\$db", "test-basic")
						}
					)
				)
			)
		)

		println("Awaiting response…")
		val response = output.receive()

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
		check(response.body.document["cursor"]?.decodeDocument()?.get("ns")?.decodeString() == "test-basic.test-basic-1")
	}

	test("Insert an element") {
		val client = MongoWireClient()

		val output = client.send(OpMsg(
			Body(
				eager(
					BsonFactory().buildDocument {
						writeString("insert", "test-basic-2")
						writeBoolean("ordered", true)
						writeString("\$db", "test-basic")
					}
				)
			),
			DocumentSequence(
				id = "documents",
				listOf(
					eager(BsonFactory().encode(DataTest(Default().newId(), "Bob", 18)) as BsonDocument)
				)
			)
		))

		println("Awaiting response…")
		val response = output.receive()

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
		check(response.body.document["writeErrors"] == null)
	}

	test("Drop a collection") {
		val client = MongoWireClient()

		val output = client.send(OpMsg(
			Body(
				eager(
					BsonFactory().buildDocument {
						writeString("drop", "test-basic-2")
						writeString("\$db", "test-basic")
					}
				)
			)
		))

		println("Awaiting response…")
		val response = output.receive()

		check(response is Message.OpMsg)
		check(response.body.document["ok"]?.decodeDouble() == 1.0)
	}

	test("Find an element that was just inserted") {
		val client = MongoWireClient()

		val insertOutput = client.send(OpMsg(
			Body(
				eager(
					BsonFactory().buildDocument {
						writeString("insert", "test-basic-3")
						writeBoolean("ordered", true)
						writeString("\$db", "test-basic")
					}
				)
			),
			DocumentSequence(
				id = "documents",
				listOf(
					eager(BsonFactory().encode(DataTest(Default().newId(), "Bob", 18)) as BsonDocument)
				)
			)
		))
		val findOutput = client.send(OpMsg(
			Body(
				eager(
					BsonFactory().buildDocument {
						writeString("find", "test-basic-3")
						writeDocument("filter") {}
						writeString("\$db", "test-basic")
					}
				)
			)
		))

		println("Awaiting response…")
		val insertResponse = insertOutput.receive()
		val findResponse = findOutput.receive()

		check(insertResponse is Message.OpMsg)
		check(insertResponse.body.document["ok"]?.decodeDouble() == 1.0)

		check(findResponse is Message.OpMsg)
		check(findResponse.body.document["ok"]?.decodeDouble() == 1.0)
		check(findResponse.body.document.selectFirst<String>("$.cursor.firstBatch[0].name") == "Bob")

		val _ = client.send(OpMsg(
			Body(
				eager(
					BsonFactory().buildDocument {
						writeString("drop", "test-basic-3")
						writeString("\$db", "test-basic")
					}
				)
			)
		))
	}
}
