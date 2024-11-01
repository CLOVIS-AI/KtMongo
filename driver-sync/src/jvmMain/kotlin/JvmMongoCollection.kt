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

package opensavvy.ktmongo.sync

import com.mongodb.client.model.UpdateOptions
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.buildBsonDocument
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.FilterExpression
import opensavvy.ktmongo.dsl.expr.UpdateExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import org.bson.BsonDocument

/**
 * Implementation of [MongoCollection] based on [MongoDB's MongoCollection][com.mongodb.kotlin.client.MongoCollection].
 *
 * To access the inner iterable, see [asKotlinClient].
 *
 * To convert an existing MongoDB iterable into an instance of this class, see [asKtMongo].
 */
class JvmMongoCollection<Document : Any> internal constructor(
	private val inner: com.mongodb.kotlin.client.MongoCollection<Document>
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
	override fun find(predicate: FilterExpression<Document>.() -> Unit): JvmMongoIterable<Document> {
		val filter = FilterExpression<Document>(context)
			.apply(predicate)
			.toBsonDocument()

		return JvmMongoIterable(inner.find(filter))
	}

	// endregion
	// region Count

	override fun count(): Long =
		inner.countDocuments()

	@OptIn(LowLevelApi::class)
	override fun count(predicate: FilterExpression<Document>.() -> Unit): Long {
		val filter = FilterExpression<Document>(context)
			.apply(predicate)
			.toBsonDocument()

		return inner.countDocuments(filter)
	}

	override fun countEstimated(): Long =
		inner.estimatedDocumentCount()

	// endregion
	// region Update

	@OptIn(LowLevelApi::class)
	override fun updateMany(filter: FilterExpression<Document>.() -> Unit, update: UpdateExpression<Document>.() -> Unit) {
		val filter = FilterExpression<Document>(context)
			.apply(filter)
			.toBsonDocument()

		val update = UpdateExpression<Document>(context)
			.apply(update)
			.toBsonDocument()

		inner.updateMany(filter, update)
	}

	@OptIn(LowLevelApi::class)
	override fun updateOne(filter: FilterExpression<Document>.() -> Unit, update: UpdateExpression<Document>.() -> Unit) {
		val filter = FilterExpression<Document>(context)
			.apply(filter)
			.toBsonDocument()

		val update = UpdateExpression<Document>(context)
			.apply(update)
			.toBsonDocument()

		inner.updateOne(filter, update)
	}

	@OptIn(LowLevelApi::class)
	override fun upsertOne(filter: FilterExpression<Document>.() -> Unit, update: UpdateExpression<Document>.() -> Unit) {
		val filter = FilterExpression<Document>(context)
			.apply(filter)
			.toBsonDocument()

		val update = UpdateExpression<Document>(context)
			.apply(update)
			.toBsonDocument()

		inner.updateOne(filter, update, UpdateOptions().upsert(true))
	}

	@OptIn(LowLevelApi::class)
	override fun findOneAndUpdate(filter: FilterExpression<Document>.() -> Unit, update: UpdateExpression<Document>.() -> Unit): Document? {
		val filter = FilterExpression<Document>(context)
			.apply(filter)
			.toBsonDocument()

		val update = UpdateExpression<Document>(context)
			.apply(update)
			.toBsonDocument()

		return inner.findOneAndUpdate(filter, update)
	}

	// endregion

}

@OptIn(LowLevelApi::class)
private fun AbstractCompoundExpression.toBsonDocument(): BsonDocument =
	buildBsonDocument {
		writeTo(this)
	}

/**
 * Converts a [MongoDB collection][com.mongodb.kotlin.client.MongoCollection] into a [KtMongo collection][JvmMongoCollection].
 */
fun <Document : Any> com.mongodb.kotlin.client.MongoCollection<Document>.asKtMongo(): JvmMongoCollection<Document> =
	JvmMongoCollection(this)
