/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.buildBsonDocument
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.*
import opensavvy.ktmongo.dsl.expr.common.Expression
import opensavvy.ktmongo.dsl.models.*
import opensavvy.ktmongo.dsl.options.CountOptions
import opensavvy.ktmongo.dsl.options.FindOptions
import opensavvy.ktmongo.dsl.options.common.LimitOption
import opensavvy.ktmongo.dsl.options.common.option
import opensavvy.ktmongo.dsl.options.toJava
import org.bson.BsonDocument

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
	override val context: BsonContext
		get() = BsonContext(inner.codecRegistry)

	// region Find

	override fun find(): JvmMongoIterable<Document> =
		JvmMongoIterable(inner.find())

	@OptIn(LowLevelApi::class)
	override fun find(
		options: FindOptions<Document>.() -> Unit,
		predicate: FilterOperators<Document>.() -> Unit,
	): MongoIterable<Document> {
		val model = Find<Document>(context)

		model.options.options()
		model.filter.predicate()

		return JvmMongoIterable(
			inner.find(model.filter.toBsonDocument())
				.limit(model.options.option<LimitOption, _>()?.toInt() ?: 0)
		)
	}

	// endregion
	// region Count

	override suspend fun count(): Long =
		inner.countDocuments()

	@OptIn(LowLevelApi::class)
	override suspend fun count(
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

	override suspend fun countEstimated(): Long =
		inner.estimatedDocumentCount()

	// endregion
	// region Update

	@OptIn(LowLevelApi::class)
	override suspend fun updateMany(
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
	override suspend fun updateOne(
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
	override suspend fun upsertOne(
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
	override suspend fun findOneAndUpdate(
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

	// endregion

}

@OptIn(LowLevelApi::class)
private fun Expression.toBsonDocument(): BsonDocument =
	buildBsonDocument {
		writeTo(this)
	}

/**
 * Converts a [MongoDB collection][com.mongodb.kotlin.client.coroutine.MongoCollection] into a [KtMongo collection][JvmMongoCollection].
 */
fun <Document : Any> com.mongodb.kotlin.client.coroutine.MongoCollection<Document>.asKtMongo(): JvmMongoCollection<Document> =
	JvmMongoCollection(this)
