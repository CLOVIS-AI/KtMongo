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

@file:JvmMultifileClass
@file:JvmName("KtMongo")

package opensavvy.ktmongo.coroutines

import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import opensavvy.ktmongo.api.operations.UpdateOperations
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.BsonValue
import opensavvy.ktmongo.bson.official.types.Jvm
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.command.*
import opensavvy.ktmongo.dsl.options.ArrayFiltersOption
import opensavvy.ktmongo.dsl.options.WithWriteConcern
import opensavvy.ktmongo.dsl.options.WriteConcernOption
import opensavvy.ktmongo.dsl.options.option
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import opensavvy.ktmongo.official.command.toJava
import opensavvy.ktmongo.official.options.*
import opensavvy.ktmongo.official.options.toJava
import opensavvy.ktmongo.official.toJava
import java.util.concurrent.TimeUnit
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import com.mongodb.client.model.ReplaceOptions as MongoReplaceOptions
import com.mongodb.client.model.UpdateOptions as MongoUpdateOptions

private class CoroutineMongoCollectionImpl<Document : Any>(
	inner: MongoCollection<Document>,
	override val factory: BsonFactory,
	override val propertyNameStrategy: PropertyNameStrategy,
	override val objectIdGenerator: ObjectIdGenerator,
	@property:LowLevelApi
	override val type: KType,
) : CoroutineMongoCollection<Document> {

	private val inner = inner
		.withCodecRegistry(factory.codecRegistry)

	override fun asOfficial(): MongoCollection<Document> =
		inner

	override val name: String
		get() = inner.namespace.collectionName

	override val fullyQualifiedName: String
		get() = inner.namespace.fullName

	private inner class CoroutineBsonContext : BsonContext,
		opensavvy.ktmongo.bson.BsonFactory by factory,
		ObjectIdGenerator by objectIdGenerator,
		PropertyNameStrategy by propertyNameStrategy

	@LowLevelApi
	override val context: BsonContext = CoroutineBsonContext()

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
			factory.buildDocument(model.filter).raw,
			model.options.toJava()
		)
	}

	override suspend fun countEstimated(): Long =
		inner.estimatedDocumentCount()

	// endregion
	// region Find

	override fun find(): CoroutineMongoFindIterable<Document> =
		inner.find().asKtMongo()

	@OptIn(LowLevelApi::class)
	override fun find(
		options: FindOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
	): CoroutineMongoFindIterable<Document> {
		val model = Find<Document>(context)

		model.options.options()
		model.filter.filter()

		return inner
			.withReadConcern(model.options.readReadConcern())
			.withReadPreference(model.options.readReadPreference())
			.find(factory.buildDocument(model.filter).raw)
			.limit(model.options.readLimit())
			.skip(model.options.readSkip())
			.maxTime(model.options.readMaxTimeMS().toLong(), TimeUnit.MILLISECONDS)
			.sort(model.options.readSortDocument())
			.asKtMongo()
	}

	// endregion
	// region Insert

	@OptIn(LowLevelApi::class)
	override suspend fun insertOne(document: Document, options: InsertOneOptions<Document>.() -> Unit) {
		val model = InsertOne(context, document, type)

		model.options.options()

		inner.withWriteConcern(model.options).insertOne(
			model.document,
			com.mongodb.client.model.InsertOneOptions()
		)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun insertMany(documents: Iterable<Document>, options: InsertManyOptions<Document>.() -> Unit) {
		val model = InsertMany(context, documents.toList(), type)

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
			filter = factory.buildDocument(model.filter).raw,
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
			filter = factory.buildDocument(model.filter).raw,
			options = DeleteOptions()
		)
	}

	// endregion
	// region Collection

	@OptIn(LowLevelApi::class)
	override suspend fun drop(options: DropOptions<Document>.() -> Unit) {
		val model = Drop<Document>(context)

		model.options.options()

		inner.withWriteConcern(model.options).drop()
	}

	// endregion
	// region Update

	@OptIn(LowLevelApi::class)
	override suspend fun updateMany(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateQuery<Document>.() -> Unit,
	): UpdateOperations.UpdateResult {
		val model = UpdateMany<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		val result = inner
			.withWriteConcern(model.options)
			.updateMany(
				factory.buildDocument(model.filter).raw,
				factory.buildDocument(model.update).raw,
				MongoUpdateOptions()
					.arrayFilters(model.options.option<ArrayFiltersOption>()?.filters.orEmpty().map { factory.readDocument(it).raw }),
			)
		return CoroutineUpdateResult(result, factory)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun updateOne(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateQuery<Document>.() -> Unit,
	): UpdateOperations.UpdateResult {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		val result = inner
			.withWriteConcern(model.options)
			.updateOne(
				factory.buildDocument(model.filter).raw,
				factory.buildDocument(model.update).raw,
				MongoUpdateOptions()
					.arrayFilters(model.options.option<ArrayFiltersOption>()?.filters.orEmpty().map { factory.readDocument(it).raw }),
			)
		return CoroutineUpdateResult(result, factory)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun upsertOne(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpsertQuery<Document>.() -> Unit,
	): CoroutineMongoCollection.UpsertResult {
		val model = UpsertOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		val result = inner
			.withWriteConcern(model.options)
			.updateOne(
				factory.buildDocument(model.filter).raw,
				factory.buildDocument(model.update).raw,
				MongoUpdateOptions()
					.upsert(true)
					.arrayFilters(model.options.option<ArrayFiltersOption>()?.filters.orEmpty().map { factory.readDocument(it).raw }),
			)
		return CoroutineUpdateResult(result, factory)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun replaceOne(
		options: ReplaceOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		document: Document,
	) {
		val model = ReplaceOne(context, document, type)

		model.options.options()
		model.filter.filter()

		inner.withWriteConcern(model.options).replaceOne(
			factory.buildDocument(model.filter).raw,
			document,
			MongoReplaceOptions(),
		)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun repsertOne(
		options: ReplaceOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		document: Document,
	) {
		val model = RepsertOne(context, document, type)

		model.options.options()
		model.filter.filter()

		inner.withWriteConcern(model.options).replaceOne(
			factory.buildDocument(model.filter).raw,
			document,
			MongoReplaceOptions().upsert(true),
		)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun findOneAndUpdate(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateQuery<Document>.() -> Unit,
	): Document? {
		val model = UpdateOne<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		return inner.withWriteConcern(model.options).findOneAndUpdate(
			factory.buildDocument(model.filter).raw,
			factory.buildDocument(model.update).raw,
			FindOneAndUpdateOptions(),
		)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun bulkWrite(
		options: BulkWriteOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		operations: BulkWrite<Document>.() -> Unit,
	) {
		val model = BulkWrite(context, type, filter)

		model.options.options()
		model.operations()

		inner.withWriteConcern(model.options).bulkWrite(
			model.operations.map { it.toJava() }.toList(),
			options = com.mongodb.client.model.BulkWriteOptions(),
		)
	}

	// endregion
	// region UpdatePipeline

	@OptIn(LowLevelApi::class)
	override suspend fun updateManyWithPipeline(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateWithPipelineQuery<Document>.() -> Unit,
	): UpdateOperations.UpdateResult {
		val model = UpdateManyWithPipeline<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		val result = inner
			.withWriteConcern(model.options)
			.updateMany(
				factory.buildDocument(model.filter).raw,
				model.updates.map { it.toJava() },
				MongoUpdateOptions(),
			)
		return CoroutineUpdateResult(result, factory)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun updateOneWithPipeline(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateWithPipelineQuery<Document>.() -> Unit,
	): UpdateOperations.UpdateResult {
		val model = UpdateOneWithPipeline<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		val result = inner
			.withWriteConcern(model.options)
			.updateOne(
				factory.buildDocument(model.filter).raw,
				model.updates.map { it.toJava() },
				MongoUpdateOptions(),
			)
		return CoroutineUpdateResult(result, factory)
	}

	@OptIn(LowLevelApi::class)
	override suspend fun upsertOneWithPipeline(
		options: UpdateOptions<Document>.() -> Unit,
		filter: FilterQuery<Document>.() -> Unit,
		update: UpdateWithPipelineQuery<Document>.() -> Unit,
	): CoroutineMongoCollection.UpsertResult {
		val model = UpsertOneWithPipeline<Document>(context)

		model.options.options()
		model.filter.filter()
		model.update.update()

		val result = inner
			.withWriteConcern(model.options)
			.updateOne(
				factory.buildDocument(model.filter).raw,
				model.updates.map { it.toJava() },
				MongoUpdateOptions().upsert(true),
			)
		return CoroutineUpdateResult(result, factory)
	}

	// endregion
	// region Aggregation

	@OptIn(LowLevelApi::class)
	override fun aggregate(): CoroutineMongoAggregationPipeline<Document> =
		CoroutineMongoAggregationPipeline(
			collectionName = name,
			context = context,
			chain = PipelineChainLink(context),
			executeAggregate = { pipeline, documentClass ->
				inner.aggregate(pipeline, documentClass)
					.asKtMongo()
			},
		)

	// endregion

	override fun toString(): String =
		"CoroutineMongoCollection($fullyQualifiedName)"
}

private class CoroutineUpdateResult(
	private val inner: com.mongodb.client.result.UpdateResult,
	private val factory: BsonFactory,
) : CoroutineMongoCollection.UpsertResult {
	override val acknowledged: Boolean
		get() = inner.wasAcknowledged()

	override val matchedCount: Long
		get() = inner.matchedCount

	override val modifiedCount: Long
		get() = inner.modifiedCount

	override val upsertedId: BsonValue?
		get() = inner.upsertedId?.let { factory.readValue(it) }

	override val upsertedCount: Int
		get() = if (inner.upsertedId == null) 0 else 1

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is CoroutineUpdateResult) return false

		if (inner != other.inner) return false
		if (factory != other.factory) return false

		return true
	}

	override fun hashCode(): Int {
		var result = inner.hashCode()
		result = 31 * result + factory.hashCode()
		return result
	}

	override fun toString(): String =
		if (acknowledged) "UpdateResult(acknowledged=true, matchedCount=$matchedCount, modifiedCount=$modifiedCount, upsertedCount=$upsertedCount, upsertedId=$upsertedId)"
		else "UpdateResult(acknowledged=false)"
}

/**
 * Instantiates a KtMongo [CoroutineMongoCollection] using an existing collection from the official Kotlin driver.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *         .asKtMongo()
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun <Document : Any> MongoCollection<Document>.asKtMongo(
	factory: BsonFactory = BsonFactory(this.codecRegistry),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
	type: KType,
): CoroutineMongoCollection<Document> =
	CoroutineMongoCollectionImpl(
		inner = this,
		factory = factory,
		propertyNameStrategy = propertyNameStrategy,
		objectIdGenerator = objectIdGenerator,
		type = type,
	)

/**
 * Instantiates a KtMongo [CoroutineMongoCollection] using an existing collection from the official Kotlin driver.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *     val database = client.database("my-app")
 *     val users = database.collection<UserDto>("users")
 *         .asKtMongo()
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
inline fun <reified Document : Any> MongoCollection<Document>.asKtMongo(
	factory: BsonFactory = BsonFactory(this.codecRegistry),
	propertyNameStrategy: PropertyNameStrategy = PropertyNameStrategy.Default,
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Jvm(),
): CoroutineMongoCollection<Document> =
	asKtMongo(
		factory = factory,
		propertyNameStrategy = propertyNameStrategy,
		objectIdGenerator = objectIdGenerator,
		type = typeOf<Document>(),
	)

@LowLevelApi
private fun <Document : Any> MongoCollection<Document>.withWriteConcern(option: WithWriteConcern): MongoCollection<Document> {
	val concern = option.option<WriteConcernOption>()?.concern
		?: return this

	return this.withWriteConcern(concern.toJava())
}
