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

@file:OptIn(LowLevelApi::class, ExperimentalCoroutinesApi::class, ExperimentalTime::class, ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.tests.api.operations

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.ExperimentalBsonPathApi
import opensavvy.ktmongo.bson.selectFirst
import opensavvy.ktmongo.bson.types.InstantAsBsonDatetimeSerializer
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.CollectionInfo
import opensavvy.ktmongo.dsl.options.TimeSeriesDsl
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.now
import opensavvy.prepared.suite.time
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class CollectionOperationsUser @OptIn(ExperimentalTime::class) constructor(
	val _id: ObjectId,
	val name: String,
	val birthdate: @Serializable(with = InstantAsBsonDatetimeSerializer::class) Instant,
)

@Serializable
data class CollectionOperationsAuditLog(
	val timestamp: @Serializable(with = InstantAsBsonDatetimeSerializer::class) Instant,
	val user: ObjectId,
	val action: String,
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

	test("Get infos about a collection that doesn't exist") {
		check(collection().infos() == null)
	}

	test("Get infos about a collection") {
		collection().create { }

		val infos = checkNotNull(collection().infos())
		check(infos.name == collection().name)
		check(infos.type == CollectionInfo.Type.Collection)
		check(infos.options.isEmpty())
		check(infos.uuid != null)
		check(infos.readOnly == false)
		check(infos.idIndex != null)
	}

	test("Get infos about a collection with only the name") {
		collection().create { }

		val infos = checkNotNull(collection().infos { nameOnly(true) })
		check(infos.name == collection().name)
		try {
			check(infos.type == CollectionInfo.Type.Collection)
		} catch (_: UnsupportedOperationException) {
		} // The official driver doesn't return this field
		check(infos.options.isEmpty())
		check(infos.uuid == null)
		check(infos.readOnly == null)
		check(infos.idIndex == null)
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

		val infos = checkNotNull(collection().infos())
		check(infos.options["capped"]?.decodeBoolean() == true)
		check(infos.options["size"]?.decodeInt32() == documentSize * 2)
		check(infos.options["max"] == null)
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

	val auditLog by client.collection<CollectionOperationsAuditLog>("operation-collection-audit-log")

	test("Create a time-series collection") {
		auditLog().create {
			timeSeries {
				timeField(CollectionOperationsAuditLog::timestamp)
				metaField(CollectionOperationsAuditLog::user)
				expiresAfter(30.minutes)
				granularity(TimeSeriesDsl.Granularity.Minutes)
			}
		}

		val users = List(3) { auditLog().newId() }
		val actions = listOf("login", "logout", "failed-login")

		auditLog().insertMany(
			List(100) {
				CollectionOperationsAuditLog(
					timestamp = Clock.System.now(), // purposefully use real time to avoid duplicate documents
					user = users.random(),
					action = actions.random(),
				)
			}
		)

		check(auditLog().count() == 100L)

		val infos = checkNotNull(auditLog().infos())
		check(infos.options.selectFirst<String>("$.timeseries.timeField") == "timestamp")
		check(infos.options.selectFirst<String>("$.timeseries.metaField") == "user")
		check(infos.options.selectFirst<String>("$.timeseries.granularity") == "minutes")
		check(infos.options.selectFirst<Long>("$.expireAfterSeconds") == 1800L)
	}

	test("Create a time-series collection using the new granularity options") {
		auditLog().create {
			timeSeries {
				timeField(CollectionOperationsAuditLog::timestamp)
				metaField(CollectionOperationsAuditLog::user)
				expiresAfter(30.minutes)
				bucketMaxSpan(10.minutes)
			}
		}

		val infos = checkNotNull(auditLog().infos())
		check(infos.options.selectFirst<String>("$.timeseries.timeField") == "timestamp")
		check(infos.options.selectFirst<String>("$.timeseries.metaField") == "user")
		check(infos.options.selectFirst<Int>("$.timeseries.bucketMaxSpanSeconds") == 600)
		check(infos.options.selectFirst<Int>("$.timeseries.bucketRoundingSeconds") == 600)
		check(infos.options.selectFirst<Long>("$.expireAfterSeconds") == 1800L)
	}
}
