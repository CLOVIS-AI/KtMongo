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

import opensavvy.ktmongo.api.MongoCollection
import opensavvy.ktmongo.api.MongoDatabase
import opensavvy.ktmongo.api.operations.UpdateOperations
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.BsonValue
import opensavvy.ktmongo.dsl.command.UpdateOptions
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery

/**
 * A collection stores related documents together.
 *
 * The Coroutine client provides a coroutine-aware API which internally uses the
 * [official Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
 *
 * Usually, all documents in a collection have the same shape (the same fields).
 * However, heterogeneous structure can be achieved by using:
 * - Kotlin collections, like [List] and [Set], the embed an arbitrary number of items.
 * - Polymorphism, for example with `sealed class`, to have different fields based on a discriminator.
 *
 * To avoid name collisions, collections are grouped into [databases][MongoDatabase].
 *
 * To obtain a collection, see [MongoDatabase.collection].
 *
 * ### Size limit
 *
 * A MongoDB document cannot exceed 16 MiB.
 *
 * You can measure the size of a document with [opensavvy.ktmongo.bson.BsonDocument.toByteArray]
 * followed by [ByteArray.size].
 *
 * The maximum nesting is 100 levels.
 * Each document or array adds a level.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/databases-and-collections/)
 * - [Size limits](https://www.mongodb.com/docs/manual/reference/limits/#bson-documents)
 *
 * @see asKtMongo Convert an existing instance from the official Kotlin driver.
 */
interface CoroutineMongoCollection<Document : Any> : MongoCollection<Document> {

	/**
	 * Obtains the underlying MongoDB collection from the official Kotlin driver.
	 */
	fun asOfficial(): com.mongodb.kotlin.client.coroutine.MongoCollection<Document>

	override val factory: BsonFactory

	/**
	 * The return value of [upsertOne].
	 */
	interface UpsertResult : UpdateOperations.UpsertResult {

		override val upsertedId: BsonValue?
	}

	@IgnorableReturnValue
	override suspend fun upsertOne(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpsertQuery<Document>.() -> Unit,
	): UpsertResult

}
