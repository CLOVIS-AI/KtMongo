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

package opensavvy.ktmongo.official

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.Command
import opensavvy.ktmongo.dsl.command.errors.MongoException
import opensavvy.ktmongo.dsl.command.errors.MongoWriteException
import com.mongodb.MongoWriteException as OfficialMongoWriteException
import com.mongodb.ServerAddress as OfficialServerAddress

private data class ServerAddressImpl(
	override val host: String,
	override val port: Int,
) : MongoException.ServerAddress {
	override fun toString(): String = "$host:$port"
}

private class WriteErrorDataImpl(
	override val code: Int,
	override val message: String,
) : MongoWriteException.WriteErrorData {
	override fun toString(): String = "WriteError(code=$code, message='$message')"
}

@LowLevelApi
internal fun OfficialServerAddress.toKtMongo(): MongoException.ServerAddress =
	ServerAddressImpl(host, port)

@LowLevelApi
fun OfficialMongoWriteException.toKtMongo(
	command: Command,
	namespace: String,
): MongoWriteException {
	val writeError = error
	val errorData = WriteErrorDataImpl(
		code = writeError.code,
		message = writeError.message,
	)

	return MongoWriteException(
		server = serverAddress.toKtMongo(),
		command = command,
		errors = listOf(errorData),
		namespace = namespace,
		cause = this,
	)
}
