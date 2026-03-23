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

@file:OptIn(ExperimentalTime::class, ExperimentalAtomicApi::class)

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

data class User(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

val BasicReadWriteTest by preparedSuite(preparedConfig = CoroutineTimeout(30.seconds)) {
	val id1 = ObjectId("69908384e10bb0a5f7d17c5b")
	val id2 = ObjectId("699083ade6e89e315640258c")
	val id3 = ObjectId("699083c9e493dd6c2e60c664")

	val users by testCollection<User>("basic-users")

	test("Simple insert and read") {
		users().insertOne(User(_id = id1, name = "Bob", age = 18))

		check(User(_id = id1, "Bob", age = 18) in users().find().toList())

		check(users().exists { User::_id eq id1 })
	}

	test("Simple upsert and read") {
		val result = users().upsertOne(
			filter = {
				User::name eq "Foo"
			},
			update = {
				User::name set "Bad"
				User::age setOnInsert 0
				User::_id setOnInsert id1
			}
		)

		check(result.upsertedCount == 1)
		check(result.matchedCount == 0L)
		check(result.modifiedCount == 0L)

		check(User(_id = id1, "Bad", 0) in users().find().toList())
	}

	test("Read ordered by age") {
		val bob = User(_id = id1, name = "Bob", age = 18)
		val alice = User(_id = id2, name = "Alice", age = 19)
		users().insertOne(bob)
		users().insertOne(alice)

		check(listOf(bob, alice) == users().find(options = { sort { ascending(User::age) } }, {}).toList())
		check(listOf(alice, bob) == users().find(options = { sort { descending(User::age) } }, {}).toList())
	}

	test("Paging") {
		val alice = User(_id = id1, name = "Alice", age = 22)
		val bob = User(_id = id2, name = "Bob", age = 18)
		val carol = User(_id = id3, name = "Carol", age = 19)
		users().insertOne(alice)
		users().insertOne(bob)
		users().insertOne(carol)

		check(listOf(alice, bob, carol) == users().find().toList())
		check(listOf(alice, bob) == users().find(options = { limit(2) }, {}).toList())
		check(listOf(bob, carol) == users().find(options = { skip(1) }, {}).toList())
		check(listOf(bob) == users().find(options = { skip(1); limit(1) }, {}).toList())
	}
}
