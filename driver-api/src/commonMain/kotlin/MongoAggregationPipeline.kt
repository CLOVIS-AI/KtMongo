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

package opensavvy.ktmongo.api

import kotlinx.coroutines.flow.Flow
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AccumulationOperators
import opensavvy.ktmongo.dsl.aggregation.AggregationPipeline
import opensavvy.ktmongo.dsl.aggregation.stages.HasUnionWithCompatibility
import opensavvy.ktmongo.dsl.aggregation.stages.ProjectStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.SetStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.UnsetStageOperators
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A multi-stage aggregation pipeline that transforms documents from a MongoDB collection.
 *
 * Pipelines are immutable. Each stage method returns a new pipeline with the stage appended.
 *
 * To obtain a pipeline, use [MongoCollection.aggregate][opensavvy.ktmongo.api.operations.AggregationOperations.aggregate].
 *
 * ### Example
 *
 * ```kotlin
 * class User(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * users.aggregate()
 *     .match { User::age gt 18 }
 *     .sort { ascending(User::name) }
 *     .toList()
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/aggregation-pipeline/)
 */
interface MongoAggregationPipeline<Document : Any> : AggregationPipeline<Document> {

	// region Iterable

	/**
	 * Access the data of this pipeline as a [MongoIterable].
	 *
	 * The methods of [MongoIterable] are available directly on this type
	 * as extension methods, there is no need to convert to a [MongoIterable] yourself.
	 *
	 * If [type] doesn't match [Document], the behavior is unspecified.
	 */
	@LowLevelApi
	fun asIterable(type: KType): MongoIterable<Document>

	// endregion
	// region Stages

	override fun limit(amount: Long): MongoAggregationPipeline<Document>

	override fun limit(amount: Int): MongoAggregationPipeline<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun sample(size: Int): MongoAggregationPipeline<Document>

	override fun set(block: SetStageOperators<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun skip(amount: Long): MongoAggregationPipeline<Document>

	override fun skip(amount: Int): MongoAggregationPipeline<Document>

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun project(block: ProjectStageOperators<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun unionWith(other: HasUnionWithCompatibility<Document>): MongoAggregationPipeline<Document>

	override fun <Out : Any> group(block: AccumulationOperators<Document, Out>.() -> Unit): MongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: Field<Out, Number>): MongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): MongoAggregationPipeline<Out>

	// endregion
}

/**
 * Returns the first document found by this query, or throws an exception.
 *
 * @throws NoSuchElementException If this query returned no results.
 * @see firstOrNull Return `null` instead of throwing an exception.
 */
@OptIn(LowLevelApi::class)
suspend inline fun <reified Document : Any> MongoAggregationPipeline<Document>.first(): Document =
	asIterable(typeOf<Document>()).first()

/**
 * Returns the first document found by this query, or returns `null`.
 *
 * @see first Throw an exception instead of returning `null`.
 */
@OptIn(LowLevelApi::class)
suspend inline fun <reified Document : Any> MongoAggregationPipeline<Document>.firstOrNull(): Document? =
	asIterable(typeOf<Document>()).firstOrNull()

/**
 * Executes [action] for each document returned by this query.
 *
 * This method streams all returned documents into the [action] function.
 * The entire response set is not loaded at once into memory.
 *
 * MongoDB cursors are batched: a batch is queried, processed, then another batch is requested, etc.
 * The batch size can be configured in the operation creating this iterable.
 *
 * If the operation contains a sort without an index, MongoDB will load all results
 * into memory. The driver will still stream the results.
 *
 * @see toList Store all results in a [List].
 * @see toSet Store all results in a [Set].
 * @see asFlow Stream all results in a [Flow].
 */
@OptIn(LowLevelApi::class)
suspend inline fun <reified Document : Any> MongoAggregationPipeline<Document>.forEach(noinline action: suspend (Document) -> Unit) =
	asIterable(typeOf<Document>()).forEach(action)

/**
 * Reads the entirety of this iterable into a [List].
 *
 * Since lists are in-memory, this method loads the entirety of the results into memory.
 *
 * @see forEach Execute an action for each result.
 * @see toSet Store all results in a [Set].
 * @see asFlow Stream all results in a [Flow].
 */
@OptIn(LowLevelApi::class)
suspend inline fun <reified Document : Any> MongoAggregationPipeline<Document>.toList(): List<Document> =
	asIterable(typeOf<Document>()).toList()

/**
 * Reads the entirety of this iterable into a [Set].
 *
 * Since sets are in-memory, this method loads the entirety of the results into memory.
 *
 * @see forEach Execute an action for each result.
 * @see toList Store all results in a [List].
 * @see asFlow Stream all results in a [Flow].
 */
@OptIn(LowLevelApi::class)
suspend inline fun <reified Document : Any> MongoAggregationPipeline<Document>.toSet(): Set<Document> =
	asIterable(typeOf<Document>()).toSet()

/**
 * Streams the results into a [Flow].
 *
 * The flow is lazy: new elements are streamed in when the consumer requests them.
 *
 * MongoDB cursors are batched: a batch is queried, processed, then another batch is requested, etc.
 * The batch size can be configured in the operation creating this iterable.
 *
 * If you intend to query a large number of batches and
 * perform complex operations on them, we recommend using
 * [buffer][kotlinx.coroutines.flow.buffer] with a low capacity,
 * to reduce latency between two batches.
 *
 * @see forEach Execute an action for each result.
 * @see toList Store all results in a [List].
 * @see toSet Store all results in a [Set].
 */
@OptIn(LowLevelApi::class)
inline fun <reified Document : Any> MongoAggregationPipeline<Document>.asFlow(): Flow<Document> =
	asIterable(typeOf<Document>()).asFlow()
