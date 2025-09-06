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

@file:OptIn(LowLevelApi::class, ExperimentalTime::class)

package opensavvy.ktmongo.bson.raw

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.serialize
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import kotlin.time.ExperimentalTime

/**
 * Test boolean representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/oid.json.
 */
fun SuiteDsl.objectId(context: Prepared<BsonContext>) = suite("ObjectId") {
	@Serializable
	data class A(val a: ObjectId)

	testBson(
		context,
		"All zeroes",
		document { writeObjectId("a", ObjectId.MIN) },
		document { writeObjectId("a", "000000000000000000000000".hexToByteArray(HexFormat.Default)) },
		serialize(A(ObjectId.MIN)),
		hex("1400000007610000000000000000000000000000"),
		json($$"""{"a": {"$oid": "000000000000000000000000"}}"""),
		verify("Read value") {
			check(read("a")?.readObjectId() == ObjectId.MIN)
		}
	)

	testBson(
		context,
		"All ones",
		document { writeObjectId("a", ObjectId.MAX) },
		document { writeObjectId("a", "ffffffffffffffffffffffff".hexToByteArray(HexFormat.Default)) },
		serialize(A(ObjectId.MAX)),
		hex("14000000076100FFFFFFFFFFFFFFFFFFFFFFFF00"),
		json($$"""{"a": {"$oid": "ffffffffffffffffffffffff"}}"""),
		verify("Read value") {
			check(read("a")?.readObjectId() == ObjectId.MAX)
		}
	)

	testBson(
		context,
		"Random",
		document { writeObjectId("a", ObjectId("56e1fc72e0c917e9c4714161")) },
		serialize(A(ObjectId("56e1fc72e0c917e9c4714161"))),
		hex("1400000007610056E1FC72E0C917E9C471416100"),
		json($$"""{"a": {"$oid": "56e1fc72e0c917e9c4714161"}}"""),
		verify("Read value") {
			check(read("a")?.readObjectId() == ObjectId("56e1fc72e0c917e9c4714161"))
		}
	)
}
