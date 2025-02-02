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
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression

/**
 * Pipeline implementing the `$limit` stage.
 */
@KtMongoDsl
interface HasLimit<Document : Any> : Pipeline<Document> {

	/**
	 * Limits the number of elements passed to the next stage to [amount].
	 *
	 * ### Using limit with sorted results
	 *
	 * Sort results aren't stable with `limit`: if multiple documents are identical, their relative order is undefined
	 * and may change from one execution to the next.
	 *
	 * To avoid surprises, include a unique field in your sort, for example `_id`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/limit/)
	 *
	 * @see HasSkip.skip Skip over an amount of elements.
	 * @see HasSample.sample Randomly limit the number of elements.
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun limit(amount: Long): Pipeline<Document> =
		withStage(LimitStage(amount, context))

	/**
	 * Limits the number of elements passed to the next stage to [amount].
	 *
	 * ### Using limit with sorted results
	 *
	 * Sort results aren't stable with `limit`: if multiple documents are identical, their relative order is undefined
	 * and may change from one execution to the next.
	 *
	 * To avoid surprises, include a unique field in your sort, for example `_id`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/limit/)
	 *
	 * @see HasSkip.skip Skip over an amount of elements.
	 * @see HasSample.sample Randomly limit the number of elements.
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun limit(amount: Int): Pipeline<Document> =
		limit(amount.toLong())
}

private class LimitStage(
	val amount: Long,
	context: BsonContext,
) : AbstractExpression(context) {

	init {
		require(amount >= 0) { "Negative limits are not allowed. Found: $amount" }
	}

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeInt64("\$limit", amount)
	}
}
