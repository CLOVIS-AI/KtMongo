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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.raw

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/boolean.json.
 */
fun SuiteDsl.boolean(context: Prepared<BsonContext>) = suite("Boolean") {
	testBson(
		context,
		name = "True",
		expectedBinaryHex = "090000000862000100",
		expectedJson = """{"b": true}""",
	) {
		writeBoolean("b", true)
	}

	testBson(
		context,
		name = "False",
		expectedBinaryHex = "090000000862000000",
		expectedJson = """{"b": false}""",
	) {
		writeBoolean("b", false)
	}
}
