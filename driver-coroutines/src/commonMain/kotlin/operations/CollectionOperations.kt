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

import opensavvy.ktmongo.dsl.command.DropOptions

/**
 * Interface grouping MongoDB operations relating to collection administration.
 */
interface CollectionOperations<Document : Any> : BaseOperations {

	/**
	 * Removes an entire collection from the database.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collection.drop()
	 * ```
	 *
	 * ### Using with filtered collections
	 *
	 * When using [filtered collections][opensavvy.ktmongo.sync.MongoCollection.filter], all elements matching the filter
	 * are removed. Other documents are not impacted, and the collection is not deleted.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.drop/)
	 */
	suspend fun drop(
		options: DropOptions<Document>.() -> Unit = {}
	)

}
