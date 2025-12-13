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

	}

}
