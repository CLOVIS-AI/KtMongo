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
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
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
	testBson(
		context,
		"subtype 0x00 (Zero-length)",
		document {
			writeBinaryData("x", 0x0u, Base64.decode(""))
		},
		hex("0D000000057800000000000000"),
		json($$"""{"x": {"$binary": {"base64": "", "subType": "00"}}}"""),
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0.toUByte())
		},
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(ByteArray(0)))
		}
	)

	testBson(
		context,
		"subtype 0x00",
		document {
			writeBinaryData("x", 0x0u, Base64.decode("//8="))
		},
		hex("0F0000000578000200000000FFFF00"),
		json($$"""{"x": {"$binary": {"base64": "//8=", "subType": "00"}}}"""),
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0.toUByte())
		},
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("//8=")))
		}
	)

	testBson(
		context,
		"subtype 0x01",
		document {
			writeBinaryData("x", 0x1u, Base64.decode("//8="))
		},
		hex("0F0000000578000200000001FFFF00"),
		json($$"""{"x": {"$binary": {"base64": "//8=", "subType": "01"}}}"""),
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 1.toUByte())
		},
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("//8=")))
		}
	)

	testBson(context, "subtype 0x02") {
		document {
			writeBinaryData("x", 0x2u, Base64.decode("//8="))
		}
		expectedBinaryHex = "13000000057800060000000202000000FFFF00"
		expectedJson = $$"""{"x": {"$binary": {"base64": "//8=", "subType": "02"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 2.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("//8=")))
		}
	}

	testBson(context, "subtype 0x03") {
		document {
			writeBinaryData("x", 0x3u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
		expectedBinaryHex = "1D000000057800100000000373FFD26444B34C6990E8E7D1DFC035D400"
		expectedJson = $$"""{"x": {"$binary": {"base64": "c//SZESzTGmQ6OfR38A11A==", "subType": "03"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 3.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("c//SZESzTGmQ6OfR38A11A==")))
		}
	}

	testBson(context, "subtype 0x04") {
		document {
			writeBinaryData("x", 0x4u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
		expectedBinaryHex = "1D000000057800100000000473FFD26444B34C6990E8E7D1DFC035D400"
		expectedJson = $$"""{"x": {"$binary": {"base64": "c//SZESzTGmQ6OfR38A11A==", "subType": "04"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 4.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("c//SZESzTGmQ6OfR38A11A==")))
		}
	}

	testBson(context, "subtype 0x04 UUID") {
		document {
			writeBinaryData("x", 0x4u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
		expectedBinaryHex = "1D000000057800100000000473FFD26444B34C6990E8E7D1DFC035D400"
		expectedJson = $$"""{"x": {"$binary": {"base64": "c//SZESzTGmQ6OfR38A11A==", "subType": "04"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 4.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("c//SZESzTGmQ6OfR38A11A==")))
		}
	}

	testBson(context, "subtype 0x05") {
		document {
			writeBinaryData("x", 0x5u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
		expectedBinaryHex = "1D000000057800100000000573FFD26444B34C6990E8E7D1DFC035D400"
		expectedJson = $$"""{"x": {"$binary": {"base64": "c//SZESzTGmQ6OfR38A11A==", "subType": "05"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 5.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("c//SZESzTGmQ6OfR38A11A==")))
		}
	}

	testBson(context, "subtype 0x07") {
		document {
			writeBinaryData("x", 0x7u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
		expectedBinaryHex = "1D000000057800100000000773FFD26444B34C6990E8E7D1DFC035D400"
		expectedJson = $$"""{"x": {"$binary": {"base64": "c//SZESzTGmQ6OfR38A11A==", "subType": "07"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 7.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("c//SZESzTGmQ6OfR38A11A==")))
		}
	}

	testBson(context, "subtype 0x08") {
		document {
			writeBinaryData("x", 0x8u, Base64.decode("c//SZESzTGmQ6OfR38A11A=="))
		}
		expectedBinaryHex = "1D000000057800100000000873FFD26444B34C6990E8E7D1DFC035D400"
		expectedJson = $$"""{"x": {"$binary": {"base64": "c//SZESzTGmQ6OfR38A11A==", "subType": "08"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 8.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("c//SZESzTGmQ6OfR38A11A==")))
		}
	}

	testBson(context, "subtype 0x80") {
		document {
			writeBinaryData("x", 0x80u, Base64.decode("//8="))
		}
		expectedBinaryHex = "0F0000000578000200000080FFFF00"
		expectedJson = $$"""{"x": {"$binary": {"base64": "//8=", "subType": "80"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x80.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("//8=")))
		}
	}

	testBson(context, $$"$type query operator (conflicts with legacy $binary form with $type field)") {
		document {
			writeDocument("x") {
				writeString("\$type", "string")
			}
		}
		expectedBinaryHex = "1F000000037800170000000224747970650007000000737472696E67000000"
		expectedJson = $$"""{"x": {"$type": "string"}}"""
		verify("Read type") {
			check(read("x")?.readDocument()?.read("\$type")?.readString() == "string")
		}
	}

	testBson(context, $$"$type query operator (conflicts with legacy $binary form with $type field) with int") {
		document {
			writeDocument("x") {
				writeInt32("\$type", 2)
			}
		}
		expectedBinaryHex = "180000000378001000000010247479706500020000000000"
		expectedJson = $$"""{"x": {"$type": 2}}"""
		verify("Read type") {
			check(read("x")?.readDocument()?.read("\$type")?.readInt32() == 2)
		}
	}

	testBson(context, "subtype 0x09 Vector FLOAT32") {
		document {
			writeBinaryData("x", 0x09u, Base64.decode("JwAAAP5CAADgQA=="))
		}
		expectedBinaryHex = "170000000578000A0000000927000000FE420000E04000"
		expectedJson = $$"""{"x": {"$binary": {"base64": "JwAAAP5CAADgQA==", "subType": "09"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x09.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("JwAAAP5CAADgQA==")))
		}
	}

	testBson(context, "subtype 0x09 Vector INT8") {
		document {
			writeBinaryData("x", 0x09u, Base64.decode("AwB/Bw=="))
		}
		expectedBinaryHex = "11000000057800040000000903007F0700"
		expectedJson = $$"""{"x": {"$binary": {"base64": "AwB/Bw==", "subType": "09"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x09.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("AwB/Bw==")))
		}
	}

	testBson(context, "subtype 0x09 Vector PACKED_BIT") {
		document {
			writeBinaryData("x", 0x09u, Base64.decode("EAB/Bw=="))
		}
		expectedBinaryHex = "11000000057800040000000910007F0700"
		expectedJson = $$"""{"x": {"$binary": {"base64": "EAB/Bw==", "subType": "09"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x09.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("EAB/Bw==")))
		}
	}

	testBson(context, "subtype 0x09 Vector (Zero-length) FLOAT32") {
		document {
			writeBinaryData("x", 0x09u, Base64.decode("JwA="))
		}
		expectedBinaryHex = "0F0000000578000200000009270000"
		expectedJson = $$"""{"x": {"$binary": {"base64": "JwA=", "subType": "09"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x09.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("JwA=")))
		}
	}

	testBson(context, "subtype 0x09 Vector (Zero-length) INT8") {
		document {
			writeBinaryData("x", 0x09u, Base64.decode("AwA="))
		}
		expectedBinaryHex = "0F0000000578000200000009030000"
		expectedJson = $$"""{"x": {"$binary": {"base64": "AwA=", "subType": "09"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x09.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("AwA=")))
		}
	}

	testBson(context, "subtype 0x09 Vector (Zero-length) PACKED_BIT") {
		document {
			writeBinaryData("x", 0x09u, Base64.decode("EAA="))
		}
		expectedBinaryHex = "0F0000000578000200000009100000"
		expectedJson = $$"""{"x": {"$binary": {"base64": "EAA=", "subType": "09"}}}"""
		verify("Read type") {
			check(read("x")?.readBinaryDataType() == 0x09.toUByte())
		}
		verify("Read data") {
			check(read("x")?.readBinaryData().contentEquals(Base64.decode("EAA=")))
		}
	}

}
