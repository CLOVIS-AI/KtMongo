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
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/string.json.
 */
fun SuiteDsl.string(context: Prepared<BsonContext>) = suite("String") {
	test("Empty string") {
		context().buildDocument {
			writeString("a", "")
		} shouldBeHex "0D000000026100010000000000"
	}

	test("Single character") {
		context().buildDocument {
			writeString("a", "b")
		} shouldBeHex "0E00000002610002000000620000"
	}

	test("Multi character") {
		context().buildDocument {
			writeString("a", "abababababab")
		} shouldBeHex "190000000261000D0000006162616261626162616261620000"
	}

	test("Two-byte UTF8 (\u00e9)") {
		context().buildDocument {
			writeString("a", "\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9")
		} shouldBeHex "190000000261000D000000C3A9C3A9C3A9C3A9C3A9C3A90000"
	}

	test("Three-byte UTF8 (\u2606)") {
		context().buildDocument {
			writeString("a", "\u2606\u2606\u2606\u2606")
		} shouldBeHex "190000000261000D000000E29886E29886E29886E298860000"
	}

	test("Embedded nulls") {
		context().buildDocument {
			writeString("a", "ab\u0000bab\u0000babab")
		} shouldBeHex "190000000261000D0000006162006261620062616261620000"
	}

	test("Required espaces") {
		context().buildDocument {
			writeString("a", "ab\\\"\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\u000c\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001fab")
		} shouldBeHex "320000000261002600000061625C220102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F61620000"
	}
}
