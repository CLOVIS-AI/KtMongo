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

package opensavvy.ktmongo.multiplatform.commands

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.multiplatform.utils.MongoClient
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import opensavvy.prepared.suite.random.randomInt
import kotlin.time.Duration.Companion.minutes

@Serializable
private data class User(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

val MultiplatformInsert by preparedSuite(preparedConfig = CoroutineTimeout(15.minutes)) {

	val collectionPostfix by randomInt(0, Int.MAX_VALUE)

	test("Simple insert") {
		val client = MongoClient()
		val database = client.database("ktmongo-test-1")
		val collection = database.collection<User>("users-${collectionPostfix()}")

		collection.insertOne(
			User(
				_id = collection.newId(),
				name = "Patrick",
				age = 42,
			)
		)
	}

}
