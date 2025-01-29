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

import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.DropCollectionOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.expr.*
import opensavvy.ktmongo.dsl.expr.common.toBsonDocument
import opensavvy.ktmongo.dsl.models.*
import opensavvy.ktmongo.dsl.options.*
import opensavvy.ktmongo.dsl.options.common.LimitOption
import opensavvy.ktmongo.dsl.options.common.option

/**
 * Implementation of [MongoCollection] based on [MongoDB's MongoCollection][com.mongodb.kotlin.client.MongoCollection].
 *
 * To access the inner iterable, see [asKotlinClient].
 *
 * To convert an existing MongoDB iterable into an instance of this class, see [asKtMongo].
 */
class JvmMongoCollection<Document : Any> internal constructor(
	private val inner: com.mongodb.kotlin.client.MongoCollection<Document>,
) : MongoCollection<Document> {

	@LowLevelApi
	fun asKotlinClient() = inner

	@LowLevelApi
	override val context: BsonContext
		get() = BsonContext(inner.codecRegistry)

	// region Find

	override fun find(): JvmMongoIterable<Document> =
		JvmMongoIterable(inner.find())

	@OptIn(LowLevelApi::class)
	override fun find(
		options: FindOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
	): MongoIterable<Document> {
		val model = Find<Document>(context)

		model.options.options()
		model.filter.filter()

		return JvmMongoIterable(
			inner.find(model.filter.toBsonDocument())
				.limit(model.options.option<LimitOption, _>()?.toInt() ?: 0)
		)
	}

	// endregion
	// region Count

	override fun count(): Long =
		inner.countDocuments()

	@OptIn(LowLevelApi::class)
	override fun count(
		options: CountOptions<Document>.() -> Unit,
		predicate: FilterOperators<Document>.() -> Unit,
	): Long {
		val model = Count<Document>(context)

		model.options.options()
		model.filter.predicate()

		return inner.countDocuments(
			model.filter.toBsonDocument(),
			model.options.toJava()
		)
	}

	override fun countEstimated(): Long =
		inner.estimatedDocumentCount()

	// endregion
	// region Update

	@OptIn(LowLevelApi::class)
	override fun updateMany(
		options: opensavvy.ktmongo.dsl.options.UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpdateOperators<Document>.() -> Unit,
	) {
		val model = UpdateMany<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.updateMany(model.filter.toBsonDocument(), model.update.toBsonDocument(), UpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override fun updateOne(
		options: opensavvy.ktmongo.dsl.options.UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpdateOperators<Document>.() -> Unit,
	) {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.updateOne(model.filter.toBsonDocument(), model.update.toBsonDocument(), UpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override fun upsertOne(
		options: opensavvy.ktmongo.dsl.options.UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpsertOperators<Document>.() -> Unit,
	) {
		val model = UpsertOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.updateOne(model.filter.toBsonDocument(), model.update.toBsonDocument(), UpdateOptions().upsert(true))
	}

	@OptIn(LowLevelApi::class)
	override fun findOneAndUpdate(
		options: opensavvy.ktmongo.dsl.options.UpdateOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		update: UpdateOperators<Document>.() -> Unit,
	): Document? {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		return inner.findOneAndUpdate(model.filter.toBsonDocument(), model.update.toBsonDocument(), FindOneAndUpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override fun bulkWrite(
		options: BulkWriteOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
		operations: BulkWrite<Document>.() -> Unit,
	) {
		val model = BulkWrite<Document>(context, filter)

		model.options.options()
		model.operations()

		inner.bulkWrite(
			model.operations.map { it.toJava() }.toList(),
			options = com.mongodb.client.model.BulkWriteOptions()
		)
	}

	// endregion
	// region Insert

	@OptIn(LowLevelApi::class)
	override fun insertOne(document: Document, options: InsertOneOptions<Document>.() -> Unit) {
		val model = InsertOne(context, document)

		model.options.options()

		inner.insertOne(
			model.document,
			com.mongodb.client.model.InsertOneOptions()
		)
	}

	@OptIn(LowLevelApi::class)
	override fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) {
		val model = InsertMany(context, documents.toList())

		model.options.options()

		inner.insertMany(
			model.documents,
			com.mongodb.client.model.InsertManyOptions()
		)
	}

	// endregion
	// region Delete

	@OptIn(LowLevelApi::class)
	override fun deleteOne(
		options: DeleteOneOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
	) {
		DeleteOneOptions<Document>(context).apply(options)
		val filter = FilterExpression<Document>(context).apply(filter)

		inner.deleteOne(
			filter = filter.toBsonDocument(),
			options = DeleteOptions()
		)
	}

	@OptIn(LowLevelApi::class)
	override fun deleteMany(
		options: DeleteManyOptions<Document>.() -> Unit,
		filter: FilterOperators<Document>.() -> Unit,
	) {
		DeleteManyOptions<Document>(context).apply(options)
		val filter = FilterExpression<Document>(context).apply(filter)

		inner.deleteOne(
			filter = filter.toBsonDocument(),
			options = DeleteOptions()
		)
	}

	// endregion
	// region Collection administration

	@OptIn(LowLevelApi::class)
	override fun drop(options: DropOptions<Document>.() -> Unit) {
		DropOptions<Document>(context).apply(options)

		inner.drop(DropCollectionOptions())
	}

	// endregion
	// region Aggregation

	@OptIn(LowLevelApi::class)
	override fun aggregate(): MongoAggregationPipeline<Document> =
		MongoAggregationPipeline<Document>(
			context = context,
			chain = PipelineChainLink(context),
			iterableBuilder = { pipeline ->
				inner.aggregate(
					pipeline.chain.toBsonList()
				).asKtMongo()
			}
		)

	// endregion

	override fun toString(): String =
		"MongoCollection(${inner.namespace})"

}

/**
 * Converts a [MongoDB collection][com.mongodb.kotlin.client.MongoCollection] into a [KtMongo collection][JvmMongoCollection].
 */
fun <Document : Any> com.mongodb.kotlin.client.MongoCollection<Document>.asKtMongo(): JvmMongoCollection<Document> =
	JvmMongoCollection(this)
