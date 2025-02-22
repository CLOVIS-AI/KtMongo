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
import opensavvy.prepared.runner.kotest.PreparedSpec

class BasicReadWriteTest : PreparedSpec({
	@Serializable
	data class User(
		val name: String,
		val age: Int,
	)

	val users by testCollection<User>("basic-users")

	test("Simple insert and read") {
		users().insertOne(User(name = "Bob", age = 18))

		check(User("Bob", age = 18) in users().find().toList())
	}

	test("Simple upsert and read") {
		users().upsertOne(
			filter = {
				User::name eq "Foo"
			},
			update = {
				User::name set "Bad"
				User::age setOnInsert 0
			}
		)

		check(User("Bad", 0) in users().find().toList())
	}

	test("Read ordered by age") {
		val bob = User(name = "Bob", age = 18)
		val alice = User(name = "Alice", age = 19)
		users().insertOne(bob)
		users().insertOne(alice)

		check(listOf(bob, alice) == users().find(options = { sort { ascending(User::age) } }, {}).toList())
		check(listOf(alice, bob) == users().find(options = { sort { descending(User::age) } }, {}).toList())
	}
})
