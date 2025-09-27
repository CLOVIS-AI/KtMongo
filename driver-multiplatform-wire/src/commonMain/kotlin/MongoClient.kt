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
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.coroutines.CoroutineContext

@LowLevelApi
interface MongoClient : AutoCloseable {

	companion object
}

@LowLevelApi
private class MultiplatformMongoClient(
	private val socket: Socket,
) : MongoClient {

	private val readChannel = socket.openReadChannel()
	private val writeChannel = socket.openWriteChannel()

	override fun close() {
		socket.close()
	}

	override fun toString() = "MongoClient(${socket.remoteAddress})"
}

@LowLevelApi
suspend fun MongoClient(
	hostName: String,
	port: Int,
	dispatcher: CoroutineContext,
): MongoClient {
	val selectorManager = SelectorManager(dispatcher)
	val socket = aSocket(selectorManager).tcp().connect(hostName, port)

	return MultiplatformMongoClient(socket)
}
