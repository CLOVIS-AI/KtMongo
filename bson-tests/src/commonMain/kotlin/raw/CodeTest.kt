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

import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
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
fun SuiteDsl.code(context: Prepared<BsonFactory>) = suite("Code") {
	testBson(
		context,
		"Empty string",
		document {
			writeJavaScript("a", "")
		},
		hex("0D0000000D6100010000000000"),
		json($$"""{"a": {"$code": ""}}"""),
		verify("Read value") {
			check(read("a")?.readJavaScript() == "")
		}
	)

	testBson(
		context,
		"Single character",
		document { writeJavaScript("a", "b") },
		hex("0E0000000D610002000000620000"),
		json($$"""{"a": {"$code": "b"}}"""),
		verify("Read value") {
			check(read("a")?.readJavaScript() == "b")
		}
	)

	testBson(
		context,
		"Multi-character",
		document { writeJavaScript("a", "abababababab") },
		hex("190000000D61000D0000006162616261626162616261620000"),
		json($$"""{"a": {"$code": "abababababab"}}"""),
		verify("Read value") {
			check(read("a")?.readJavaScript() == "abababababab")
		}
	)

	testBson(
		context,
		"two-byte UTF-8",
		document { writeJavaScript("a", "\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9") },
		hex("190000000D61000D000000C3A9C3A9C3A9C3A9C3A9C3A90000"),
		json($$"""{"a": {"$code": "éééééé"}}"""),
		verify("Read value") {
			check(read("a")?.readJavaScript() == "\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9")
		}
	)

	testBson(
		context,
		"three-byte UTF-8",
		document { writeJavaScript("a", "\u2606\u2606\u2606\u2606") },
		hex("190000000D61000D000000E29886E29886E29886E298860000"),
		json($$"""{"a": {"$code": "☆☆☆☆"}}"""),
		verify("Read value") {
			check(read("a")?.readJavaScript() == "\u2606\u2606\u2606\u2606")
		}
	)

	testBson(
		context,
		"Embedded nulls",
		document { writeJavaScript("a", "ab\u0000bab\u0000babab") },
		hex("190000000D61000D0000006162006261620062616261620000"),
		verify("Read value") {
			check(read("a")?.readJavaScript() == "ab\u0000bab\u0000babab")
		}
	)
}
