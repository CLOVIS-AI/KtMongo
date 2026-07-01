/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.tests.api.operations

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.options.ReadConcern
import opensavvy.ktmongo.dsl.options.ReadPreference
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import kotlin.time.Duration.Companion.seconds

@Serializable
data class FindOperationsUser(
	val _id: ObjectId,
	val name: String,
)

fun SuiteDsl.verifyFindOperations(
	client: Prepared<MongoClient>,
) = suite("Find operations") {
	val collection by client.collection<FindOperationsUser>("operation-find-users")

	test("Find all") {
		collection().insertMany(
			FindOperationsUser(
				_id = collection().newId(),
				name = "Alice",
			),
			FindOperationsUser(
				_id = collection().newId(),
				name = "Bob",
			),
		)

		check(collection().find().toList().isNotEmpty()) { "Expected at least one result, got none" }
	}

	test("Find with filter") {
		collection().insertMany(
			FindOperationsUser(
				_id = collection().newId(),
				name = "Alice",
			),
			FindOperationsUser(
				_id = collection().newId(),
				name = "Bob",
			),
		)

		val results = collection().find {
			FindOperationsUser::name eq "Alice"
		}.toList()

		check(results.size == 1)
		check(results[0].name == "Alice")
	}

	test("FindOne returns match") {
		collection().insertMany(
			FindOperationsUser(
				_id = collection().newId(),
				name = "Alice",
			),
			FindOperationsUser(
				_id = collection().newId(),
				name = "Bob",
			),
		)

		val result = collection().findOne {
			FindOperationsUser::name eq "Bob"
		}

		check(result != null)
		check(result.name == "Bob")
	}

	test("FindOne returns null when no match") {
		val result = collection().findOne {
			FindOperationsUser::name eq "nobody"
		}

		check(result == null)
	}

	suite("Options") {
		test("limit") {
			collection().insertMany(
				FindOperationsUser(_id = collection().newId(), name = "Alice"),
				FindOperationsUser(_id = collection().newId(), name = "Bob"),
				FindOperationsUser(_id = collection().newId(), name = "Carol"),
			)

			val results = collection().find(options = { limit(2) }) {
				FindOperationsUser::name.exists()
			}.toList()

			check(results.size == 2)
		}

		test("skip") {
			collection().insertMany(
				FindOperationsUser(_id = collection().newId(), name = "Alice"),
				FindOperationsUser(_id = collection().newId(), name = "Bob"),
				FindOperationsUser(_id = collection().newId(), name = "Carol"),
			)

			val results = collection().find({
				skip(2)
			}) {
				FindOperationsUser::name.exists()
			}.toList()

			check(results.size == 1)
		}

		test("sort ascending") {
			collection().insertMany(
				FindOperationsUser(_id = collection().newId(), name = "Carol"),
				FindOperationsUser(_id = collection().newId(), name = "Alice"),
				FindOperationsUser(_id = collection().newId(), name = "Bob"),
			)

			val results = collection().find({
				sort {
					ascending(FindOperationsUser::name)
				}
			}) {
				FindOperationsUser::name.exists()
			}.toList()

			check(results.map { it.name } == listOf("Alice", "Bob", "Carol"))
		}

		test("sort descending") {
			collection().insertMany(
				FindOperationsUser(_id = collection().newId(), name = "Carol"),
				FindOperationsUser(_id = collection().newId(), name = "Alice"),
				FindOperationsUser(_id = collection().newId(), name = "Bob"),
			)

			val results = collection().find({
				sort {
					descending(FindOperationsUser::name)
				}
			}) {
				FindOperationsUser::name.exists()
			}.toList()

			check(results.map { it.name } == listOf("Carol", "Bob", "Alice"))
		}

		test("maxTime") {
			val _ = collection().find({
				maxTime(30.seconds)
			}) {
				FindOperationsUser::name.exists()
			}.toList()

			// TODO: write a test that can check that the option is correctly applied
		}

		test("readConcern") {
			val _ = collection().find({
				readConcern(ReadConcern.Local)
			}) {
				FindOperationsUser::name.exists()
			}.toList()

			// TODO: write a test that can check that the option is correctly applied
		}

		test("readPreference") {
			val _ = collection().find({
				readPreference(ReadPreference.Primary)
			}) {
				FindOperationsUser::name.exists()
			}.toList()

			// TODO: write a test that can check that the option is correctly applied
		}
	}
}
