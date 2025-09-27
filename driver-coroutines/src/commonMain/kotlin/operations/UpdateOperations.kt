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

import opensavvy.ktmongo.coroutines.MongoCollection
import opensavvy.ktmongo.coroutines.filter
import opensavvy.ktmongo.dsl.command.BulkWrite
import opensavvy.ktmongo.dsl.command.BulkWriteOptions
import opensavvy.ktmongo.dsl.command.ReplaceOptions
import opensavvy.ktmongo.dsl.command.UpdateOptions
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery

/**
 * Interface grouping MongoDB operations allowing to update existing information.
 */
interface UpdateOperations<Document : Any> : BaseOperations {

	/**
	 * Updates all documents that match [filter] according to [update].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.updateMany(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     update = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.updateMany {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 *
	 * @param filter Optional filter to select which documents are updated.
	 * If no filter is specified, all documents are updated.
	 * @see updateOne
	 */
	suspend fun updateMany(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpdateQuery<Document>.() -> Unit,
	)

	/**
	 * Updates a single document that matches [filter] according to [update].
	 *
	 * If multiple documents match [filter], only the first one found is updated.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     update = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.updateOne {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateMany Update more than one document.
	 * @see findOneAndUpdate Also returns the result of the update.
	 */
	suspend fun updateOne(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpdateQuery<Document>.() -> Unit,
	)

	/**
	 * Updates a single document that matches [filter] according to [update].
	 *
	 * If multiple documents match [filter], only the first one is updated.
	 *
	 * If no documents match [filter], a new one is created.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.upsertOne(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     update = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * If a document exists that has the `name` of "Patrick", its age is set to 15.
	 * If none exists, a document with `name` "Patrick" and `age` 15 is created.
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.upsertOne {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [The update operation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 * - [The behavior of upsert functions](https://www.mongodb.com/docs/manual/reference/method/db.collection.update/#insert-a-new-document-if-no-match-exists--upsert-)
	 *
	 * @see updateOne
	 */
	suspend fun upsertOne(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpsertQuery<Document>.() -> Unit,
	)

	/**
	 * Replaces a document that matches [filter] by [document].
	 *
	 * If multiple documents match [filter], only the first one found is updated.
	 *
	 * ### Data races
	 *
	 * This operator is often used by first reading a document, processing it, and replacing it.
	 * This can be dangerous in distributed systems because another replica of the server could have updated
	 * the document between the read and the write.
	 *
	 * If this is a concern, it is recommended to use [updateOne] with explicit operators on the data that has changed,
	 * allowing to do the modification in a single operation. Doing the update that way, MongoDB is responsible
	 * for ensuring the read and the write are atomic.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.replaceOne(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     document = User("Bob", 15)
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.replaceOne(User("Patrick", 15))
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateOne Updates an existing document.
	 * @see updateMany Update more than one document.
	 * @see repsertOne Replaces a document, or inserts it if it doesn't exist.
	 * @see findOneAndUpdate Also returns the result of the update.
	 */
	suspend fun replaceOne(
		options: ReplaceOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		document: Document,
	)

	/**
	 * Replaces a document that matches [filter] by [document].
	 *
	 * If multiple documents match [filter], only the first one found is updated.
	 *
	 * If no documents match [filter], [document] is [inserted][InsertOperations.insertOne].
	 *
	 * ### Data races
	 *
	 * This operator is often used by first reading a document, processing it, and replacing it.
	 * This can be dangerous in distributed systems because another replica of the server could have updated
	 * the document between the read and the write.
	 *
	 * If this is a concern, it is recommended to use [updateOne] with explicit operators on the data that has changed,
	 * allowing to do the modification in a single operation. Doing the update that way, MongoDB is responsible
	 * for ensuring the read and the write are atomic.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.repsertOne(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     document = User("Bob", 15)
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.repsertOne(User("Patrick", 15))
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateOne Updates an existing document.
	 * @see replaceOne Replaces an existing document.
	 * @see findOneAndUpdate Also returns the result of the update.
	 */
	suspend fun repsertOne(
		options: ReplaceOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		document: Document,
	)

	/**
	 * Updates one element that matches [filter] according to [update] and returns it, atomically.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.findOneAndUpdate(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     update = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.findOneAndUpdate {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/findAndModify/)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateMany Update more than one document.
	 * @see updateOne Do not return the value.
	 */
	suspend fun findOneAndUpdate(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpdateQuery<Document>.() -> Unit,
	): Document?

	/**
	 * Performs multiple update operations in a single request.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.bulkWrite {
	 *     upsertOne(
	 *         filter = {
	 *             User::name eq "Patrick"
	 *         },
	 *         update = {
	 *             User::age set 15
	 *         }
	 *     )
	 *
	 *     updateMany {
	 *         User::age inc 1
	 *     }
	 * }
	 * ```
	 *
	 * To see which operations are available and their respective syntax, see [BulkWrite].
	 *
	 * ### Using filtered writes
	 *
	 * We can group operations by the filter they apply on:
	 * ```kotlin
	 * collection.bulkWrite {
	 *     filtered(filter = { User::isAlive eq true }) {
	 *         updateOne(…)
	 *         updateOne(…)
	 *         updateMany(…)
	 *     }
	 *
	 *     updateOne(…)
	 * }
	 * ```
	 *
	 * To learn more, see [filtered][BulkWrite.filtered].
	 *
	 * ### Using filtered collections
	 *
	 * If we want all operations to use the same filter, we can declare it before calling
	 * the operation:
	 * ```kotlin
	 * collection.filter {
	 *     User::isAlive eq true
	 * }.bulkWrite {
	 *     updateOne(…)
	 *     updateOne(…)
	 *     updateMany(…)
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite)
	 */
	suspend fun bulkWrite(
		options: BulkWriteOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		operations: BulkWrite<Document>.() -> Unit,
	)


}
