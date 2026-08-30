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

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.ktmongo.dsl.aggregation.stages.*
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.official.toJava
import opensavvy.ktmongo.sync.api.MongoAggregationPipeline
import opensavvy.ktmongo.sync.api.toList
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

private class SyncMongoAggregationPipelineImpl<Document : Any> @OptIn(LowLevelApi::class) constructor(
	private val collection: SyncMongoCollection<*>,
	context: BsonContext,
	chain: PipelineChainLink,
	private val executeAggregate: (List<Bson>, Class<Document>) -> SyncMongoAggregateIterable<Document>,
) : AbstractPipeline<Document>(context, chain),
	AggregationPipeline<Document>,
	SyncMongoAggregationPipeline<Document> {

	// region Execution

	@LowLevelApi
	@Suppress("UNCHECKED_CAST")
	override fun asIterable(type: KType): SyncMongoAggregateIterable<Document> =
		executeAggregate(chain.toBsonList().map { it.toJava() }, (type.classifier as KClass<Document>).java)

	// endregion
	// region Pipeline

	@LowLevelApi
	@DangerousMongoApi
	override fun withStage(stage: BsonNode): SyncMongoAggregationPipelineImpl<Document> =
		SyncMongoAggregationPipelineImpl(collection, context, chain.withStage(stage), executeAggregate)

	@Suppress("UNCHECKED_CAST")
	@LowLevelApi
	@DangerousMongoApi
	override fun <New : Any> reinterpret(): SyncMongoAggregationPipelineImpl<New> =
		this as SyncMongoAggregationPipelineImpl<New>

	// endregion
	// region Stages

	override fun limit(amount: Long): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.limit(amount) as SyncMongoAggregationPipelineImpl<Document>

	override fun limit(amount: Int): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.limit(amount) as SyncMongoAggregationPipelineImpl<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.match(filter) as SyncMongoAggregationPipelineImpl<Document>

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.matchExpr(filter) as SyncMongoAggregationPipelineImpl<Document>

	override fun sample(size: Int): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.sample(size) as SyncMongoAggregationPipelineImpl<Document>

	override fun <Out : Any> set(block: SetStageOperators<Document, Out>.() -> Unit): SyncMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.set(block) as SyncMongoAggregationPipelineImpl<Out>

	override fun skip(amount: Long): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.skip(amount) as SyncMongoAggregationPipelineImpl<Document>

	override fun skip(amount: Int): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.skip(amount) as SyncMongoAggregationPipelineImpl<Document>

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.sort(block) as SyncMongoAggregationPipelineImpl<Document>

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.unset(block) as SyncMongoAggregationPipelineImpl<Document>

	override fun <Out : Any> project(block: ProjectStageOperators<Document, Out>.() -> Unit): SyncMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.project(block) as SyncMongoAggregationPipelineImpl<Out>

	override fun unionWith(other: HasUnionWithCompatibility<Document>): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.unionWith(other) as SyncMongoAggregationPipelineImpl<Document>

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): SyncMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.lookup(block) as SyncMongoAggregationPipelineImpl<Document>

	override fun <Out : Any> group(block: GroupStageOperators<Document, Out>.() -> Unit): SyncMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.group(block) as SyncMongoAggregationPipelineImpl<Out>

	override fun <Out : Any> countTo(field: Field<Out, Number>): SyncMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.countTo(field) as SyncMongoAggregationPipelineImpl<Out>

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): SyncMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.countTo(field) as SyncMongoAggregationPipelineImpl<Out>

	// endregion
	// region $unionWith support

	@OptIn(LowLevelApi::class)
	override fun embedInUnionWith(writer: BsonFieldWriter) = with(writer) {
		writeString("coll", collection.name)
		writeArray("pipeline") {
			this@SyncMongoAggregationPipelineImpl.writeTo(this)
		}
	}

	// endregion
	// region $lookup support

	@LowLevelApi
	override fun embedInLookup(writer: BsonFieldWriter) = with(writer) {
		writeString("from", collection.name)

		if (chain.isNotEmpty()) {
			writeArray("pipeline") {
				this@SyncMongoAggregationPipelineImpl.writeTo(this)
			}
		}
	}

	// endregion
	// region Debug mode

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override suspend fun debug(limit: Int): MongoAggregationPipeline.PipelineDebugReport {
		val stages = chain.toList().runningFold(collection.aggregate()) { pipeline, link ->
			pipeline.withStage(link)
		}.map { pipeline ->
			val currentStage = (pipeline as SyncMongoAggregationPipelineImpl<*>).chain.toBsonList().lastOrNull()
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
				MongoAggregationPipeline.StageDebugReport.Failure(
					stage = currentStage,
					failure = e,
				)
			}
		}

		return MongoAggregationPipeline.PipelineDebugReport(stages, limit = limit)
	}

	// endregion

	override fun toString(): String =
		"$collection.aggregate(${super.toString()})"
}

@LowLevelApi
internal fun <Document : Any> SyncMongoAggregationPipeline(
	collection: SyncMongoCollection<*>,
	context: BsonContext,
	chain: PipelineChainLink,
	executeAggregate: (List<Bson>, Class<Document>) -> SyncMongoAggregateIterable<Document>,
): SyncMongoAggregationPipeline<Document> =
	SyncMongoAggregationPipelineImpl(collection, context, chain, executeAggregate)
