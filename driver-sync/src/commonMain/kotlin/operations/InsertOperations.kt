/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.sync.operations

import opensavvy.ktmongo.dsl.options.InsertManyOptions
import opensavvy.ktmongo.dsl.options.InsertOneOptions
import opensavvy.ktmongo.sync.filter

/**
 * Interface grouping MongoDB operations relating to inserting new documents.
 */
interface InsertOperations<Document> : BaseOperations {

	/**
	 * Inserts a [document].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.insertOne(User(name = "Bob", age = 18))
	 * ```
	 *
	 * Note that `insertOne` ignores [filtered collection][opensavvy.ktmongo.sync.MongoCollection.filter].
	 * That is, `insertOne` on a filtered collection behaves exactly the same as the same `insertOne` on the underlying
	 * real collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.insertOne/)
	 *
	 * @see insertMany Insert multiple documents.
	 */
	fun insertOne(
		document: Document,
		options: InsertOneOptions<Document>.() -> Unit = {},
	)

	/**
	 * Inserts multiple [documents] in a single operation.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.insertMany(users)
	 * ```
	 *
	 * Note that `insertOne` ignores [filtered collection][opensavvy.ktmongo.sync.MongoCollection.filter].
	 * That is, `insertOne` on a filtered collection behaves exactly the same as the same `insertOne` on the underlying
	 * real collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/#insertone)
	 *
	 * @see insertOne Insert a single document.
	 */
	fun insertMany(
		documents: Iterable<Document>,
		options: InsertManyOptions<Document>.() -> Unit = {},
	)

	/**
	 * Inserts multiple [documents] in a single operation.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.insertMany(
	 *     User(name = "Bob", age = 18),
	 *     User(name = "Alice", age = 17)
	 * )
	 * ```
	 *
	 * Note that `insertOne` ignores [filtered collection][opensavvy.ktmongo.sync.MongoCollection.filter].
	 * That is, `insertOne` on a filtered collection behaves exactly the same as the same `insertOne` on the underlying
	 * real collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/#insertone)
	 *
	 * @see insertOne Insert a single document.
	 */
	fun insertMany(
		vararg documents: Document,
		options: InsertManyOptions<Document>.() -> Unit = {},
	) {
		insertMany(documents.asList(), options)
	}

}
