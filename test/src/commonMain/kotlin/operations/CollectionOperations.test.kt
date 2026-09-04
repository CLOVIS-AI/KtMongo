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

@file:OptIn(LowLevelApi::class, ExperimentalCoroutinesApi::class)

package opensavvy.ktmongo.tests.api.operations

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.types.InstantAsBsonDatetimeSerializer
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.now
import opensavvy.prepared.suite.time
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class CollectionOperationsUser @OptIn(ExperimentalTime::class) constructor(
	val _id: ObjectId,
	val name: String,
	val birthdate: @Serializable(with = InstantAsBsonDatetimeSerializer::class) Instant,
)

fun SuiteDsl.verifyCollectionOperations(
	client: Prepared<MongoClient>,
) = suite("Collection operations") {
	val collection by client.collection<CollectionOperationsUser>("operation-collection-users")

	test("Drop an empty collection") {
		collection().drop()
	}

	test("Drop a non-empty collection") {
		collection().insertMany(
			CollectionOperationsUser(
				_id = collection().newId(),
				name = "Alice",
				birthdate = time.now,
			),
			CollectionOperationsUser(
				_id = collection().newId(),
				name = "Bob",
				birthdate = time.now,
			),
		)

		collection().drop()

		check(collection().count() == 0L)
	}

	test("Create a capped collection") {
		val id = collection().newId()

		val documentSize = collection().factory.buildDocument {
			writeSafe("user", CollectionOperationsUser(id, "Alice", time.now))
		}.toByteArray().size

		collection().create {
			// Roughly the size of two users
			// Therefore, if we insert 3, the first one should be removed
			capped(documentSize.toLong() * 2)
		}

		collection().insertOne(
			CollectionOperationsUser(
				_id = collection().newId(),
				name = "Bob",
				birthdate = time.now,
			)
		)

		collection().insertOne(
			CollectionOperationsUser(
				_id = collection().newId(),
				name = "Charlie",
				birthdate = time.now,
			)
		)

		check(collection().count() == 2L)

		collection().insertOne(
			CollectionOperationsUser(
				_id = collection().newId(),
				name = "Deborah",
				birthdate = time.now,
			)
		)

		check(collection().find().toList().map { it.name }.toSet() == setOf("Charlie", "Deborah"))
	}

	test("Create a collection with simple validation") {
		collection().create {
			validator {
				CollectionOperationsUser::birthdate hasType BsonType.Datetime
			}
		}

		// The type is correct, so this should be successful
		collection().insertOne(
			CollectionOperationsUser(
				_id = collection().newId(),
				name = "Alice",
				birthdate = time.now,
			)
		)

		// Create a user where the date is a string, should fail
		checkThrows<Exception> { // TODO specify which exception
			collection()
				.filter {
					CollectionOperationsUser::name eq "Bob"
				}
				.upsertOneWithPipeline {
					set {
						CollectionOperationsUser::birthdate set of(time.now.toString()).unsafeCast()
					}
				}
		}
	}
}
