/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.types

import opensavvy.ktmongo.bson.types.Timestamp.Companion.MAX_COUNTER
import opensavvy.ktmongo.bson.types.Timestamp.Companion.MAX_INSTANT
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Internal MongoDB timestamp used in the oplog.
 *
 * Use the [kotlin.time.Instant] type for operations involving dates.
 */
class Timestamp(
	/**
	 * The raw value for this timestamp.
	 *
	 * The first four bytes are an increment, the last four are a timestamp.
	 * Note that this means sorting the timestamps by this value will not output a date-based sort.
	 * However, the [Comparable] implementation of this class does allow sorting with a date-based order.
	 */
	val value: Long,
) : Comparable<Timestamp> {

	/**
	 * Constructs a timestamp from its [Timestamp.instant] and [Timestamp.counter] components.
	 */
	@ExperimentalTime
	constructor(instant: Instant, counter: UInt) : this(
		(counter.toULong() shl 32).toLong() + (instant.epochSeconds % (1L shl 32))
	)

	/**
	 * Date and time represented by this [Timestamp], with a precision of one second.
	 *
	 * A [Timestamp] can represent seconds between the UNIX epoch (Jan 1st 1970) and [MAX_INSTANT].
	 */
	@ExperimentalTime
	val instant: Instant
		get() = Instant.fromEpochSeconds(value and UInt.MAX_VALUE.toLong())

	/**
	 * Incrementing counter.
	 */
	val counter: UInt
		get() = (value.toULong() shr 32).toUInt()

	@OptIn(ExperimentalTime::class)
	override fun compareTo(other: Timestamp): Int =
		chronologicalComparator.compare(this, other)

	// region Identity

	override fun equals(other: Any?): Boolean =
		other is Timestamp && value == other.value

	override fun hashCode(): Int = value.hashCode()

	@OptIn(ExperimentalTime::class)
	override fun toString(): String =
		"Timestamp($instant, #$counter)"

	// endregion

	companion object {

		/**
		 * The smallest possible [Timestamp] instance.
		 *
		 * This timestamp marks the UNIX epoch, Jan 1st 1970.
		 */
		val MIN get() = Timestamp(0)

		/**
		 * The largest possible [Timestamp] instance.
		 *
		 * It is composed using [MAX_INSTANT] and [MAX_COUNTER].
		 */
		val MAX get() = Timestamp(-1)

		/**
		 * The maximum possible instant that can be represented by a [Timestamp], which will happen during the year 2106.
		 */
		@ExperimentalTime
		val MAX_INSTANT get() = Instant.fromEpochSeconds(UInt.MAX_VALUE.toLong())

		/**
		 * The maximum possible counter for a given instant.
		 */
		val MAX_COUNTER get() = UInt.MAX_VALUE

		@ExperimentalTime
		val chronologicalComparator = compareBy<Timestamp>(
			{ it.instant },
			{ it.counter },
		)
	}
}
