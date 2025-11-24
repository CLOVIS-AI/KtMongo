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
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import kotlin.time.Duration.Companion.seconds

@Serializable
data class MapsUser(
	val name: String,
	val grades: Map<String, Int> = emptyMap(),
	val friends: Map<String, MapsUser> = emptyMap(),
)

val MapsTest by preparedSuite(preparedConfig = CoroutineTimeout(30.seconds)) {
	val users by testCollection<MapsUser>("maps")

	suite("Not empty map") {
		val cases = mapOf(
			"Map has two elements" to MapsUser("Bob", grades = mapOf("maths" to 1, "physics" to 2)),
			"Map has one element" to MapsUser("Bob", grades = mapOf("maths" to 1)),
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)
			users().insertOne(MapsUser("Should not be returned"))

			check(users().find { MapsUser::grades.isMapNotEmpty() }.toList() == listOf(user))
		}
	}

	suite("Empty map") {
		val cases = mapOf(
			"Map is not present" to MapsUser("Marcel"),
			"Map is empty" to MapsUser("Marcel", grades = emptyMap()),
		)

		for ((case, user) in cases) test(case) {
			users().insertOne(user)
			users().insertOne(MapsUser("Should not be returned", grades = mapOf("maths" to 1)))

			check(users().find { MapsUser::grades.isMapEmpty() }.toList() == listOf(user))
		}
	}

	test("Position operator: $") {
		val user = MapsUser("Alex", friends = mapOf("alice" to MapsUser("Alice"), "bob" to MapsUser("Bob")))
		users().insertOne(user)
		users().insertOne(MapsUser("Should not be returned", friends = mapOf("arthur" to MapsUser("Arthur"))))

		check(users().find { MapsUser::friends["bob"].exists() }.toList() == listOf(user))
	}

}
