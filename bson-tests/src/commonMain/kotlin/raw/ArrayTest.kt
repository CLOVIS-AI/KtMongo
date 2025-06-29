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
 * Test array representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/array.json.
 */
fun SuiteDsl.array(context: Prepared<BsonContext>) = suite("Array") {
	testBson(
		context,
		"Empty",
		document {
			writeArray("a") {}
		},
		hex("0D000000046100050000000000"),
		json("""{"a": []}"""),
		verify("Read value") {
			check(read("a")?.readArray() != null)
		}
	)

	testBson(
		context,
		"Single-element array",
		document {
			writeArray("a") {
				writeInt32(10)
			}
		},
		hex("140000000461000C0000001030000A0000000000"),
		json("""{"a": [10]}"""),
		verify("Read value") {
			check(read("a")?.readArray()?.read(0)?.readInt32() == 10)
		}
	)

	testBson(
		context,
		"Single Element Array with index set incorrectly to empty string",
		hex("130000000461000B00000010000A0000000000"),
		json("""{"a": [10]}"""),
		verify("Read value") {
			check(read("a")?.readArray()?.read(0)?.readInt32() == 10)
		}
	)

	testBson(
		context,
		"Single Element Array with index set incorrectly to ab",
		hex("150000000461000D000000106162000A0000000000"),
		json("""{"a": [10]}"""),
		verify("Read value") {
			check(read("a")?.readArray()?.read(0)?.readInt32() == 10)
		}
	)

	testBson(
		context,
		"Multi Element Array with duplicate indexes",
		hex("1b000000046100130000001030000a000000103000140000000000"),
		json("""{"a": [10, 20]}"""),
		verify("Read first value") {
			check(read("a")?.readArray()?.read(0)?.readInt32() == 10)
		},
		verify("Read second value") {
			check(read("a")?.readArray()?.read(1)?.readInt32() == 20)
		}
	)
}
