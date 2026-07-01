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
import opensavvy.ktmongo.api.MongoDatabase
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A grouping of collections with the same theme.
 *
 * The Coroutine client provides a coroutine-aware API which internally uses the
 * [official Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
 *
 * ### What is a database?
 *
 * [Collections][MongoCollection] are grouped into databases to avoid name collisions.
 * Databases are similar to Kotlin packages.
 * If multiple applications are deployed in the same MongoDB instance in their own database,
 * they can use the same collection names (e.g. `users`) without conflicts.
 *
 * Each database has a [name] that must be unique within a MongoDB deployment.
 *
 * ### Access
 *
 * To obtain a database, see [MongoClient.database].
 *
 * To obtain a collection, see [collection].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/databases-and-collections/)
 *
 * @see asKtMongo Convert an existing instance from the official Kotlin driver.
 */
interface CoroutineMongoDatabase : MongoDatabase {

	/**
	 * Obtains the underlying MongoDB database from the official Kotlin driver.
	 */
	fun asOfficial(): com.mongodb.kotlin.client.coroutine.MongoDatabase

	@LowLevelApi
	override fun <Document : Any> collection(name: String, type: KType): CoroutineMongoCollection<Document>

	/**
	 * Creates a [MongoCollection] object.
	 *
	 * This method is purely a client-side operation, it does nothing in the MongoDB server.
	 * In MongoDB, databases and collections are created implicitly on the first insert.
	 *
	 * For an example, see [MongoClient].
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	final inline fun <reified Document : Any> collection(name: String): CoroutineMongoCollection<Document> =
		collection(name, type = typeOf<Document>())

}
