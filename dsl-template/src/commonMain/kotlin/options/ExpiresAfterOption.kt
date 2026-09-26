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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The duration after which the document expires.
 *
 * See [TimeSeriesDsl.expiresAfter].
 */
class ExpiresAfterOption private constructor(
	val duration: Duration,
	context: BsonContext,
	@Suppress("unused") overloadMarker: Unit,
) : AbstractOption("expireAfterSeconds", context) {

	constructor(
		duration: Duration,
		context: BsonContext,
	) : this(
		if (duration == Duration.ZERO) duration else duration.coerceAtLeast(1.seconds),
		context,
		Unit,
	)

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeInt64(duration.inWholeSeconds)
	}

	@LowLevelApi
	override fun merge(other: Option): Option {
		require(other is ExpiresAfterOption) { "Cannot merge sort options of different types: ${this::class} and ${other::class}" }

		// If this option is specified for both time-series and clustered collections,
		// the shortest specified time should apply
		return ExpiresAfterOption(
			duration = minOf(this.duration, other.duration),
			context,
		)
	}
}
