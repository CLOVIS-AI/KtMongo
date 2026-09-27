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
import kotlinx.coroutines.flow.map
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.command.Aggregate
import opensavvy.ktmongo.dsl.command.Command
import opensavvy.ktmongo.dsl.command.Find
import opensavvy.ktmongo.dsl.command.FindOptions
import opensavvy.ktmongo.dsl.command.errors.MongoDriverException
import opensavvy.ktmongo.dsl.options.CommentOption
import opensavvy.ktmongo.dsl.options.MaxTimeOption
import opensavvy.ktmongo.dsl.options.option
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import kotlin.reflect.KType

internal class MultiplatformMongoIterableMappingImpl<In : Any, Out : Any>(
	private val upstream: MultiplatformMongoIterable<In>,
	private val transform: suspend (In) -> Out,
) : MultiplatformMongoIterable<Out> {
	override suspend fun first(): Out =
		transform(upstream.first())

	override suspend fun firstOrNull(): Out? =
		upstream.firstOrNull()?.let { transform(it) }

	override suspend fun forEach(action: suspend (Out) -> Unit) =
		upstream.forEach { action(transform(it)) }

	override fun asFlow(): Flow<Out> =
		upstream.asFlow().map(transform)

	override fun toString(): String =
		upstream.toString()
}

internal abstract class AbstractMultiplatformMongoIterable<Document : Any>(
	protected val namespace: MultiplatformNamespace,
	protected val outputType: KType,
) : MultiplatformMongoIterable<Document> {

	final override suspend fun first(): Document =
		firstOrNull() ?: throw NoSuchElementException("No element found")

	protected abstract val command: Command
	protected abstract fun createFirstBatch(): DriverMessage
	protected abstract fun createNextBatch(cursorId: Long): DriverMessage

	@OptIn(LowLevelApi::class)
	final override suspend fun forEach(action: suspend (Document) -> Unit) {
		val cursorId = run {
			// Within a 'run' block to free memory of the first batch when the next ones are being computed
			val request = createFirstBatch()
			val firstBatch = namespace.client.sendSingle(request)
			checkOpMsg(firstBatch, request, command, namespace)
			checkNoSyntaxErrors(firstBatch.bson, request, command, namespace)

			val cursor = firstBatch.bson["cursor"]?.decodeDocument()

			val cursorId = cursor?.get("id")?.decodeInt64()
				?: throw MongoDriverException(
					message = "No cursor ID found in the database response",
					response = firstBatch.bson,
					request = request.message.bson,
					server = namespace.client.serverAddress,
					command = command,
					namespace = namespace.namespace,
				)

			val batch = cursor["firstBatch"]?.decodeArray()?.asList().orEmpty()

			if (batch.isEmpty())
				return

			for (item in batch) {
				action(item.decode(outputType))
			}

			cursorId
		}

		if (cursorId == 0L) {
			// MongoDB returns a cursor ID of 0 if there is no further information to read
			return
		}

		while (true) {
			val request = createNextBatch(cursorId)
			val nextBatch = namespace.client.sendSingle(request)
			checkOpMsg(nextBatch, request, command, namespace)
			checkNoSyntaxErrors(nextBatch.bson, request, command, namespace)

			TODO("Received batch: $nextBatch")
		}
	}

	final override fun asFlow(): Flow<Document> = flow {
		forEach {
			emit(it)
		}
	}

	// Force children to override
	abstract override fun toString(): String
}

internal class MultiplatformMongoIterableFindImpl<Document : Any>(
	private val collection: MultiplatformMongoCollectionImpl<*>,
	private val options: FindOptions<Document>.() -> Unit,
	private val filter: FilterQuery<Document>.() -> Unit,
	outputType: KType,
	private val isDefault: Boolean,
) : AbstractMultiplatformMongoIterable<Document>(collection, outputType) {

	override val command by lazy {
		Find<Document>(collection.context).apply {
			this.options.options()
			this.filter.filter()
		}
	}

	override suspend fun firstOrNull(): Document? =
		MultiplatformMongoIterableFindImpl(
			collection = collection,
			options = {
				options()
				limit(1)
			},
			filter = filter,
			outputType = outputType,
			isDefault = isDefault
		).asFlow()
			.firstOrNull()

	@OptIn(LowLevelApi::class)
	override fun createFirstBatch(): DriverMessage = collection.database.client.createDriverMessage {
		document {
			writeString("find", collection.name)
			writeString($$"$db", collection.database.name)

			command.writeTo(this)
		}
	}

	@OptIn(LowLevelApi::class)
	override fun createNextBatch(cursorId: Long): DriverMessage = collection.database.client.createDriverMessage {
		document {
			writeInt64("getMore", cursorId)
			writeString("collection", collection.name)
			writeString($$"$db", collection.database.name)

			// TODO re-specify the batch size option

			command.options.option<MaxTimeOption>()?.writeTo(this)
			command.options.option<CommentOption>()?.writeTo(this)
		}
	}

	override fun toString(): String =
		"$collection.find(${if (isDefault) "{}" else command.toString()})"
}

@LowLevelApi
internal class MultiplatformMongoIterableAggregateImpl<Document : Any>(
	private val collection: MultiplatformMongoCollectionImpl<*>,
	private val chain: PipelineChainLink,
	outputType: KType,
) : AbstractMultiplatformMongoIterable<Document>(collection, outputType) {

	override val command: Command by lazy {
		Aggregate<Document>(
			context = collection.context,
			chain = chain,
			outputType = outputType,
		)
	}

	override suspend fun firstOrNull(): Document? =
		MultiplatformMongoIterableAggregateImpl<Document>(
			collection = collection,
			chain = chain.withStage(LimitOneStage(collection.context)),
			outputType = outputType,
		).asFlow()
			.firstOrNull()

	override fun createFirstBatch(): DriverMessage = collection.database.client.createDriverMessage {
		document {
			writeString("aggregate", collection.name)
			writeString($$"$db", collection.database.name)

			command.writeTo(this)

			// TODO add options
		}
	}

	override fun createNextBatch(cursorId: Long): DriverMessage = collection.database.client.createDriverMessage {
		document {
			writeInt64("getMore", cursorId)
			writeString("collection", collection.name)
			writeString($$"$db", collection.database.name)

			// TODO add options
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
