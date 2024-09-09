/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson

import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.TestDsl
import kotlin.test.assertEquals

@Suppress("DEPRECATION")
fun SuiteDsl.primitiveWriter(
	writer: Prepared<BsonPrimitiveWriter>,
	read: suspend TestDsl.() -> String,
) = suite("BsonPrimitiveWriter $writer") {

	suspend fun TestDsl.checkResult(
		// language=bson
		bson: String,
	) {
		val actual = read()
		assertEquals(bson, actual)
	}

	test("Int") {
		writer().writeDocument {
			writeInt32("foo", 42)
		}
		checkResult("""{"foo": 42}""")
	}

	test("More complex example") {
		writer().writeDocument {
			writeDBPointer("user", "myproject.users", ObjectId("507f1f77bcf86cd799439011"))
			writeInt64("age", 18)
			writeBoolean("isAlive", true)
		}
		val ref = "\$ref"
		val id = "\$id"
		val oid = "\$oid"
		checkResult("""{"user": {"$ref": "myproject.users", "$id": {"$oid": "507f1f77bcf86cd799439011"}}, "age": 18, "isAlive": true}""")
	}
}
