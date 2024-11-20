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

package opensavvy.ktmongo.dsl.models

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.FilterOperators
import opensavvy.ktmongo.dsl.expr.UpdateOperators
import opensavvy.ktmongo.dsl.expr.UpsertOperators
import opensavvy.ktmongo.dsl.options.BulkWriteOptions
import opensavvy.ktmongo.dsl.options.UpdateOptions
import opensavvy.ktmongo.dsl.tree.CompoundNode
import opensavvy.ktmongo.dsl.tree.Node
import opensavvy.ktmongo.dsl.tree.acceptAll

sealed interface AvailableInBulkWrite<Document> : Node

/**
 * Performing multiple write operations in a single request.
 *
 * ### Example
 *
 * ```kotlin
 * users.bulkWrite {
 *     updateOne({ User::name eq "foo" }) {
 *         User::age set 18
 *     }
 *
 *     upsertOne({ User::name eq "bob" }) {
 *         User::age setOnInsert 18
 *         User::age inc 1
 *     }
 * }
 * ```
 *
 * ### Filtered writes
 *
 * If we have multiple writes that share a similar filter, we can extract it to be common between them.
 *
 * ```kotlin
 * users.bulkWrite {
 *     updateOne({ User::name eq "foo" }) {
 *         User::age set 18
 *     }
 *
 *     filtered({ User::isAlive eq true }) {
 *         updateMany({ User::name eq "bar" }) {
 *             User::age inc 2
 *         }
 *
 *         updateOne({ User::name eq "baz" }) {
 *             User::age inc 1
 *         }
 *     }
 * }
 * ```
 *
 * To learn more, see [filtered].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/)
 *
 * @see BulkWriteOptions Options
 */
@KtMongoDsl
class BulkWrite<Document> private constructor(
	val context: BsonContext,
	private val globalFilter: FilterOperators<Document>.() -> Unit,
	val options: BulkWriteOptions<Document>,
) : CompoundNode<AvailableInBulkWrite<Document>> {

	private val _operations = ArrayList<AvailableInBulkWrite<Document>>()
	val operations: Sequence<AvailableInBulkWrite<Document>> get() = _operations.asSequence()

	constructor(context: BsonContext, globalFilter: FilterOperators<Document>.() -> Unit) : this(context, globalFilter, BulkWriteOptions(context))

	@LowLevelApi
	@DangerousMongoApi
	override fun accept(node: AvailableInBulkWrite<Document>) {
		_operations += node
	}

	/**
	 * Declares a [filter] that is shared between all children [operations].
	 *
	 * ### Example
	 *
	 * Sometimes, we have multiple operations in a single bulk write that share the same filter.
	 * This method allows to declare it a single time.
	 *
	 * ```kotlin
	 * users.bulkWrite {
	 *     filtered({ User::isAlive eq true }) {
	 *         updateOne { /* … */ }
	 *         updateOne { /* … */ }
	 *     }
	 * }
	 * ```
	 */
	fun filtered(
		filter: FilterOperators<Document>.() -> Unit,
		operations: BulkWrite<Document>.() -> Unit,
	) {
		val parent = this

		val child = BulkWrite<Document>(
			context = context,
			globalFilter = {
				parent.globalFilter(this)
				filter()
			}
		)

		child.operations()

		@OptIn(LowLevelApi::class, DangerousMongoApi::class)
		acceptAll(child.operations.asIterable())
	}

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
	 * collection.bulkWrite {
	 *     updateMany(
	 *         filter = { User::name eq "Patrick" },
	 *         update = {
	 *             User::age set 15
	 *         }
	 *     )
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/#updateone-and-updatemany)
	 *
	 * @see updateOne Update a single document.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun updateMany(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterOperators<Document>.() -> Unit = {},
		update: UpdateOperators<Document>.() -> Unit,
	) {
		val model = UpdateMany<Document>(context)

		model.options.options()
		model.filter.globalFilter()
		model.filter.filter()
		model.update.update()

		accept(model)
	}

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
	 * collection.bulkWrite {
	 *     updateOne(
	 *         filter = { User::name eq "Patrick" },
	 *         update = {
	 *             User::age set 15
	 *         }
	 *     )
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/#updateone-and-updatemany)
	 *
	 * @see updateMany Update multiple documents.
	 * @see upsertOne Create a document if none are found.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun updateOne(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterOperators<Document>.() -> Unit = {},
		update: UpdateOperators<Document>.() -> Unit,
	) {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.globalFilter()
		model.filter.filter()
		model.update.update()

		accept(model)
	}

	/**
	 * Updates a single document that matches [filter] according to [update].
	 *
	 * If multiple documents match [filter], only the first one found is updated.
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
	 * collection.bulkWrite {
	 *     upsertOne(
	 *         filter = { User::name eq "Patrick" },
	 *         update = {
	 *             User::age set 15
	 *         }
	 *     )
	 * }
	 * ```
	 *
	 * If a document exists that has the `name` of "Patrick", its age is set to 15.
	 * If none exist, a document with `name` "Patrick" and `age` 15 is created.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/#updateone-and-updatemany)
	 * - [The behavior of upsert functions](https://www.mongodb.com/docs/manual/reference/method/db.collection.update/#insert-a-new-document-if-no-match-exists--upsert-)
	 *
	 * @see updateOne Do nothing if no matching documents are found.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun upsertOne(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterOperators<Document>.() -> Unit = {},
		update: UpsertOperators<Document>.() -> Unit,
	) {
		val model = UpsertOne<Document>(context)

		model.options.options()
		model.filter.globalFilter()
		model.filter.filter()
		model.update.update()

		accept(model)
	}

}