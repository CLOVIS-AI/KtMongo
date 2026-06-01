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

import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A single MongoDB instance can store multiple databases.
 *
 * Each database has a unique [name] and isolates applications to avoid name collisions even
 * if two applications use the same [MongoCollection] name.
 *
 * To obtain a database, see [MongoClient.database].
 *
 * To obtain a collection, see [collection].
 */
interface MongoDatabase {

	/**
	 * The [MongoClient] which created this database.
	 *
	 * The [MongoClient] instance is responsible for the global configuration.
	 */
	val client: MongoClient

	/**
	 * The unique name of this database.
	 */
	val name: String

	/**
	 * Creates a [MongoCollection] object.
	 *
	 * This method is purely a client-side operation, it does nothing in the MongoDB server.
	 * In MongoDB, databases and collections are created implicitly on the first insert.
	 *
	 * For an example, see [MongoClient].
	 *
	 * Prefer using the overload that doesn't have a [type] argument.
	 * If [type] is specified, it must match [Document].
	 * Otherwise, the behavior is unspecified.
	 */
	@LowLevelApi
	fun <Document : Any> collection(name: String, type: KType): MongoCollection<Document>

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
	final inline fun <reified Document : Any> collection(name: String): MongoCollection<Document> =
		collection(name, type = typeOf<Document>())

}
