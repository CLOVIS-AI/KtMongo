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
	test("+1.0") {
		context().buildDocument {
			writeDouble("d", 1.0)
		} shouldBeHex "10000000016400000000000000F03F00"
	}

	test("-1.0") {
		context().buildDocument {
			writeDouble("d", -1.0)
		} shouldBeHex "10000000016400000000000000F0BF00"
	}

	test("+1.0001220703125") {
		context().buildDocument {
			writeDouble("d", +1.0001220703125)
		} shouldBeHex "10000000016400000000008000F03F00"
	}

	test("-1.0001220703125") {
		context().buildDocument {
			writeDouble("d", -1.0001220703125)
		} shouldBeHex "10000000016400000000008000F0BF00"
	}

	test("1.2345678921232E+18") {
		context().buildDocument {
			writeDouble("d", 1.2345678921232E+18)
		} shouldBeHex "100000000164002A1BF5F41022B14300"
	}

	test("-1.2345678921232E+18") {
		context().buildDocument {
			writeDouble("d", -1.2345678921232E+18)
		} shouldBeHex "100000000164002A1BF5F41022B1C300"
	}

	test("0.0") {
		context().buildDocument {
			writeDouble("d", 0.0)
		} shouldBeHex "10000000016400000000000000000000"
	}

	test("-0.0") {
		context().buildDocument {
			writeDouble("d", -0.0)
		} shouldBeHex "10000000016400000000000000008000"
	}

	test("NaN") {
		context().buildDocument {
			writeDouble("d", NaN)
		} shouldBeHex "10000000016400000000000000F87F00"
	}

	test("Infinity") {
		context().buildDocument {
			writeDouble("d", Double.POSITIVE_INFINITY)
		} shouldBeHex "10000000016400000000000000F07F00"
	}

	test("-Infinity") {
		context().buildDocument {
			writeDouble("d", Double.NEGATIVE_INFINITY)
		} shouldBeHex "10000000016400000000000000F0FF00"
	}
}
