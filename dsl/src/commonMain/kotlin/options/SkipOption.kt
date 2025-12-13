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

import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Number of documents to skip before processing the request.
 *
 * For more information, see [WithSkip].
 */
class SkipOption(
	val skip: Long,
	context: BsonContext,
) : AbstractOption("skip", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeInt64(skip)
	}
}

/**
 * Number of documents to skip before processing the request.
 *
 * See [skip].
 */
interface WithSkip : Options {

	/**
	 * The number of documents to skip before processing the request.
	 *
	 * ```kotlin
	 * collections.count {
	 *     options {
	 *         skip(99)
	 *     }
	 * }
	 * ```
	 */
	fun skip(skip: Int) {
		skip(skip.toLong())
	}

	/**
	 * The number of documents to skip before processing the request.
	 *
	 * ```kotlin
	 * collections.count {
	 *     options {
	 *         skip(99L)
	 *     }
	 * }
	 * ```
	 *
	 * Note that not all drivers support specifying a skip larger than an `Int`.
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun skip(skip: Long) {
		accept(SkipOption(skip, context))
	}

}
