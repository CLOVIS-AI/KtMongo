/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@OptIn(LowLevelApi::class, ExperimentalStdlibApi::class)
@Suppress("DEPRECATION")
fun SuiteDsl.writerTests(
	prepareContext: Prepared<BsonContext>,
) = suite("BsonPrimitiveWriter") {

	test("An Int in a root document") {
		val result = prepareContext().buildDocument {
			writeInt32("foo", 42)
		}
		check(result.toString() == """{"foo": 42}""")
	}

	test("More complex example") {
		val result = prepareContext().buildDocument {
			writeDBPointer("user", "myproject.users", "67d43dbc64b52e612b1b2f7b".hexToByteArray())// (0x67d43dbc), 0x67d43dbc, 0x64b52e612b1b2f7b)
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
		check(result.toString() == """{"user": {"$ref": "myproject.users", "$id": {"$oid": "67d43dbc64b52e612b1b2f7b"}}, "age": 18, "isAlive": true, "children": [{"name": "Paul"}, {"name": "Alice"}]}""")
	}

	test("An empty document") {
		val result = prepareContext().buildDocument {}
		check(result.toString() == """{}""")
	}

	test("An empty array") {
		val result = prepareContext().buildArray {}
		check(result.toString() == """[]""")
	}

	test("An array with multiple elements") {
		val result = prepareContext().buildArray {
			writeInt32(123)
			writeBoolean(false)
			writeDocument {
				writeString("name", "Paul")
				writeInt32("age", 18)
			}
		}

		check(result.toString() == """[123, false, {"name": "Paul", "age": 18}]""")
	}
}
