/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractPipeline
import opensavvy.ktmongo.dsl.aggregation.AggregationPipeline
import opensavvy.ktmongo.dsl.aggregation.PipelineChainLink
import opensavvy.ktmongo.dsl.aggregation.stages.HasUnionWithCompatibility
import opensavvy.ktmongo.dsl.aggregation.stages.ProjectStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.SetStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.UnsetStageOperators
import opensavvy.ktmongo.dsl.expr.FilterOperators
import opensavvy.ktmongo.dsl.expr.common.Expression
import opensavvy.ktmongo.dsl.options.common.SortOptionDsl

class MongoAggregationPipeline<Output : Any> @OptIn(LowLevelApi::class) internal constructor(
	private val collection: String,
	context: BsonContext,
	chain: PipelineChainLink,
	private val iterableBuilder: (MongoAggregationPipeline<*>) -> MongoIterable<*>,
) : AbstractPipeline<Output>(context, chain), AggregationPipeline<Output>, LazyMongoIterable<Output> {

	// region Pipeline methods

	@LowLevelApi
	@DangerousMongoApi
	override fun withStage(stage: Expression): MongoAggregationPipeline<Output> =
		MongoAggregationPipeline(collection, context, chain.withStage(stage), iterableBuilder)

	@Suppress("UNCHECKED_CAST") // The type is phantom, the cast is guaranteed to succeed
	@LowLevelApi
	@DangerousMongoApi
	override fun <New : Any> reinterpret(): MongoAggregationPipeline<New> =
		this as MongoAggregationPipeline<New>

	// endregion
	// region Lazy iterable

	@Suppress("UNCHECKED_CAST")
	override fun asIterable(): MongoIterable<Output> =
		iterableBuilder(this) as MongoIterable<Output>

	// endregion
	// region Stages

	@KtMongoDsl
	override fun limit(amount: Long): MongoAggregationPipeline<Output> =
		super.limit(amount) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun limit(amount: Int): MongoAggregationPipeline<Output> =
		super.limit(amount) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun match(filter: FilterOperators<Output>.() -> Unit): MongoAggregationPipeline<Output> =
		super.match(filter) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun sample(size: Int): MongoAggregationPipeline<Output> =
		super.sample(size) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun set(block: SetStageOperators<Output>.() -> Unit): MongoAggregationPipeline<Output> =
		super.set(block) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun skip(amount: Long): MongoAggregationPipeline<Output> =
		super.skip(amount) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun skip(amount: Int): MongoAggregationPipeline<Output> =
		super.skip(amount) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun sort(block: SortOptionDsl<Output>.() -> Unit): MongoAggregationPipeline<Output> =
		super.sort(block) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun unset(block: UnsetStageOperators<Output>.() -> Unit): MongoAggregationPipeline<Output> =
		super.unset(block) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun project(block: ProjectStageOperators<Output>.() -> Unit): MongoAggregationPipeline<Output> =
		super.project(block) as MongoAggregationPipeline<Output>

	@KtMongoDsl
	override fun unionWith(other: HasUnionWithCompatibility<Output>): MongoAggregationPipeline<Output> =
		super.unionWith(other) as MongoAggregationPipeline<Output>

	// endregion
	// region $unionWith support

	@OptIn(LowLevelApi::class)
	override fun embedInUnionWith(writer: BsonFieldWriter) = with(writer) {
		writeString("coll", collection)
		writeArray("pipeline") {
			this@MongoAggregationPipeline.writeTo(this)
		}
	}

	// endregion

}
