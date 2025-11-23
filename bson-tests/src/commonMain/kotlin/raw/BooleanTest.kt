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

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.serialize
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/boolean.json.
 */
fun SuiteDsl.boolean(context: Prepared<BsonFactory>) = suite("Boolean") {
	@Serializable
	data class B(val b: Boolean)

	testBson(
		context,
		"True",
		document {
			writeBoolean("b", true)
		},
		serialize(B(true)),
		hex("090000000862000100"),
		json("""{"b": true}"""),
		verify("Read value") {
			check(read("b")?.readBoolean() == true)
		}
	)

	testBson(
		context,
		"False",
		document {
			writeBoolean("b", false)
		},
		serialize(B(false)),
		hex("090000000862000000"),
		json("""{"b": false}"""),
		verify("Read value") {
			check(read("b")?.readBoolean() == false)
		}
	)
}
