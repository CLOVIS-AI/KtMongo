package fr.qsh.ktmongo.sync

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.EstimatedDocumentCountOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.FindIterable
import fr.qsh.ktmongo.dsl.expr.FilterExpression
import fr.qsh.ktmongo.dsl.expr.UpdateExpression

private class FilteredMongoCollection<Document : Any>(
	private val upstream: MongoCollection<Document>,
	private val baseFilter: FilterExpression<Document>.() -> Unit,
) : MongoCollection<Document> {
	override fun find(): FindIterable<Document> = upstream.find(baseFilter)

	override fun count(options: CountOptions): Long = upstream.count(options, baseFilter)

	// countEstimated is a real count when a filter is present, it's slower but at least it won't break the app
	override fun countEstimated(options: EstimatedDocumentCountOptions): Long = upstream.countForReal(
		options,
		baseFilter,
	)

	override fun findOneAndUpdate(
		options: FindOneAndUpdateOptions,
		filter: FilterExpression<Document>.() -> Unit,
		update: UpdateExpression<Document>.() -> Unit
	): Document? =
		upstream.findOneAndUpdate(
			options,
			filter = { baseFilter(); filter() },
			update = update,
		)

	override fun updateOne(
		options: UpdateOptions,
		filter: FilterExpression<Document>.() -> Unit,
		update: UpdateExpression<Document>.() -> Unit,
	): UpdateResult =
		upstream.updateOne(
			options,
			filter = { baseFilter(); filter() },
			update = update,
		)

	override fun updateMany(
		options: UpdateOptions,
		filter: FilterExpression<Document>.() -> Unit,
		update: UpdateExpression<Document>.() -> Unit,
	): UpdateResult =
		upstream.updateMany(
			options,
			filter = { baseFilter(); filter() },
			update = update,
		)

	override fun count(
		options: CountOptions,
		predicate: FilterExpression<Document>.() -> Unit,
	): Long =
		upstream.count(options) {
			baseFilter()
			predicate()
		}

	override fun find(predicate: FilterExpression<Document>.() -> Unit): FindIterable<Document> =
		upstream.find {
			baseFilter()
			predicate()
		}
}

/**
 * Returns a filtered collection that only contains the elements that match [predicate].
 *
 * This function creates a logical view of the collection: by itself, this function does nothing, and MongoDB is never
 * aware of the existence of this logical view. However, operations invoked on the returned collection will only affect
 * elements from the original that match the [predicate].
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
fun <Document : Any> MongoCollection<Document>.filter(predicate: FilterExpression<Document>.() -> Unit): MongoCollection<Document> =
	FilteredMongoCollection(this, predicate)
