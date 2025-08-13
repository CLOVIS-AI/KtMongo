/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

@file:OptIn(LowLevelApi::class, DangerousMongoApi::class)

package opensavvy.ktmongo.bson.multiplatform.serialization

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.multiplatform.context
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.SuiteDsl

private fun SuiteDsl.testDocument(
	name: String,
	block: BsonFieldWriter.() -> Unit,
) = test(name) {
	val a = context().buildDocument(block)

	val b = context().openDocument().apply(block).build()

	check(a.toString() == b.toString())
}

private fun SuiteDsl.testArray(
	name: String,
	block: BsonValueWriter.() -> Unit,
) = test(name) {
	val a = context().buildArray(block)

	val b = context().openArray().apply(block).build()

	check(a.toString() == b.toString())
}

val CompletableTest by preparedSuite {

	testDocument("Simple document example") {
		writeString("a", "b")
		writeInt64("b", 12)
	}

	testArray("Simple array example") {
		writeString("b")
		writeInt64(12)
	}
}
