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

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AccumulationOperators
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.stages.*
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.sync.api.MongoAggregationPipeline
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * An aggregation pipeline built on top of the
 * [official Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
 *
 * To start a pipeline, call [MongoCollection.aggregate][SyncMongoCollection.aggregate].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/aggregation-pipeline/)
 */
interface SyncMongoAggregationPipeline<Document : Any> : MongoAggregationPipeline<Document> {

	@LowLevelApi
	override fun asIterable(type: KType): SyncMongoAggregateIterable<Document>

	override fun limit(amount: Long): SyncMongoAggregationPipeline<Document>

	override fun limit(amount: Int): SyncMongoAggregationPipeline<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): SyncMongoAggregationPipeline<Document>

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): SyncMongoAggregationPipeline<Document>

	override fun sample(size: Int): SyncMongoAggregationPipeline<Document>

	override fun set(block: SetStageOperators<Document>.() -> Unit): SyncMongoAggregationPipeline<Document>

	override fun skip(amount: Long): SyncMongoAggregationPipeline<Document>

	override fun skip(amount: Int): SyncMongoAggregationPipeline<Document>

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): SyncMongoAggregationPipeline<Document>

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): SyncMongoAggregationPipeline<Document>

	override fun project(block: ProjectStageOperators<Document>.() -> Unit): SyncMongoAggregationPipeline<Document>

	override fun unionWith(other: HasUnionWithCompatibility<Document>): SyncMongoAggregationPipeline<Document>

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): SyncMongoAggregationPipeline<Document>

	override fun <Out : Any> group(block: AccumulationOperators<Document, Out>.() -> Unit): SyncMongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: Field<Out, Number>): SyncMongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): SyncMongoAggregationPipeline<Out>

}

/**
 * Access the data of this pipeline as a [MongoIterable].
 *
 * The methods of [MongoIterable] are available directly on this type
 * as extension methods, there is no need to convert to a [MongoIterable] yourself.
 */
@OptIn(LowLevelApi::class)
inline fun <reified Document : Any> SyncMongoAggregationPipeline<Document>.asIterable(): SyncMongoAggregateIterable<Document> =
	this.asIterable(typeOf<Document>())
