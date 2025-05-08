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
import kotlin.time.Duration

/**
 * Maximum [timeout] spent processing the request.
 *
 * For more information, see [WithMaxTime].
 */
class MaxTimeOption(
	val timeout: Duration,
	context: BsonContext,
) : AbstractOption("maxTimeMS", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeInt64(timeout.inWholeMilliseconds)
	}
}

/**
 * Maximum duration spent processing the request.
 *
 * See [maxTime].
 */
interface WithMaxTime : Options {

	/**
	 * Specifies a maximum amount of time for processing the request.
	 *
	 * ```kotlin
	 * collections.count {
	 *     options {
	 *         maxTime(10.seconds)
	 *     }
	 * }
	 * ```
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun maxTime(timeout: Duration) {
		accept(MaxTimeOption(timeout, context))
	}

}
