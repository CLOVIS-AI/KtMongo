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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.api.MongoDatabase
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A grouping of collections with the same theme.
 *
 * The Multiplatform driver provides a coroutine-aware API which works on all supported Kotlin platforms.
 *
 * ### What is a database?
 *
 * [Collections][MultiplatformMongoCollection] are grouped into databases to avoid name collisions.
 * Databases are similar to Kotlin packages.
 * If multiple applications are deployed in the same MongoDB instance in their own database,
 * they can use the same collection names (e.g. `users`) without conflicts.
 *
 * Each database has a [name] that must be unique within a MongoDB deployment.
 *
 * ### Access
 *
 * To obtain a database, see [MultiplatformMongoClient.database].
 *
 * To obtain a collection, see [collection].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/databases-and-collections/)
 */
interface MultiplatformMongoDatabase : MongoDatabase {

	/**
	 * The [MultiplatformMongoClient] which created this database.
	 *
	 * The [MultiplatformMongoClient] instance is responsible for the global configuration.
	 */
	val client: MultiplatformMongoClient

	/**
	 * Creates a [MultiplatformMongoCollection] object.
	 *
	 * This method is purely a client-side operation, it does nothing in the MongoDB server.
	 * In MongoDB, databases and collections are created implicitly on the first insert.
	 *
	 * For an example, see [MultiplatformMongoClient].
	 *
	 * Prefer using the overload that doesn't have a [type] argument.
	 * If [type] is specified, it must match [Document].
	 * Otherwise, the behavior is unspecified.
	 */
	@LowLevelApi
	override fun <Document : Any> collection(name: String, type: KType): MultiplatformMongoCollection<Document>

	/**
	 * Creates a [MultiplatformMongoCollection] object.
	 *
	 * This method is purely a client-side operation, it does nothing in the MongoDB server.
	 * In MongoDB, databases and collections are created implicitly on the first insert.
	 *
	 * For an example, see [MultiplatformMongoClient].
	 */
	@OptIn(LowLevelApi::class)
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	final inline fun <reified Document : Any> collection(name: String): MultiplatformMongoCollection<Document> =
		collection(name, type = typeOf<Document>())

}
