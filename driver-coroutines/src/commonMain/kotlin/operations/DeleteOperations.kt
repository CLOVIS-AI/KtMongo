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

import opensavvy.ktmongo.dsl.options.DeleteManyOptions
import opensavvy.ktmongo.dsl.options.DeleteOneOptions
import opensavvy.ktmongo.dsl.query.FilterQuery

/**
 * Interface grouping MongoDB operations relating to deleting documents.
 */
interface DeleteOperations<Document : Any> : BaseOperations {

	/**
	 * Deletes the first document found that matches [filter].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int
	 * )
	 *
	 * collection.deleteOne {
	 *     User::name eq "Bob"
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.deleteOne)
	 */
	suspend fun deleteOne(
		options: DeleteOneOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit,
	)

	/**
	 * Deletes all documents that match [filter].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int
	 * )
	 *
	 * collection.deleteMany {
	 *     User::age lt 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.deleteMany/)
	 */
	suspend fun deleteMany(
		options: DeleteManyOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit,
	)

}
