package fr.qsh.ktmongo.sync

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.EstimatedDocumentCountOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.FindIterable
import fr.qsh.ktmongo.dsl.LowLevelApi
import fr.qsh.ktmongo.dsl.expr.FilterExpression
import fr.qsh.ktmongo.dsl.expr.UpdateExpression
import fr.qsh.ktmongo.dsl.expr.common.AbstractCompoundExpression
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import com.mongodb.kotlin.client.MongoCollection as OfficialMongoCollection

class NativeMongoCollection<Document : Any>(
	private val unsafe: OfficialMongoCollection<Document>,
) : MongoCollection<Document> {

	@LowLevelApi
	fun asOfficialMongoCollection() = unsafe

	@OptIn(LowLevelApi::class)
	private fun AbstractCompoundExpression.toBsonDocument(): BsonDocument {
		val bson = BsonDocument()

		BsonDocumentWriter(bson).use { writer ->
			this.writeTo(writer)
		}

		return bson
	}

	@OptIn(LowLevelApi::class)
	private fun AbstractCompoundExpression.toNestedBsonDocument(): BsonDocument {
		val bson = BsonDocument()

		BsonDocumentWriter(bson).use { writer ->
			writer.writeStartDocument()
			this.writeTo(writer)
			writer.writeEndDocument()
		}

		return bson
	}

	// region Find

	override fun find(): FindIterable<Document> =
		when (val session = getCurrentSession()) {
			null -> unsafe.find()
			else -> unsafe.find(session)
		}

	override fun find(predicate: FilterExpression<Document>.() -> Unit): FindIterable<Document> {
		val bson = FilterExpression<Document>(unsafe.codecRegistry)
			.apply(predicate)
			.toBsonDocument()

		return when (val session = getCurrentSession()) {
			null -> unsafe.find(bson)
			else -> unsafe.find(session, bson)
		}
	}

	// endregion
	// region Count

	override fun count(options: CountOptions): Long =
		when (val session = getCurrentSession()) {
			null -> unsafe.countDocuments(options = options)
			else -> unsafe.countDocuments(session, options = options)
		}

	override fun count(
		options: CountOptions,
		predicate: FilterExpression<Document>.() -> Unit,
	): Long {
		val bson = FilterExpression<Document>(unsafe.codecRegistry)
			.apply(predicate)
			.toBsonDocument()

		return when (val session = getCurrentSession()) {
			null -> unsafe.countDocuments(bson, options)
			else -> unsafe.countDocuments(session, bson, options)
		}
	}

	override fun countEstimated(options: EstimatedDocumentCountOptions): Long =
		when (getCurrentSession()) {
			null -> unsafe.estimatedDocumentCount(options)
			else -> countForReal(options) // Downgrade to a regular count
		}

	// endregion
	// region Update

	override fun updateOne(
		options: UpdateOptions,
		filter: FilterExpression<Document>.() -> Unit,
		update: UpdateExpression<Document>.() -> Unit,
	): UpdateResult {
		val filterBson = FilterExpression<Document>(unsafe.codecRegistry)
			.apply(filter)
			.toBsonDocument()

		val updateBson = UpdateExpression<Document>(unsafe.codecRegistry)
			.apply(update)
			.toNestedBsonDocument()

		return when (val session = getCurrentSession()) {
			null -> unsafe.updateOne(filterBson, updateBson, options)
			else -> unsafe.updateOne(session, filterBson, updateBson, options)
		}
	}

	override fun updateMany(
		options: UpdateOptions,
		filter: FilterExpression<Document>.() -> Unit,
		update: UpdateExpression<Document>.() -> Unit,
	): UpdateResult {
		val filterBson = FilterExpression<Document>(unsafe.codecRegistry)
			.apply(filter)
			.toBsonDocument()

		val updateBson = UpdateExpression<Document>(unsafe.codecRegistry)
			.apply(update)
			.toNestedBsonDocument()

		return when (val session = getCurrentSession()) {
			null -> unsafe.updateMany(filterBson, updateBson, options)
			else -> unsafe.updateMany(session, filterBson, updateBson, options)
		}
	}

	override fun findOneAndUpdate(
		options: FindOneAndUpdateOptions,
		filter: FilterExpression<Document>.() -> Unit,
		update: UpdateExpression<Document>.() -> Unit,
	): Document? {
		val filterBson = FilterExpression<Document>(unsafe.codecRegistry)
			.apply(filter)
			.toBsonDocument()

		val updateBson = UpdateExpression<Document>(unsafe.codecRegistry)
			.apply(update)
			.toNestedBsonDocument()

		return when (val session = getCurrentSession()) {
			null -> unsafe.findOneAndUpdate(filterBson, updateBson, options)
			else -> unsafe.findOneAndUpdate(session, filterBson, updateBson, options)
		}
	}

	// endregion
}

/**
 * Converts a MongoDB collection from the Kotlin driver into a KtMongo collection.
 *
 * ### Example
 *
 * ```kotlin
 * val client = MongoClient.create()
 * val database = client.getDatabase("test")
 * val collection = database.getCollection<User>("users").asKtMongo()
 * ```
 */
fun <T : Any> OfficialMongoCollection<T>.asKtMongo() =
	NativeMongoCollection(this)
