/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.options.common

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.expr.common.CompoundExpression

/**
 * Limits the number of elements returned by a query.
 *
 * See [limit].
 */
interface LimitOption : CompoundExpression {

	/**
	 * The maximum number of matching documents to return.
	 */
	fun limit(limit: Int) {
		limit(limit.toLong())
	}

	/**
	 * The maximum number of matching documents to return.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun limit(limit: Long) {
		accept(LimitOptionExpression(limit, context))
	}

	private class LimitOptionExpression(
		private val limit: Long,
		context: BsonContext,
	) : AbstractExpression(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeInt64("limit", limit)
		}
	}

}
