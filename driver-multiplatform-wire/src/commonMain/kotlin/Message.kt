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

import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi

interface Message {

	/**
	 * The message type.
	 *
	 * The possible opcodes are documented [in the wire protocol documentation](https://www.mongodb.com/docs/manual/reference/mongodb-wire-protocol/#opcodes).
	 */
	val opcode: Int

	@LowLevelApi
	val content: BsonDocument
}

data object Find : Message {

	override val opcode: Int
		get() = 2013

	@LowLevelApi
	override val content: BsonDocument
		get() = BsonFactory().buildDocument {
			writeString("find", "test-basic")
			writeDocument("filter") {}
			writeString("\$db", "java-test")
		}
}
