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

package opensavvy.ktmongo.api

import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A grouping of collections with the same theme.
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
 */
interface MongoDatabase {

	/**
	 * The unique name of this database.
	 *
	 * This name must be unique within a MongoDB deployment.
	 *
	 * - Two databases cannot have a name that only differs by case (e.g. `salesData` and `SalesData` cannot coexist).
	 * - Once a database is created, you must always access it with the same case as when it was created.
	 * The creation of a database happens on the first write operation in one of its collections.
	 * - It is recommended to avoid the following characters: `/\. "$*<>:|?`. Depending on the platform MongoDB is running on, some of them may be forbidden.
	 * - The name cannot be empty.
	 * - The name cannot be longer than 64 bytes.
	 *
	 * ### External resources
	 *
	 * - [Name restrictions](https://www.mongodb.com/docs/manual/reference/limits/?atlas-provider=aws&atlas-class=general#naming-restrictions)
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
	// name: CharSequence instead of String to allow implementations to define a more specific overload
	// to customize the return type
	final inline fun <reified Document : Any> collection(name: CharSequence): MongoCollection<Document> =
		collection(name.toString(), type = typeOf<Document>())

}
