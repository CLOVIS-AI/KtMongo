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

package opensavvy.ktmongo.coroutines.operations

import opensavvy.ktmongo.dsl.expr.FilterExpression
import opensavvy.ktmongo.dsl.expr.UpdateExpression

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
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
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
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
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
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
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
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
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
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * If a document exists that has the `name` of "Patrick", its age is set to 15.
	 * If none exists, a document with `name` "Patrick" and `age` 15 is created.
	 *
	 * ### External resources
	 *
	 * - [The update operation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 * - [The behavior of upsert functions](https://www.mongodb.com/docs/manual/reference/method/db.collection.update/#insert-a-new-document-if-no-match-exists--upsert-)
	 *
	 * @see updateOne
	 */
	suspend fun upsertOne(
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
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
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
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
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
	): Document?

}
