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
import opensavvy.ktmongo.dsl.query.common.AbstractExpression

/**
 * Pipeline implementing the `$skip` stage.
 */
@KtMongoDsl
interface HasSkip<Document : Any> : Pipeline<Document> {

	/**
	 * Skips over the specified [amount] of documents that pass into the stage,
	 * and passes the remaining documents to the next stage.
	 *
	 * ### Using skip with sorted results
	 *
	 * Sort results aren't stable with `skip`: if multiple documents are identical, their relative order is undefined
	 * and may change from one execution to the next.
	 *
	 * To avoid surprises, include a unique field in your sort, for example `_id`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/skip/)
	 *
	 * @see HasLimit.limit Limit the number of elements.
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun skip(amount: Long): Pipeline<Document> =
		withStage(SkipStage(amount, context))

	/**
	 * Skips over the specified [amount] of documents that pass into the stage,
	 * and passes the remaining documents to the next stage.
	 *
	 * ### Using skip with sorted results
	 *
	 * Sort results aren't stable with `skip`: if multiple documents are identical, their relative order is undefined
	 * and may change from one execution to the next.
	 *
	 * To avoid surprises, include a unique field in your sort, for example `_id`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/skip/)
	 *
	 * @see HasLimit.limit Limit the number of elements.
	 */
	@KtMongoDsl
	fun skip(amount: Int): Pipeline<Document> =
		skip(amount.toLong())

}

private class SkipStage(
	val amount: Long,
	context: BsonContext,
) : AbstractExpression(context) {

	init {
		require(amount >= 0) { "At least 0 elements should be skipped. Found: $amount" }
	}

	@LowLevelApi
	override fun simplify(): AbstractExpression? =
		when {
			amount == 0L -> null
			else -> this
		}

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeInt64("\$skip", amount)
	}
}
