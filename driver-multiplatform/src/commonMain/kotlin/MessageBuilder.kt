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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.multiplatform.wire.Message
import opensavvy.ktmongo.multiplatform.wire.MessageSection

// Everything is 'internal' to allow everything to be inlined

@OptIn(LowLevelApi::class)
internal class MessageBuilder(
	internal val factory: BsonFactory,
) {
	internal var document: BsonDocument? = null
	internal val sequences = ArrayList<MessageSection.DocumentSequence>()

	inline fun document(crossinline block: BsonFieldWriter.() -> Unit) {
		check(document == null) { "Cannot set 'document' multiple times" }
		document = factory.buildDocument {
			block()
		}
	}

	inline fun sequence(id: String, block: MessageSequenceBuilder.() -> Unit) {
		sequences += MessageSection.DocumentSequence(
			id = id,
			lazyDocuments = MessageSequenceBuilder(factory).apply(block).documents,
		)
	}
}

@LowLevelApi
internal class MessageSequenceBuilder(
	internal val factory: BsonFactory,
) {
	internal val documents = ArrayList<Lazy<BsonDocument>>()

	inline fun document(crossinline block: BsonFieldWriter.() -> Unit) {
		documents += eager(
			factory.buildDocument {
				block()
			}
		)
	}
}

internal fun MultiplatformMongoClient.createOpMsg(
	block: MessageBuilder.() -> Unit,
): Message.OpMsg {
	val builder = MessageBuilder(factory).apply(block)
	return Message.OpMsg(
		body = MessageSection.Body(eager(builder.document!!)),
		sequences = builder.sequences.asSequence(),
	)
}

/**
 * Instantiates a [Lazy] value that isn't lazy.
 *
 * This allows our API to contain lazy values without forcing us to be lazy everywhere.
 *
 * For example, we often want to be lazy during request sending (so all serialization happens as close as possible to the socket)
 * but not during reception (to extract information as quickly as possible and return the lock).
 */
internal fun <T> eager(value: T): Lazy<T> =
	object : Lazy<T> {
		override val value: T
			get() = value

		override fun isInitialized(): Boolean =
			true

		override fun toString(): String =
			"Lazy($value)"
	}
