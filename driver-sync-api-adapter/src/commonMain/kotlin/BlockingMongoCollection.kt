/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.sync.api.blocking

import opensavvy.ktmongo.api.MongoCollection
import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.api.operations.UpdateOperations
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.BsonValue
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.*
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import kotlin.reflect.KType
import opensavvy.ktmongo.sync.api.MongoCollection as SyncMongoCollection

class BlockingMongoCollection<Document : Any>(
	private val inner: SyncMongoCollection<Document>,
) : MongoCollection<Document> {
	override val name: String
		get() = inner.name

	override val fullyQualifiedName: String
		get() = inner.fullyQualifiedName

	override val factory: BsonFactory
		get() = inner.factory

	override val propertyNameStrategy: PropertyNameStrategy
		get() = inner.propertyNameStrategy

	override val objectIdGenerator: ObjectIdGenerator
		get() = inner.objectIdGenerator

	@LowLevelApi
	override val type: KType
		get() = inner.type

	override fun filter(filter: FilterQuery<Document>.() -> Unit): BlockingMongoCollection<Document> =
		BlockingMongoCollection(inner.filter(filter))

	override fun aggregate(): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.aggregate())

	@LowLevelApi
	override val context: BsonContext
		get() = inner.context

	override suspend fun drop(options: DropOptions<Document>.() -> Unit) = wrapBlocking {
		inner.drop(options)
	}

	override suspend fun count(): Long = wrapBlocking {
		inner.count()
	}

	override suspend fun count(options: CountOptions<Document>.() -> Unit, predicate: FilterQuery<Document>.() -> Unit): Long = wrapBlocking {
		inner.count(options, predicate)
	}

	override suspend fun countEstimated(): Long = wrapBlocking {
		inner.countEstimated()
	}

	override suspend fun deleteOne(options: DeleteOneOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) = wrapBlocking {
		inner.deleteOne(options, filter)
	}

	override suspend fun deleteMany(options: DeleteManyOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) = wrapBlocking {
		inner.deleteMany(options, filter)
	}

	override fun find(): MongoIterable<Document> =
		BlockingMongoIterable(inner.find())

	override fun find(options: FindOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit): MongoIterable<Document> =
		BlockingMongoIterable(inner.find(options, filter))

	override suspend fun insertOne(document: Document, options: InsertOneOptions<Document>.() -> Unit) = wrapBlocking {
		inner.insertOne(document, options)
	}

	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) = wrapBlocking {
		inner.insertMany(documents, options)
	}

	override suspend fun updateMany(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): BlockingUpdateResult = wrapBlocking {
		BlockingUpdateResult(inner.updateMany(options, filter, update))
	}

	override suspend fun updateOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): BlockingUpdateResult = wrapBlocking {
		BlockingUpdateResult(inner.updateOne(options, filter, update))
	}

	override suspend fun upsertOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpsertQuery<Document>.() -> Unit): BlockingUpsertResult = wrapBlocking {
		BlockingUpsertResult(inner.upsertOne(options, filter, update))
	}

	class BlockingUpdateResult(
		private val inner: opensavvy.ktmongo.sync.api.operations.UpdateOperations.UpdateResult,
	) : UpdateOperations.UpdateResult {
		override val acknowledged: Boolean
			get() = inner.acknowledged

		override val matchedCount: Long
			get() = inner.matchedCount

		override val modifiedCount: Long
			get() = inner.modifiedCount
	}

	class BlockingUpsertResult(
		private val inner: opensavvy.ktmongo.sync.api.operations.UpdateOperations.UpsertResult,
	) : UpdateOperations.UpsertResult, UpdateOperations.UpdateResult by BlockingUpdateResult(inner) {
		override val upsertedId: BsonValue?
			get() = inner.upsertedId

		override val upsertedCount: Int
			get() = inner.upsertedCount
	}

	override suspend fun replaceOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) = wrapBlocking {
		inner.replaceOne(options, filter, document)
	}

	override suspend fun repsertOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) = wrapBlocking {
		inner.repsertOne(options, filter, document)
	}

	override suspend fun findOneAndUpdate(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): Document? = wrapBlocking {
		inner.findOneAndUpdate(options, filter, update)
	}

	override suspend fun bulkWrite(options: BulkWriteOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, operations: BulkWrite<Document>.() -> Unit) = wrapBlocking {
		inner.bulkWrite(options, filter, operations)
	}

	override suspend fun updateManyWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): BlockingUpdateResult = wrapBlocking {
		BlockingUpdateResult(inner.updateManyWithPipeline(options, filter, update))
	}

	override suspend fun updateOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): BlockingUpdateResult = wrapBlocking {
		BlockingUpdateResult(inner.updateOneWithPipeline(options, filter, update))
	}

	override suspend fun upsertOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): BlockingUpsertResult = wrapBlocking {
		BlockingUpsertResult(inner.upsertOneWithPipeline(options, filter, update))
	}

	override fun toString(): String =
		inner.toString()
}
