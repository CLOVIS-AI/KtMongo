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

import io.kotest.matchers.shouldBe
import opensavvy.ktmongo.bson.BsonContext
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
		"Min value"
	) {
		document { writeInt64("a", Long.MIN_VALUE) }
		expectedBinaryHex = "10000000126100000000000000008000"
		expectedJson = """{"a": -9223372036854775808}"""
		verify("Read value") { read("a")?.readInt64() shouldBe Long.MIN_VALUE }
	}

	testBson(
		context,
		"Max value"
	) {
		document { writeInt64("a", Long.MAX_VALUE) }
		expectedBinaryHex = "10000000126100FFFFFFFFFFFFFF7F00"
		expectedJson = """{"a": 9223372036854775807}"""
		verify("Read value") { read("a")?.readInt64() shouldBe Long.MAX_VALUE }
	}

	testBson(
		context,
		"-1"
	) {
		document { writeInt64("a", -1) }
		expectedBinaryHex = "10000000126100FFFFFFFFFFFFFFFF00"
		expectedJson = """{"a": -1}"""
		verify("Read value") { read("a")?.readInt64() shouldBe -1 }
	}

	testBson(
		context,
		"0"
	) {
		document { writeInt64("a", 0) }
		expectedBinaryHex = "10000000126100000000000000000000"
		expectedJson = """{"a": 0}"""
		verify("Read value") { read("a")?.readInt64() shouldBe 0 }
	}

	testBson(
		context,
		"+1"
	) {
		document { writeInt64("a", 1) }
		expectedBinaryHex = "10000000126100010000000000000000"
		expectedJson = """{"a": 1}"""
		verify("Read value") { read("a")?.readInt64() shouldBe 1 }
	}
}
