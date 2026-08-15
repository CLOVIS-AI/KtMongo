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

package opensavvy.ktmongo.multiplatform.wire

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.isActive

/**
 * Custom abstraction to allow creating fake sockets for testing.
 */
internal interface MongoSocket : AutoCloseable {

	val isActive: Boolean

	fun openReadChannel(): ByteReadChannel

	fun openWriteChannel(): ByteWriteChannel
}

private class KtorMongoSocket(
	private val socket: Socket,
	private val selectorManager: SelectorManager,
) : MongoSocket {

	override val isActive: Boolean
		get() = socket.isActive

	override fun openReadChannel(): ByteReadChannel =
		socket.openReadChannel()

	override fun openWriteChannel(): ByteWriteChannel =
		socket.openWriteChannel()

	override fun close() {
		socket.close()
		selectorManager.close()
	}

	override fun toString(): String =
		"Ktor ${socket.remoteAddress}"
}

internal fun MongoSocket(socket: Socket, selectorManager: SelectorManager): MongoSocket =
	KtorMongoSocket(socket, selectorManager)
