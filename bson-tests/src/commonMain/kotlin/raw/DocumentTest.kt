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
import io.kotest.matchers.shouldNotBe
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/document.json.
 */
fun SuiteDsl.document(context: Prepared<BsonContext>) = suite("Document") {
	testBson(
		context,
		"Empty subdocument"
	) {
		document { writeDocument("x") {} }
		expectedBinaryHex = "0D000000037800050000000000"
		expectedJson = """{"x": {}}"""
		verify("Read value") { read("x")?.readDocument() shouldNotBe null }
	}

	testBson(
		context,
		"Document with an empty string key"
	) {
		document { writeDocument("x") { writeString("", "b") } }
		expectedBinaryHex = "150000000378000D00000002000200000062000000"
		expectedJson = """{"x": {"": "b"}}"""
		verify("Read value") { read("x")?.readDocument()?.read("")?.readString() shouldBe "b" }
	}

	testBson(
		context,
		"Document with a single-character key"
	) {
		document { writeDocument("x") { writeString("a", "b") } }
		expectedBinaryHex = "160000000378000E0000000261000200000062000000"
		expectedJson = """{"x": {"a": "b"}}"""
		verify("Read value") { read("x")?.readDocument()?.read("a")?.readString() shouldBe "b" }
	}

	testBson(
		context,
		"Document with a dollar-prefixed key"
	) {
		document { writeDocument("x") { writeString("\$a", "b") } }
		expectedBinaryHex = "170000000378000F000000022461000200000062000000"
		expectedJson = $$"""{"x": {"$a": "b"}}"""
		verify("Read value") { read("x")?.readDocument()?.read("\$a")?.readString() shouldBe "b" }
	}

	testBson(
		context,
		"Document with a dollar key"
	) {
		document { writeDocument("x") { writeString("$", "a") } }
		expectedBinaryHex = "160000000378000E0000000224000200000061000000"
		expectedJson = """{"x": {"$": "a"}}"""
		verify("Read value") { read("x")?.readDocument()?.read("$")?.readString() shouldBe "a" }
	}

	testBson(
		context,
		"Document with a dotted key"
	) {
		document { writeDocument("x") { writeString("a.b", "c") } }
		expectedBinaryHex = "180000000378001000000002612E62000200000063000000"
		expectedJson = """{"x": {"a.b": "c"}}"""
		verify("Read value") { read("x")?.readDocument()?.read("a.b")?.readString() shouldBe "c" }
	}

	testBson(
		context,
		"Document with a dot key"
	) {
		document { writeDocument("x") { writeString(".", "a") } }
		expectedBinaryHex = "160000000378000E000000022E000200000061000000"
		expectedJson = """{"x": {".": "a"}}"""
		verify("Read value") { read("x")?.readDocument()?.read(".")?.readString() shouldBe "a" }
	}
}
