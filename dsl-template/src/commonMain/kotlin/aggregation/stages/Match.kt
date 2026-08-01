/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode

/**
 * Pipeline implementing the `$match` stage.
 */
@KtMongoDsl
interface HasMatch<Document : Any> : Pipeline<Document> {

	/**
	 * Filters documents based on a specified [filter].
	 *
	 * Matched documents are passed to the next pipeline stage.
	 *
	 * ### Pipeline optimization
	 *
	 * Place the `match` call as early in the pipeline as possible.
	 * Because `match` limits the total number of elements being processed, earlier `match` operations
	 * minimize the amount of processing down the pipe.
	 *
	 * If you place a `match` at the very beginning of a pipeline, the query can take advantage of indexes.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/match/)
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun match(
		filter: FilterQuery<Document>.() -> Unit,
	): Pipeline<Document> =
		withStage(MatchStage(FilterQuery<Document>(context).apply(filter), context))

	/**
	 * Filters documents based on a specific [filter], written using [aggregation syntax][AggregationOperators].
	 *
	 * Matched documents are passed to the next pipeline stage.
	 *
	 * This method is a helper to combine the [match] stage with the [expr][FilterQuery.expr] operator.
	 *
	 * ### Example
	 *
	 * Find all incoherent users: users modified before they were created.
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val creationDate: Instant,
	 *     val modificationDate: Instant,
	 * )
	 *
	 * val incoherent = users.aggregate()
	 *     .matchExpr { User::modificationDate lt User::creationDate }
	 *     .toList()
	 * ```
	 *
	 * The above query is syntax sugar for this identical query:
	 * ```kotlin
	 * val incoherent = users.aggregate()
	 *     .match {
	 *         expr { User::modificationDate lt User::creationDate }
	 *     }
	 *     .toList()
	 * ```
	 *
	 * @see match The `$match` stage.
	 * @see FilterQuery.expr The `$expr` operator, allowing to use aggregation syntax in a filter.
	 */
	@KtMongoDsl
	fun matchExpr(
		filter: AggregationOperators.() -> Value<Document, Boolean>,
	): Pipeline<Document> =
		match { expr(filter) }

}

private class MatchStage(
	val expression: FilterQuery<*>,
	context: BsonContext,
) : AbstractBsonNode(context) {
	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$match") {
			expression.writeTo(this)
		}
	}
}
