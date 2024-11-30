/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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
import opensavvy.prepared.runner.kotest.PreparedSpec

class ArraysTest : PreparedSpec({
	@Serializable
	data class User(
		val name: String,
		val grades: List<Int> = emptyList(),
		val friends: List<User> = emptyList(),
	)

	val users by testCollection<User>("arrays")

	suite("Not empty array") {
		val cases = mapOf(
			"Array has two elements" to User("Bob", grades = listOf(1, 2)),
			"Array has one element" to User("Bob", grades = listOf(1))
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)

			check(users().findOne { User::grades.isNotEmpty() } == user)
		}
	}

	suite("Empty array") {
		val cases = mapOf(
			"Array is not present" to User("Marcel"),
			"Array is empty" to User("Marcel", grades = emptyList()),
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)

			check(users().findOne { User::grades.isEmpty() } == user)
		}
	}

})
