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
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/int32.json.
 */
fun SuiteDsl.int32(context: Prepared<BsonFactory>) = suite("Int32") {
	@Serializable
	data class I(val i: Int)

	testBson(
		context,
		"Min value",
		document { writeInt32("i", Int.MIN_VALUE) },
		serialize(I(Int.MIN_VALUE)),
		hex("0C0000001069000000008000"),
		json("""{"i": -2147483648}"""),
		verify("Read value") {
			check(read("i")?.readInt32() == Int.MIN_VALUE)
		}
	)

	testBson(
		context,
		"Max value",
		document { writeInt32("i", Int.MAX_VALUE) },
		serialize(I(Int.MAX_VALUE)),
		hex("0C000000106900FFFFFF7F00"),
		json("""{"i": 2147483647}"""),
		verify("Read value") {
			check(read("i")?.readInt32() == Int.MAX_VALUE)
		}
	)

	testBson(
		context,
		"-1",
		document { writeInt32("i", -1) },
		serialize(I(-1)),
		hex("0C000000106900FFFFFFFF00"),
		json("""{"i": -1}"""),
		verify("Read value") {
			check(read("i")?.readInt32() == -1)
		}
	)

	testBson(
		context,
		"0",
		document { writeInt32("i", 0) },
		serialize(I(0)),
		hex("0C0000001069000000000000"),
		json("""{"i": 0}"""),
		verify("Read value") {
			check(read("i")?.readInt32() == 0)
		}
	)

	testBson(
		context,
		"+1",
		document { writeInt32("i", 1) },
		serialize(I(1)),
		hex("0C0000001069000100000000"),
		json("""{"i": 1}"""),
		verify("Read value") {
			check(read("i")?.readInt32() == 1)
		}
	)
}
