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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.command.Find
import opensavvy.ktmongo.dsl.command.FindOptions
import opensavvy.ktmongo.dsl.options.CommentOption
import opensavvy.ktmongo.dsl.options.MaxTimeOption
import opensavvy.ktmongo.dsl.options.option
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.multiplatform.wire.Message
import kotlin.reflect.KType

internal class MultiplatformMongoIterableFindImpl<Document : Any>(
	private val collection: MultiplatformMongoCollection<*>,
	private val options: FindOptions<Document>.() -> Unit,
	private val filter: FilterQuery<Document>.() -> Unit,
	private val type: KType,
	private val isDefault: Boolean,
) : MultiplatformMongoIterable<Document> {

	@OptIn(LowLevelApi::class)
	private val model by lazy(LazyThreadSafetyMode.NONE) {
		Find<Document>(collection.context).apply {
			this.options.options()
			this.filter.filter()
		}
	}

	override suspend fun first(): Document = firstOrNull()
		?: throw NoSuchElementException("No element found")

	override suspend fun firstOrNull(): Document? =
		MultiplatformMongoIterableFindImpl(
			collection = collection,
			options = {
				options()
				limit(1)
			},
			filter = filter,
			type = type,
			isDefault = isDefault
		).asFlow()
			.firstOrNull()

	@OptIn(LowLevelApi::class)
	override suspend fun forEach(action: suspend (Document) -> Unit) {
		val firstBatch = collection.database.client.wire.sendSingle(
			collection.database.client.createOpMsg {
				document {
					writeString("find", collection.name)
					writeString($$"$db", collection.database.name)

					model.writeTo(this)
				}
			}
		)

		firstBatch as Message.OpMsg
		check(firstBatch.body.document["ok"]?.decodeDouble() == 1.0)

		val cursor = firstBatch.body.document["cursor"]?.decodeDocument()

		val cursorId = cursor?.get("id")?.decodeInt64()
			?: error("No cursor ID found in $firstBatch")

		val batch = cursor["firstBatch"]?.decodeArray()?.asList().orEmpty()

		if (batch.isEmpty())
			return

		for (item in batch) {
			action(item.decode(type))
		}

		if (cursorId == 0L)
			return

		while (true) {
			val nextBatch = collection.database.client.wire.sendSingle(
				collection.database.client.createOpMsg {
					document {
						writeInt64("getMore", cursorId)
						writeString("collection", collection.name)
						writeString($$"$db", collection.database.name)

						// TODO re-specify the batch size option

						model.options.option<MaxTimeOption>()?.writeTo(this)
						model.options.option<CommentOption>()?.writeTo(this)
					}
				}
			)

			TODO("Received batch: $nextBatch")
		}
	}

	override fun asFlow(): Flow<Document> = flow {
		forEach {
			emit(it)
		}
	}

	override fun toString(): String =
		"$collection.find(${if (isDefault) "{}" else model.toString()})"
}

@LowLevelApi
internal class MultiplatformMongoIterableAggregateImpl<Document : Any>(
	private val collection: MultiplatformMongoCollection<*>,
	private val chain: PipelineChainLink,
	private val type: KType,
) : MultiplatformMongoIterable<Document> {

	override suspend fun first(): Document = firstOrNull()
		?: throw NoSuchElementException("No element found")

	override suspend fun firstOrNull(): Document? =
		MultiplatformMongoIterableAggregateImpl<Document>(
			collection = collection,
			chain = chain.withStage(LimitOneStage(collection.context)),
			type = type,
		).asFlow()
			.firstOrNull()

	@OptIn(LowLevelApi::class)
	override suspend fun forEach(action: suspend (Document) -> Unit) {
		val firstBatch = collection.database.client.wire.sendSingle(
			collection.database.client.createOpMsg {
				document {
					writeString("aggregate", collection.name)
					writeString($$"$db", collection.database.name)
					writeArray("pipeline") {
						chain.writeTo(this)
					}
					writeDocument("cursor") {}

					// TODO add options
				}
			}
		)

		firstBatch as Message.OpMsg
		check(firstBatch.body.document["ok"]?.decodeDouble() == 1.0)

		val cursor = firstBatch.body.document["cursor"]?.decodeDocument()

		val cursorId = cursor?.get("id")?.decodeInt64()
			?: error("No cursor ID found in $firstBatch")

		val batch = cursor["firstBatch"]?.decodeArray()?.asList().orEmpty()

		if (batch.isEmpty())
			return

		for (item in batch) {
			action(item.decode(type))
		}
	}

	override fun asFlow(): Flow<Document> = flow {
		forEach {
			emit(it)
		}
	}

	override fun toString(): String =
		"$collection.aggregate($chain)"
}

private class LimitOneStage(
	context: BsonContext,
) : AbstractBsonNode(context) {

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeInt64($$"$limit", 1)
	}
}
