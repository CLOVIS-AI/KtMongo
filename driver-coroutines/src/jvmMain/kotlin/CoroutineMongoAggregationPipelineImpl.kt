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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractPipeline
import opensavvy.ktmongo.dsl.aggregation.AccumulationOperators
import opensavvy.ktmongo.dsl.aggregation.AggregationPipeline
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.aggregation.stages.HasUnionWithCompatibility
import opensavvy.ktmongo.dsl.aggregation.stages.ProjectStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.SetStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.UnsetStageOperators
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.official.toJava
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

private class CoroutineMongoAggregationPipelineImpl<Document : Any> @OptIn(LowLevelApi::class) constructor(
	private val collectionName: String,
	context: BsonContext,
	chain: PipelineChainLink,
	private val executeAggregate: (List<Bson>, Class<Document>) -> CoroutineMongoAggregateIterable<Document>,
) : AbstractPipeline<Document>(context, chain),
	AggregationPipeline<Document>,
	CoroutineMongoAggregationPipeline<Document> {

	// region Execution

	@LowLevelApi
	@Suppress("UNCHECKED_CAST")
	override fun asIterable(type: KType): CoroutineMongoAggregateIterable<Document> =
		executeAggregate(chain.toBsonList().map { it.toJava() }, (type.classifier as KClass<Document>).java)

	// endregion
	// region Pipeline

	@LowLevelApi
	@DangerousMongoApi
	override fun withStage(stage: BsonNode): CoroutineMongoAggregationPipelineImpl<Document> =
		CoroutineMongoAggregationPipelineImpl(collectionName, context, chain.withStage(stage), executeAggregate)

	@Suppress("UNCHECKED_CAST")
	@LowLevelApi
	@DangerousMongoApi
	override fun <New : Any> reinterpret(): CoroutineMongoAggregationPipelineImpl<New> =
		this as CoroutineMongoAggregationPipelineImpl<New>

	// endregion
	// region Stages

	@KtMongoDsl
	override fun limit(amount: Long): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.limit(amount) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun limit(amount: Int): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.limit(amount) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun match(filter: FilterQuery<Document>.() -> Unit): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.match(filter) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun sample(size: Int): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.sample(size) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun set(block: SetStageOperators<Document>.() -> Unit): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.set(block) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun skip(amount: Long): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.skip(amount) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun skip(amount: Int): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.skip(amount) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun sort(block: SortOptionDsl<Document>.() -> Unit): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.sort(block) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.unset(block) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun project(block: ProjectStageOperators<Document>.() -> Unit): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.project(block) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun unionWith(other: HasUnionWithCompatibility<Document>): CoroutineMongoAggregationPipelineImpl<Document> =
		super<AggregationPipeline>.unionWith(other) as CoroutineMongoAggregationPipelineImpl<Document>

	@KtMongoDsl
	override fun <Out : Any> group(block: AccumulationOperators<Document, Out>.() -> Unit): CoroutineMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.group(block) as CoroutineMongoAggregationPipelineImpl<Out>

	@KtMongoDsl
	override fun <Out : Any> countTo(field: Field<Out, Number>): CoroutineMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.countTo(field) as CoroutineMongoAggregationPipelineImpl<Out>

	@KtMongoDsl
	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): CoroutineMongoAggregationPipelineImpl<Out> =
		super<AggregationPipeline>.countTo(field) as CoroutineMongoAggregationPipelineImpl<Out>

	// endregion
	// region $unionWith support

	@OptIn(LowLevelApi::class)
	override fun embedInUnionWith(writer: BsonFieldWriter) = with(writer) {
		writeString("coll", collectionName)
		writeArray("pipeline") {
			this@CoroutineMongoAggregationPipelineImpl.writeTo(this)
		}
	}

	// endregion

}

@LowLevelApi
internal fun <Document : Any> CoroutineMongoAggregationPipeline(
	collectionName: String,
	context: BsonContext,
	chain: PipelineChainLink,
	executeAggregate: (List<Bson>, Class<Document>) -> CoroutineMongoAggregateIterable<Document>,
): CoroutineMongoAggregationPipeline<Document> =
	CoroutineMongoAggregationPipelineImpl(collectionName, context, chain, executeAggregate)
