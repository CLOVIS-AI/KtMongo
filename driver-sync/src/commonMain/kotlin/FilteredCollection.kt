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

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.FilterExpression
import opensavvy.ktmongo.dsl.expr.FilterOperators
import opensavvy.ktmongo.dsl.expr.UpdateOperators
import opensavvy.ktmongo.dsl.expr.UpsertOperators
import opensavvy.ktmongo.dsl.expr.common.toBsonDocument
import opensavvy.ktmongo.dsl.models.BulkWrite
import opensavvy.ktmongo.dsl.options.*

private class FilteredCollection<Document : Any>(
	private val upstream: MongoCollection<Document>,
	private val globalFilter: FilterOperators<Document>.() -> Unit,
) : MongoCollection<Document> {

	override fun find(): MongoIterable<Document> =
		upstream.find(filter = globalFilter)

	override fun find(
		options: FindOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
	): MongoIterable<Document> =
		upstream.find(options) {
			globalFilter()
			filter()
		}

	@LowLevelApi
	override val context: BsonContext
		get() = upstream.context

	override fun count(): Long =
		upstream.count(predicate = globalFilter)

	override fun count(
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

	override fun countEstimated(): Long =
		count()

	override fun updateMany(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpdateOperators<Document>.() -> Unit,
	) {
		upstream.updateMany(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)
	}

	override fun updateOne(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpdateOperators<Document>.() -> Unit,
	) {
		upstream.updateOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)
	}

	override fun upsertOne(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpsertOperators<Document>.() -> Unit,
	) {
		upstream.upsertOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)
	}

	override fun findOneAndUpdate(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpdateOperators<Document>.() -> Unit,
	): Document? =
		upstream.findOneAndUpdate(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override fun bulkWrite(
		options: BulkWriteOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		operations: BulkWrite<Document>.() -> Unit
	) = upstream.bulkWrite(
		options = options,
		filter = {
			globalFilter()
			filter()
		},
		operations = operations,
	)

	override fun deleteOne(options: DeleteOneOptions<Document>.() -> Unit, filter: FilterOperators<Document>.() -> Unit) {
		upstream.deleteOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			}
		)
	}

	override fun deleteMany(options: DeleteManyOptions<Document>.() -> Unit, filter: FilterOperators<Document>.() -> Unit) {
		upstream.deleteMany(
			options = options,
			filter = {
				globalFilter()
				filter()
			}
		)
	}

	override fun drop(options: DropOptions<Document>.() -> Unit) {
		deleteMany(
			options = {
			},
			filter = {
				globalFilter()
			}
		)
	}

	override fun insertOne(document: Document, options: InsertOneOptions<Document>.() -> Unit) =
		upstream.insertOne(document, options)

	override fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) =
		upstream.insertMany(documents, options)

	override fun aggregate(): MongoAggregationPipeline<Document> =
		upstream.aggregate().match(globalFilter)

	@OptIn(LowLevelApi::class)
	override fun toString(): String {
		val filter = FilterExpression<Document>(context)
			.apply(globalFilter)
			.toBsonDocument()

		return "$upstream.filter $filter"
	}
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
