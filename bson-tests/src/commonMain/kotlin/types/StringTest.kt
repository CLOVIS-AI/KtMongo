/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.serialize
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/string.json.
 */
fun SuiteDsl.verifyStrings(factory: Prepared<BsonFactory>) = suite("String") {
	@Serializable
	data class A(val a: String)

	testBson(
		factory,
		"Empty string",
		document {
			writeString("a", "")
		},
		serialize(A("")),
		hex("0D000000026100010000000000"),
		json("""{"a": ""}"""),
		verify("Read value") {
			check(this["a"]?.decodeString() == "")
		}
	)

	testBson(
		factory,
		"Single character",
		document {
			writeString("a", "b")
		},
		serialize(A("b")),
		hex("0E00000002610002000000620000"),
		json("""{"a": "b"}"""),
		verify("Read value") {
			check(this["a"]?.decodeString() == "b")
		}
	)

	testBson(
		factory,
		"Multi character",
		document {
			writeString("a", "abababababab")
		},
		serialize(A("abababababab")),
		hex("190000000261000D0000006162616261626162616261620000"),
		json("""{"a": "abababababab"}"""),
		verify("Read value") {
			check(this["a"]?.decodeString() == "abababababab")
		}
	)

	testBson(
		factory,
		"Two-byte UTF8",
		document {
			writeString("a", "\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9")
		},
		serialize(A("\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9")),
		hex("190000000261000D000000C3A9C3A9C3A9C3A9C3A9C3A90000"),
		json("""{"a": "éééééé"}"""),
		verify("Read value") {
			check(this["a"]?.decodeString() == "éééééé")
		}
	)

	testBson(
		factory,
		"Three-byte UTF8",
		document {
			writeString("a", "\u2606\u2606\u2606\u2606")
		},
		serialize(A("\u2606\u2606\u2606\u2606")),
		hex("190000000261000D000000E29886E29886E29886E298860000"),
		json("""{"a": "☆☆☆☆"}"""),
		verify("Read value") {
			check(this["a"]?.decodeString() == "☆☆☆☆")
		}
	)

	testBson(
		factory,
		"Embedded nulls",
		document {
			writeString("a", "ab\u0000bab\u0000babab")
		},
		serialize(A("ab\u0000bab\u0000babab")),
		hex("190000000261000D0000006162006261620062616261620000"),
		verify("Read value") {
			check(this["a"]?.decodeString() == "ab\u0000bab\u0000babab")
		}
	)

	val specialCharacters = "ab\\\"\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\u000c\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001fab"

	val escapedJson = buildString {
		for (c in specialCharacters) {
			when (c) {
				'"' -> append("\\\"")
				'\\' -> append("\\\\")
				'\b' -> append("\\b")
				'\t' -> append("\\t")
				'\n' -> append("\\n")
				'' -> append("\\f")
				'\r' -> append("\\r")
				else if (c.code < 0x20) -> append("\\u" + c.code.toString(16).padStart(4, '0'))
				else -> append(c)
			}
		}
	}

	testBson(
		factory,
		"Required escapes",
		document {
			writeString("a", specialCharacters)
		},
		serialize(A(specialCharacters)),
		hex("320000000261002600000061625C220102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F61620000"),
		json("{\"a\": \"$escapedJson\"}"),
		verify("Read value") {
			check(this["a"]?.decodeString() == specialCharacters)
		}
	)
}
