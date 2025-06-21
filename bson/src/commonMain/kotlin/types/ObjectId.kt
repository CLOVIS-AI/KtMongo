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

import opensavvy.ktmongo.bson.types.ObjectId.Companion.maxAt
import opensavvy.ktmongo.bson.types.ObjectId.Companion.minAt
import kotlin.experimental.and
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A 12-bytes identifier for MongoDB objects.
 *
 * This class allows accessing all fields of an ObjectId as well as constructing instances from existing data.
 * However, it doesn't provide a way to generate new randomized ObjectId instances (as that depends on the database configuration).
 * To do so, see [opensavvy.ktmongo.bson.BsonContext.newId].
 */
@ExperimentalTime
class ObjectId : Comparable<ObjectId> {

	/**
	 * The ObjectId creation timestamp, with a resolution of one second.
	 *
	 * This timestamp can represent time from the UNIX epoch (Jan 1 1970) and is stored as 32 unsigned bits
	 * (approximately Feb 2 2016, see [ObjectId.MAX]'s timestamp for an exact value).
	 */
	val timestamp: Instant

	/**
	 * 5-byte random value generated per client-side process.
	 *
	 * This random value is unique to the machine and process.
	 * If the process restarts of the primary node of the process changes, this value is re-regenerated.
	 *
	 * @see ObjectId.PROCESS_ID_BOUND
	 */
	val processId: Long

	/**
	 * A 3-byte incrementing counter per client-side process, initialized to a random value. Always positive.
	 *
	 * The counter resets when a process restarts.
	 *
	 * @see ObjectId.COUNTER_BOUND
	 */
	val counter: Int

	/**
	 * Constructs a new [ObjectId] from its different components.
	 */
	constructor(
		timestamp: Instant,
		processId: Long,
		counter: Int,
	) {
		require(processId < PROCESS_ID_BOUND) { "The process identifier part of an ObjectId must fit in 5 bytes ($PROCESS_ID_BOUND), but found: $processId" }
		require(counter >= 0) { "The counter part of an ObjectId must be positive, but found: $counter" }
		require(counter < COUNTER_BOUND) { "The counter part of an ObjectId must fit in 3 bytes ($COUNTER_BOUND), but found: $counter" }

		this.timestamp = timestamp
		this.processId = processId
		this.counter = counter
	}

	/**
	 * Constructs a new [ObjectId] by reading a byte array.
	 *
	 * [bytes] should be exactly 12-bytes long.
	 *
	 * To access the bytes of an existing ObjectId, see [ObjectId.bytes].
	 */
	constructor(bytes: ByteArray) {
		require(bytes.size == 12) { "ObjectId must be 12 bytes long, found ${bytes.size}" }

		val timestampPart = (bytes[0].toUByte().toUInt() shl 24) +
			(bytes[1].toUByte().toUInt() shl 16) +
			(bytes[2].toUByte().toUInt() shl 8) +
			(bytes[3].toUByte().toUInt())

		timestamp = Instant.fromEpochSeconds(timestampPart.toLong())

		processId = (bytes[4].toUByte().toLong() shl 32) +
			(bytes[5].toUByte().toLong() shl 24) +
			(bytes[6].toUByte().toLong() shl 16) +
			(bytes[7].toUByte().toLong() shl 8) +
			(bytes[8].toUByte().toLong())

		counter = (bytes[9].toUByte().toInt() shl 16) +
			(bytes[10].toUByte().toInt() shl 8) +
			(bytes[11].toUByte().toInt())
	}

	/**
	 * Constructs a new [ObjectId] by reading a hexadecimal representation.
	 *
	 * [hex] should be exactly 24 characters long (12 bytes).
	 *
	 * To access the hexadecimal representation of an existing ObjectId, see [ObjectId.hex].
	 */
	@OptIn(ExperimentalStdlibApi::class)
	constructor(hex: String) : this(hexToBytes(hex))

	/**
	 * Generates a byte representation of this [ObjectId] instance.
	 *
	 * Because [ByteArray] is mutable, each access will generate a new array.
	 *
	 * The output array can be passed to the [ObjectId] constructor to obtain a new identical [ObjectId] instance.
	 */
	val bytes: ByteArray
		get() {
			val bytes = ByteArray(12)

			val timestamp = timestamp.epochSeconds.toUInt()
			bytes[0] = (timestamp shr 24).toByte() and 0xFF.toByte()
			bytes[1] = (timestamp shr 16).toByte() and 0xFF.toByte()
			bytes[2] = (timestamp shr 8).toByte() and 0xFF.toByte()
			bytes[3] = timestamp.toByte() and 0xFF.toByte()

			require(processId < PROCESS_ID_BOUND) { "The process identifier part of an ObjectId must fit in 5 bytes ($PROCESS_ID_BOUND), but found: $processId" }
			bytes[4] = (processId shr 32).toByte() and 0xFF.toByte()
			bytes[5] = (processId shr 24).toByte() and 0xFF.toByte()
			bytes[6] = (processId shr 16).toByte() and 0xFF.toByte()
			bytes[7] = (processId shr 8).toByte() and 0xFF.toByte()
			bytes[8] = processId.toByte() and 0xFF.toByte()

			require(counter >= 0) { "The counter part of an ObjectId must be positive, but found: $counter" }
			require(counter < COUNTER_BOUND) { "The counter part of an ObjectId must fit in 3 bytes ($COUNTER_BOUND), but found: $counter" }
			bytes[9] = (counter shr 16).toByte() and 0xFF.toByte()
			bytes[10] = (counter shr 8).toByte() and 0xFF.toByte()
			bytes[11] = counter.toByte() and 0xFF.toByte()

			return bytes
		}

	/**
	 * Generates a hex representation of this [ObjectId] instance.
	 *
	 * The output string can be passed to [ObjectId] constructor to obtain a new identical [ObjectId] instance.
	 */
	@OptIn(ExperimentalStdlibApi::class)
	val hex: String by lazy(LazyThreadSafetyMode.PUBLICATION) { bytes.toHexString(HexFormat.Default) }

	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String =
		"ObjectId($hex)"

	override fun compareTo(other: ObjectId): Int {
		if (timestamp.epochSeconds != other.timestamp.epochSeconds)
			return timestamp.epochSeconds.compareTo(other.timestamp.epochSeconds)

		if (processId != other.processId)
			return processId.compareTo(other.processId)

		return counter.compareTo(other.counter)
	}

	operator fun compareTo(other: Instant): Int {
		if (timestamp.epochSeconds != other.epochSeconds)
			return timestamp.epochSeconds.compareTo(other.epochSeconds)

		// They have the same seconds component, let's differentiate them with the milliseconds
		// We deliberately ignore the milliseconds of the ObjectId
		return (timestamp.epochSeconds * 1000).compareTo(other.toEpochMilliseconds())
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ObjectId) return false

		if (processId != other.processId) return false
		if (counter != other.counter) return false
		if (timestamp.epochSeconds != other.timestamp.epochSeconds) return false

		return true
	}

	override fun hashCode(): Int {
		var result = processId.hashCode()
		result = 31 * result + counter
		result = 31 * result + timestamp.epochSeconds.hashCode()
		return result
	}

	companion object {

		/**
		 * The smallest integer that is not allowed in [ObjectId.processId].
		 *
		 * The minimum allowed value is 0.
		 */
		const val PROCESS_ID_BOUND = 1.toLong() shl (5 * 8)

		/**
		 * The smallest integer that is not allowed in [ObjectId.counter].
		 *
		 * The minimum allowed value is 0.
		 */
		const val COUNTER_BOUND = 1 shl (3 * 8)

		/**
		 * The minimum possible [ObjectId]: the one that is lesser or equal to all possible [ObjectId] instances.
		 */
		val MIN = ObjectId(Instant.fromEpochSeconds(0), 0, 0)

		/**
		 * The maximum possible [ObjectId]: the one that is greater or equal to all possible [ObjectId] instances.
		 */
		val MAX = ObjectId("FFFFFFFFFFFFFFFFFFFFFFFF")

		@OptIn(ExperimentalStdlibApi::class)
		private fun hexToBytes(hex: String): ByteArray {
			require(hex.length == 24) { "An ObjectId must be 24-characters long, found ${hex.length} characters: '$hex'" }
			return hex.hexToByteArray()
		}

		/**
		 * The minimum [ObjectId] created at [timestamp].
		 *
		 * It is guaranteed that:
		 * - All [ObjectId] instances created at [timestamp] are greater or equal to the output of this function.
		 * - All [ObjectId] instances created before [timestamp] are strictly lesser than the output of this function.
		 * - All [ObjectId] instances created after [timestamp] are strictly greater than the output of this function.
		 *
		 * This function is particularly helpful to create queries against ranges of timestamps.
		 *
		 * @see maxAt
		 * @see toObjectIdRange
		 */
		fun minAt(timestamp: Instant): ObjectId =
			ObjectId(timestamp, 0, 0)

		/**
		 * The maximum [ObjectId] created at [timestamp].
		 *
		 * It is guaranteed that:
		 * - All [ObjectId] instances created at [timestamp] are lesser or equal to the output of this function.
		 * - All [ObjectId] instances created before [timestamp] are strictly lesser than the output of this function.
		 * - All [ObjectId] instances created after [timestamp] are strictly greater than the output of this function.
		 *
		 * This function is particularly helpful to create queries against ranges of timestamps.
		 *
		 * @see minAt
		 * @see toObjectIdRange
		 */
		fun maxAt(timestamp: Instant): ObjectId =
			ObjectId(timestamp, PROCESS_ID_BOUND - 1, COUNTER_BOUND - 1)
	}
}

@ExperimentalTime
operator fun Instant.compareTo(objectId: ObjectId): Int =
	-objectId.compareTo(this)

/**
 * Converts a range of [Instant] to a range of [ObjectId].
 *
 * @see minAt
 * @see maxAt
 */
@ExperimentalTime
fun ClosedRange<Instant>.toObjectIdRange(): ClosedRange<ObjectId> =
	minAt(start)..maxAt(endInclusive)

/**
 * Converts a range of [Instant] to a range of [ObjectId].
 *
 * @see minAt
 * @see maxAt
 */
@ExperimentalTime
fun OpenEndRange<Instant>.toObjectIdRange(): OpenEndRange<ObjectId> =
	minAt(start)..<minAt(endExclusive)
