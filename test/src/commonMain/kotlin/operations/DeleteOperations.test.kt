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
data class DeleteOperationsUser(
	val _id: ObjectId,
	val name: String,
)

fun SuiteDsl.verifyDeleteOperations(
	client: Prepared<MongoClient>,
) = suite("Delete operations") {
	val collection by client.collection<DeleteOperationsUser>("operation-delete-users")

	test("Delete one") {
		collection().insertMany(
			DeleteOperationsUser(
				_id = collection().newId(),
				name = "Ali Dantic",
			),
			DeleteOperationsUser(
				_id = collection().newId(),
				name = "Edgard Atoi",
			),
		)

		collection().deleteOne {
			DeleteOperationsUser::name eq "Ali Dantic"
		}
	}

	test("Delete many") {
		collection().insertMany(
			DeleteOperationsUser(
				_id = collection().newId(),
				name = "Ali Dantic",
			),
			DeleteOperationsUser(
				_id = collection().newId(),
				name = "Edgard Atoi",
			),
			DeleteOperationsUser(
				_id = collection().newId(),
				name = "Alain Térieur",
			),
			DeleteOperationsUser(
				_id = collection().newId(),
				name = "Agathe De Blouze",
			),
		)

		collection().deleteOne {
			DeleteOperationsUser::name lte "Az"
		}
	}
}
