/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.test

import com.mongodb.MongoTimeoutException
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.CoroutineName
import opensavvy.ktmongo.coroutines.MongoCollection
import opensavvy.ktmongo.coroutines.asKtMongo
import opensavvy.prepared.suite.PreparedProvider
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.shared

@PublishedApi
internal val database by shared(CoroutineName("mongodb-establish-connection")) {
	val options = "connectTimeoutMS=3000&serverSelectionTimeoutMS=3000"
	val client = try {
		MongoClient.create("mongodb://localhost:27017/?$options")
			.also { it.getDatabase("ktmongo-sync-tests").getCollection<String>("test").countDocuments() }
	} catch (e: MongoTimeoutException) {
		System.err.println("Cannot connect to localhost:27017. Did you start the docker-compose services? [This is normal in CI]\n${e.stackTraceToString()}")
		MongoClient.create("mongodb://mongo:27017/?$options")
	}
	client.getDatabase("ktmongo-sync-tests")
}

actual inline fun <reified Document : Any> testCollectionExact(name: String): PreparedProvider<MongoCollection<Document>> = prepared(CoroutineName("mongodb-create-collection-$name")) {
	val collection = database().getCollection<Document>(name)
	collection.asKtMongo()
}
