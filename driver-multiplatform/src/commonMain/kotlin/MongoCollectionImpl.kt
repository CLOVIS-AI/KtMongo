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

import kotlinx.coroutines.CancellationException
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.InsertOne
import opensavvy.ktmongo.dsl.command.InsertOneOptions
import opensavvy.ktmongo.multiplatform.wire.Message
import opensavvy.ktmongo.multiplatform.wire.MessageSection
import kotlin.reflect.KType

@OptIn(LowLevelApi::class)
internal class MongoCollectionImpl<Document : Any>(
	override val database: MongoDatabase,
	override val name: String,
	override val type: KType,
) : MongoCollection<Document> {

	override suspend fun insertOne(
		document: Document,
		options: InsertOneOptions<Document>.() -> Unit,
	) {
		val command = lazy {
			database.client.factory.buildDocument {
				writeString("insert", name)
				writeString($$"$db", database.name)

				InsertOne(
					context = database.client.context,
					document = document,
					documentType = type,
				).writeTo(this)
			}
		}

		val responses = database.client.wire.send(
			Message.OpMsg(
				body = MessageSection.Body(
					command,
				)
			)
		)

		val message = responses.receive()
		responses.cancel(CancellationException("insertOne expects a single response"))

		check(message is Message.OpMsg)
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override fun toString(): String =
		"MongoCollection($fullyQualifiedName)"
}
