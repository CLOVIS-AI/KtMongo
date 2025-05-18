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

package opensavvy.ktmongo.bson.multiplatform.types

import opensavvy.ktmongo.bson.multiplatform.types.ObjectId.Companion.counterMax
import opensavvy.ktmongo.bson.multiplatform.types.ObjectId.Companion.processIdMax
import kotlin.experimental.and
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A 12-bytes identifier for MongoDB objects.
 */
@ExperimentalTime
class ObjectId(
	/**
	 * The ObjectId creation timestamp, with a resolution of one second.
	 */
	val timestamp: Instant,

	/**
	 * 5-byte random value generated per client-side process.
	 *
	 * This random value is unique to the machine and process.
	 * If the process restarts of the primary node of the process changes, this value is re-regenerated.
	 *
	 * @see ObjectId.processIdMax
	 */
	val processId: Long,

	/**
	 * A 3-byte incrementing counter per client-side process, initialized to a random value. Always positive.
	 *
	 * The counter resets when a process restarts.
	 *
	 * @see ObjectId.counterMax
	 */
	val counter: Int,
) : Comparable<ObjectId> {

	init {
		require(bytes.size == 12) { "ObjectId must be 12 bytes long, found ${bytes.size}" }
		require(processId < processIdMax) { "The process identifier part of an ObjectId must fit in 5 bytes ($processIdMax), but found: $processId" }
		require(counter >= 0) { "The counter part of an ObjectId must be positive, but found: $counter" }
		require(counter < counterMax) { "The counter part of an ObjectId must fit in 3 bytes ($counterMax), but found: $counter" }
	}

	/**
	 * Generates a byte representation of this [ObjectId] instance.
	 *
	 * Because [ByteArray] is mutable, each access will generate a new array.
	 *
	 * The output array can be passed to [ObjectId.fromBytes] to obtain a new identical [ObjectId] instance.
	 */
	val bytes: ByteArray
		get() = partsToArray(timestamp, processId, counter)

	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String =
		"ObjectId(${bytes.toHexString(HexFormat.Default)})"

	override fun compareTo(other: ObjectId): Int {
		if (timestamp.epochSeconds != other.timestamp.epochSeconds)
			return timestamp.epochSeconds.compareTo(other.timestamp.epochSeconds)

		if (processId != other.processId)
			return processId.compareTo(other.processId)

		return counter.compareTo(other.counter)
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
		const val processIdMax = 1.toLong() shl (5 * 8)

		/**
		 * The smallest integer that is not allowed in [ObjectId.counter].
		 *
		 * The minimum allowed value is 0.
		 */
		const val counterMax = 1 shl (3 * 8)

		/**
		 * Reads 12 [bytes] into an [ObjectId].
		 *
		 * The symmetric operation is [ObjectId.bytes].
		 */
		fun fromBytes(bytes: ByteArray): ObjectId = arrayToParts(bytes)
	}
}

@ExperimentalTime
private fun partsToArray(
	timestamp: Instant,
	processId: Long,
	counter: Int,
): ByteArray {
	val bytes = ByteArray(12)

	val timestamp = timestamp.epochSeconds.toUInt()
	bytes[0] = (timestamp shr 24).toByte() and 0xFF.toByte()
	bytes[1] = (timestamp shr 16).toByte() and 0xFF.toByte()
	bytes[2] = (timestamp shr 8).toByte() and 0xFF.toByte()
	bytes[3] = timestamp.toByte() and 0xFF.toByte()

	require(processId < processIdMax) { "The process identifier part of an ObjectId must fit in 5 bytes ($processIdMax), but found: $processId" }
	bytes[4] = (processId shr 32).toByte() and 0xFF.toByte()
	bytes[5] = (processId shr 24).toByte() and 0xFF.toByte()
	bytes[6] = (processId shr 16).toByte() and 0xFF.toByte()
	bytes[7] = (processId shr 8).toByte() and 0xFF.toByte()
	bytes[8] = processId.toByte() and 0xFF.toByte()

	require(counter >= 0) { "The counter part of an ObjectId must be positive, but found: $counter" }
	require(counter < counterMax) { "The counter part of an ObjectId must fit in 3 bytes ($counterMax), but found: $counter" }
	bytes[9] = (counter shr 16).toByte() and 0xFF.toByte()
	bytes[10] = (counter shr 8).toByte() and 0xFF.toByte()
	bytes[11] = counter.toByte() and 0xFF.toByte()

	return bytes
}

@OptIn(ExperimentalTime::class)
private fun arrayToParts(
	bytes: ByteArray,
): ObjectId {
	require(bytes.size == 12) { "ObjectId must be 12 bytes long, found ${bytes.size}" }

	val timestampPart = (bytes[0].toUByte().toUInt() shl 24) +
		(bytes[1].toUByte().toUInt() shl 16) +
		(bytes[2].toUByte().toUInt() shl 8) +
		(bytes[3].toUByte().toUInt())

	val timestamp = Instant.fromEpochSeconds(timestampPart.toLong())

	val processId = (bytes[4].toUByte().toLong() shl 32) +
		(bytes[5].toUByte().toLong() shl 24) +
		(bytes[6].toUByte().toLong() shl 16) +
		(bytes[7].toUByte().toLong() shl 8) +
		(bytes[8].toUByte().toLong())

	val counter = (bytes[9].toUByte().toInt() shl 16) +
		(bytes[10].toUByte().toInt() shl 8) +
		(bytes[11].toUByte().toInt())

	return ObjectId(timestamp, processId, counter)
}
