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

@file:OptIn(ExperimentalTime::class, ExperimentalBsonPathApi::class, LowLevelApi::class)

package opensavvy.ktmongo.bson.path

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.prepared
import kotlin.time.ExperimentalTime

@Serializable
data class Profile(
	val name: String,
	val age: Int,
)

@Serializable
enum class Species {
	Goat,
	Cat,
	Bird,
}

@Serializable
data class Pet(
	val name: String,
	val age: Int?,
	val species: Species,
)

@Serializable
data class User(
	val _id: ObjectId,
	val profile: Profile,
	val pets: List<Pet>,
)

@Serializable
data class IntList(val items: List<Int>)

fun SuiteDsl.bsonPathTests(
	context: Prepared<BsonFactory>,
) = suite("BsonPath") {

	val bob = User(
		_id = ObjectId("68f93c04a7b4c7c3fc6bff38"),
		profile = Profile("Bob", 46),
		pets = listOf(
			Pet("Barbie", 3, Species.Goat),
			Pet("Poupette", 1, Species.Bird),
			Pet("Michael", null, Species.Cat),
		)
	)

	val bobDoc by prepared {
		context().write(bob)
	}

	test("Select a simple field") {
		check(bobDoc().select<ObjectId>(BsonPath["_id"]).firstOrNull() == ObjectId("68f93c04a7b4c7c3fc6bff38"))
	}

	test("Select a simple field, with the 'at' syntax") {
		val id: ObjectId = bobDoc() at BsonPath["_id"]
		check(id == ObjectId("68f93c04a7b4c7c3fc6bff38"))
	}

	@Suppress("UNUSED", "UnusedVariable")
	test("Select a simple field that doesn't exist") {
		checkThrows<NoSuchElementException> {
			val age: String = bobDoc() at BsonPath["age"]
		}
	}

	test("Select a nested field") {
		check(bobDoc().select<String>("$.profile.name").firstOrNull() == "Bob")
	}

	test("Select a document") {
		check(bobDoc().select<Profile>("$.profile").firstOrNull() == Profile("Bob", 46))
	}

	test("Select a list") {
		check(bobDoc().select<List<Pet>>("$.pets").first()[0].name == "Barbie")
	}

	test("Select a list item") {
		check(bobDoc().select<Pet>("$.pets[1]").firstOrNull() == Pet("Poupette", 1, Species.Bird))
	}

	test("Select a list item's field") {
		check(bobDoc().select<String>("$.pets[1].name").firstOrNull() == "Poupette")
	}

	test("Select a list item's field from the end") {
		check(bobDoc().select<String>("$.pets[-1].name").firstOrNull() == "Michael")
	}

	test("Select an enum") {
		check(bobDoc().select<Species>("$.pets[1].species").firstOrNull() == Species.Bird)
	}

	test("Select a nullable field") {
		check(bobDoc().select<Int?>("$.pets[1].age").firstOrNull() == 1)
		check(bobDoc().select<Int?>("$.pets[2].age").firstOrNull() == null)
	}

	test("Select all pet names") {
		check(bobDoc().select<String>("$.pets.*.name").toList() == listOf("Barbie", "Poupette", "Michael"))
	}

	suite("Slices") {
		suite("Indices") {
			val sliceDoc by prepared {
				context().write(IntList(listOf(0, 1, 2, 3, 4, 5, 6)))
			}

			test("Default step") {
				check(sliceDoc().select<Int>("$.items[1:3]").toList() == listOf(1, 2))
			}

			test("Slice with no end index") {
				check(sliceDoc().select<Int>("$.items[5:]").toList() == listOf(5, 6))
			}

			test("Slice with step 2") {
				check(sliceDoc().select<Int>("$.items[1:5:2]").toList() == listOf(1, 3))
			}

			test("Slice with negative step") {
				check(sliceDoc().select<Int>("$.items[5:1:-2]").toList() == listOf(5, 3))
			}

			test("Slice in reverse order") {
				check(sliceDoc().select<Int>("$.items[::-1]").toList() == listOf(6, 5, 4, 3, 2, 1, 0))
			}
		}

		test("Simple bounds") {
			check(bobDoc().select<String>("$.pets[1:3].name").toList() == listOf("Poupette", "Michael"))
		}

		test("No bounds") {
			check(bobDoc().select<String>("$.pets[:].name").toList() == listOf("Barbie", "Poupette", "Michael"))
		}

		test("Positive step") {
			check(bobDoc().select<String>("$.pets[::2].name").toList() == listOf("Barbie", "Michael"))
		}

		test("Negative step") {
			check(bobDoc().select<String>("$.pets[::-2].name").toList() == listOf("Michael", "Barbie"))
		}

		test("Reversed") {
			check(bobDoc().select<String>("$.pets[::-1].name").toList() == listOf("Michael", "Poupette", "Barbie"))
		}
	}

	suite("Multi-selector segments") {
		val multiDoc by prepared {
			context().buildDocument {
				writeArray("a") {
					writeString("a")
					writeString("b")
					writeString("c")
					writeString("d")
					writeString("e")
					writeString("f")
					writeString("g")
				}
			}
		}

		test("Multiple indices") {
			check(multiDoc().select<String>("$.a[0, 3]").toList() == listOf("a", "d"))
		}

		test("Slice and index") {
			check(multiDoc().select<String>("$.a[0:2, 5]").toList() == listOf("a", "b", "f"))
		}

		test("Same selector twice") {
			check(multiDoc().select<String>("$.a[0, 0]").toList() == listOf("a", "a"))
		}
	}

	suite("Filters") {
		val filterDoc by prepared {
			context().buildDocument {
				writeDocument("obj") {
					writeString("x", "y")
				}
				writeArray("arr") {
					writeInt32(2)
					writeInt32(3)
				}
				writeBoolean("selected", true)
			}
		}

		test("Test the presence of a field") {
			check(filterDoc().select<Boolean>("$[?@.arr].selected").toList() == listOf(true))
		}

		test("Test the absence of a field") {
			check(filterDoc().select<Boolean>("$[?!@.baz].selected").toList() == listOf(true))
		}

		test("Two missing fields are equal") {
			check(filterDoc().select<Boolean>("$[?(@.absent1 == @.absent2)].selected").toList() == listOf(true))
		}

		test("== implies <=") {
			check(filterDoc().select<Boolean>("$[?(@.absent1 <= @.absent2)].selected").toList() == listOf(true))
		}

		test("A missing field is not equal to a specific value") {
			check(filterDoc().select<Boolean>("$[?(@.absent == 'g')].selected").toList() == listOf<Boolean>())
		}

		test("Two missing fields are equal (negated)") {
			check(filterDoc().select<Boolean>("$[?(@.absent1 != @.absent2)].selected").toList() == listOf<Boolean>())
		}

		test("A missing field is not equal to a specific value (negated)") {
			check(filterDoc().select<Boolean>("$[?@.absent != 'g'].selected").toList() == listOf(true))
		}

		test("Numeric comparison: <=") {
			check(filterDoc().select<Boolean>("$[?1 <= 2].selected").toList() == listOf(true))
		}

		test("Numeric comparison: >") {
			check(filterDoc().select<Boolean>("$[?1 > 2].selected").toList() == listOf<Boolean>())
		}

		test("Negation") {
			check(filterDoc().select<Boolean>("$[?(!false)].selected").toList() == listOf(true))
		}

		test("Double-negation") {
			check(filterDoc().select<Boolean>("$[?!!true].selected").toList() == listOf(true))
		}

		test("Boolean equality") {
			check(filterDoc().select<Boolean>("$[?true == true].selected").toList() == listOf(true))
		}

		test("Boolean inequality") {
			check(filterDoc().select<Boolean>("$[?true != false].selected").toList() == listOf(true))
		}

		test("Integers are not equal to strings") {
			check(filterDoc().select<Boolean>("$[?13 == '13'].selected").toList() == listOf<Boolean>())
		}

		test("String comparison: <=") {
			check(filterDoc().select<Boolean>("$[?'a' <= 'b'].selected").toList() == listOf(true))
		}

		test("String comparison: >") {
			check(filterDoc().select<Boolean>("$[?'a' > 'b'].selected").toList() == listOf<Boolean>())
		}

		test("Type mismatch between objects and arrays (from the root)") {
			check(filterDoc().select<Boolean>("$[?$.obj == $.arr].selected").toList() == listOf<Boolean>())
		}

		test("Type mismatch between objects and arrays (current)") {
			check(filterDoc().select<Boolean>("$[?@.obj == @.arr].selected").toList() == listOf<Boolean>())
		}

		test("Type mismatch between objects and arrays (from the root, negated)") {
			check(filterDoc().select<Boolean>("$[?$.obj != $.arr].selected").toList() == listOf(true))
		}

		test("Type mismatch between objects and arrays (current)") {
			check(filterDoc().select<Boolean>("$[?@.obj != @.arr].selected").toList() == listOf(true))
		}

		test("Object comparison (from the root)") {
			check(filterDoc().select<Boolean>("$[?$.obj == $.obj].selected").toList() == listOf(true))
		}

		test("Object comparison (current)") {
			check(filterDoc().select<Boolean>("$[?@.obj == @.obj].selected").toList() == listOf(true))
		}

		test("Object comparison (from the root, negated)") {
			check(filterDoc().select<Boolean>("$[?$.obj != $.obj].selected").toList() == listOf<Boolean>())
		}

		test("Object comparison (current, negated)") {
			check(filterDoc().select<Boolean>("$[?@.obj != @.obj].selected").toList() == listOf<Boolean>())
		}

		test("Array comparison (from the root)") {
			check(filterDoc().select<Boolean>("$[?$.arr == $.arr].selected").toList() == listOf(true))
		}

		test("Array comparison (current)") {
			check(filterDoc().select<Boolean>("$[?@.arr == @.arr].selected").toList() == listOf(true))
		}

		test("Array comparison (from the root, negated)") {
			check(filterDoc().select<Boolean>("$[?$.arr != $.arr].selected").toList() == listOf<Boolean>())
		}

		test("Array comparison (current, negated)") {
			check(filterDoc().select<Boolean>("$[?@.arr != @.arr].selected").toList() == listOf<Boolean>())
		}

		test("Type mismatch between object and integer (from the root)") {
			check(filterDoc().select<Boolean>("$[?$.obj == 17].selected").toList() == listOf<Boolean>())
		}

		test("Type mismatch between object and integer (current)") {
			check(filterDoc().select<Boolean>("$[?@.obj == 17].selected").toList() == listOf<Boolean>())
		}

		test("Type mismatch between object and integer (from the root, negated)") {
			check(filterDoc().select<Boolean>("$[?$.obj != 17].selected").toList() == listOf(true))
		}

		test("Type mismatch between object and integer (current, negated)") {
			check(filterDoc().select<Boolean>("$[?@.obj != 17].selected").toList() == listOf(true))
		}

		test("Comparing objects and arrays is not allowed: <=") {
			check(filterDoc().select<Boolean>("$[?@.obj <= @.arr].selected").toList() == listOf<Boolean>())
		}

		test("Comparing objects and arrays is not allowed: <") {
			check(filterDoc().select<Boolean>("$[?@.obj < @.arr].selected").toList() == listOf<Boolean>())
		}

		test("== implies <= on objects") {
			check(filterDoc().select<Boolean>("$[?@.obj <= @.obj].selected").toList() == listOf(true))
		}

		test("== implies <= on arrays") {
			check(filterDoc().select<Boolean>("$[?@.arr <= @.arr].selected").toList() == listOf(true))
		}

		test("Arrays do not offer comparison: <=") {
			check(filterDoc().select<Boolean>("$[?1 <= @.arr].selected").toList() == listOf<Boolean>())
		}

		test("Arrays do not offer comparison: <") {
			check(filterDoc().select<Boolean>("$[?1 < @.arr].selected").toList() == listOf<Boolean>())
		}

		test("Arrays do not offer comparison: >=") {
			check(filterDoc().select<Boolean>("$[?1 >= @.arr].selected").toList() == listOf<Boolean>())
		}

		test("Arrays do not offer comparison: >") {
			check(filterDoc().select<Boolean>("$[?1 > @.arr].selected").toList() == listOf<Boolean>())
		}

		test("== implies <= on booleans") {
			check(filterDoc().select<Boolean>("$[?true <= true].selected").toList() == listOf(true))
		}

		test("Booleans do not offer comparison") {
			check(filterDoc().select<Boolean>("$[?true > true].selected").toList() == listOf<Boolean>())
		}
	}

}
