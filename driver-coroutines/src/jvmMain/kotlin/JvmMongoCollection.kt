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

package opensavvy.ktmongo.coroutines

import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.DropCollectionOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import opensavvy.ktmongo.bson.official.JvmBsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.command.*
import opensavvy.ktmongo.dsl.options.WithWriteConcern
import opensavvy.ktmongo.dsl.options.WriteConcernOption
import opensavvy.ktmongo.dsl.options.option
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import opensavvy.ktmongo.official.command.toJava
import opensavvy.ktmongo.official.options.*
import opensavvy.ktmongo.official.options.toJava
import opensavvy.ktmongo.official.toJava
import java.util.concurrent.TimeUnit

/**
 * Implementation of [MongoCollection] based on [MongoDB's MongoCollection][com.mongodb.kotlin.client.coroutine.MongoCollection].
 *
 * To access the inner iterable, see [asKotlinClient].
 *
 * To convert an existing MongoDB iterable into an instance of this class, see [asKtMongo].
 */
class JvmMongoCollection<Document : Any> internal constructor(
	private val inner: com.mongodb.kotlin.client.coroutine.MongoCollection<Document>,
) : MongoCollection<Document> {

	@LowLevelApi
	fun asKotlinClient() = inner

	@LowLevelApi
	override val context: JvmBsonContext
		get() = JvmBsonContext(inner.codecRegistry)

	// region Find

	override fun find(): JvmMongoIterable<Document> =
		JvmMongoIterable(inner.find(), repr = { "$this.find()" })

	@OptIn(LowLevelApi::class)
	override fun find(
		options: FindOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
	): JvmMongoIterable<Document> {
		val model = Find<Document>(context)

		model.options.options()
		model.filter.filter()

		return JvmMongoIterable(
			inner.withReadConcern(model.options.readReadConcern())
				.find(context.buildDocument(model.filter).raw)
				.limit(model.options.readLimit())
				.skip(model.options.readSkip())
				.maxTime(model.options.readMaxTimeMS().toLong(), TimeUnit.MILLISECONDS)
				.sort((model.options.readSortDocument())),
			repr = { "$this.find($model)" }
		)
	}

	// endregion
	// region Count

	override suspend fun count(): Long =
		inner.countDocuments()

	@OptIn(LowLevelApi::class)
	override suspend fun count(
		options: CountOptions<Document>.() -> Unit,
		predicate: FilterQuery<Document>.() -> Unit,
	): Long {
		val model = Count<Document>(context)

		model.options.options()
		model.filter.predicate()

		return inner.countDocuments(
			context.buildDocument(model.filter).raw,
			model.options.toJava()
		)
	}

	override suspend fun countEstimated(): Long =
		inner.estimatedDocumentCount()

	// endregion
	// region Update

	@OptIn(LowLevelApi::class)
	override suspend fun updateMany(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateQuery<Document>.() -> Unit,
	) {
		val model = UpdateMany<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.withWriteConcern(model.options).updateMany(context.buildDocument(model.filter).raw, context.buildDocument(model.update).raw, UpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override suspend fun updateOne(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateQuery<Document>.() -> Unit,
	) {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.withWriteConcern(model.options).updateOne(context.buildDocument(model.filter).raw, context.buildDocument(model.update).raw, UpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override suspend fun upsertOne(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpsertQuery<Document>.() -> Unit,
	) {
		val model = UpsertOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.withWriteConcern(model.options).updateOne(context.buildDocument(model.filter).raw, context.buildDocument(model.update).raw, UpdateOptions().upsert(true))
	}

	@OptIn(LowLevelApi::class)
	override suspend fun findOneAndUpdate(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateQuery<Document>.() -> Unit,
	): Document? {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		return inner.withWriteConcern(model.options).findOneAndUpdate(context.buildDocument(model.filter).raw, context.buildDocument(model.update).raw, FindOneAndUpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override suspend fun bulkWrite(
		options: BulkWriteOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		operations: BulkWrite<Document>.() -> Unit,
	) {
		val model = BulkWrite(context, filter)

		model.options.options()
		model.operations()

		inner.withWriteConcern(model.options).bulkWrite(
			model.operations.map { it.toJava() }.toList(),
			options = com.mongodb.client.model.BulkWriteOptions()
		)
	}

	// endregion
	// region Update with pipeline

	@OptIn(LowLevelApi::class)
	override suspend fun updateManyWithPipeline(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateWithPipelineQuery<Document>.() -> Unit,
	) {
		val model = UpdateManyWithPipeline<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.withWriteConcern(model.options).updateMany(context.buildDocument(model.filter).raw, model.updates.map { it.toJava() }, UpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override suspend fun updateOneWithPipeline(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateWithPipelineQuery<Document>.() -> Unit,
	) {
		val model = UpdateOneWithPipeline<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.withWriteConcern(model.options).updateOne(context.buildDocument(model.filter).raw, model.updates.map { it.toJava() }, UpdateOptions())
	}

	@OptIn(LowLevelApi::class)
	override suspend fun upsertOneWithPipeline(
		options: opensavvy.ktmongo.dsl.command.UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateWithPipelineQuery<Document>.() -> Unit,
	) {
		val model = UpsertOneWithPipeline<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		inner.withWriteConcern(model.options).updateOne(context.buildDocument(model.filter).raw, model.updates.map { it.toJava() }, UpdateOptions().upsert(true))
	}

	// endregion
	// region Insert

	@OptIn(LowLevelApi::class)
	override suspend fun insertOne(document: Document, options: InsertOneOptions<Document>.() -> Unit) {
		val model = InsertOne(context, document)

		model.options.options()

		inner.withWriteConcern(model.options).insertOne(
			model.document,
			com.mongodb.client.model.InsertOneOptions()
		)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) {
		val model = InsertMany(context, documents.toList())

		model.options.options()

		inner.withWriteConcern(model.options).insertMany(
			model.documents,
			com.mongodb.client.model.InsertManyOptions()
		)
	}

	// endregion
	// region Delete

	@OptIn(LowLevelApi::class)
	override suspend fun deleteOne(
		options: DeleteOneOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
	) {
		val model = DeleteOne<Document>(context)

		model.filter.filter()
		model.options.options()

		inner.withWriteConcern(model.options).deleteOne(
			filter = context.buildDocument(model.filter).raw,
			options = DeleteOptions()
		)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun deleteMany(
		options: DeleteManyOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
	) {
		val model = DeleteMany<Document>(context)

		model.filter.filter()
		model.options.options()

		inner.withWriteConcern(model.options).deleteOne(
			filter = context.buildDocument(model.filter).raw,
			options = DeleteOptions()
		)
	}

	// endregion
	// region Collection administration

	@OptIn(LowLevelApi::class)
	override suspend fun drop(options: DropOptions<Document>.() -> Unit) {
		DropOptions<Document>(context).apply(options)

		inner.drop(DropCollectionOptions())
	}

	// endregion
	// region Aggregation

	@OptIn(LowLevelApi::class)
	override fun aggregate(): MongoAggregationPipeline<Document> =
		MongoAggregationPipeline(
			collection = inner.namespace.collectionName,
			context = context,
			chain = PipelineChainLink(context),
			iterableBuilder = { pipeline ->
				val flow = inner.aggregate(
					pipeline.chain.toBsonList().map { it.toJava() }
				)

				object : MongoIterable<Document> {
					override suspend fun firstOrNull(): Document? =
						flow.firstOrNull()

					override suspend fun forEach(action: suspend (Document) -> Unit) =
						flow.collect(action)

					override fun asFlow(): Flow<Document> =
						flow
				}
			}
		)

	// endregion

	override fun toString(): String =
		"MongoCollection(${inner.namespace})"

}

/**
 * Converts a [MongoDB collection][com.mongodb.kotlin.client.coroutine.MongoCollection] into a [KtMongo collection][JvmMongoCollection].
 */
fun <Document : Any> com.mongodb.kotlin.client.coroutine.MongoCollection<Document>.asKtMongo(): JvmMongoCollection<Document> =
	JvmMongoCollection(this)

@LowLevelApi
private fun <Document : Any> com.mongodb.kotlin.client.coroutine.MongoCollection<Document>.withWriteConcern(option: WithWriteConcern): com.mongodb.kotlin.client.coroutine.MongoCollection<Document> {
	val concern = option.option<WriteConcernOption>()?.concern
		?: return this

	return this.withWriteConcern(concern.toJava())
}
