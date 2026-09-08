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
import opensavvy.ktmongo.dsl.command.Command
import opensavvy.ktmongo.dsl.command.errors.MongoException
import opensavvy.ktmongo.dsl.command.errors.MongoWriteException

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
	command: Command,
	collection: MultiplatformMongoCollection<*>,
	server: MongoException.ServerAddress = collection.database.client.serverAddress,
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
		command = command,
		namespace = collection.fullyQualifiedName,
		errors = errors.map { WriteErrorDataImpl(it) },
	)
}
