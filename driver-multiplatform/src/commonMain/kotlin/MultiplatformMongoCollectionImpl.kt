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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.api.firstOrNull
import opensavvy.ktmongo.api.operations.UpdateOperations
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.multiplatform.BsonValue
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.command.*
import opensavvy.ktmongo.dsl.options.LimitOption
import opensavvy.ktmongo.dsl.options.SkipOption
import opensavvy.ktmongo.dsl.options.option
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import opensavvy.ktmongo.multiplatform.wire.Message
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KType

@OptIn(LowLevelApi::class)
internal class MultiplatformMongoCollectionImpl<Document : Any>(
	override val database: MultiplatformMongoDatabase,
	override val name: String,
	override val type: KType,
	override val factory: BsonFactory,
	override val propertyNameStrategy: PropertyNameStrategy,
	override val objectIdGenerator: ObjectIdGenerator,
) : MultiplatformMongoCollection<Document> {

	@OptIn(ExperimentalAtomicApi::class)
	@LowLevelApi
	override val context: BsonContext = BsonContext(
		bsonFactory = factory,
		objectIdGenerator = objectIdGenerator,
		nameStrategy = propertyNameStrategy,
	)

	override suspend fun insertOne(
		document: Document,
		options: InsertOneOptions<Document>.() -> Unit,
	) {
		val message = database.client.wire.sendSingle(
			database.client.createOpMsg {
				document {
					writeString("insert", name)
					writeString($$"$db", database.name)

					InsertOne(
						context = database.client.context,
						document = document,
						documentType = type,
					).apply {
						this.options.options()
					}.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) {
		val message = database.client.wire.sendSingle(
			database.client.createOpMsg {
				document {
					writeString("insert", name)
					writeString($$"$db", database.name)

					InsertMany(
						context = database.client.context,
						documents = documents.toList(),
						documentType = type,
					).apply {
						this.options.options()
					}.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0) { "Message is not OK: $message" }
		check(message.body.document["writeErrors"] == null) { "Write errors occurred: $message" }
	}

	override fun filter(filter: FilterQuery<Document>.() -> Unit): MultiplatformMongoCollection<Document> =
		createFilteredCollection(this, filter)

	override fun aggregate(): MultiplatformMongoAggregationPipeline<Document> =
		MultiplatformMongoAggregationPipelineImpl(this, PipelineChainLink(context))

	override suspend fun create(options: CreateCollectionOptions<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override suspend fun drop(options: DropOptions<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	private fun BsonValue.decodeLong(message: Any?): Long = when (this.type) {
		BsonType.Int32 -> decodeInt32().toLong()
		BsonType.Int64 -> decodeInt64()
		else -> error("Unexpected count type: $type in $message")
	}

	@OptIn(DangerousMongoApi::class)
	override suspend fun count(): Long {
		val result = Field.unsafe<Long>("c")

		val message = this.aggregate()
			.countTo(result)
			.reinterpret<BsonDocument>()
			.firstOrNull()

		return message
			?.get("c")
			?.decodeLong(message)
			?: 0L
	}

	@OptIn(DangerousMongoApi::class)
	override suspend fun count(options: CountOptions<Document>.() -> Unit, predicate: FilterQuery<Document>.() -> Unit): Long {
		val result = Field.unsafe<Long>("c")
		val model = Count<Document>(context).apply {
			this.options.options()
		}

		// TODO handle the maxTime option

		val message = this.aggregate()
			.match { predicate() }
			.let {
				val limit = model.options.option<LimitOption>()
				if (limit == null) it else it.limit(limit.limit)
			}
			.let {
				val skip = model.options.option<SkipOption>()
				if (skip == null) it else it.skip(skip.skip)
			}
			.countTo(result)
			.reinterpret<BsonDocument>()

		return message
			.firstOrNull()
			?.get("c")
			?.decodeLong(message)
			?: 0L
	}

	override suspend fun countEstimated(): Long {
		val message = database.client.wire.sendSingle(
			database.client.createOpMsg {
				document {
					writeString("count", name)
					writeString($$"$db", database.name)

					Count<Document>(
						context = database.client.context,
					).writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		return message.body.document["n"]
			?.decodeLong(message)
			?: error("Missing count in $message")
	}

	override suspend fun deleteOne(options: DeleteOneOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) {
		val message = database.client.wire.sendSingle(
			database.client.createOpMsg {
				document {
					writeString("delete", name)
					writeString($$"$db", database.name)

					DeleteOne<Document>(
						context = database.client.context,
					).apply {
						this.options.options()
						this.filter.filter()
					}.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun deleteMany(options: DeleteManyOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) {
		val message = database.client.wire.sendSingle(
			database.client.createOpMsg {
				document {
					writeString("delete", name)
					writeString($$"$db", database.name)

					DeleteMany<Document>(
						context = database.client.context,
					).apply {
						this.options.options()
						this.filter.filter()
					}.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override fun find(): MultiplatformMongoIterable<Document> =
		MultiplatformMongoIterableFindImpl(
			collection = this,
			operation = Find(context),
			type = type,
			isDefault = true,
		)

	override fun find(options: FindOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit): MultiplatformMongoIterable<Document> =
		MultiplatformMongoIterableFindImpl(
			collection = this,
			operation = Find<Document>(context).apply {
				this.options.options()
				this.filter.filter()
			},
			type = type,
			isDefault = false,
		)

	override suspend fun updateMany(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		TODO("Not yet implemented")
	}

	override suspend fun updateOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		TODO("Not yet implemented")
	}

	override suspend fun upsertOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpsertQuery<Document>.() -> Unit): UpdateOperations.UpsertResult {
		TODO("Not yet implemented")
	}

	override suspend fun replaceOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) {
		TODO("Not yet implemented")
	}

	override suspend fun repsertOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) {
		TODO("Not yet implemented")
	}

	override suspend fun findOneAndUpdate(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): Document? {
		TODO("Not yet implemented")
	}

	override suspend fun bulkWrite(options: BulkWriteOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, operations: BulkWrite<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override suspend fun updateManyWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		TODO("Not yet implemented")
	}

	override suspend fun updateOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		TODO("Not yet implemented")
	}

	override suspend fun upsertOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpsertResult {
		TODO("Not yet implemented")
	}

	override fun toString(): String =
		"MultiplatformMongoCollection($fullyQualifiedName)"
}
