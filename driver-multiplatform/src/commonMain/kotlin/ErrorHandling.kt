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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.Command
import opensavvy.ktmongo.dsl.command.errors.*
import opensavvy.ktmongo.dsl.options.MaxTimeOption
import opensavvy.ktmongo.dsl.options.option
import opensavvy.ktmongo.multiplatform.wire.Message
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private class WriteErrorDataImpl(
	private val doc: BsonDocument,
) : MongoWriteException.WriteErrorData {
	override val code: Int
		get() = doc["code"]?.decodeInt32() ?: error("Missing code in write error: $doc")

	override val message: String
		get() = doc["errmsg"]?.decodeString() ?: error("Missing message in write error: $doc")

	override fun toString(): String =
		doc.toString()
}

internal fun checkNoWriteErrors(
	doc: BsonDocument,
	request: DriverMessage,
	command: Command,
	namespace: MultiplatformNamespace,
	server: MongoException.ServerAddress = namespace.client.serverAddress,
) {
	val field = doc["writeErrors"]
		?: return // No errors, nothing to do

	val errors = when (field.type) {
		BsonType.Array -> field.decodeArray().asList()
		BsonType.Document -> listOf(field)
		else -> error("Unexpected type for writeErrors: ${field.type} ($field) in $doc")
	}.map { it.decodeDocument() }

	throw MongoWriteException(
		server = server,
		response = doc,
		request = request.message.bson,
		command = command,
		namespace = namespace.namespace,
		errors = errors.map { WriteErrorDataImpl(it) },
	)
}

@OptIn(LowLevelApi::class)
internal fun checkNoSyntaxErrors(
	doc: BsonDocument,
	request: DriverMessage,
	command: Command,
	namespace: MultiplatformNamespace,
	server: MongoException.ServerAddress = namespace.client.serverAddress,
) {
	if (doc["ok"]?.decodeDouble() == 1.0) {
		return // No errors, nothing to do
	}

	val code = doc["code"]?.decodeInt32() ?: -1

	when (code) {
		50 -> throw MongoTimeoutException(
			timeout = command.options.option<MaxTimeOption>()?.timeout,
			response = doc,
			request = request.message.bson,
			command = command,
			server = server,
			namespace = namespace.namespace,
		)

		else -> throw MongoSyntaxException(
			errorMessage = doc["errmsg"]?.decodeString() ?: "No error message were provided",
			code = doc["code"]?.decodeInt32() ?: -1,
			codeName = doc["codeName"]?.decodeString() ?: "<unknown>",
			response = doc,
			request = request.message.bson,
			command = command,
			server = server,
			namespace = namespace.namespace,
		)
	}
}

@OptIn(ExperimentalContracts::class)
internal fun checkOpMsg(
	message: Message,
	request: DriverMessage,
	command: Command,
	namespace: MultiplatformNamespace,
	server: MongoException.ServerAddress = namespace.client.serverAddress,
) {
	contract { returns() implies (message is Message.OpMsg) }

	if (message !is Message.OpMsg)
		throw MongoDriverException(
			message = "The Multiplatform driver only supports OP_MSG messages (${Message.OpMsg::class}), but it received a ${message::class}\n\tunknown message $message",
			response = null,
			request = request.message.bson,
			command = command,
			server = server,
			namespace = namespace.namespace,
		)
}
