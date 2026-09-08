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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import opensavvy.ktmongo.api.MongoAggregationPipeline
import opensavvy.ktmongo.api.toList
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.ktmongo.dsl.aggregation.stages.*
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.BsonNode
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

internal class MultiplatformMongoAggregationPipelineImpl<Document : Any> @OptIn(LowLevelApi::class) constructor(
	private val collection: MultiplatformMongoCollection<*>,
	chain: PipelineChainLink,
) : AbstractPipeline<Document>(collection.context, chain),
	AggregationPipeline<Document>,
	MultiplatformMongoAggregationPipeline<Document> {

	// region Execution

	@LowLevelApi
	override fun asIterable(type: KType): MultiplatformMongoIterableAggregateImpl<Document> =
		MultiplatformMongoIterableAggregateImpl(collection, chain, type)

	// endregion
	// region Pipeline

	@LowLevelApi
	@DangerousMongoApi
	override fun withStage(stage: BsonNode): MultiplatformMongoAggregationPipelineImpl<Document> =
		MultiplatformMongoAggregationPipelineImpl(collection, chain.withStage(stage))

	@Suppress("UNCHECKED_CAST")
	@LowLevelApi
	@DangerousMongoApi
	override fun <New : Any> reinterpret(): MultiplatformMongoAggregationPipelineImpl<New> =
		this as MultiplatformMongoAggregationPipelineImpl<New>

	// endregion
	// region Stages

	override fun limit(amount: Long): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.limit(amount) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun limit(amount: Int): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.limit(amount) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.match(filter) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.matchExpr(filter) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun sample(size: Int): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.sample(size) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun <Out : Any> set(block: SetStageOperators<Document, Out>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.set(block) as MultiplatformMongoAggregationPipelineImpl<Out>

	override fun skip(amount: Long): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.skip(amount) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun skip(amount: Int): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.skip(amount) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.sort(block) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.unset(block) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun <Out : Any> project(block: ProjectStageOperators<Document, Out>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.project(block) as MultiplatformMongoAggregationPipelineImpl<Out>

	override fun unionWith(other: HasUnionWithCompatibility<Document>): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.unionWith(other) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.lookup(block) as MultiplatformMongoAggregationPipelineImpl<Document>

	override fun <Out : Any> group(block: GroupStageOperators<Document, Out>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.group(block) as MultiplatformMongoAggregationPipelineImpl<Out>

	override fun <Out : Any> countTo(field: Field<Out, Number>): MultiplatformMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.countTo(field) as MultiplatformMongoAggregationPipelineImpl<Out>

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): MultiplatformMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.countTo(field) as MultiplatformMongoAggregationPipelineImpl<Out>

	override fun <Item, Out : Any> unwind(block: UnwindStageOperators<Document, Item, Out>.() -> Unit): MultiplatformMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.unwind(block) as MultiplatformMongoAggregationPipelineImpl<Out>

	// endregion
	// region $unionWith support

	@OptIn(LowLevelApi::class)
	override fun embedInUnionWith(writer: BsonFieldWriter) = with(writer) {
		writeString("coll", collection.name)
		writeArray("pipeline") {
			this@MultiplatformMongoAggregationPipelineImpl.writeTo(this)
		}
	}

	// endregion
	// region $lookup support

	@LowLevelApi
	override fun embedInLookup(writer: BsonFieldWriter) = with(writer) {
		writeString("from", collection.name)

		if (chain.isNotEmpty()) {
			writeArray("pipeline") {
				this@MultiplatformMongoAggregationPipelineImpl.writeTo(this)
			}
		}
	}

	// endregion
	// region Debug

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override suspend fun debug(limit: Int): MongoAggregationPipeline.PipelineDebugReport = coroutineScope {
		val stages = chain.toList().runningFold(collection.aggregate()) { pipeline, link ->
			pipeline.withStage(link)
		}.map { pipeline ->
			async {
				val currentStage = (pipeline as MultiplatformMongoAggregationPipelineImpl<*>).chain.toBsonList().lastOrNull()
					?: collection.factory.buildDocument {} // Empty document represents the pipeline with no operations at all

				try {
					val results = pipeline
						.limit(limit)
						.reinterpret<BsonDocument>()
						.toList()

					MongoAggregationPipeline.StageDebugReport.Success(
						stage = currentStage,
						results = results,
					)
				} catch (e: Exception) {
					ensureActive()
					MongoAggregationPipeline.StageDebugReport.Failure(
						stage = currentStage,
						failure = e,
					)
				}
			}
		}.awaitAll()

		MongoAggregationPipeline.PipelineDebugReport(stages, limit = limit)
	}

	// endregion

	override fun toString(): String =
		"$collection.aggregate(${super.toString()})"

}
