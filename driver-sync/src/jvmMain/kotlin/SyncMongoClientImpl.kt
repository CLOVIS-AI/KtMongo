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

package opensavvy.ktmongo.sync

import com.mongodb.kotlin.client.MongoClient
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.types.Jvm
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private class SyncMongoClientImpl(
	private val inner: MongoClient,
	override val factory: BsonFactory,
	override val objectIdGenerator: ObjectIdGenerator,
	override val propertyNameStrategy: PropertyNameStrategy,
) : SyncMongoClient {

	override fun asOfficial(): MongoClient =
		inner

	@OptIn(ExperimentalAtomicApi::class)
	override val context: BsonContext by lazy {
		BsonContext(factory, objectIdGenerator, propertyNameStrategy)
	}

	override fun database(
		name: String,
		factory: opensavvy.ktmongo.bson.BsonFactory,
		objectIdGenerator: ObjectIdGenerator,
		propertyNameStrategy: PropertyNameStrategy,
	): SyncMongoDatabase {
		require(factory is BsonFactory) { "The client $this only supports factories of ${BsonFactory::class}, but ${factory::class} was provided: $factory" }
		return inner.getDatabase(name).asKtMongo(factory, propertyNameStrategy, objectIdGenerator)
	}

	override fun close() {
		inner.close()
	}

	override fun toString(): String =
		"SyncMongoClient()"
}

/**
 * Instantiates a KtMongo [SyncMongoClient] using an existing client from the official Kotlin driver.
 *
 * This method allows taking advantage of the full configuration power of the official client.
 *
 * ### Example
 *
 * ```kotlin
 * fun main() = runBlocking {
 *     val client = SyncMongoClient()
 *
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun SyncMongoClient(
	connectionString: String = "mongodb://localhost:27017",
	factory: BsonFactory? = null,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
): SyncMongoClient = MongoClient.create(connectionString)
	.asKtMongo(factory, objectIdGenerator, propertyNameStrategy)

/**
 * Instantiates a KtMongo [SyncMongoClient] using an existing client from the official Kotlin driver.
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
fun MongoClient.asKtMongo(
	factory: BsonFactory? = null,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
): SyncMongoClient =
	SyncMongoClientImpl(
		inner = this,
		factory = factory ?: BsonFactory(this.codecRegistry),
		objectIdGenerator = objectIdGenerator,
		propertyNameStrategy = propertyNameStrategy,
	)
