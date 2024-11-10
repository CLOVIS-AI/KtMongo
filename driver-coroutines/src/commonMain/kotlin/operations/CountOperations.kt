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

import opensavvy.ktmongo.dsl.expr.FilterOperators
import opensavvy.ktmongo.dsl.options.CountOptions

/**
 * Interface grouping MongoDB operations relating to counting documents.
 */
interface CountOperations<Document : Any> : BaseOperations {

	/**
	 * Counts how many documents exist in the collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.countDocuments/)
	 *
	 * @see countEstimated Faster alternative when the result doesn't need to be exact.
	 */
	suspend fun count(): Long

	/**
	 * Counts how many documents match [predicate] in the collection.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.count {
	 *     User::name eq "foo"
	 *     User::age eq 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.countDocuments/)
	 */
	suspend fun count(
		options: CountOptions<Document>.() -> Unit = {},
		predicate: FilterOperators<Document>.() -> Unit
	): Long

	/**
	 * Counts all documents in the collection.
	 *
	 * This function reads collection metadata instead of actually counting through all documents.
	 * This makes it much more performant (almost no CPU nor RAM usage), but the count may be slightly out of date.
	 *
	 * In particular, it may become inaccurate when:
	 * - there are orphaned documents in a shared cluster,
	 * - an unclean shutdown happened.
	 *
	 * Views do not possess the required metadata.
	 * When this function is called on a view (either a MongoDB view or a [filter] logical view), a regular [count] is executed instead.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.estimatedDocumentCount/)
	 *
	 * @see count Perform the count for real.
	 */
	suspend fun countEstimated(): Long

}
