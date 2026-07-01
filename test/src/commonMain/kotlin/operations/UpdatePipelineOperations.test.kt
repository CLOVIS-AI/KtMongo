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
data class UpdatePipelineOperationsUser(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

fun SuiteDsl.verifyUpdatePipelineOperations(
	client: Prepared<MongoClient>,
) = suite("UpdatePipeline operations") {
	val collection by client.collection<UpdatePipelineOperationsUser>("operation-updatepipeline-users")

	test("updateManyWithPipeline") {
		collection().insertMany(
			UpdatePipelineOperationsUser(
				_id = collection().newId(),
				name = "Alice",
				age = 20,
			),
			UpdatePipelineOperationsUser(
				_id = collection().newId(),
				name = "Bob",
				age = 25,
			),
		)

		val result = collection().updateManyWithPipeline {
			set {
				UpdatePipelineOperationsUser::age set 30
			}
		}

		check(result.acknowledged)
		check(result.matchedCount == 2L)
		check(result.modifiedCount == 2L)
	}

	test("updateOneWithPipeline") {
		collection().insertMany(
			UpdatePipelineOperationsUser(
				_id = collection().newId(),
				name = "Alice",
				age = 20,
			),
			UpdatePipelineOperationsUser(
				_id = collection().newId(),
				name = "Bob",
				age = 25,
			),
		)

		val result = collection().updateOneWithPipeline(
			filter = {
				UpdatePipelineOperationsUser::name eq "Alice"
			},
		) {
			set {
				UpdatePipelineOperationsUser::age set 21
			}
		}

		check(result.acknowledged)
		check(result.matchedCount == 1L)
		check(result.modifiedCount == 1L)
	}

	test("upsertOneWithPipeline") {
		val result = collection().upsertOneWithPipeline(
			filter = {
				UpdatePipelineOperationsUser::name eq "Charlie"
			},
		) {
			set {
				UpdatePipelineOperationsUser::age set 18
			}
		}

		check(result.acknowledged)
		check(result.matchedCount == 0L)
		check(result.modifiedCount == 0L)
		check(result.upsertedCount == 1)
		checkNotNull(result.upsertedId?.decodeObjectId())
	}
}
