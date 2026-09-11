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
		val model = InsertOne(
			context = database.client.context,
			document = document,
			documentType = type,
		).apply {
			this.options.options()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("insert", name)
					writeString($$"$db", database.name)

					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
		checkNoWriteErrors(message.body.document, model, this)
	}

	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) {
		val message = database.client.sendSingle(
			database.client.createDriverMessage {
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
		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("create", name)
					writeString($$"$db", database.name)

					CreateCollection<Document>(
						context = database.client.context,
					).apply {
						this.options.options()
					}.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun drop(options: DropOptions<Document>.() -> Unit) {
		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("drop", name)
					writeString($$"$db", database.name)

					Drop<Document>(
						context = database.client.context,
					).apply {
						this.options.options()
					}.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	@OptIn(DangerousMongoApi::class)
	override suspend fun count(): Long {
		val result = Field.unsafe<Long>("c")

		val message = this.aggregate()
			.countTo(result)
			.unsafeCast<BsonDocument>()
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
			.unsafeCast<BsonDocument>()

		return message
			.firstOrNull()
			?.get("c")
			?.decodeLong(message)
			?: 0L
	}

	override suspend fun countEstimated(): Long {
		val message = database.client.sendSingle(
			database.client.createDriverMessage {
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
		val message = database.client.sendSingle(
			database.client.createDriverMessage {
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
		val message = database.client.sendSingle(
			database.client.createDriverMessage {
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
			options = {},
			filter = {},
			type = type,
			isDefault = true,
		)

	override fun find(options: FindOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit): MultiplatformMongoIterable<Document> =
		MultiplatformMongoIterableFindImpl(
			collection = this,
			options = options,
			filter = filter,
			type = type,
			isDefault = false,
		)

	override suspend fun updateMany(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		val model = UpdateMany<Document>(
			context = database.client.context,
		).apply {
			this.options.options()
			this.filter.filter()
			this.update.update()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		// TODO handle unacknowledged updates

		return MultiplatformAcknowledgedUpdateResult(message.body.document)
	}

	override suspend fun updateOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		val model = UpdateOne<Document>(
			context = database.client.context,
		).apply {
			this.options.options()
			this.filter.filter()
			this.update.update()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		// TODO handle unacknowledged updates

		return MultiplatformAcknowledgedUpdateResult(message.body.document)
	}

	override suspend fun upsertOne(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpsertQuery<Document>.() -> Unit): MultiplatformMongoCollection.UpsertResult {
		val model = UpsertOne<Document>(
			context = database.client.context,
		).apply {
			this.options.options()
			this.filter.filter()
			this.update.update()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		// TODO handle unacknowledged updates

		return MultiplatformAcknowledgedUpdateResult(message.body.document)
	}

	override suspend fun replaceOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) {
		val model = ReplaceOne(
			context = database.client.context,
			document = document,
			documentType = type,
		).apply {
			this.options.options()
			this.filter.filter()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun repsertOne(options: ReplaceOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, document: Document) {
		val model = RepsertOne(
			context = database.client.context,
			document = document,
			documentType = type,
		).apply {
			this.options.options()
			this.filter.filter()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun findOneAndUpdate(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateQuery<Document>.() -> Unit): Document? {
		TODO("Not yet implemented")
	}

	override suspend fun bulkWrite(options: BulkWriteOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, operations: BulkWrite<Document>.() -> Unit) {
		val model = BulkWrite(
			context = database.client.context,
			documentType = type,
			globalFilter = filter,
		).apply {
			this.options.options()
			this.operations()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeInt32("bulkWrite", 1)
					writeString($$"$db", "admin") // Hard-coded, mandatory

					writeArray("nsInfo") {
						writeDocument {
							writeString("ns", fullyQualifiedName)
						}
					}

					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun updateManyWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		val model = UpdateManyWithPipeline<Document>(
			context = database.client.context,
		).apply {
			this.options.options()
			this.filter.filter()
			this.update.update()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		// TODO handle unacknowledged updates

		return MultiplatformAcknowledgedUpdateResult(message.body.document)
	}

	override suspend fun updateOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): UpdateOperations.UpdateResult {
		val model = UpdateOneWithPipeline<Document>(
			context = database.client.context,
		).apply {
			this.options.options()
			this.filter.filter()
			this.update.update()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		// TODO handle unacknowledged updates

		return MultiplatformAcknowledgedUpdateResult(message.body.document)
	}

	override suspend fun upsertOneWithPipeline(options: UpdateOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit, update: UpdateWithPipelineQuery<Document>.() -> Unit): MultiplatformMongoCollection.UpsertResult {
		val model = UpsertOneWithPipeline<Document>(
			context = database.client.context,
		).apply {
			this.options.options()
			this.filter.filter()
			this.update.update()
		}

		val message = database.client.sendSingle(
			database.client.createDriverMessage {
				document {
					writeString("update", name)
					writeString($$"$db", database.name)
					model.writeTo(this)
				}
			}
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
		check(message.body.document["writeErrors"] == null) { "There were write errors: ${message.body.document["writeErrors"]}" }

		// TODO handle unacknowledged updates

		return MultiplatformAcknowledgedUpdateResult(message.body.document)
	}

	override fun toString(): String =
		"MultiplatformMongoCollection($fullyQualifiedName)"
}

private data class MultiplatformAcknowledgedUpdateResult(
	private val doc: BsonDocument,
) : MultiplatformMongoCollection.UpsertResult {

	override val upsertedId: BsonValue?
		get() = doc["upserted"]?.decodeArray()
			?.get(0)?.decodeDocument()
			?.get("_id")

	override val upsertedCount: Int
		get() = doc["upserted"]?.decodeArray()
			?.size ?: 0

	override val acknowledged: Boolean
		get() = true

	override val matchedCount: Long
		get() = doc["n"]?.decodeLong(doc)
			?.minus(upsertedCount) // In the case of an insert, the wire protocol returns 1, but the Java driver reports 0
			?: 0L

	override val modifiedCount: Long
		get() = doc["nModified"]?.decodeLong(doc) ?: 0L

	override fun toString(): String =
		"UpdateResult(acknowledged=true, matchedCount=$matchedCount, modifiedCount=$modifiedCount, upsertedCount=$upsertedCount, upsertedId=$upsertedId; decodedFrom=$doc)"
}

private data object MultiplatformUnacknowledgedUpdateResult : MultiplatformMongoCollection.UpsertResult {

	override val upsertedId: Nothing
		get() = throw UnsupportedOperationException("Unacknowledged updates do not provide the upsertedId field")

	override val upsertedCount: Nothing
		get() = throw UnsupportedOperationException("Unacknowledged updates do not provide the upsertedCount field")

	override val acknowledged: Boolean
		get() = false

	override val matchedCount: Nothing
		get() = throw UnsupportedOperationException("Unacknowledged updates do not provide the matchedCount field")

	override val modifiedCount: Nothing
		get() = throw UnsupportedOperationException("Unacknowledged updates do not provide the modifiedCount field")

	override fun toString(): String =
		"UpdateResult(acknowledged=false)"
}

private fun BsonValue.decodeLong(message: Any?): Long = when (this.type) {
	BsonType.Int32 -> decodeInt32().toLong()
	BsonType.Int64 -> decodeInt64()
	else -> error("Unexpected count type: $type in $message")
}
