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
import kotlin.Double.Companion.NaN

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/double.json.
 */
fun SuiteDsl.double(context: Prepared<BsonContext>) = suite("Double") {
	testBson(
		context,
		"+1.0"
	) {
		document { writeDouble("d", 1.0) }
		expectedBinaryHex = "10000000016400000000000000F03F00"
		expectedJson = """{"d": 1.0}"""
		verify("Read value") {
			check(read("d")?.readDouble() == 1.0)
		}
	}

	testBson(
		context,
		"-1.0"
	) {
		document { writeDouble("d", -1.0) }
		expectedBinaryHex = "10000000016400000000000000F0BF00"
		expectedJson = """{"d": -1.0}"""
		verify("Read value") {
			check(read("d")?.readDouble() == -1.0)
		}
	}

	testBson(
		context,
		"+1.0001220703125"
	) {
		document { writeDouble("d", +1.0001220703125) }
		expectedBinaryHex = "10000000016400000000008000F03F00"
		expectedJson = """{"d": 1.0001220703125}"""
		verify("Read value") {
			check(read("d")?.readDouble() == 1.0001220703125)
		}
	}

	testBson(
		context,
		"-1.0001220703125"
	) {
		document { writeDouble("d", -1.0001220703125) }
		expectedBinaryHex = "10000000016400000000008000F0BF00"
		expectedJson = """{"d": -1.0001220703125}"""
		verify("Read value") {
			check(read("d")?.readDouble() == -1.0001220703125)
		}
	}

	testBson(
		context,
		"+1.2345678921232E+18"
	) {
		document { writeDouble("d", 1.2345678921232E+18) }
		expectedBinaryHex = "100000000164002A1BF5F41022B14300"
		expectedJson = """{"d": 1.2345678921232E18}"""
		verify("Read value") {
			check(read("d")?.readDouble() == 1.2345678921232E+18)
		}
	}

	testBson(
		context,
		"-1.2345678921232E+18"
	) {
		document { writeDouble("d", -1.2345678921232E+18) }
		expectedBinaryHex = "100000000164002A1BF5F41022B1C300"
		expectedJson = """{"d": -1.2345678921232E18}"""
		verify("Read value") {
			check(read("d")?.readDouble() == -1.2345678921232E+18)
		}
	}

	testBson(
		context,
		"+0.0"
	) {
		document { writeDouble("d", 0.0) }
		expectedBinaryHex = "10000000016400000000000000000000"
		expectedJson = """{"d": 0.0}"""
		verify("Read value") {
			check(read("d")?.readDouble() == 0.0)
		}
	}

	testBson(
		context,
		"-0.0"
	) {
		document { writeDouble("d", -0.0) }
		expectedBinaryHex = "10000000016400000000000000008000"
		expectedJson = """{"d": -0.0}"""
		verify("Read value") {
			check(read("d")?.readDouble() == -0.0)
		}
	}

	testBson(
		context,
		"NaN"
	) {
		document { writeDouble("d", NaN) }
		expectedBinaryHex = "10000000016400000000000000F87F00"
		expectedJson = $$"""{"d": {"$numberDouble": "NaN"}}"""
		verify("Read value") {
			check(read("d")?.readDouble()?.isNaN() == true)
		}
	}

	testBson(
		context,
		"+Infinity"
	) {
		document { writeDouble("d", Double.POSITIVE_INFINITY) }
		expectedBinaryHex = "10000000016400000000000000F07F00"
		expectedJson = $$"""{"d": {"$numberDouble": "Infinity"}}"""
		verify("Read value") {
			check(read("d")?.readDouble() == Double.POSITIVE_INFINITY)
		}
	}

	testBson(
		context,
		"-Infinity"
	) {
		document { writeDouble("d", Double.NEGATIVE_INFINITY) }
		expectedBinaryHex = "10000000016400000000000000F0FF00"
		expectedJson = $$"""{"d": {"$numberDouble": "-Infinity"}}"""
		verify("Read value") {
			check(read("d")?.readDouble() == Double.NEGATIVE_INFINITY)
		}
	}
}
