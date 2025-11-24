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
import opensavvy.ktmongo.coroutines.filter
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import opensavvy.prepared.suite.prepared
import kotlin.time.Duration.Companion.seconds

val FilteredCollectionTest by preparedSuite(preparedConfig = CoroutineTimeout(30.seconds)) {
	@Serializable
	data class User(
		val name: String = "MISSING",
		val isAlive: Boolean = true,
		val age: Int = 0,
	)

	val users by testCollection<User>("filtered-users")
	val usersAlive by prepared { users().filter { User::isAlive eq true } }

	test("The filter applies to count") {
		users().upsertOne(
			filter = { User::name eq "Joe" }
		) {
			User::isAlive set true
		}

		users().upsertOne(
			filter = { User::name eq "Bob" }
		) {
			User::isAlive set false
		}

		check(users().count() == 2L)
		check(usersAlive().count() == 1L) { "There should only be one user alive" }
	}

	test("The filter applies to find without filters") {
		users().upsertOne(
			filter = { User::name eq "Joe" }
		) {
			User::isAlive set true
		}

		users().upsertOne(
			filter = { User::name eq "Bob" }
		) {
			User::isAlive set false
		}

		check(usersAlive().find().toList().size == 1)
	}

	test("The filter applies to find with filters") {
		users().upsertOne(
			filter = {
				User::name eq "Joe"
				User::age eq 17
			}
		) {
			User::isAlive set true
		}

		users().upsertOne(
			filter = { User::name eq "Fred" }
		) {
			User::isAlive set true
		}

		users().upsertOne(
			filter = { User::name eq "Bob" }
		) {
			User::isAlive set false
		}

		users().upsertOne(
			filter = {
				User::name eq "Joe"
				User::age eq 99
			}
		) {
			User::isAlive set false
		}

		check(usersAlive().find { User::name eq "Joe" }.toList().size == 1) { "There is only one user that is named Joe *and* is alive" }
	}
}
