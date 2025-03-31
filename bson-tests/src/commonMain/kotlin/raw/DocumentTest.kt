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
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/document.json.
 */
fun SuiteDsl.document(context: Prepared<BsonContext>) = suite("Document") {
	test("Empty subdocument") {
		context().buildDocument {
			writeDocument("x") {}
		} shouldBeHex "0D000000037800050000000000"
	}

	test("Document with an empty string key") {
		context().buildDocument {
			writeDocument("x") {
				writeString("", "b")
			}
		} shouldBeHex "150000000378000D00000002000200000062000000"
	}

	test("Document with a single-character key") {
		context().buildDocument {
			writeDocument("x") {
				writeString("a", "b")
			}
		} shouldBeHex "160000000378000E0000000261000200000062000000"
	}

	test("Document with a dollar-prefixed key") {
		context().buildDocument {
			writeDocument("x") {
				writeString("\$a", "b")
			}
		} shouldBeHex "170000000378000F000000022461000200000062000000"
	}

	test("Document with a dollar key") {
		context().buildDocument {
			writeDocument("x") {
				writeString("$", "a")
			}
		} shouldBeHex "160000000378000E0000000224000200000061000000"
	}

	test("Document with a dotted key") {
		context().buildDocument {
			writeDocument("x") {
				writeString("a.b", "c")
			}
		} shouldBeHex "180000000378001000000002612E62000200000063000000"
	}

	test("Document with a dot key") {
		context().buildDocument {
			writeDocument("x") {
				writeString(".", "a")
			}
		} shouldBeHex "160000000378000E000000022E000200000061000000"
	}
}
