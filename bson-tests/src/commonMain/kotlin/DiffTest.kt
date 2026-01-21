/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

@file:OptIn(LowLevelApi::class, ExperimentalBsonDiffApi::class)

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.assertions.log
import kotlin.test.assertEquals

fun SuiteDsl.validateDiffAlgorithms(
	factory: Prepared<BsonFactory>,
) = suite("Diff algorithms") {

	suspend fun TestDsl.checkDiff(a: Bson, b: Bson, expected: String?) {
		log(a) { "a" }
		log(b) { "b" }

		val diff = (a diff b)
			?.trim()
			?.split("\n")
			?.joinToString("\n") { it.trimEnd() }

		when {
			diff == expected -> {
				if (expected == null) {
					check(a == b) { "Found no difference, as expected, but they are not marked as equals. This is probably a bug in the equals function in ${a::class}." }
				} else {
					println("Found the expected difference:\n$diff")
					check(a != b) { "Found the expected difference, however the two values were returned as equal. This is probably a bug in the equals function in ${a::class}." }
				}
			}

			expected == null -> assertEquals(expected, diff, "Expected no diff")
			else -> assertEquals(expected, diff)
		}
	}

	suite("Scalars") {

		test("Two equal integers") {
			val a = factory().buildDocument {
				writeInt32("a", 42)
			}

			val b = factory().buildDocument {
				writeInt32("a", 42)
			}

			checkDiff(a, b, expected = null)
		}

		test("Two different integers") {
			val a = factory().buildDocument {
				writeInt32("a", 43)
			}

			val b = factory().buildDocument {
				writeInt32("a", 44)
			}

			checkDiff(a, b, expected = """
				✗ a: 43
				     44
			""".trimIndent())
		}

		test("Two different integer types") {
			val a = factory().buildDocument {
				writeInt32("a", 42)
			}

			val b = factory().buildDocument {
				writeInt64("a", 42)
			}

			checkDiff(a, b, expected = """
				✗ a: {Int32} 42
				     {Int64} 42
			""".trimIndent())
		}

		test("Two equal texts") {
			val a = factory().buildDocument {
				writeString("a", "foo")
			}

			val b = factory().buildDocument {
				writeString("a", "foo")
			}

			checkDiff(a, b, expected = null)
		}

		test("Two different texts") {
			val a = factory().buildDocument {
				writeString("a", "foo")
			}

			val b = factory().buildDocument {
				writeString("a", "bar")
			}

			checkDiff(a, b, expected = """
				✗ a: "foo"
				     "bar"
			""".trimIndent())
		}

		test("A text that exists in one but not the other") {
			val a = factory().buildDocument {
				writeString("a", "foo")
			}

			val b = factory().buildDocument {
			}

			checkDiff(a, b, expected = """
				✗ a: "foo"
				     (field not present)
			""".trimIndent())
		}

		test("A text that exists in one but not the other (opposite)") {
			val a = factory().buildDocument {
			}

			val b = factory().buildDocument {
				writeString("a", "foo")
			}

			checkDiff(a, b, expected = """
				✗ a: (field not present)
				     "foo"
			""".trimIndent())
		}
	}

	suite("Documents") {

		test("Simple example") {
			val a = factory().buildDocument {
				writeString("a", "foo")
				writeDocument("b") {
					writeString("name", "Bob")
					writeInt32("age", 18)
				}
			}

			val b = factory().buildDocument {
				writeString("a", "foo")
				writeDocument("b") {
					writeString("name", "Fred")
					writeInt32("age", 18)
				}
			}

			checkDiff(a, b, expected = """
				✓ a: "foo"
				✗ b:
				     ✗ name: "Bob"
				             "Fred"
				     ✓ age: 18
			""".trimIndent())
		}
	}

	suite("Arrays") {
		test("Identical") {
			val a = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
					writeString("Bob")
				}
			}

			val b = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
					writeString("Bob")
				}
			}

			checkDiff(a, b, expected = null)
		}

		test("Different element: type") {
			val a = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
					writeString("Bob")
				}
			}

			val b = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt64(2)
					writeString("Bob")
				}
			}

			checkDiff(a, b, expected = """
				✗ a:
				     ✓ 0: 1
				     ✗ 1: {Int32} 2
				          {Int64} 2
				     ✓ 2: "Bob"
			""".trimIndent())
		}

		test("Different element: value") {
			val a = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
					writeString("Bob")
				}
			}

			val b = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(3)
					writeString("Bob")
				}
			}

			checkDiff(a, b, expected = """
				✗ a:
				     ✓ 0: 1
				     ✗ 1: 2
				          3
				     ✓ 2: "Bob"
			""".trimIndent())
		}

		test("One too many element") {
			val a = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
					writeString("Bob")
				}
			}

			val b = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
				}
			}

			checkDiff(a, b, expected = """
				✗ a:
				     ✓ 0: 1
				     ✓ 1: 2
				     ✗ 2: "Bob"
				          (field not present)
			""".trimIndent())
		}

		test("One too many element (opposite)") {
			val a = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
				}
			}

			val b = factory().buildDocument {
				writeArray("a") {
					writeInt32(1)
					writeInt32(2)
					writeString("Bob")
				}
			}

			checkDiff(a, b, expected = """
				✗ a:
				     ✓ 0: 1
				     ✓ 1: 2
				     ✗ 2: (field not present)
				          "Bob"
			""".trimIndent())
		}
	}

}
