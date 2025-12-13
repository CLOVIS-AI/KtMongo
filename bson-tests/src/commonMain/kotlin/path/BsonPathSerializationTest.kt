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

@file:OptIn(ExperimentalTime::class, ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.bson.path

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.types.ObjectId
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
		check(bobDoc().select<String>(BsonPath["profile"]["name"]).firstOrNull() == "Bob")
	}

	test("Select a document") {
		check(bobDoc().select<Profile>(BsonPath["profile"]).firstOrNull() == Profile("Bob", 46))
	}

	test("Select a list") {
		check(bobDoc().select<List<Pet>>(BsonPath["pets"]).first()[0].name == "Barbie")
	}

	test("Select a list item") {
		check(bobDoc().select<Pet>(BsonPath["pets"][1]).firstOrNull() == Pet("Poupette", 1, Species.Bird))
	}

	test("Select a list item's field") {
		check(bobDoc().select<String>(BsonPath["pets"][1]["name"]).firstOrNull() == "Poupette")
	}

	test("Select an enum") {
		check(bobDoc().select<Species>(BsonPath["pets"][1]["species"]).firstOrNull() == Species.Bird)
	}

	test("Select a nullable field") {
		check(bobDoc().select<Int?>(BsonPath["pets"][1]["age"]).firstOrNull() == 1)
		check(bobDoc().select<Int?>(BsonPath["pets"][2]["age"]).firstOrNull() == null)
	}

}
