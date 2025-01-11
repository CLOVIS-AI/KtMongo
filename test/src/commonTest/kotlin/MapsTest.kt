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

package opensavvy.ktmongo.sync

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.kotest.PreparedSpec

class MapsTest : PreparedSpec({

	@Serializable
	data class User(
		val name: String,
		val grades: Map<String, Int> = emptyMap(),
		val friends: Map<String, User> = emptyMap(),
	)

	val users by testCollection<User>("maps")

	suite("Not empty map") {
		val cases = mapOf(
			"Map has two elements" to User("Bob", grades = mapOf("maths" to 1, "physics" to 2)),
			"Map has one element" to User("Bob", grades = mapOf("maths" to 1)),
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)
			users().insertOne(User("Should not be returned"))

			check(users().find { User::grades.isMapNotEmpty() }.toList() == listOf(user))
		}
	}

	suite("Empty map") {
		val cases = mapOf(
			"Map is not present" to User("Marcel"),
			"Map is empty" to User("Marcel", grades = emptyMap()),
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)
			users().insertOne(User("Should not be returned", grades = mapOf("maths" to 1)))

			check(users().find { User::grades.isMapEmpty() }.toList() == listOf(user))
		}
	}

	test("Position operator: $") {
		val user = User("Alex", friends = mapOf("alice" to User("Alice"), "bob" to User("Bob")))
		users().insertOne(user)
		users().insertOne(User("Should not be returned", friends = mapOf("arthur" to User("Arthur"))))

		check(users().find { User::friends["bob"].exists() }.toList() == listOf(user))
	}

})
