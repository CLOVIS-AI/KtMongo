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

	test($$"pull") {
		users().insertOne(
			ArrayUser(
				name = "Bob",
				grades = listOf(17, 8, 9, 3),
				friends = listOf(
					ArrayUser("Fred", grades = listOf(1, 7)),
					ArrayUser("Alice", grades = listOf(18, 16)),
					ArrayUser("Julia", grades = listOf(13)),
				)
			)
		)

		users().updateOne {
			ArrayUser::grades pullValues {
				gt(4)
				lte(10)
			}
		}

		check(users().findOne {}?.grades == listOf(17, 3))

		users().updateOne {
			ArrayUser::grades pull 3
		}

		check(users().findOne {}?.grades == listOf(17))

		users().updateOne {
			ArrayUser::friends pull {
				or {
					ArrayUser::name gte "I"  // Removes Julia
					ArrayUser::grades.any lt 10  // Removes Fred
				}
			}
		}

		check(users().findOne {}?.friends == listOf(ArrayUser("Alice", grades = listOf(18, 16))))
	}

	suite("Array filters") {
		test("On the value itself") {
			users().insertOne(
				ArrayUser("Bob", grades = listOf(1, 2, 3, 4)),
			)

			users().updateOne {
				ArrayUser::grades.filter {
					it gte 3
				} inc 1
			}

			check(users().findOne {} == ArrayUser("Bob", grades = listOf(1, 2, 4, 5)))
		}

		test("On a field of the value") {
			users().insertOne(
				ArrayUser(
					"Bob",
					friends = listOf(
						ArrayUser("Alice", grades = listOf(1)),
						ArrayUser("Nepomucène", grades = listOf(2, 3)),
						ArrayUser("Archibald", grades = listOf(1, 3)),
					)
				),
			)

			users().updateOne {
				ArrayUser::friends.filter {
					(it / ArrayUser::grades).any eq 1
				} / ArrayUser::name set "Found"
			}

			val expected = ArrayUser(
				"Bob",
				friends = listOf(
					ArrayUser("Found", grades = listOf(1)),
					ArrayUser("Nepomucène", grades = listOf(2, 3)),
					ArrayUser("Found", grades = listOf(1, 3)),
				)
			)

			check(users().findOne {} == expected)
		}

		test("Multiple criteria") {
			users().insertOne(
				ArrayUser(
					"Bob",
					friends = listOf(
						ArrayUser("Alice", grades = listOf(1)),
						ArrayUser("Nepomucène", grades = listOf(2, 3)),
						ArrayUser("Archibald", grades = listOf(1, 3)),
					)
				),
			)

			users().updateOne {
				ArrayUser::friends.filter {
					it / ArrayUser::name lt "B" // Select both that start with A
					(it / ArrayUser::grades).any eq 3
				} / ArrayUser::name set "Found"
			}

			val expected = ArrayUser(
				"Bob",
				friends = listOf(
					ArrayUser("Alice", grades = listOf(1)),
					ArrayUser("Nepomucène", grades = listOf(2, 3)),
					ArrayUser("Found", grades = listOf(1, 3)),
				)
			)

			check(users().findOne {} == expected)
		}

		test("Logical or") {
			users().insertOne(
				ArrayUser(
					"Bob",
					friends = listOf(
						ArrayUser("Alice", grades = listOf(1)),
						ArrayUser("Nepomucène", grades = listOf(2, 3)),
						ArrayUser("Archibald", grades = listOf(4, 5)),
					)
				),
			)

			users().updateOne {
				ArrayUser::friends.filter {
					or {
						it / ArrayUser::name eq "Alice"
						(it / ArrayUser::grades).any eq 5
					}
				} / ArrayUser::name set "Found"
			}

			val expected = ArrayUser(
				"Bob",
				friends = listOf(
					ArrayUser("Found", grades = listOf(1)),
					ArrayUser("Nepomucène", grades = listOf(2, 3)),
					ArrayUser("Found", grades = listOf(4, 5)),
				)
			)

			check(users().findOne {} == expected)
		}

		test("Two different filters") {
			users().insertOne(
				ArrayUser(
					"Bob",
					friends = listOf(
						ArrayUser("Alice", friends = listOf(
							ArrayUser("Jean", grades = listOf(1)),
							ArrayUser("Jacques", grades = listOf(3)),
						), grades = listOf(7)),
						ArrayUser("Nepomucène", friends = listOf(
							ArrayUser("Jérard", grades = listOf(2)),
							ArrayUser("Julie", grades = listOf(4)),
						), grades = listOf(2)),
						ArrayUser("Archibald", grades = listOf(1, 3)),
					)
				),
			)

			users().upsertOne {
				val gradesGreaterThanThree = ArrayUser::friends.filter {
					(it / ArrayUser::grades).any gte 3
				}

				val friendGradesLesserThanThree = (gradesGreaterThanThree / ArrayUser::friends).filter {
					(it / ArrayUser::grades).any lt 3
				}

				friendGradesLesserThanThree / ArrayUser::name set "Found"
			}

			val expected = ArrayUser(
				"Bob",
				friends = listOf(
					ArrayUser("Alice", friends = listOf(
						ArrayUser("Found", grades = listOf(1)),
						ArrayUser("Jacques", grades = listOf(3)),
					), grades = listOf(7)),
					ArrayUser("Nepomucène", friends = listOf(
						ArrayUser("Jérard", grades = listOf(2)),
						ArrayUser("Julie", grades = listOf(4)),
					), grades = listOf(2)),
					ArrayUser("Archibald", grades = listOf(1, 3)),
				)
			)

			check(users().findOne {} == expected)
		}

		test("Combine with the all positional operator") {
			users().insertOne(
				ArrayUser(
					"Bob",
					friends = listOf(
						ArrayUser("Alice", friends = listOf(
							ArrayUser("Jean", grades = listOf(1)),
							ArrayUser("Jacques", grades = listOf(3)),
						), grades = listOf(7)),
						ArrayUser("Nepomucène", friends = listOf(
							ArrayUser("Jérard", grades = listOf(2)),
							ArrayUser("Julie", grades = listOf(4)),
						), grades = listOf(2)),
						ArrayUser("Archibald", grades = listOf(1, 3)),
					)
				),
			)

			users().updateMany {
				val allGrades = ArrayUser::friends.all

				val friendGradesLesserThanThree = (allGrades / ArrayUser::friends).filter {
					(it / ArrayUser::grades).any lt 3
				}

				friendGradesLesserThanThree / ArrayUser::name set "Found"
			}

			val expected = ArrayUser(
				"Bob",
				friends = listOf(
					ArrayUser("Alice", friends = listOf(
						ArrayUser("Found", grades = listOf(1)),
						ArrayUser("Jacques", grades = listOf(3)),
					), grades = listOf(7)),
					ArrayUser("Nepomucène", friends = listOf(
						ArrayUser("Found", grades = listOf(2)),
						ArrayUser("Julie", grades = listOf(4)),
					), grades = listOf(2)),
					ArrayUser("Archibald", grades = listOf(1, 3)),
				)
			)

			check(users().findOne {} == expected)
		}
	}

}
