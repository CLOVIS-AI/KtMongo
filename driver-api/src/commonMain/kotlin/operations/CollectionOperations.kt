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

package opensavvy.ktmongo.api.operations

import opensavvy.ktmongo.dsl.command.CreateCollectionOptions
import opensavvy.ktmongo.dsl.command.DropOptions

/**
 * Interface grouping MongoDB operations relating to collection administration.
 */
interface CollectionOperations<Document : Any> : BaseOperations {

	/**
	 * Explicitly creates this collection.
	 *
	 * MongoDB will automatically create a collection with default settings on the first write operation.
	 * This method is useful to create a collection with non-default settings.
	 *
	 * If this method is called and the collection already exists with the same options, nothing happens.
	 * If this method is called and the collection already exists with different options, an error is thrown.
	 *
	 * ### Example
	 *
	 * Create a capped collection: a collection with a maximum size on disk:
	 *
	 * ```kotlin
	 * database.collection<Log>("logs")
	 *     .create {
	 *         capped(sizeBytes = 1024 * 1024) // 1 MiB
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Protocol documentation](https://www.mongodb.com/docs/manual/reference/command/create/)
	 * - [`mongosh` documentation](https://www.mongodb.com/docs/manual/reference/method/db.createCollection)
	 */
	suspend fun create(
		options: CreateCollectionOptions<Document>.() -> Unit = {},
	)

	/**
	 * Removes an entire collection from the database.
	 *
	 * All documents within the collection are deleted.
	 *
	 * Indexes attached to this collection are also deleted.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collection.drop()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Protocol documentation](https://www.mongodb.com/docs/manual/reference/command/drop/)
	 * - [`mongosh` documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.drop/)
	 */
	suspend fun drop(
		options: DropOptions<Document>.() -> Unit = {},
	)

}
