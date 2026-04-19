/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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
@file:Suppress("SENSELESS_COMPARISON")

package opensavvy.ktmongo.bson

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.assertions.log
import opensavvy.prepared.suite.assertions.matches

@Serializable
data class BsonArrayProfile(
	val name: String,
	val score: Double,
)

@Serializable
data class BsonArrayUser(
	val id: String,
	val profile: BsonArrayProfile,
	val friends: List<BsonArrayProfile>,
)

fun SuiteDsl.verifyBsonArrays(factory: Prepared<BsonFactory>) = suite("BSON arrays") {

	test("Decode a simple array") {
		val array = factory().buildArray {
			writeDouble(1.1)
			writeDouble(2.2)
			writeDouble(3.3)
		}

		check(array.decode<List<Double>>() == listOf(1.1, 2.2, 3.3))
		check(array.decode<MutableList<Double>>() == listOf(1.1, 2.2, 3.3))
		check(array.decodeElements<Double>() == listOf(1.1, 2.2, 3.3))
	}

	test("Decode a simple array as a set") {
		val array = factory().buildArray {
			writeDouble(1.1)
			writeDouble(2.2)
			writeDouble(3.3)
		}

		check(array.decode<Set<Double>>() == setOf(1.1, 2.2, 3.3))
		check(array.decode<MutableSet<Double>>() == setOf(1.1, 2.2, 3.3))
	}

	test("Decode an array of objects") {
		val array = factory().buildArray {
			writeDocument {
				writeString("name", "Alice")
				writeDouble("score", 1.2)
			}
			writeDocument {
				writeString("name", "Bob")
				writeDouble("score", 2.3)
			}
		}

		val alice = BsonArrayProfile("Alice", 1.2)
		val bob = BsonArrayProfile("Bob", 2.3)

		check(array.decode<List<BsonArrayProfile>>() == listOf(alice, bob))
		check(array.decodeElements<BsonArrayProfile>() == listOf(alice, bob))
		check(array.decode<Set<BsonArrayProfile>>() == setOf(bob, alice)) // Order shouldn't matter
		check(array.decode<Array<BsonArrayProfile>>().contentEquals(arrayOf(alice, bob)))
	}

	test("Decode a simple array with heterogeneous types") {
		val array = factory().buildArray {
			writeDouble(1.0)
			writeString("foo")
		}

		val exception = checkThrows<BsonDecodingException> {
			array.decode<List<Double>>()
		}
		check(exception.message matches "Could not decode .*List.*[Dd]ouble.*\n\tfrom value \\[1.0, \"foo\"\\]")

		val exceptionElements = checkThrows<BsonDecodingException> {
			array.decodeElements<Double>()
		}
		check(exceptionElements.message matches "Could not decode elements of type .*[Dd]ouble.*\n\tfrom value \\[1.0, \"foo\"\\]")
	}

	test("Decode an array of documents") {
		val array = factory().buildArray {
			writeDocument {
				writeString("name", "Bob")
				writeDouble("score", 12.0)
			}
			writeDocument {
				writeString("name", "Alice")
				writeDouble("score", 13.7)
			}
		}

		val expected = listOf(
			BsonArrayProfile("Bob", 12.0),
			BsonArrayProfile("Alice", 13.7),
		)

		check(array.decode<List<BsonArrayProfile>>() == expected)
		check(array.decodeElements<BsonArrayProfile>() == expected)
	}

	test("Decode an empty array") {
		val array = factory().buildArray {}

		check(array.decode<List<BsonArrayProfile>>() == emptyList<BsonArrayProfile>())
		check(array.decodeElements<BsonArrayProfile>() == emptyList<BsonArrayProfile>())
	}

	test("Decode an array with nullable elements") {
		val array = factory().buildArray {
			writeDocument {
				writeString("name", "Bob")
				writeDouble("score", 12.0)
			}
			writeNull()
			writeDocument {
				writeString("name", "Alice")
				writeDouble("score", 13.7)
			}
		}

		val expected = listOf(
			BsonArrayProfile("Bob", 12.0),
			null,
			BsonArrayProfile("Alice", 13.7),
		)

		check(array.decode<List<BsonArrayProfile?>>() == expected)
		check(array.decodeElements<BsonArrayProfile?>() == expected)

		val exception = checkThrows<BsonDecodingException> {
			check(array.decode<List<BsonArrayProfile>>() == expected)
		}
		check(exception.message matches "Could not decode .*List.*BsonArrayProfile.*\n\tfrom value \\[\\{\"name\": \"Bob\", \"score\": 12.0\\}, null, \\{\"name\": \"Alice\", \"score\": 13.7\\}\\]")

		val exceptionElements = checkThrows<BsonDecodingException> {
			check(array.decodeElements<BsonArrayProfile>() == expected)
		}
		check(exceptionElements.message matches "Could not decode elements of type .*BsonArrayProfile.*\n\tfrom value \\[\\{\"name\": \"Bob\", \"score\": 12.0\\}, null, \\{\"name\": \"Alice\", \"score\": 13.7\\}\\]")
	}

	test("Decode an array within a document") {
		val user = factory().buildDocument {
			writeString("id", "123456")
			writeDocument("profile") {
				writeString("name", "Bob")
				writeDouble("score", 12.0)
			}
			writeArray("friends") {
				writeDocument {
					writeString("name", "Alice")
					writeDouble("score", 13.7)
				}
				writeDocument {
					writeDouble("score", 14.5)
					writeString("name", "Charlie")
				}
			}
		}

		check(user.decode<BsonArrayUser>() == BsonArrayUser("123456", BsonArrayProfile("Bob", 12.0), listOf(BsonArrayProfile("Alice", 13.7), BsonArrayProfile("Charlie", 14.5))))
	}

	suite("Iteration") {
		test("Iterable") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
			}

			check(array.asIterable().toString() == """["Bob", 45, null]""")

			val iter = array.asIterable().iterator()

			check(iter.hasNext())
			val bob = log(iter.next())
			check(bob.decodeString() == "Bob")
			check(bob.toString() == "\"Bob\"")
			check(bob.hashCode() == 66965)

			check(iter.hasNext())
			val fortyFive = log(iter.next())
			check(fortyFive.decodeInt32() == 45)
			check(fortyFive.toString() == "45")
			check(fortyFive.hashCode() == 45)

			check(iter.hasNext())
			val nullValue = log(iter.next())
			check(nullValue.decodeNull() == null)
			check(nullValue.toString() == "null")
			check(nullValue.hashCode() == 0)

			check(!iter.hasNext())
			checkThrows<NoSuchElementException> { iter.next() }

			check(bob != fortyFive)
		}

		test("Iterator") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
			}

			val iter = array.iterator()

			check(iter.hasNext())
			val bob = log(iter.next())
			check(bob.decodeString() == "Bob")
			check(bob.toString() == "\"Bob\"")
			check(bob.hashCode() == 66965)

			check(iter.hasNext())
			val fortyFive = log(iter.next())
			check(fortyFive.decodeInt32() == 45)
			check(fortyFive.toString() == "45")
			check(fortyFive.hashCode() == 45)

			check(iter.hasNext())
			val nullValue = log(iter.next())
			check(nullValue.decodeNull() == null)
			check(nullValue.toString() == "null")
			check(nullValue.hashCode() == 0)

			check(!iter.hasNext())
			checkThrows<NoSuchElementException> { iter.next() }

			check(bob != fortyFive)
		}

		test("List") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
				writeInt32(45)
			}

			val other = factory().buildDocument {
				writeInt32("c", 45)
				writeInt32("d", 75)
			}

			val list = array.asList()

			check(list.toString() == """["Bob", 45, null, 45]""")
			check(list.size == 4)
			check(list.isNotEmpty())
			check(list[0].decodeString() == "Bob")
			check(list[1].decodeInt32() == 45)
			check(list[2].decodeNull() == null)
			check(list[3].decodeInt32() == 45)
			checkThrows<IndexOutOfBoundsException> { list[4] }
			checkThrows<IndexOutOfBoundsException> { list[-1] }
			check(other["c"] in list)
			check(other["d"] !in list)
			check(list.containsAll(listOf(list[0], other["c"])))
			check(!list.containsAll(other.asMap().values))
			check(list.indexOf(other["c"]) == 1)
			check(list.indexOf(other["d"]) == -1)
			check(list.lastIndexOf(other["c"]) == 3)
			check(list.lastIndexOf(other["d"]) == -1)
			check(list == listOf(list[0], list[1], list[2], list[3]))
			check(list.hashCode() == 1995921126)

			val iter = list.iterator()

			check(iter.hasNext())
			check(iter.next().decodeString() == "Bob")

			check(iter.hasNext())
			check(iter.next().decodeInt32() == 45)

			check(iter.hasNext())
			check(iter.next().decodeNull() == null)

			check(iter.hasNext())
			check(iter.next().decodeInt32() == 45)

			check(!iter.hasNext())
			checkThrows<NoSuchElementException> { iter.next() }
		}

		test("List iterator") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
				writeInt32(45)
			}

			val iter = array.asList().listIterator()

			check(!iter.hasPrevious())
			check(iter.previousIndex() == -1)
			checkThrows<NoSuchElementException> { iter.previous() }

			check(iter.hasNext())
			check(iter.nextIndex() == 0)
			check(iter.next().decodeString() == "Bob")

			check(iter.hasNext())
			check(iter.nextIndex() == 1)
			check(iter.next().decodeInt32() == 45)

			check(iter.hasNext())
			check(iter.nextIndex() == 2)
			check(iter.next().decodeNull() == null)

			check(iter.hasNext())
			check(iter.nextIndex() == 3)
			check(iter.next().decodeInt32() == 45)

			check(!iter.hasNext())
			check(iter.nextIndex() == array.size)
			checkThrows<NoSuchElementException> { iter.next() }

			check(iter.hasPrevious())
			check(iter.previousIndex() == 3)
			check(iter.previous().decodeInt32() == 45)

			check(iter.hasPrevious())
			check(iter.previousIndex() == 2)
			check(iter.previous().decodeNull() == null)
		}

		test("Sublist") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
				writeDouble(45.0)
			}

			val list = array.asList().subList(2, 4)
			check(list.size == 2)
			check(list[0].decodeNull() == null)
			check(list[1].decodeDouble() == 45.0)
		}

		test("Sequence") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
				writeInt32(45)
			}

			val values = array.asSequence().toList()

			check(values.size == 4)
			check(values[0].decodeString() == "Bob")
			check(values[1].decodeInt32() == 45)
			check(values[2].decodeNull() == null)
			check(values[3].decodeInt32() == 45)

			check(array.asSequence().toString() == """["Bob", 45, null, 45]""")
		}

		test("With index") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
				writeInt32(45)
			}

			val iter = array.withIndex().iterator()

			check(iter.hasNext())
			val bob = log(iter.next())
			check(bob.index == 0)
			check(bob.value.decodeString() == "Bob")

			check(iter.hasNext())
			val fortyFive = log(iter.next())
			check(fortyFive.index == 1)
			check(fortyFive.value.decodeInt32() == 45)

			check(iter.hasNext())
			val nullValue = log(iter.next())
			check(nullValue.index == 2)
			check(nullValue.value.decodeNull() == null)

			check(iter.hasNext())
			check(iter.next().value == fortyFive.value)

			check(!iter.hasNext())
		}
	}

	suite("Direct access") {
		test("Small array") {
			val array = factory().buildArray {
				writeString("Bob")
				writeInt32(45)
				writeNull()
				writeDouble(45.0)
			}

			check(array.size == 4)
			check(!array.isEmpty())
			check(array.isNotEmpty())
			check(array.indices == 0..3)
			check(array[0]?.decodeString() == "Bob")
			check(array[1]?.decodeInt32() == 45)
			check(array[2]?.decodeNull() == null)
			check(array[3]?.decodeDouble() == 45.0)
			check(array[4] == null)
			check(array.toString() == """["Bob", 45, null, 45.0]""")

			check(array.asValue().decodeArray() == array) { "Round-trip through BsonValue" }
		}

		test("Complex document with nested arrays") {
			val document = factory().buildDocument {
				writeString("name", "Alice")
				writeArray("animals") {
					writeString("Elephant")
					writeString("Monkey")
					writeString("Llama")
					writeInt32(123)
					writeArray {
						writeString("Chocolate")
						writeString("Banana")
						writeString("Strawberry")
						writeString("Potato")
						writeBoolean(false)
						writeDouble(129879.0)
						writeDocument {}
					}
				}
			}

			val animals = checkNotNull(document["animals"]?.decodeArray())

			check(animals.size == 5)
			check(animals[0]?.decodeString() == "Elephant")
			check(animals[1]?.decodeString() == "Monkey")
			check(animals[2]?.decodeString() == "Llama")
			check(animals[3]?.decodeInt32() == 123)

			val nested = checkNotNull(animals[4]?.decodeArray())

			check(nested[0]?.decodeString() == "Chocolate")
			check(nested[1]?.decodeString() == "Banana")
			check(nested[2]?.decodeString() == "Strawberry")
			check(nested[3]?.decodeString() == "Potato")
			check(nested[4]?.decodeBoolean() == false)
			check(nested[5]?.decodeDouble() == 129879.0)
			check(nested[6]?.decodeDocument()?.isEmpty() == true)

			check(document.asValue().decodeDocument() == document) { "Round-trip through BsonValue" }
		}
	}
}
