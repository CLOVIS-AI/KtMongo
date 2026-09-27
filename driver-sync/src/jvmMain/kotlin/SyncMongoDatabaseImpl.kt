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

import com.mongodb.kotlin.client.MongoDatabase
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.types.Jvm
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.reflect.KType

private class SyncMongoDatabaseImpl(
	private val inner: MongoDatabase,
	override val factory: BsonFactory,
	override val objectIdGenerator: ObjectIdGenerator,
	override val propertyNameStrategy: PropertyNameStrategy,
) : SyncMongoDatabase {

	override fun asOfficial(): MongoDatabase =
		inner

	@OptIn(ExperimentalAtomicApi::class)
	override val context: BsonContext by lazy {
		BsonContext(factory, objectIdGenerator, propertyNameStrategy)
	}

	@LowLevelApi
	@Suppress("UNCHECKED_CAST")
	override fun <Document : Any> collection(name: String, type: KType): SyncMongoCollection<Document> =
		inner.getCollection(name, (type.classifier as KClass<Document>).java)
			.asKtMongo(type = type, database = inner, factory = factory, propertyNameStrategy = propertyNameStrategy, objectIdGenerator = objectIdGenerator)

	override val name: String
		get() = inner.name

	override fun toString(): String =
		"SyncMongoDatabase($name)"
}

/**
 * Instantiates a KtMongo [SyncMongoDatabase] using an existing client from the official Kotlin driver.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *     val database = client.database("my-app")
 *         .asKtMongo()
 *
 *     val users = database.collection<UserDto>("users")
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun MongoDatabase.asKtMongo(
	factory: BsonFactory = BsonFactory(this.codecRegistry),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
): SyncMongoDatabase =
	SyncMongoDatabaseImpl(
		inner = this,
		factory = factory,
		propertyNameStrategy = propertyNameStrategy,
		objectIdGenerator = objectIdGenerator,
	)
