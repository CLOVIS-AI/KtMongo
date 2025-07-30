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
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/int64.json.
 */
fun SuiteDsl.int64(context: Prepared<BsonContext>) = suite("Int64") {
	testBson(
		context,
		"Min value",
		document { writeInt64("a", Long.MIN_VALUE) },
		hex("10000000126100000000000000008000"),
		json("""{"a": -9223372036854775808}"""),
		verify("Read value") {
			check(read("a")?.readInt64() == Long.MIN_VALUE)
		}
	)

	testBson(
		context,
		"Max value",
		document { writeInt64("a", Long.MAX_VALUE) },
		hex("10000000126100FFFFFFFFFFFFFF7F00"),
		json("""{"a": 9223372036854775807}"""),
		verify("Read value") {
			check(read("a")?.readInt64() == Long.MAX_VALUE)
		}
	)

	testBson(
		context,
		"-1",
		document { writeInt64("a", -1) },
		hex("10000000126100FFFFFFFFFFFFFFFF00"),
		json("""{"a": -1}"""),
		verify("Read value") {
			check(read("a")?.readInt64() == -1L)
		}
	)

	testBson(
		context,
		"0",
		document { writeInt64("a", 0) },
		hex("10000000126100000000000000000000"),
		json("""{"a": 0}"""),
		verify("Read value") {
			check(read("a")?.readInt64() == 0L)
		}
	)

	testBson(
		context,
		"+1",
		document { writeInt64("a", 1) },
		hex("10000000126100010000000000000000"),
		json("""{"a": 1}"""),
		verify("Read value") {
			check(read("a")?.readInt64() == 1L)
		}
	)
}
