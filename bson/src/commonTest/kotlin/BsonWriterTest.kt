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

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.SuiteDsl
import kotlin.test.assertEquals

@OptIn(LowLevelApi::class)
@Suppress("DEPRECATION")
fun SuiteDsl.writerTests() = suite("BsonPrimitiveWriter") {

	test("An Int in a root document") {
		val result = buildBsonDocument {
			writeInt32("foo", 42)
		}
		assertEquals("""{"foo": 42}""", result.toString())
	}

	test("More complex example") {
		val result = buildBsonDocument {
			writeDBPointer("user", "myproject.users", ObjectId("507f1f77bcf86cd799439011"))
			writeInt64("age", 18)
			writeBoolean("isAlive", true)

			writeArray("children") {
				writeDocument {
					writeString("name", "Paul")
				}
				writeDocument {
					writeString("name", "Alice")
				}
			}
		}
		val ref = "\$ref"
		val id = "\$id"
		val oid = "\$oid"
		assertEquals("""{"user": {"$ref": "myproject.users", "$id": {"$oid": "507f1f77bcf86cd799439011"}}, "age": 18, "isAlive": true, "children": [{"name": "Paul"}, {"name": "Alice"}]}""", result.toString())
	}
}
