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

package opensavvy.ktmongo.api

/**
 * Entry-point to the KtMongo drivers.
 *
 * This interface exists to unify the API of the different KtMongo drivers.
 * Each implementation may add its own specificities.
 *
 * Each implementation provides its own way to obtain an instance of this interface.
 *
 * ### Organizing data
 *
 * Accessing MongoDB data happens in three steps:
 * - [MongoClient]: represents the connection to the MongoDB application, handles
 * the lifecycle and the configuration.
 * - [MongoDatabase] (accessed with [MongoClient.database]): each database groups data together.
 * This allows deploying multiple applications (or the same application multiple times)
 * without name collisions.
 * - [MongoCollection] (accessed with [MongoDatabase.collection]): each collection stores data together.
 * Documents in a collection may have a different structure.
 *
 * ### Example
 *
 * ```kotlin
 * @Serializable
 * class User(
 *     val _id: ObjectId,
 *     val name: String,
 *     val age: Int,
 * )
 *
 * fun main() = runBlocking {
 *     val client: MongoClient = // Implementation-dependent accessor
 *
 *     val database = client.database("my-app")
 *     val users = database.collection<User>("users")
 *
 *     println("The database contains ${users.count()} users.")
 * }
 * ```
 */
interface MongoClient : AutoCloseable {

	/**
	 * Creates a [MongoDatabase] object.
	 *
	 * This method is purely a client-side operation, it does nothing in the MongoDB server.
	 * In MongoDB, databases and collections are created implicitly on the first insert.
	 *
	 * To learn more about the restrictions on the database [name], see [MongoDatabase.name].
	 *
	 * For an example, see [MongoClient].
	 */
	fun database(name: String): MongoDatabase
}
