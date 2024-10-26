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

import opensavvy.ktmongo.coroutines.MongoIterable
import opensavvy.ktmongo.dsl.expr.FilterExpression

/**
 * Interface grouping MongoDB operations allowing to search for information.
 */
interface FindOperations<Document : Any> {

	/**
	 * Finds all documents in this collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.find/)
	 */
	fun find(): MongoIterable<Document>

	/**
	 * Finds all documents in this collection that satisfy [predicate].
	 *
	 * If multiple predicates are specified, an [and][opensavvy.ktmongo.dsl.expr.FilterExpression.and] operator is implied.
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
	fun find(predicate: FilterExpression<Document>.() -> Unit): MongoIterable<Document>

	/**
	 * Finds a document in this collection that satisfies [predicate].
	 *
	 * If multiple predicates are specified, and [and][FilterExpression.and] operator is implied.
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
	suspend fun findOne(predicate: FilterExpression<Document>.() -> Unit): Document? =
		find(predicate).firstOrNull()

}
