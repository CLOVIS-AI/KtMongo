/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
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
