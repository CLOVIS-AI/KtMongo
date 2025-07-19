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
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/int32.json.
 */
fun SuiteDsl.int32(context: Prepared<BsonContext>) = suite("Int32") {
	testBson(
		context,
		"Min value"
	) {
		document { writeInt32("i", Int.MIN_VALUE) }
		expectedBinaryHex = "0C0000001069000000008000"
		expectedJson = """{"i": -2147483648}"""
		verify("Read value") {
			check(read("i")?.readInt32() == Int.MIN_VALUE)
		}
	}

	testBson(
		context,
		"Max value"
	) {
		document { writeInt32("i", Int.MAX_VALUE) }
		expectedBinaryHex = "0C000000106900FFFFFF7F00"
		expectedJson = """{"i": 2147483647}"""
		verify("Read value") {
			check(read("i")?.readInt32() == Int.MAX_VALUE)
		}
	}

	testBson(
		context,
		"-1"
	) {
		document { writeInt32("i", -1) }
		expectedBinaryHex = "0C000000106900FFFFFFFF00"
		expectedJson = """{"i": -1}"""
		verify("Read value") {
			check(read("i")?.readInt32() == -1)
		}
	}

	testBson(
		context,
		"0"
	) {
		document { writeInt32("i", 0) }
		expectedBinaryHex = "0C0000001069000000000000"
		expectedJson = """{"i": 0}"""
		verify("Read value") {
			check(read("i")?.readInt32() == 0)
		}
	}

	testBson(
		context,
		"+1"
	) {
		document { writeInt32("i", 1) }
		expectedBinaryHex = "0C0000001069000100000000"
		expectedJson = """{"i": 1}"""
		verify("Read value") {
			check(read("i")?.readInt32() == 1)
		}
	}
}
