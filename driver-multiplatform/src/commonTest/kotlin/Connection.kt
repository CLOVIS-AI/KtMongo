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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.multiplatform.utils.MongoClient
import opensavvy.prepared.runner.testballoon.preparedSuite

val MultiplatformConnection by preparedSuite {

	test("Connect") {
		val client = MongoClient()

		println(client.context)
	}

	test("Instantiate database") {
		val client = MongoClient()

		// This method returns a MongoDatabase.
		// It doesn't create the database in MongoDB, that will happen during the first 'insert'.
		val database = client.database("test1")

		check(database.toString() == "MongoDatabase(test1)")
	}

	test("Instantiate a collection") {
		val client = MongoClient()
		val database = client.database("test1")

		// This method returns a MongoCollection.
		// It doesn't create the collection in MongoDB. That will happen during the first 'insert'.
		val collection = database.collection<String>("test2")
		// 'String' isn't a valid document type, but that only matters for actual requests.

		check(collection.toString() == "MongoCollection(test1.test2)")
	}

}
