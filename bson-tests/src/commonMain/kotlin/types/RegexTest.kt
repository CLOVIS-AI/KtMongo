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

package opensavvy.ktmongo.bson.types

import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test regex representation.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/regex.json.
 */
fun SuiteDsl.verifyRegexes(factory: Prepared<BsonFactory>) = suite("Regex") {
	testBson(
		factory,
		"Empty regex with no options",
		document {
			writeRegularExpression("a", "", "")
		},
		hex("0A0000000B6100000000"),
		json($$"""{"a": {"$regularExpression": {"pattern": "", "options": ""}}}"""),
		verify("Read pattern") {
			check(this["a"]?.decodeRegularExpressionPattern() == "")
		},
		verify("Read options") {
			check(this["a"]?.decodeRegularExpressionOptions() == "")
		}
	)

	testBson(
		factory,
		"Regex with no options",
		document {
			writeRegularExpression("a", "abc", "")
		},
		hex("0D0000000B6100616263000000"),
		json($$"""{"a": {"$regularExpression": {"pattern": "abc", "options": ""}}}"""),
		verify("Read pattern") {
			check(this["a"]?.decodeRegularExpressionPattern() == "abc")
		},
		verify("Read options") {
			check(this["a"]?.decodeRegularExpressionOptions() == "")
		}
	)

	testBson(
		factory,
		"Regex with options",
		document {
			writeRegularExpression("a", "abc", "im")
		},
		hex("0F0000000B610061626300696D0000"),
		json($$"""{"a": {"$regularExpression": {"pattern": "abc", "options": "im"}}}"""),
		verify("Read pattern") {
			check(this["a"]?.decodeRegularExpressionPattern() == "abc")
		},
		verify("Read options") {
			check(this["a"]?.decodeRegularExpressionOptions() == "im")
		}
	)

	testBson(
		factory,
		"Regex with slash",
		document {
			writeRegularExpression("a", "ab/cd", "im")
		},
		hex("110000000B610061622F636400696D0000"),
		json($$"""{"a": {"$regularExpression": {"pattern": "ab/cd", "options": "im"}}}"""),
		verify("Read pattern") {
			check(this["a"]?.decodeRegularExpressionPattern() == "ab/cd")
		},
		verify("Read options") {
			check(this["a"]?.decodeRegularExpressionOptions() == "im")
		}
	)

	testBson(
		factory,
		"Flags not alphabetized",
		document {
			writeRegularExpression("a", "abc", "imx")
		},
		document {
			writeRegularExpression("a", "abc", "mix")
		},
		hex("100000000B610061626300696D780000"),
		json($$"""{"a": {"$regularExpression": {"pattern": "abc", "options": "imx"}}}"""),
		verify("Read pattern") {
			check(this["a"]?.decodeRegularExpressionPattern() == "abc")
		},
		verify("Read options") {
			check(this["a"]?.decodeRegularExpressionOptions() == "imx")
		}
	)

	testBson(
		factory,
		"Required escapes",
		document {
			writeRegularExpression("a", "ab\\\"ab", "")
		},
		hex("100000000B610061625C226162000000"),
		json($$"""{"a": {"$regularExpression": {"pattern": "ab\\\"ab", "options": ""}}}"""),
		verify("Read pattern") {
			check(this["a"]?.decodeRegularExpressionPattern() == "ab\\\"ab")
		},
		verify("Read options") {
			check(this["a"]?.decodeRegularExpressionOptions() == "")
		}
	)
}
