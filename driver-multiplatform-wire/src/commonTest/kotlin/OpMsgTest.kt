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

@file:OptIn(LowLevelApi::class, ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.multiplatform.wire

import opensavvy.ktmongo.bson.ExperimentalBsonPathApi
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.select
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite

val OpMsgTest by preparedSuite {

	test("Access field of simple message") {
		val msg = OpMsg(BsonFactory()) {
			writeInt64("_id", 123456789)
			writeString("name", "Bob")
		}

		check(msg["_id"]?.decodeInt64() == 123456789L)
		check(msg["name"]?.decodeString() == "Bob")
	}

	test("Access field of sequence") {
		val msg = OpMsg(BsonFactory()) {
			writeInt64("_id", 123456789)
		}.withSequence(
			"users",
			{ writeString("name", "Alice") },
			{ writeString("name", "Bob") },
		)

		check(msg["users"]?.decodeArray()?.asIterable()?.map { it.decodeDocument()["name"]?.decodeString() } == listOf("Alice", "Bob"))

		check(msg.bson["_id"]?.decodeInt64() == 123456789L)
		check(msg.bson.select<String>("$.users.*.name").toList() == listOf("Alice", "Bob"))
	}

	// There is no support for nested sequences
	// https://github.com/mongodb/specifications/blob/master/source/message/OP_MSG.md

}
