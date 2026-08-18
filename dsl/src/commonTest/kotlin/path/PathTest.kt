/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.path.PathSegment.*
import opensavvy.ktmongo.dsl.path.PathSegment.Field

@OptIn(LowLevelApi::class)
val PathTest by multiContextSuite {

	suite("String representation") {
		test("Root field") {
			check(Path("test").toString() == "test")
		}

		test("Nested field") {
			check((Path("test") / Field("bar")).toString() == "test.bar")
		}

		test("Deeper nested field") {
			check((Path("test") / Field("bar") / Field("foo")).toString() == "test.bar.foo")
		}

		test("Indexed") {
			check((Path("test") / Indexed(3) / Field("bar")).toString() == "test.3.bar")
		}

		test("Positional") {
			check((Path("test") / Positional / Field("bar")).toString() == "test.$.bar")
		}

		test("All positional") {
			check((Path("test") / AllPositional / Field("bar")).toString() == "test.$[].bar")
		}
	}

	suite("Comparison") {
		test("A path is equal to itself") {
			check(Path("a").compareTo(Path("a")) == 0)
			check((Path("a") / Field("b")).compareTo(Path("a") / Field("b")) == 0)
		}

		test("Shorter paths are less than longer paths sharing the same prefix") {
			check(Path("a") < Path("a") / Field("b"))
			check((Path("a") / Field("b")) > Path("a"))
		}

		test("Fields are compared alphanumerically") {
			check((Path("a") / Field("b")) < Path("a") / Field("foo"))
			check((Path("a") / Field("foo")) > Path("a") / Field("b"))
		}

		test("Indexed segments are compared numerically") {
			check((Path("d") / Indexed(1)) < Path("d") / Indexed(2))
			check((Path("d") / Indexed(2)) > Path("d") / Indexed(1))
		}

		test("FilteredPositional segments are compared alphanumerically by filter name") {
			check((Path("a") / FilteredPositional("x")) < Path("a") / FilteredPositional("y"))
			check((Path("a") / FilteredPositional("y")) > Path("a") / FilteredPositional("x"))
		}

		test("Segment types are ordered: AllPositional < Positional < Field < FilteredPositional < Indexed") {
			check((Path("a") / AllPositional) < Path("a") / Positional)
			check((Path("a") / Positional) < Path("a") / Field("b"))
			check((Path("a") / Field("b")) < Path("a") / FilteredPositional("x"))
			check((Path("a") / FilteredPositional("x")) < Path("a") / Indexed(0))

			// And in reverse
			check((Path("a") / Indexed(0)) > Path("a") / FilteredPositional("x"))
			check((Path("a") / FilteredPositional("x")) > Path("a") / Field("b"))
			check((Path("a") / Field("b")) > Path("a") / Positional)
			check((Path("a") / Positional) > Path("a") / AllPositional)
		}

		test("Common prefixes are skipped before comparing the differing segment") {
			check((Path("a") / Field("foo") / Field("bar")) < Path("a") / Field("foo") / Field("baz"))
			check((Path("a") / Field("foo")) < Path("d"))
		}

		test("Example order from the documentation") {
			val paths = listOf(
				Path("d") / Indexed(2),
				Path("a") / Field("foo") / Field("baz"),
				Path("d"),
				Path("a"),
				Path("a") / Field("foo"),
				Path("a") / Field("b"),
				Path("d") / Indexed(1),
				Path("a") / Field("foo") / Field("bar"),
			)

			val expected = listOf(
				Path("a"),
				Path("a") / Field("b"),
				Path("a") / Field("foo"),
				Path("a") / Field("foo") / Field("bar"),
				Path("a") / Field("foo") / Field("baz"),
				Path("d"),
				Path("d") / Indexed(1),
				Path("d") / Indexed(2),
			)

			check(paths.sorted() == expected)
		}
	}

}
