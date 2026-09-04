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

import opensavvy.ktmongo.api.MongoAggregationPipeline
import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.api.operations.UpdateOperations
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.*
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import opensavvy.ktmongo.multiplatform.wire.Message
import opensavvy.ktmongo.multiplatform.wire.MessageSection
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
		val command = lazy {
			database.client.factory.buildDocument {
				writeString("insert", name)
				writeString($$"$db", database.name)

				InsertOne(
					context = database.client.context,
					document = document,
					documentType = type,
				).writeTo(this)
			}
		}

		val message = database.client.wire.sendSingle(
			Message.OpMsg(
				body = MessageSection.Body(
					command,
				)
			)
		)

		check(message is Message.OpMsg)
		check(message.body.document["ok"]?.decodeDouble() == 1.0)
	}

	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override fun filter(filter: FilterQuery<Document>.() -> Unit): MultiplatformMongoCollection<Document> {
		TODO("Not yet implemented")
	}

	override fun aggregate(): MongoAggregationPipeline<Document> {
		TODO("Not yet implemented")
	}

	override suspend fun create(options: CreateCollectionOptions<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override suspend fun drop(options: DropOptions<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override suspend fun count(): Long {
		val command = lazy {
			database.client.factory.buildDocument {
				writeString("count", name)
				writeString($$"$db", database.name)

				Count<Document>(
					context = database.client.context,
				).writeTo(this)
			}
		}

		val message = database.client.wire.sendSingle(
			Message.OpMsg(
				body = MessageSection.Body(
					command,
				)
			)
		)

		message as Message.OpMsg
		check(message.body.document["ok"]?.decodeDouble() == 1.0)

		val count = message.body.document["n"]

		return when (count?.type) {
			BsonType.Int32 -> count.decodeInt32().toLong()
			BsonType.Int64 -> count.decodeInt64()
			else -> error("Unexpected count type: ${count?.type} in $message")
		}
	}

	override suspend fun count(options: CountOptions<Document>.() -> Unit, predicate: FilterQuery<Document>.() -> Unit): Long {
		TODO("Not yet implemented")
	}

	override suspend fun countEstimated(): Long {
		TODO("Not yet implemented")
	}

	override suspend fun deleteOne(options: DeleteOneOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override suspend fun deleteMany(options: DeleteManyOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit) {
		TODO("Not yet implemented")
	}

	override fun find(): MongoIterable<Document> {
		TODO("Not yet implemented")
	}

	override fun find(options: FindOptions<Document>.() -> Unit, filter: FilterQuery<Document>.() -> Unit): MongoIterable<Document> {
		TODO("Not yet implemented")
	}

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
