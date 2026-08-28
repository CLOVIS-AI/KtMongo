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

package opensavvy.ktmongo.sync.api.blocking

import opensavvy.ktmongo.api.MongoAggregationPipeline
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.stages.*
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.BsonNode
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import opensavvy.ktmongo.sync.api.MongoAggregationPipeline as SyncMongoAggregationPipeline

class BlockingMongoAggregationPipeline<Document : Any>(
	private val inner: SyncMongoAggregationPipeline<Document>,
) : MongoAggregationPipeline<Document> {
	@LowLevelApi
	override fun asIterable(type: KType): BlockingMongoIterable<Document> =
		BlockingMongoIterable(inner.asIterable(type))

	override fun limit(amount: Long): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.limit(amount))

	override fun limit(amount: Int): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.limit(amount))

	override fun match(filter: FilterQuery<Document>.() -> Unit): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.match(filter))

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.matchExpr(filter))

	override fun sample(size: Int): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.sample(size))

	override fun <Out : Any> set(block: SetStageOperators<Document, Out>.() -> Unit): BlockingMongoAggregationPipeline<Out> =
		BlockingMongoAggregationPipeline(inner.set(block))

	override fun skip(amount: Long): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.skip(amount))

	override fun skip(amount: Int): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.skip(amount))

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.sort(block))

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.unset(block))

	override fun <Out : Any> project(block: ProjectStageOperators<Document, Out>.() -> Unit): BlockingMongoAggregationPipeline<Out> =
		BlockingMongoAggregationPipeline(inner.project(block))

	override fun unionWith(other: HasUnionWithCompatibility<Document>): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.unionWith(other))

	override fun <Out : Any> group(block: GroupStageOperators<Document, Out>.() -> Unit): BlockingMongoAggregationPipeline<Out> =
		BlockingMongoAggregationPipeline(inner.group(block))

	override fun <Out : Any> countTo(field: Field<Out, Number>): BlockingMongoAggregationPipeline<Out> =
		BlockingMongoAggregationPipeline(inner.countTo(field))

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): BlockingMongoAggregationPipeline<Out> =
		BlockingMongoAggregationPipeline(inner.countTo(field))

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.lookup(block))

	override fun <Item, Out : Any> unwind(block: UnwindStageOperators<Document, Item, Out>.() -> Unit): BlockingMongoAggregationPipeline<Out> =
		BlockingMongoAggregationPipeline(inner.unwind(block))

	override suspend fun debug(limit: Int): MongoAggregationPipeline.PipelineDebugReport {
		val upstream = inner.debug(limit)

		return MongoAggregationPipeline.PipelineDebugReport(
			stages = upstream.stages.map {
				when (it) {
					is SyncMongoAggregationPipeline.StageDebugReport.Success -> MongoAggregationPipeline.StageDebugReport.Success(
						stage = it.stage,
						results = it.results,
					)

					is SyncMongoAggregationPipeline.StageDebugReport.Failure -> MongoAggregationPipeline.StageDebugReport.Failure(
						stage = it.stage,
						failure = it.failure,
					)
				}
			},
			limit = upstream.limit,
		).also {
			// This module is only used in tests, we don't care about performance, especially of debug mode
			// Let's make sure the toString() implementation is exactly identical
			check(it.toString() == upstream.toString())
		}
	}

	@LowLevelApi
	override val context: BsonContext
		get() = inner.context

	@DangerousMongoApi
	@LowLevelApi
	override fun withStage(stage: BsonNode): BlockingMongoAggregationPipeline<Document> =
		BlockingMongoAggregationPipeline(inner.withStage(stage) as SyncMongoAggregationPipeline<Document>)

	@DangerousMongoApi
	@LowLevelApi
	override fun <New : Any> reinterpret(): BlockingMongoAggregationPipeline<New> =
		BlockingMongoAggregationPipeline(inner.reinterpret<New>() as SyncMongoAggregationPipeline<New>)

	@LowLevelApi
	override fun writeTo(writer: BsonValueWriter) =
		inner.writeTo(writer)

	override fun toString(): String =
		inner.toString()

	@LowLevelApi
	override fun embedInLookup(writer: BsonFieldWriter) =
		inner.embedInLookup(writer)

	@LowLevelApi
	override fun embedInUnionWith(writer: BsonFieldWriter) =
		inner.embedInUnionWith(writer)
}
