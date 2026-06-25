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
import opensavvy.ktmongo.dsl.options.WriteConcern
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@Serializable
data class InsertOperationsUser(
	val _id: ObjectId? = null,
	val name: String,
)

fun SuiteDsl.verifyInsertOperations(
	client: Prepared<MongoClient>,
) = suite("Insert operations") {
	val collection by client.collection<InsertOperationsUser>("operation-insert-users")

	suite("Simple") {
		test("Insert a document") {
			collection().insertOne(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				)
			)
		}

		test("Insert a document without ID") {
			collection().insertOne(
				InsertOperationsUser(
					name = "Bob",
				)
			)
		}

		test("Insert multiple documents at once") {
			collection().insertMany(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Alice",
				),
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				)
			)
		}
	}

	suite("Options") {
		test("insertOne • Write concern") {
			collection().insertOne(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				options = {
					writeConcern(WriteConcern.FireAndForget)
				}
			)

			// TODO: write a test that can check that the option is correctly applied
		}

		test("insertMany • Write concern") {
			collection().insertMany(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				options = {
					writeConcern(WriteConcern.FireAndForget)
				}
			)

			// TODO: write a test that can check that the option is correctly applied
		}
	}
}
