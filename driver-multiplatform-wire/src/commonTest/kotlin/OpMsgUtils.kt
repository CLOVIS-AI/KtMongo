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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.multiplatform.wire.Message.OpMsg

/**
 * Creates an [OpMsg] message with the given [body] and no [OpMsg.sequences].
 */
@OptIn(LowLevelApi::class)
fun OpMsg(body: BsonFieldWriter.() -> Unit): OpMsg =
	OpMsg(
		MessageSection.Body(
			eager(
				BsonFactory().buildDocument(body)
			)
		),
		sequences = emptySequence()
	)

/**
 * Creates a copy of this [OpMsg], concatenating a new sequence named [id] and composed of the given [documents].
 */
@OptIn(LowLevelApi::class)
fun OpMsg.withSequence(
	id: String,
	vararg documents: BsonFieldWriter.() -> Unit,
): OpMsg = OpMsg(
	body,
	sequences + MessageSection.DocumentSequence(
		id,
		documents.map { eager(BsonFactory().buildDocument(it)) }
	)
)
