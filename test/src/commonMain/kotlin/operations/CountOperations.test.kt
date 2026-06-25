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
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.prepared

@Serializable
data class CountOperationsUser(
	val _id: ObjectId,
	val name: String,
)

fun SuiteDsl.verifyCountOperations(
	client: Prepared<MongoClient>,
) = suite("Count operations") {
	val collection by client.collection<CountOperationsUser>("operation-count-users")

	suite("Empty collection") {
		test("Count should be 0") {
			check(collection().count() == 0L)
		}

		test("Count with a predicate should be 0") {
			check(collection().count { CountOperationsUser::name eq "Bob" } == 0L)
		}

		test("Does an element exist, in an empty collection") {
			check(!collection().exists {})
		}

		test("Count (estimated) of an empty collection") {
			check(collection().countEstimated() == 0L)
		}
	}

	suite("Collection with one element") {
		val createBob by prepared {
			collection().insertOne(
				CountOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				)
			)
		}

		test("Count should be 1") {
			createBob()

			check(collection().count() == 1L)
		}

		test("Count with a predicate that includes the field should be 1") {
			createBob()

			check(collection().count { CountOperationsUser::name eq "Bob" } == 1L)
		}

		test("Count with a predicate that doesn't include the field should be 0") {
			createBob()

			check(collection().count { CountOperationsUser::name eq "Alice" } == 0L)
		}

		test("Bob exists") {
			createBob()

			check(collection().exists { CountOperationsUser::name eq "Bob" })
		}

		test("Alice doesn't exist") {
			createBob()

			check(!collection().exists { CountOperationsUser::name eq "Alice" })
		}

		test("Count (estimated) should still be 1") {
			createBob()

			check(collection().countEstimated() == 1L)
		}
	}

	suite("Collection with multiple documents") {
		val createDocuments by prepared {
			collection().insertMany(
				CountOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				CountOperationsUser(
					_id = collection().newId(),
					name = "Alice",
				),
				CountOperationsUser(
					_id = collection().newId(),
					name = "Fred",
				),
			)
		}

		test("Count should be 3") {
			createDocuments()

			check(collection().count() == 3L)
		}

		test("Count should be 3, but with a skip") {
			createDocuments()

			check(collection().count({ skip(1) }) {} == 2L)
		}

		test("Count with a predicate that includes two documents") {
			createDocuments()

			check(collection().count { CountOperationsUser::name gte "Bob" } == 2L)
		}

		test("Count with a predicate that includes two documents, but with a limit") {
			createDocuments()

			check(collection().count({ limit(1) }) { CountOperationsUser::name gte "Bob" } == 1L)
		}

		test("Count with a predicate that doesn't include any document should be 0") {
			createDocuments()

			check(collection().count { CountOperationsUser::name eq "Absent" } == 0L)
		}

		test("Bob exists") {
			createDocuments()

			check(collection().exists { CountOperationsUser::name eq "Bob" })
		}

		test("Absent doesn't exist") {
			createDocuments()

			check(!collection().exists { CountOperationsUser::name eq "Absent" })
		}

		test("Count (estimated) should still be 3") {
			createDocuments()

			check(collection().countEstimated() == 3L)
		}
	}
}
