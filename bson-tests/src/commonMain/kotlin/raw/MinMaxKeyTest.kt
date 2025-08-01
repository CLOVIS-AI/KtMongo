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
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test MinKey and MaxKey representations.
 *
 * Adapted from the BSON corpus tests.
 */
fun SuiteDsl.minMaxKey(context: Prepared<BsonContext>) = suite("MinMaxKey") {
	testBson(
		context,
		"Minkey",
		document {
			writeMinKey("a")
		},
		hex("08000000FF610000"),
		json($$"""{"a": {"$minKey": 1}}"""),
		verify("Read value") {
			check(read("a")?.readMinKey() == Unit)
		}
	)

	testBson(
		context,
		"Maxkey",
		document {
			writeMaxKey("a")
		},
		hex("080000007F610000"),
		json($$"""{"a": {"$maxKey": 1}}"""),
		verify("Read value") {
			check(read("a")?.readMaxKey() == Unit)
		}
	)
}
