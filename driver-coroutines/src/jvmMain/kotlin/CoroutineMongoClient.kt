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

@file:JvmMultifileClass
@file:JvmName("KtMongo")

package opensavvy.ktmongo.coroutines

import opensavvy.ktmongo.api.MongoClient

/**
 * Entry-point to the KtMongo Coroutines driver.
 *
 * The Coroutine client provides a coroutine-aware API which internally uses the
 * [official Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
 *
 * ### Organizing data
 *
 * Accessing MongoDB data happens in three steps:
 * - [CoroutineMongoClient]: represents the connection to the MongoDB application, handles
 * the lifecycle and the configuration.
 * - [CoroutineMongoDatabase] (accessed with [CoroutineMongoClient.database]): each database groups data together.
 * This allows deploying multiple applications (or the same application multiple times)
 * without name collisions.
 * - [CoroutineMongoCollection] (accessed with [CoroutineMongoDatabase.collection]): each collection stores data together.
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
 *     val client = CoroutineMongoClient("mongodb://localhost:27017")
 *
 *     val database = client.database("my-app")
 *     val users = database.collection<User>("users")
 *
 *     println("The database contains ${users.count()} users.")
 * }
 * ```
 *
 * @see asKtMongo Convert an existing instance from the official Kotlin driver.
 */
interface CoroutineMongoClient : MongoClient {

	/**
	 * Obtains the underlying MongoDB client from the official Kotlin driver.
	 */
	fun asOfficial(): com.mongodb.kotlin.client.coroutine.MongoClient

	override fun database(name: String): CoroutineMongoDatabase
}
