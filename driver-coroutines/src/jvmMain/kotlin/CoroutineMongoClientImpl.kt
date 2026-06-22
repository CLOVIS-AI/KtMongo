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

import com.mongodb.kotlin.client.coroutine.MongoClient

private class CoroutineMongoClientImpl(
	private val inner: MongoClient,
) : CoroutineMongoClient {

	override fun asOfficial(): MongoClient =
		inner

	override fun database(name: String): CoroutineMongoDatabase {
		TODO("Not yet implemented")
	}

	override fun toString(): String =
		"CoroutineMongoClient()"
}

/**
 * Instantiates a KtMongo [CoroutineMongoClient] using an existing client from the official Kotlin driver.
 *
 * This method allows taking advantage of the full configuration power of the official client.
 *
 * ### Example
 *
 * ```kotlin
 * fun main() = runBlocking {
 *     val client = CoroutineMongoClient()
 *
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun CoroutineMongoClient(
	connectionString: String = "mongodb://localhost:27017",
): CoroutineMongoClient =
	CoroutineMongoClientImpl(MongoClient.create(connectionString))

/**
 * Instantiates a KtMongo [CoroutineMongoClient] using an existing client from the official Kotlin driver.
 *
 * This method allows taking advantage of the full configuration power of the official client.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *         .asKtMongo()
 *
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun MongoClient.asKtMongo(): CoroutineMongoClient =
	CoroutineMongoClientImpl(this)
