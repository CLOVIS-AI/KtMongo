/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

import kotlinx.coroutines.Job
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.multiplatform.wire.MongoWireClient
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext

/**
 * Entry-point to the KtMongo Multiplatform driver.
 *
 * The Multiplatform driver provides a coroutine-aware API which works on all supported Kotlin platforms.
 *
 * ### Organizing data
 *
 * Accessing MongoDB data happens in three steps:
 * - [MultiplatformMongoClient]: represents the connection to the MongoDB application, handles
 * the lifecycle and the configuration.
 * - [MultiplatformMongoDatabase] (accessed with [MultiplatformMongoClient.database]): each database groups data together.
 * This allows deploying multiple applications (or the same application multiple times)
 * without name collisions.
 * - [MultiplatformMongoCollection] (accessed with [MultiplatformMongoDatabase.collection]): each collection stores data together.
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
 *     val client = MongoClient(
 *         hostname = "localhost",
 *         port = 27017,
 *         coroutineContext = currentCoroutineContext(),
 *     )
 *
 *     val database = client.database("my-app")
 *     val users = database.collection<User>("users")
 *
 *     println("The database contains ${users.count()} users.")
 * }
 * ```
 */
@OptIn(LowLevelApi::class)
class MultiplatformMongoClient internal constructor(
	internal val wire: MongoWireClient,
	val factory: BsonFactory,
	val context: BsonContext,
) : MongoClient {

	/**
	 * Creates a [MultiplatformMongoDatabase] object.
	 *
	 * This method is purely a client-side operation, it does nothing in the MongoDB server.
	 * In MongoDB, databases and collections are created implicitly on the first insert.
	 *
	 * For an example, see [MultiplatformMongoClient].
	 */
	override fun database(name: String): MultiplatformMongoDatabase =
		MultiplatformMongoDatabaseImpl(this, name)

	override suspend fun close() {
		wire.close()
	}
}

/**
 * Connects to the database at the specified [hostname] and [port].
 *
 * By default, connects to `"mongo://localhost:27017"`.
 *
 * ### Example
 *
 * ```kotlin
 * val job = Job()
 * val client = MongoClient(coroutineContext = job)
 * val collection = client.database("mydb").collection<User>("mycollection")
 *
 * println(collection.count())
 *
 * job.cancel("Shutting down the client")
 * ```
 *
 * @param coroutineContext The coroutine context used to maintain the connection, including background tasks.
 * Specify a custom [Job] to control the lifecycle of the client (call [Job.cancel] to close the client).
 * @param bsonFactory The [BsonFactory] instance used to serialize and deserialize BSON values.
 * Pass a custom instance to configure polymorphic serialization and other matters.
 * @param objectIdGenerator The algorithm used to generate new [ObjectId] instances.
 * @param propertyNameStrategy The algorithm used to convert from the DSL path syntax accesses
 * to MongoDB field paths.
 */
@ExperimentalAtomicApi
@OptIn(LowLevelApi::class)
suspend fun MultiplatformMongoClient(
	hostname: String = "localhost",
	port: Int = 27017,
	coroutineContext: CoroutineContext,
	bsonFactory: BsonFactory = BsonFactory(),
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Default(),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
): MultiplatformMongoClient = MultiplatformMongoClient(
	wire = MongoWireClient(
		hostname,
		port,
		bsonFactory,
		coroutineContext,
	),
	factory = bsonFactory,
	context = BsonContext(bsonFactory, objectIdGenerator, propertyNameStrategy),
)
