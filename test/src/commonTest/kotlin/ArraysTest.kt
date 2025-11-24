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

package opensavvy.ktmongo.sync

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ArrayUser(
	val name: String,
	val grades: List<Int> = emptyList(),
	val friends: List<ArrayUser> = emptyList(),
)

val ArraysTest by preparedSuite(preparedConfig = CoroutineTimeout(30.seconds)) {
	val users by testCollection<ArrayUser>("arrays")

	suite("Not empty array") {
		val cases = mapOf(
			"Array has two elements" to ArrayUser("Bob", grades = listOf(1, 2)),
			"Array has one element" to ArrayUser("Bob", grades = listOf(1))
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)

			check(users().findOne { ArrayUser::grades.isNotEmpty() } == user)
		}
	}

	suite("Empty array") {
		val cases = mapOf(
			"Array is not present" to ArrayUser("Marcel"),
			"Array is empty" to ArrayUser("Marcel", grades = emptyList()),
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)

			check(users().findOne { ArrayUser::grades.isEmpty() } == user)
		}
	}

	test("Position operator: $") {
		@Serializable
		data class Pet(
			val name: String,
			val age: Int,
		)

		@Serializable
		data class Profile(
			val name: String,
			val pets: List<Pet>,
		)

		val profiles = testCollection<Profile>("arrays-positional")
			.immediate("profiles")

		val initial = listOf(
			Profile("Bob", listOf(Pet("Bobby", 1), Pet("Cacahuète", 10))),
			Profile("Julia", listOf(Pet("Chouquette", 7)))
		)

		profiles.insertMany(initial)

		// Cacahuète got one year older
		val expected = listOf(
			Profile("Bob", listOf(Pet("Bobby", 1), Pet("Cacahuète", 11))),
			Profile("Julia", listOf(Pet("Chouquette", 7)))
		)

		profiles.updateMany(
			filter = {
				Profile::pets.any / Pet::name eq "Cacahuète"
			},
			update = {
				Profile::pets.selected / Pet::age inc 1
			}
		)

		check(expected == profiles.find().toList())
	}

	test("All position operator: $[]") {
		@Serializable
		data class Pet(
			val name: String,
			val age: Int,
		)

		@Serializable
		data class Profile(
			val name: String,
			val pets: List<Pet>,
		)

		val profiles = testCollection<Profile>("arrays-positional")
			.immediate("profiles")

		val initial = listOf(
			Profile("Bob", listOf(Pet("Bobby", 1), Pet("Cacahuète", 10))),
			Profile("Julia", listOf(Pet("Chouquette", 7)))
		)

		profiles.insertMany(initial)

		// All pets get one year older
		val expected = listOf(
			Profile("Bob", listOf(Pet("Bobby", 2), Pet("Cacahuète", 11))),
			Profile("Julia", listOf(Pet("Chouquette", 8)))
		)

		profiles.updateMany {
			Profile::pets.all / Pet::age inc 1
		}

		check(expected == profiles.find().toList())
	}

}
