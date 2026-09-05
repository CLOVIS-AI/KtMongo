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

import opensavvy.ktmongo.api.MongoAggregationPipeline
import opensavvy.ktmongo.api.MongoIterable
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
import kotlin.reflect.typeOf

/**
 * An aggregation pipeline built on top of the Multiplatform driver.
 *
 * To start a pipeline, call [opensavvy.ktmongo.api.MongoCollection.aggregate].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/aggregation-pipeline/)
 */
interface MultiplatformMongoAggregationPipeline<Document : Any> : MongoAggregationPipeline<Document> {

	@LowLevelApi
	override fun asIterable(type: KType): MultiplatformMongoIterable<Document>

	@LowLevelApi
	@DangerousMongoApi
	override fun withStage(stage: BsonNode): MultiplatformMongoAggregationPipeline<Document>

	@LowLevelApi
	@DangerousMongoApi
	override fun <New : Any> reinterpret(): MultiplatformMongoAggregationPipeline<New>

	override fun limit(amount: Long): MultiplatformMongoAggregationPipeline<Document>

	override fun limit(amount: Int): MultiplatformMongoAggregationPipeline<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): MultiplatformMongoAggregationPipeline<Document>

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): MultiplatformMongoAggregationPipeline<Document>

	override fun sample(size: Int): MultiplatformMongoAggregationPipeline<Document>

	override fun <Out : Any> set(block: SetStageOperators<Document, Out>.() -> Unit): MultiplatformMongoAggregationPipeline<Out>

	override fun skip(amount: Long): MultiplatformMongoAggregationPipeline<Document>

	override fun skip(amount: Int): MultiplatformMongoAggregationPipeline<Document>

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): MultiplatformMongoAggregationPipeline<Document>

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): MultiplatformMongoAggregationPipeline<Document>

	override fun <Out : Any> project(block: ProjectStageOperators<Document, Out>.() -> Unit): MultiplatformMongoAggregationPipeline<Out>

	override fun unionWith(other: HasUnionWithCompatibility<Document>): MultiplatformMongoAggregationPipeline<Document>

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): MultiplatformMongoAggregationPipeline<Document>

	override fun <Out : Any> group(block: GroupStageOperators<Document, Out>.() -> Unit): MultiplatformMongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: Field<Out, Number>): MultiplatformMongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): MultiplatformMongoAggregationPipeline<Out>

	override fun <Item, Out : Any> unwind(block: UnwindStageOperators<Document, Item, Out>.() -> Unit): MultiplatformMongoAggregationPipeline<Out>

}

/**
 * Access the data of this pipeline as a [MongoIterable].
 *
 * The methods of [MongoIterable] are available directly on this type
 * as extension methods, there is no need to convert to a [MongoIterable] yourself.
 */
@OptIn(LowLevelApi::class)
inline fun <reified Document : Any> MultiplatformMongoAggregationPipeline<Document>.asIterable(): MultiplatformMongoIterable<Document> =
	this.asIterable(typeOf<Document>())
