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
import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.AggregationPipeline
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

	@LowLevelApi
	@DangerousMongoApi
	override fun withStage(stage: BsonNode): MongoAggregationPipeline<Document>

	@LowLevelApi
	@DangerousMongoApi
	override fun <New : Any> reinterpret(): MongoAggregationPipeline<New>

	override fun limit(amount: Long): MongoAggregationPipeline<Document>

	override fun limit(amount: Int): MongoAggregationPipeline<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): MongoAggregationPipeline<Document>

	override fun sample(size: Int): MongoAggregationPipeline<Document>

	override fun <Out : Any> set(block: SetStageOperators<Document, Out>.() -> Unit): MongoAggregationPipeline<Out>

	override fun skip(amount: Long): MongoAggregationPipeline<Document>

	override fun skip(amount: Int): MongoAggregationPipeline<Document>

	override fun sort(block: SortOptionDsl<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun unset(block: UnsetStageOperators<Document>.() -> Unit): MongoAggregationPipeline<Document>

	override fun <Out : Any> project(block: ProjectStageOperators<Document, Out>.() -> Unit): MongoAggregationPipeline<Out>

	override fun unionWith(other: HasUnionWithCompatibility<Document>): MongoAggregationPipeline<Document>

	override fun <Out : Any> group(block: GroupStageOperators<Document, Out>.() -> Unit): MongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: Field<Out, Number>): MongoAggregationPipeline<Out>

	override fun <Out : Any> countTo(field: KProperty1<Out, Number>): MongoAggregationPipeline<Out>

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): MongoAggregationPipeline<Document>

	// endregion
	// region Debug

	/**
	 * Creates a [debug report][PipelineDebugReport] for this pipeline.
	 *
	 * A debug report helps understand what a pipeline does by displaying intermediate results after each stage.
	 *
	 * This method is useful during development, in tests, or to evaluate within the debugger.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .match { User::age gte 18 }
	 *     .sort { descending(User::age) }
	 *     .limit(2)
	 *     .debug()
	 * ```
	 * Then, print the debug report to the standard output or view it under the debugger.
	 *
	 * @param limit The maximum number of documents to display after each stage.
	 */
	suspend fun debug(
		limit: Int = 10,
	): PipelineDebugReport

	/**
	 * A debug report to help understand how this pipeline behaves.
	 *
	 * The report lists the different [stages] declared in the pipeline (in order).
	 * For each stage, the report gives the first few output documents.
	 *
	 * To generate a debug report, call [MongoAggregationPipeline.debug].
	 */
	data class PipelineDebugReport(
		/**
		 * The different stages declared in the pipeline, in order.
		 *
		 * The very first item is this list is the initial collection.
		 * Because it corresponds to no actual stage, it is represented by an empty document.
		 */
		val stages: List<StageDebugReport>,

		/**
		 * The `limit` parameter that was passed to [MongoAggregationPipeline.debug].
		 */
		val limit: Int,
	) {

		init {
			require(stages.isNotEmpty()) { "Even if the pipeline is empty, the first reported stage should be the direct collection content: Found ${stages.size} stages" }
		}

		override fun toString(): String = buildString {
			appendLine("Pipeline debug (first $limit resulting documents after each stage):")
			for (stage in stages) {
				appendLine("  ${stage.toString().replace("\n", "\n  ")}")
			}
		}
	}

	/**
	 * A debug report to help understand how a specific stage behaves.
	 *
	 * The report describes a [stage] and its output.
	 * A stage may be [successful][Success] or a [failure][Failure].
	 *
	 * To generate a debug report, call [MongoAggregationPipeline.debug].
	 */
	sealed class StageDebugReport {

		/**
		 * The stage that was executed.
		 *
		 * Note that the very first stage in a pipeline is always an empty document.
		 * See [PipelineDebugReport.stages] for more information.
		 */
		abstract val stage: BsonDocument

		/**
		 * A debug report for a successful stage.
		 */
		data class Success(
			override val stage: BsonDocument,
			/**
			 * The first documents output by this stage.
			 *
			 * To control the number of documents returned, see the parameters of [MongoAggregationPipeline.debug].
			 */
			val results: List<BsonDocument>,
		) : StageDebugReport() {

			override fun toString(): String = buildString {
				appendLine("Stage: $stage")
				for (result in results) {
					appendLine("  ${result.toString().replace("\n", "\n    ")}")
				}
			}
		}

		/**
		 * A debug report for a failed stage.
		 */
		data class Failure(
			override val stage: BsonDocument,
			/**
			 * The error that was thrown when trying to access the output of this stage.
			 */
			val failure: Throwable,
		) : StageDebugReport() {

			override fun toString(): String = buildString {
				appendLine("Stage: $stage")
				appendLine("  ${failure.stackTraceToString().replace("\n", "\n    ")}")
			}
		}
	}

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
