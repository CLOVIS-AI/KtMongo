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
data class UpdateOperationsUser(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

fun SuiteDsl.verifyUpdateOperations(
	client: Prepared<MongoClient>,
) = suite("Update operations") {
	val collection by client.collection<UpdateOperationsUser>("operation-update-users")

	test("Update one") {
		collection().insertMany(
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Ali Dantic",
				age = 30,
			),
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Edgard Atoi",
				age = 25,
			),
		)

		val result = collection().updateOne(
			filter = { UpdateOperationsUser::name eq "Ali Dantic" },
			update = { UpdateOperationsUser::age set 31 },
		)

		check(result.acknowledged)
		check(result.matchedCount == 1L)
		check(result.modifiedCount == 1L)
	}

	test("Update many") {
		collection().insertMany(
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Ali Dantic",
				age = 30,
			),
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Edgard Atoi",
				age = 25,
			),
		)

		val result = collection().updateMany {
			UpdateOperationsUser::age inc 1
		}

		check(result.acknowledged)
		check(result.matchedCount == 2L)
		check(result.modifiedCount == 2L)
	}

	test("Upsert one") {
		val result = collection().upsertOne(
			filter = { UpdateOperationsUser::name eq "Patrick" },
			update = { UpdateOperationsUser::age set 15 },
		)

		check(result.acknowledged)
		check(result.matchedCount == 0L)
		check(result.modifiedCount == 0L)
		check(result.upsertedCount == 1)
		checkNotNull(result.upsertedId?.decodeObjectId())
	}

	test("Replace one") {
		val id = collection().newId()

		collection().insertOne(
			UpdateOperationsUser(
				_id = id,
				name = "Ali Dantic",
				age = 30,
			),
		)

		collection().replaceOne(
			filter = { UpdateOperationsUser::name eq "Ali Dantic" },
			document = UpdateOperationsUser(
				_id = id,
				name = "Patrick",
				age = 31,
			),
		)
	}

	test("Repsert one") {
		collection().repsertOne(
			filter = { UpdateOperationsUser::name eq "Agathe De Blouze" },
			document = UpdateOperationsUser(
				_id = collection().newId(),
				name = "Agathe De Blouze",
				age = 22,
			),
		)
	}

	test("Find one and update") {
		collection().insertOne(
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Alain Térieur",
				age = 40,
			),
		)

		val result = collection().findOneAndUpdate(
			filter = { UpdateOperationsUser::name eq "Alain Térieur" },
			update = { UpdateOperationsUser::age set 41 },
		)

		check(result?.name == "Alain Térieur")
		check(result.age == 40)
	}

	test("Bulk write") {
		collection().insertMany(
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Ali Dantic",
				age = 30,
			),
			UpdateOperationsUser(
				_id = collection().newId(),
				name = "Edgard Atoi",
				age = 25,
			),
		)

		collection().bulkWrite {
			upsertOne(
				filter = { UpdateOperationsUser::name eq "Ali Dantic" },
			) {
				UpdateOperationsUser::age set 15
			}

			updateMany {
				UpdateOperationsUser::age inc 1
			}
		}

		check(collection().findOne { UpdateOperationsUser::name eq "Ali Dantic" }?.age == 16)
		check(collection().findOne { UpdateOperationsUser::name eq "Edgard Atoi" }?.age == 26)
	}
}
