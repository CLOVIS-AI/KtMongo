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

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.serialize
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/document.json.
 */
fun SuiteDsl.document(context: Prepared<BsonFactory>) = suite("Document") {
	@Serializable
	data class A(val a: String)

	@Serializable
	data class X(val x: A)

	testBson(
		context,
		"Empty subdocument",
		document { writeDocument("x") {} },
		hex("0D000000037800050000000000"),
		json("""{"x": {}}"""),
		verify("Read value") {
			check(read("x")?.readDocument() != null)
		}
	)

	testBson(
		context,
		"Document with an empty string key",
		document { writeDocument("x") { writeString("", "b") } },
		hex("150000000378000D00000002000200000062000000"),
		json("""{"x": {"": "b"}}"""),
		verify("Read value") {
			check(read("x")?.readDocument()?.read("")?.readString() == "b")
		}
	)

	testBson(
		context,
		"Document with a single-character key",
		document { writeDocument("x") { writeString("a", "b") } },
		hex("160000000378000E0000000261000200000062000000"),
		serialize(X(A("b"))),
		json("""{"x": {"a": "b"}}"""),
		verify("Read value") {
			check(read("x")?.readDocument()?.read("a")?.readString() == "b")
		}
	)

	testBson(
		context,
		"Document with a dollar-prefixed key",
		document { writeDocument("x") { writeString("\$a", "b") } },
		hex("170000000378000F000000022461000200000062000000"),
		json($$"""{"x": {"$a": "b"}}"""),
		verify("Read value") {
			check(read("x")?.readDocument()?.read("\$a")?.readString() == "b")
		}
	)

	testBson(
		context,
		"Document with a dollar key",
		document { writeDocument("x") { writeString("$", "a") } },
		hex("160000000378000E0000000224000200000061000000"),
		json("""{"x": {"$": "a"}}"""),
		verify("Read value") {
			check(read("x")?.readDocument()?.read("$")?.readString() == "a")
		}
	)

	testBson(
		context,
		"Document with a dotted key",
		document { writeDocument("x") { writeString("a.b", "c") } },
		hex("180000000378001000000002612E62000200000063000000"),
		json("""{"x": {"a.b": "c"}}"""),
		verify("Read value") {
			check(read("x")?.readDocument()?.read("a.b")?.readString() == "c")
		}
	)

	testBson(
		context,
		"Document with a dot key",
		document { writeDocument("x") { writeString(".", "a") } },
		hex("160000000378000E000000022E000200000061000000"),
		json("""{"x": {".": "a"}}"""),
		verify("Read value") {
			check(read("x")?.readDocument()?.read(".")?.readString() == "a")
		}
	)
}
