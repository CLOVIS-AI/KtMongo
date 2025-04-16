/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.coroutines.operations

import opensavvy.ktmongo.coroutines.MongoIterable
import opensavvy.ktmongo.dsl.command.FindOptions
import opensavvy.ktmongo.dsl.query.FilterQuery

/**
 * Interface grouping MongoDB operations allowing to search for information.
 */
interface FindOperations<Document : Any> : BaseOperations {

	/**
	 * Finds all documents in this collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.find/)
	 */
	fun find(): MongoIterable<Document>

	/**
	 * Finds all documents in this collection that satisfy [filter].
	 *
	 * If multiple predicates are specified, an [and][FilterQuery.and] operator is implied.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name eq "foo"
	 *     User::age eq 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.find/)
	 *
	 * @see findOne When only one result is expected.
	 */
	fun find(
		options: FindOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit,
	): MongoIterable<Document>

	/**
	 * Finds a document in this collection that satisfies [filter].
	 *
	 * If multiple predicates are specified, and [and][FilterQuery.and] operator is implied.
	 *
	 * This function doesn't check that there is exactly one value in the collection.
	 * It simply returns the first matching document it finds.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.findOne {
	 *     User::name eq "foo"
	 *     User::age eq 10
	 * }
	 * ```
	 *
	 * @see find When multiple results are expected.
	 */
	suspend fun findOne(
		options: FindOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit,
	): Document? =
		find(options, filter).firstOrNull()

}
