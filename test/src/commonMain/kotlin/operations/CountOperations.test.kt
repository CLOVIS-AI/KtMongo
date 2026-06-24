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
}
