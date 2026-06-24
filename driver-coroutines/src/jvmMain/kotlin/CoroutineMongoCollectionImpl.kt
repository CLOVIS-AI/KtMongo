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

import com.mongodb.kotlin.client.coroutine.MongoCollection
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.types.Jvm
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.Count
import opensavvy.ktmongo.dsl.command.CountOptions
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.official.options.toJava
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private class CoroutineMongoCollectionImpl<Document : Any>(
	private val inner: MongoCollection<Document>,
	override val factory: BsonFactory,
	override val propertyNameStrategy: PropertyNameStrategy,
	override val objectIdGenerator: ObjectIdGenerator,
	@property:LowLevelApi
	override val type: KType,
) : CoroutineMongoCollection<Document> {

	override fun asOfficial(): MongoCollection<Document> =
		inner

	override val name: String
		get() = inner.namespace.collectionName

	override val fullyQualifiedName: String
		get() = inner.namespace.fullName

	private inner class CoroutineBsonContext : BsonContext,
		opensavvy.ktmongo.bson.BsonFactory by factory,
		ObjectIdGenerator by objectIdGenerator,
		PropertyNameStrategy by propertyNameStrategy

	@LowLevelApi
	override val context: BsonContext = CoroutineBsonContext()

	// region Count

	override suspend fun count(): Long =
		inner.countDocuments()

	@OptIn(LowLevelApi::class)
	override suspend fun count(
		options: CountOptions<Document>.() -> Unit,
		predicate: FilterQuery<Document>.() -> Unit,
	): Long {
		val model = Count<Document>(context)

		model.options.options()
		model.filter.predicate()

		return inner.countDocuments(
			factory.buildDocument(model.filter).raw,
			model.options.toJava()
		)
	}

	override suspend fun countEstimated(): Long =
		inner.estimatedDocumentCount()

	// endregion

	override fun toString(): String =
		"CoroutineMongoCollection($fullyQualifiedName)"
}

/**
 * Instantiates a KtMongo [CoroutineMongoCollection] using an existing collection from the official Kotlin driver.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *         .asKtMongo()
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun <Document : Any> MongoCollection<Document>.asKtMongo(
	factory: BsonFactory = BsonFactory(this.codecRegistry),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
	type: KType,
): CoroutineMongoCollection<Document> =
	CoroutineMongoCollectionImpl(
		inner = this,
		factory = factory,
		propertyNameStrategy = propertyNameStrategy,
		objectIdGenerator = objectIdGenerator,
		type = type,
	)

/**
 * Instantiates a KtMongo [CoroutineMongoCollection] using an existing collection from the official Kotlin driver.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *         .asKtMongo()
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
inline fun <reified Document : Any> MongoCollection<Document>.asKtMongo(
	factory: BsonFactory = BsonFactory(this.codecRegistry),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
): CoroutineMongoCollection<Document> =
	asKtMongo(
		factory = factory,
		propertyNameStrategy = propertyNameStrategy,
		objectIdGenerator = objectIdGenerator,
		type = typeOf<Document>(),
	)
