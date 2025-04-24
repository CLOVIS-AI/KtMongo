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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Maximum number of elements analyzed by this operation.
 *
 * For more information, see [WithLimit].
 */
class LimitOption(
	val limit: Long,
	context: BsonContext,
) : AbstractOption("limit", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeInt64(limit)
	}
}

/**
 * Limits the number of elements returned by a query.
 *
 * See [limit].
 */
interface WithLimit : Options {

	/**
	 * The maximum number of matching documents to return.
	 *
	 * ```kotlin
	 * collections.count {
	 *     options {
	 *         limit(99)
	 *     }
	 * }
	 * ```
	 */
	fun limit(limit: Int) {
		limit(limit.toLong())
	}

	/**
	 * The maximum number of matching documents to return.
	 *
	 * ```kotlin
	 * collections.count {
	 *     options {
	 *         limit(99L)
	 *     }
	 * }
	 * ```
	 *
	 * Note that not all drivers support specifying a limit larger than an `Int`.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun limit(limit: Long) {
		accept(LimitOption(limit, context))
	}

}
