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

package opensavvy.ktmongo.coroutines

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.FilterOperators
import opensavvy.ktmongo.dsl.expr.UpdateOperators
import opensavvy.ktmongo.dsl.expr.UpsertOperators
import opensavvy.ktmongo.dsl.options.CountOptions
import opensavvy.ktmongo.dsl.options.FindOptions

private class FilteredCollection<Document : Any>(
	private val upstream: MongoCollection<Document>,
	private val globalFilter: FilterOperators<Document>.() -> Unit,
) : MongoCollection<Document> {

	override fun find(): MongoIterable<Document> =
		upstream.find(predicate = globalFilter)

	override fun find(
		options: FindOptions<Document>.() -> Unit,
		predicate: FilterOperators<Document>.() -> Unit,
	): MongoIterable<Document> =
		upstream.find(options) {
			globalFilter()
			predicate()
		}

	@LowLevelApi
	override val context: BsonContext
		get() = upstream.context

	override suspend fun count(): Long =
		upstream.count(predicate = globalFilter)

	override suspend fun count(
		options: CountOptions<Document>.() -> Unit,
		predicate: FilterOperators<Document>.() -> Unit,
	): Long =
		upstream.count(
			options = options,
			predicate = {
				globalFilter()
				predicate()
			}
		)

	override suspend fun countEstimated(): Long =
		count()

	override suspend fun updateMany(filter: FilterOperators<Document>.() -> Unit, update: UpdateOperators<Document>.() -> Unit) =
		upstream.updateMany(
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun updateOne(filter: FilterOperators<Document>.() -> Unit, update: UpdateOperators<Document>.() -> Unit) =
		upstream.updateOne(
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun upsertOne(filter: FilterOperators<Document>.() -> Unit, update: UpsertOperators<Document>.() -> Unit) =
		upstream.upsertOne(
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun findOneAndUpdate(filter: FilterOperators<Document>.() -> Unit, update: UpdateOperators<Document>.() -> Unit): Document? =
		upstream.findOneAndUpdate(
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)
}

/**
 * Returns a filtered collection that only contains the elements that match [filter].
 *
 * This function creates a logical view of the collection: by itself, this function does nothing, and MongoDB is never
 * aware of the existence of this logical view. However, operations invoked on the returned collection will only affect
 * elements from the original that match the [filter].
 *
 * Unlike actual MongoDB views, which are read-only, collections returned by this function can also be used for write operations.
 *
 * ### Example
 *
 * A typical usage of this function is to reuse filters for multiple operations.
 * For example, if you have a concept of logical deletion, this function can be used to hide deleted values.
 *
 * ```kotlin
 * class Order(
 *     val id: String,
 *     val date: Instant,
 *     val deleted: Boolean,
 * )
 *
 * val allOrders = database.getCollection<Order>("orders").asKtMongo()
 * val activeOrders = allOrders.filter { Order::deleted ne true }
 *
 * allOrders.find()    // Returns all orders, deleted or not
 * activeOrders.find() // Only returns orders that are not logically deleted
 * ```
 */
fun <Document : Any> MongoCollection<Document>.filter(filter: FilterOperators<Document>.() -> Unit): MongoCollection<Document> =
	FilteredCollection(this, filter)
