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

package opensavvy.ktmongo.coroutines

import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.api.operations.UpdateOperations
import opensavvy.ktmongo.bson.official.BsonFactory
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

private class CoroutineFilteredMongoCollectionImpl<Document : Any>(
	private val upstream: CoroutineMongoCollection<Document>,
	private val globalFilter: FilterQuery<Document>.() -> Unit,
) : CoroutineMongoCollection<Document> {

	override val name: String
		get() = upstream.name

	override val fullyQualifiedName: String
		get() = upstream.fullyQualifiedName

	override val propertyNameStrategy: PropertyNameStrategy
		get() = upstream.propertyNameStrategy

	override val objectIdGenerator: ObjectIdGenerator
		get() = upstream.objectIdGenerator

	@LowLevelApi
	override val type: KType
		get() = upstream.type

	override fun filter(filter: FilterQuery<Document>.() -> Unit): CoroutineMongoCollection<Document> =
		upstream.filter {
			globalFilter()
			filter()
		}

	override fun asOfficial(): com.mongodb.kotlin.client.coroutine.MongoCollection<Document> =
		upstream.asOfficial()

	override val factory: BsonFactory
		get() = upstream.factory

	override suspend fun upsertOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpsertQuery<Document>.() -> Unit): CoroutineMongoCollection.UpsertResult =
		upstream.upsertOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update
		)

	override suspend fun upsertOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): CoroutineMongoCollection.UpsertResult =
		upstream.upsertOneWithPipeline(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update
		)

	override fun aggregate(): CoroutineMongoAggregationPipeline<Document> =
		upstream.aggregate()
			.match { globalFilter() }

	@LowLevelApi
	override val context: BsonContext
		get() = upstream.context

	override suspend fun drop(options: DropOptions<Document>.() -> Unit) =
		upstream.drop(options)

	override suspend fun count(): Long =
		upstream.count {
			globalFilter()
		}

	override suspend fun count(options: CountOptions<Document>.() -> Unit, predicate: FilterQuery<Document>.() -> Unit): Long =
		upstream.count(
			options = options,
			predicate = {
				globalFilter()
				predicate()
			}
		)

	override suspend fun countEstimated(): Long =
		count()

	override suspend fun deleteOne(options: DeleteOneOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) =
		upstream.deleteOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			}
		)

	override suspend fun deleteMany(options: DeleteManyOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) =
		upstream.deleteMany(
			options = options,
			filter = {
				globalFilter()
				filter()
			}
		)

	override fun find(): MongoIterable<Document> =
		upstream.find { globalFilter() }

	override fun find(options: FindOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit): MongoIterable<Document> =
		upstream.find(
			options = options,
			filter = {
				globalFilter()
				filter()
			}
		)

	override suspend fun insertOne(document: Document, options: InsertOneOptions<Document>.() -> Unit) =
		upstream.insertOne(
			document = document,
			options = options,
		)

	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) =
		upstream.insertMany(
			documents = documents,
			options = options,
		)

	override suspend fun updateMany(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): UpdateOperations.UpdateResult =
		upstream.updateMany(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun updateOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): UpdateOperations.UpdateResult =
		upstream.updateOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun replaceOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) =
		upstream.replaceOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			document = document,
		)

	override suspend fun repsertOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) =
		upstream.repsertOne(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			document = document,
		)

	override suspend fun findOneAndUpdate(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): Document? =
		upstream.findOneAndUpdate(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun bulkWrite(options: BulkWriteOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, operations: BulkWrite<Document>.() -> Unit) =
		upstream.bulkWrite(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			operations = operations,
		)

	override suspend fun updateManyWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpdateResult =
		upstream.updateManyWithPipeline(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	override suspend fun updateOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpdateResult =
		upstream.updateOneWithPipeline(
			options = options,
			filter = {
				globalFilter()
				filter()
			},
			update = update,
		)

	@OptIn(LowLevelApi::class)
	override fun toString(): String {
		val filter = FilterQuery<Document>(context)
		globalFilter(filter)

		return "$upstream.filter($filter)"
	}
}

internal fun <Document : Any> createFilteredCollection(
	upstream: CoroutineMongoCollection<Document>,
	globalFilter: FilterQuery<Document>.() -> Unit,
): CoroutineMongoCollection<Document> =
	CoroutineFilteredMongoCollectionImpl(upstream, globalFilter)
