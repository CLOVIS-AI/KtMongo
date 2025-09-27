/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.shared

val mongoAddress by shared {
	val job = Job()
	currentCoroutineContext().job.invokeOnCompletion { e -> job.cancel("Finished searching for the address. (ended with: $e)") }
	val manager = SelectorManager(Dispatchers.Default + job + CoroutineName("FindMongoAddressManager"))

	val candidateHostNames = listOf("localhost", "mongo")

	println("Searching for the address of the running MongoDB instance…")
	for (hostName in candidateHostNames) {
		val address = InetSocketAddress(hostName, 27017)

		try {
			println("» Trying $address…")
			aSocket(manager).tcp().connect(address.hostname, address.port) {
				socketTimeout = 100
			}.use {
				println("  Connected successfully!")
			}
			println("  Closed the socket.")
			return@shared address
		} catch (e: Exception) {
			println("  Could not connect to MongoDB on socket $address • $e")
		}
	}

	error("Could not find on which port MongoDB is running.")
}

val MongoClient by prepared {
	val socket = mongoAddress()
	MongoClient(socket.hostname, socket.port, backgroundScope.coroutineContext)
}

val SocketTest by preparedSuite {
	test("Create and close a socket") {
		println("Creating socket manager…")
		val manager = SelectorManager(backgroundScope.coroutineContext + Dispatchers.Default + CoroutineName("FindMongoAddressManager"))

		println("Creating address…")
		val address = InetSocketAddress("google.com", 80)

		println("Connecting to $address…")
		aSocket(manager).tcp().connect(address).use {
			println("Connected!")
		}
		println("Disconnected!")
	}
}
