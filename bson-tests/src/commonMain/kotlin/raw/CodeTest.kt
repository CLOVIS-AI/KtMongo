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
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Test code representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/code.json.
 */
@OptIn(ExperimentalEncodingApi::class)
fun SuiteDsl.code(context: Prepared<BsonContext>) = suite("Code") {
	test("Empty string") {
		context().buildDocument {
			writeJavaScript("a", "")
		} shouldBeHex "0D0000000D6100010000000000"
	}

	test("Single character") {
		context().buildDocument {
			writeJavaScript("a", "b")
		} shouldBeHex "0E0000000D610002000000620000"
	}

	test("Multi-character") {
		context().buildDocument {
			writeJavaScript("a", "abababababab")
		} shouldBeHex "190000000D61000D0000006162616261626162616261620000"
	}

	test("two-byte UTF-8 (\u00e9)") {
		context().buildDocument {
			writeJavaScript("a", "\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9")
		} shouldBeHex "190000000D61000D000000C3A9C3A9C3A9C3A9C3A9C3A90000"
	}

	test("three-byte UTF-8 (\u2606)") {
		context().buildDocument {
			writeJavaScript("a", "\u2606\u2606\u2606\u2606")
		} shouldBeHex "190000000D61000D000000E29886E29886E29886E298860000"
	}

	test("Embedded nulls") {
		context().buildDocument {
			writeJavaScript("a", "ab\u0000bab\u0000babab")
		} shouldBeHex "190000000D61000D0000006162006261620062616261620000"
	}

}
