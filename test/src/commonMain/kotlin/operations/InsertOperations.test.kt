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
import opensavvy.ktmongo.dsl.command.InsertOne
import opensavvy.ktmongo.dsl.command.errors.MongoWriteException
import opensavvy.ktmongo.dsl.options.WriteConcern
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.config.Ignored
import kotlin.time.Duration.Companion.milliseconds

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

		test("Cannot insert two documents with the same ID") {
			val id = collection().newId()

			val alice = InsertOperationsUser(
				_id = id,
				name = "Alice",
			)

			val bob = InsertOperationsUser(
				_id = id,
				name = "Bob",
			)

			collection().insertOne(alice)

			val e = checkThrows<MongoWriteException> {
				collection().insertOne(bob)
			}
			check((e.command as? InsertOne<*>)?.document == bob)
			check(e.errors.size == 1)
			check(e.errors[0].code == 11000)
			check(e.namespace == collection().fullyQualifiedName)
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

		test("insertMany • Minuscule delay", Ignored) { // TODO
			val users = List(1000) {
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				)
			}

			collection().insertMany(
				documents = users,
				options = {
					maxTime(1.milliseconds)
				}
			)

			// TODO: the MongoDB Java driver does not support this option
			//       https://jira.mongodb.org/browse/JAVA-6301
		}

		test("insertMany • Ordered") {
			val duplicate = collection().newId()

			collection().insertOne(
				InsertOperationsUser(_id = duplicate, name = "Alice")
			)

			// Insert three documents, one of which is a duplicate
			// Because the operations are not ordered, the two others should still be added

			checkThrows<Exception> { // TODO: specify a dedicated KtMongo exception
				collection().insertMany(
					InsertOperationsUser(_id = collection().newId(), name = "Bob"),
					InsertOperationsUser(_id = duplicate, name = "Charlie"),
					InsertOperationsUser(_id = collection().newId(), name = "Deborah"),
					options = { unordered() }
				)
			}

			val expected = setOf("Alice", "Bob", "Deborah")
			check(collection().find().toList().map { it.name }.toSet() == expected)
		}

		test("insertOne • Bypass schema validation") {
			collection().insertOne(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				options = {
					writeConcern(WriteConcern.Majority) // Required by the Java driver
					bypassDocumentValidation()
				}
			)

			// TODO: write a test that can check that the option is correctly applied
		}

		test("insertMany • Bypass schema validation") {
			collection().insertMany(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				options = {
					writeConcern(WriteConcern.Majority) // Required by the Java driver
					bypassDocumentValidation()
				}
			)

			// TODO: write a test that can check that the option is correctly applied
		}

		test("insertOne • Comment") {
			collection().insertOne(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				options = {
					comment("Create user")
				}
			)

			// TODO: write a test that can check that the option is correctly applied
		}

		test("insertMany • Comment") {
			collection().insertMany(
				InsertOperationsUser(
					_id = collection().newId(),
					name = "Bob",
				),
				options = {
					comment("Create user")
				}
			)

			// TODO: write a test that can check that the option is correctly applied
		}
	}
}
