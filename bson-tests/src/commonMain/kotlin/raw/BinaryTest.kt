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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Test binary representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/binary.json.
 */
@OptIn(ExperimentalEncodingApi::class)
fun SuiteDsl.binary(context: Prepared<BsonContext>) = suite("Binary") {
	test("subtype 0x00 (Zero-length)") {
		context().buildDocument {
			writeBinaryData("x", 0x0u, Base64.decode(""))
		} shouldBeHex "0D000000057800000000000000"
	}

	test("subtype 0x00") {
		context().buildDocument {
			writeBinaryData("x", 0x0u, Base64.decode("//8="))
		} shouldBeHex "0F0000000578000200000000FFFF00"
	}

	test("subtype 0x01") {
		context().buildDocument {
			writeBinaryData("x", 0x1u, Base64.decode("//8="))
		} shouldBeHex "0F0000000578000200000001FFFF00"
	}

	test("subtype 0x02") {
		context().buildDocument {
			writeBinaryData("x", 0x2u, Base64.decode("//8="))
		} shouldBeHex "13000000057800060000000202000000FFFF00"
	}

	test("subtype 0x03") {
		context().buildDocument {
			writeBinaryData("x", 0x3u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		} shouldBeHex "1D000000057800100000000373FFD26444B34C6990E8E7D1DFC035D400"
	}

	test("subtype 0x04") {
		context().buildDocument {
			writeBinaryData("x", 0x4u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		} shouldBeHex "1D000000057800100000000473FFD26444B34C6990E8E7D1DFC035D400"
	}

	test("subtype 0x05") {
		context().buildDocument {
			writeBinaryData("x", 0x5u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		} shouldBeHex "1D000000057800100000000573FFD26444B34C6990E8E7D1DFC035D400"
	}

	test("subtype 0x07") {
		context().buildDocument {
			writeBinaryData("x", 0x7u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		} shouldBeHex "1D000000057800100000000773FFD26444B34C6990E8E7D1DFC035D400"
	}

	test("subtype 0x08") {
		context().buildDocument {
			writeBinaryData("x", 0x8u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		} shouldBeHex "1D000000057800100000000873FFD26444B34C6990E8E7D1DFC035D400"
	}

	test("subtype 0x80") {
		context().buildDocument {
			writeBinaryData("x", 0x80u, Base64.decode("//8="))
		} shouldBeHex "0F0000000578000200000080FFFF00"
	}

	test("\$type query operator (conflicts with legacy \$binary form with \$type field)") {
		context().buildDocument {
			writeDocument("x") {
				writeString("\$type", "string")
			}
		} shouldBeHex "1F000000037800170000000224747970650007000000737472696E67000000"
	}

	test("\$type query operator (conflicts with legacy \$binary form with \$type field)") {
		context().buildDocument {
			writeDocument("x") {
				writeInt32("\$type", 2)
			}
		} shouldBeHex "180000000378001000000010247479706500020000000000"
	}

	test("subtype 0x09 Vector FLOAT32") {
		context().buildDocument {
			writeBinaryData("x", 0x09u, Base64.decode("JwAAAP5CAADgQA=="))
		} shouldBeHex "170000000578000A0000000927000000FE420000E04000"
	}

	test("subtype 0x09 Vector INT8") {
		context().buildDocument {
			writeBinaryData("x", 0x09u, Base64.decode("AwB/Bw=="))
		} shouldBeHex "11000000057800040000000903007F0700"
	}

	test("subtype 0x09 Vector PACKED_BIT") {
		context().buildDocument {
			writeBinaryData("x", 0x09u, Base64.decode("EAB/Bw=="))
		} shouldBeHex "11000000057800040000000910007F0700"
	}

	test("subtype 0x09 Vector (Zero-length) FLOAT32") {
		context().buildDocument {
			writeBinaryData("x", 0x09u, Base64.decode("JwA="))
		} shouldBeHex "0F0000000578000200000009270000"
	}

	test("subtype 0x09 Vector (Zero-length) INT8") {
		context().buildDocument {
			writeBinaryData("x", 0x09u, Base64.decode("AwA="))
		} shouldBeHex "0F0000000578000200000009030000"
	}

	test("subtype 0x09 Vector (Zero-length) PACKED_BIT") {
		context().buildDocument {
			writeBinaryData("x", 0x09u, Base64.decode("EAA="))
		} shouldBeHex "0F0000000578000200000009100000"
	}

}
