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

package opensavvy.ktmongo.bson

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.assertions.log
import opensavvy.prepared.suite.assertions.matches

@Serializable
data class BsonDocumentUser(
	val a: String = "Bob",
	val b: Int,
)

fun SuiteDsl.verifyBsonDocuments(factory: Prepared<BsonFactory>) = suite("BSON documents") {

	test("Decode a simple document") {
		val document = factory().buildDocument {
			writeString("a", "Bob")
			writeInt32("b", 45)
		}

		check(document.decode<BsonDocumentUser>() == BsonDocumentUser("Bob", 45))
	}

	// TODO in https://gitlab.com/opensavvy/ktmongo/-/work_items/119
	//      Bugged in org.bson:bson-kotlin
	// test("Decode a single document with an optional field") {
	// 	val document = factory().buildDocument {
	// 		writeInt32("b", 45)
	// 	}
	//
	// 	check(document.decode<BsonDocumentUser>() == BsonDocumentUser("Bob", 45))
	// }

	test("Decode a single document with a missing mandatory field") {
		val document = factory().buildDocument {
			writeString("a", "Bob")
		}

		val exception = checkThrows<BsonDecodingException> {
			document.decode<BsonDocumentUser>()
		}

		check(exception.message matches "Could not decode opensavvy.ktmongo.bson.BsonDocumentUser.*\n\tfrom value \\{\"a\": \"Bob\"\\}.*\n*.*")
	}

	suite("Iteration") {
		test("Iterable") {
			val document = factory().buildDocument {
				writeString("a", "Bob")
				writeInt32("b", 45)
			}

			check(document.asIterable().toString() == """{"a": "Bob", "b": 45}""")

			val iter = document.asIterable().iterator()

			check(iter.hasNext())
			val a = log(iter.next())
			check(a.name == "a")
			check(a.value.decodeString() == "Bob")
			check(a.toString() == """(a, "Bob")""")
			check(a.hashCode() == 69972)

			check(iter.hasNext())
			val b = log(iter.next())
			check(b.name == "b")
			check(b.value.decodeInt32() == 45)
			check(b.toString() == """(b, 45)""")
			check(b.hashCode() == 3083)

			check(!iter.hasNext())
			checkThrows<NoSuchElementException> { iter.next() }

			check(a != b)
		}

		test("Iterator") {
			val document = factory().buildDocument {
				writeString("a", "Bob")
				writeInt32("b", 45)
			}

			val iter = document.iterator()

			check(iter.hasNext())
			val a = log(iter.next())
			check(a.name == "a")
			check(a.value.decodeString() == "Bob")
			check(a.toString() == """(a, "Bob")""")
			check(a.hashCode() == 69972)

			check(iter.hasNext())
			val b = log(iter.next())
			check(b.name == "b")
			check(b.value.decodeInt32() == 45)
			check(b.toString() == """(b, 45)""")
			check(b.hashCode() == 3083)

			check(!iter.hasNext())
			checkThrows<NoSuchElementException> { iter.next() }

			check(a != b)
		}

		test("Map") {
			val document = factory().buildDocument {
				writeString("a", "Bob")
				writeInt32("b", 45)
			}

			val other = factory().buildDocument {
				writeInt32("c", 45)
				writeInt32("d", 75)
			}

			val map = document.asMap()

			check(map.toString() == """{"a": "Bob", "b": 45}""")
			check(map.size == 2)
			check(map.isNotEmpty())
			check(map["a"]?.decodeString() == "Bob")
			check(map["b"]?.decodeInt32() == 45)
			check(map["c"] == null)
			check("a" in map)
			check("b" in map)
			check("c" !in map)
			check(map.containsValue(other["c"]))
			check(!map.containsValue(other["d"]))
			check(map.keys == setOf("a", "b"))
			check(map.values.toList() == listOf(map["a"], map["b"]))
			check(map.values.size == 2)
			check(!map.values.isEmpty())
			check(map.values.contains(other["c"]))
			check(!map.values.contains(other["d"]))
			check(map.values.containsAll(listOf(other["c"], map["a"])))
			check(map.entries.contains(map.entries.first()))
			check(!map.entries.containsAll(other.asMap().entries))
			check(map.entries.first() != other.asMap().entries.first())
			check(map == mapOf("a" to map["a"], "b" to map["b"]))
			check(map.hashCode() == 67139)

			val iter = map.iterator()

			check(iter.hasNext())
			val a = log(iter.next())
			check(a.key == "a")
			check(a.value.decodeString() == "Bob")

			check(iter.hasNext())
			val b = log(iter.next())
			check(b.key == "b")
			check(b.value.decodeInt32() == 45)

			check(!iter.hasNext())
			checkThrows<NoSuchElementException> { iter.next() }
		}

		test("Sequence") {
			val document = factory().buildDocument {
				writeString("a", "Bob")
				writeInt32("b", 45)
			}

			val fields = document.asSequence().toList()

			check(fields.size == 2)
			check(fields[0].name == "a")
			check(fields[0].value.decodeString() == "Bob")
			check(fields[1].name == "b")
			check(fields[1].value.decodeInt32() == 45)

			check(fields[0] != fields[1])

			check(document.asSequence().toString() == """{"a": "Bob", "b": 45}""")
		}
	}

	suite("Direct access") {
		test("Small document") {
			val document = factory().buildDocument {
				writeString("a", "Bob")
				writeInt32("b", 45)
			}

			check(document.size == 2)
			check(!document.isEmpty())
			check(document.isNotEmpty())
			check(document.fields == setOf("a", "b"))
			check(document["a"]?.decodeString() == "Bob")
			check(document["b"]?.decodeInt32() == 45)
			check(document["c"] == null)
			check(document.toString() == """{"a": "Bob", "b": 45}""")

			check(document.asValue().decodeDocument() == document) { "Round-trip through BsonValue" }
		}

		test("Large document") {
			val document = factory().buildDocument {
				writeString("a", "Bob")
				writeArray("b") {
					writeInt32(52)
					writeDocument {
						writeString("f", "Fred")
						writeDouble("a", 2.7)
					}
				}
				writeDocument("empty") {}
				writeDocument("full") {
					writeInt32("0", 0)
					writeInt32("1", 1)
					writeInt32("2", 2)
				}
			}

			check(document.size == 4)
			check(!document.isEmpty())
			check(document.isNotEmpty())
			check(document.fields == setOf("a", "b", "empty", "full"))
			check(document["a"]?.decodeString() == "Bob")
			check(document["b"]?.decodeArray()?.get(0)?.decodeInt32() == 52)

			val child = checkNotNull(document["b"]?.decodeArray()?.get(1)?.decodeDocument())
			check(child.size == 2)
			check(!child.isEmpty())
			check(child.isNotEmpty())
			check(child["f"]?.decodeString() == "Fred")
			check(child["a"]?.decodeDouble() == 2.7)
			check(child.fields == setOf("f", "a"))
			check(child.toString() == """{"f": "Fred", "a": 2.7}""")

			val empty = checkNotNull(document["empty"]?.decodeDocument())
			check(empty.isEmpty()) // Check before size to verify behavior before scanning any field
			check(empty.size == 0)
			check(empty.isEmpty()) // Check after size to verify behavior after scanning any field
			check(!empty.isNotEmpty())
			check(empty.fields == emptySet<String>())
			check(empty["a"] == null)
			check(empty.toString() == "{}")

			val full = checkNotNull(document["full"]?.decodeDocument())
			check(full.size == 3)
			check(!full.isEmpty())
			check(full.isNotEmpty())
			check(full.fields == setOf("0", "1", "2"))
			check(full["0"]?.decodeInt32() == 0)
			check(full["1"]?.decodeInt32() == 1)
			check(full["2"]?.decodeInt32() == 2)
			check(full["3"] == null)
			check(full.toString() == """{"0": 0, "1": 1, "2": 2}""")

			check(document.asValue().decodeDocument() == document) { "Round-trip through BsonValue" }
		}
	}

}
