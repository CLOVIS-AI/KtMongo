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

import opensavvy.ktmongo.bson.path.bsonPathTests
import opensavvy.ktmongo.bson.raw.*
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@OptIn(LowLevelApi::class, ExperimentalStdlibApi::class)
@Suppress("DEPRECATION")
fun SuiteDsl.validateBsonFactory(
	prepareFactory: Prepared<BsonFactory>,
) {

	test("An Int in a root document") {
		val result = prepareFactory().buildDocument {
			writeInt32("foo", 42)
		}
		check(result.toString() == """{"foo": 42}""")
	}

	test("More complex example") {
		val result = prepareFactory().buildDocument {
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
		check(result.toString() == """{"age": 18, "isAlive": true, "children": [{"name": "Paul"}, {"name": "Alice"}]}""")
	}

	test("An empty document") {
		val result = prepareFactory().buildDocument {}
		check(result.toString() == """{}""")
	}

	test("An empty array") {
		val result = prepareFactory().buildArray {}
		check(result.toString() == """[]""")
	}

	test("An array with multiple elements") {
		val result = prepareFactory().buildArray {
			writeInt32(123)
			writeBoolean(false)
			writeDocument {
				writeString("name", "Paul")
				writeInt32("age", 18)
			}
		}

		check(result.toString() == """[123, false, {"name": "Paul", "age": 18}]""")
	}

	suite("BSON corpus") {
		boolean(prepareFactory)
		int32(prepareFactory)
		int64(prepareFactory)
		double(prepareFactory)
		string(prepareFactory)
		reprNull(prepareFactory)
		reprUndefined(prepareFactory)
		document(prepareFactory)
		array(prepareFactory)
		binary(prepareFactory)
		code(prepareFactory)
		datetime(prepareFactory)
		minMaxKey(prepareFactory)
		regex(prepareFactory)
		timestamp(prepareFactory)
		objectId(prepareFactory)
	}

	@OptIn(DangerousMongoApi::class)
	test("Pipe objects") {
		val pipe = prepareFactory().buildDocument {
			writeInt32("four", 2 + 2)
			writeString("foo", "bar")
			writeArray("grades") {
				writeInt32(4)
				writeInt32(7)
			}
		}

		prepareFactory().buildDocument {
			write("root") {
				pipe(pipe.reader().asValue())
			}
		} shouldBeJson """{"root": {"four": 4, "foo": "bar", "grades": [4, 7]}}"""
	}

	bsonPathTests(prepareFactory)
}
