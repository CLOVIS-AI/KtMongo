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

@file:OptIn(ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.bson

import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.assertions.checkThrows

val BsonPathTest by preparedSuite {

	suite("Display") {

		test("The root") {
			check(BsonPath.toString() == "$")
		}

		test("A regular field") {
			check(BsonPath["foo"].toString() == "$.foo")
		}

		test("A regular array item") {
			check(BsonPath[0].toString() == "$[0]")
		}

		test("Fields that require escaping") {
			check(BsonPath["foo bar"].toString() == "$['foo bar']")
			check(BsonPath["baz.foo"].toString() == "$['baz.foo']")
		}

		test("Chaining") {
			check(BsonPath["store"]["book"][0]["title"].toString() == "$.store.book[0].title")
		}

		test("All fields") {
			check(BsonPath["foo"].all.toString() == "$.foo.*")
		}

		test("Slice without step") {
			check(BsonPath.sliced(1, 10).toString() == "$[1:10]")
			check(BsonPath.sliced(end = 10).toString() == "$[:10]")
			check(BsonPath.sliced(start = 1).toString() == "$[1:]")
			check(BsonPath.sliced().toString() == "$[:]")
		}

		test("Slice with step") {
			check(BsonPath.sliced(1, 10, 2).toString() == "$[1:10:2]")
			check(BsonPath.sliced(end = 9, step = 2).toString() == "$[:9:2]")
			check(BsonPath.sliced(step = -1).toString() == "$[::-1]")
			check(BsonPath.sliced(end = 0, step = -2).toString() == "$[:0:-2]")
		}

		test("Reversed") {
			check(BsonPath.reversed().toString() == "$[::-1]")
		}
	}

	suite("Parsing") {

		test("A BsonPath expression must start with a $") {
			checkThrows<IllegalArgumentException> {
				BsonPath.parse("foo")
			}
		}

		test("Parse a simple field with dot notation") {
			check(BsonPath.parse("$.book").toString() == "$.book")
			check(BsonPath.parse("$.book") == BsonPath["book"])
		}

		test("Parse a simple field with bracket notation") {
			check(BsonPath.parse("$['book']").toString() == "$.book")
			check(BsonPath.parse("$['book']") == BsonPath["book"])
		}

		test("Parse a simple field with bracket notation and double quotes") {
			check(BsonPath.parse("$[\"book\"]").toString() == "$.book")
			check(BsonPath.parse("$[\"book\"]") == BsonPath["book"])
		}

		test("Parse a combined field names") {
			check(BsonPath.parse("$.foo['bar'][\"baz\"]").toString() == "$.foo.bar.baz")
			check(BsonPath.parse("$.foo['bar'][\"baz\"]") == BsonPath["foo"]["bar"]["baz"])
		}

		test("Parse a simple array index") {
			check(BsonPath.parse("$[0]").toString() == "$[0]")
			check(BsonPath.parse("$[0]") == BsonPath[0])
		}

		test("Parse all fields") {
			check(BsonPath.parse("$.foo[*]").toString() == "$.foo.*")
			check(BsonPath.parse("$.foo[*]") == BsonPath["foo"].all)
		}

		test("Parse all fields, with dot notation") {
			check(BsonPath.parse("$.foo.*").toString() == "$.foo.*")
			check(BsonPath.parse("$.foo.*") == BsonPath["foo"].all)
		}

		test("Slice without bounds") {
			check(BsonPath.parse("$[:]").toString() == "$[:]")
			check(BsonPath.parse("$[:]") == BsonPath.sliced())
			check(BsonPath.parse("$[:]") == BsonPath.sliced(0..Int.MAX_VALUE))
		}

		test("Reversing slice") {
			check(BsonPath.parse("$[::-1]").toString() == "$[::-1]")
			check(BsonPath.parse("$[::-1]") == BsonPath.sliced(step = -1))
			check(BsonPath.parse("$[::-1]") == BsonPath.reversed())
		}

		test("IntProgression cannot be used to reverse a slice") {
			check(BsonPath.parse("$[2147483647::-1]") == BsonPath.sliced(Int.MAX_VALUE downTo 0 step 1))
		}

		test("Slice with minimum") {
			check(BsonPath.parse("$[5:]").toString() == "$[5:]")
			check(BsonPath.parse("$[5:]") == BsonPath.sliced(start = 5))
			check(BsonPath.parse("$[5:]") == BsonPath.sliced(5..Int.MAX_VALUE))
		}

		test("Slice with maximum") {
			check(BsonPath.parse("$[:5]").toString() == "$[:5]")
			check(BsonPath.parse("$[:5]") == BsonPath.sliced(end = 5))
			check(BsonPath.parse("$[:5]") == BsonPath.sliced(0..<5))
		}

		test("Slice with minimum and maximum") {
			check(BsonPath.parse("$[2:4]").toString() == "$[2:4]")
			check(BsonPath.parse("$[2:4]") == BsonPath.sliced(2, 4))
			check(BsonPath.parse("$[2:4]") == BsonPath.sliced(2..<4))
		}

		test("Slice with step") {
			check(BsonPath.parse("$[1:7:2]").toString() == "$[1:7:2]")
			check(BsonPath.parse("$[1:7:2]") == BsonPath.sliced(1, 7, 2))
			check(BsonPath.parse("$[1:6:2]") == BsonPath.sliced(1..<7 step 2)) // 1:7:2 is 1,3,5 (because 7 is an exclusive bound), the Kotlin IntProgression class simplifies the range
		}

		test("Slice with negative step") {
			check(BsonPath.parse("$[7:1:-1]").toString() == "$[7:1:-1]")
			check(BsonPath.parse("$[7:1:-1]") == BsonPath.sliced(7, 1, -1))
			check(BsonPath.parse("$[7:1:-1]") == BsonPath.sliced(7 downTo 2 step 1))
		}

		test("Slice with negative step of 2") {
			check(BsonPath.parse("$[7:1:-2]").toString() == "$[7:1:-2]")
			check(BsonPath.parse("$[7:1:-2]") == BsonPath.sliced(7, 1, -2))
			check(BsonPath.parse("$[7:2:-2]") == BsonPath.sliced(7 downTo 2 step 2)) // 7:1:-2 is 7,5,3 (because 1 is an exclusive bound), the Kotlin IntProgression class simplifies the range
		}

		test("Default slice with negative step") {
			check(BsonPath.parse("$[::-2]").toString() == "$[::-2]")
			check(BsonPath.parse("$[::-2]") == BsonPath.sliced(step = -2))
		}

		test("IntProgression range simplifications") {
			check(BsonPath.sliced(1..<7 step 2) == BsonPath.sliced(1..<6 step 2))
			check(BsonPath.sliced(7 downTo 1 step 2) == BsonPath.sliced(7 downTo 0 step 2))
		}

	}

}
