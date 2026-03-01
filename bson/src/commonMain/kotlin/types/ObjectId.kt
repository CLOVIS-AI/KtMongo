/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opensavvy.ktmongo.bson.types.ObjectId.Companion.maxAt
import opensavvy.ktmongo.bson.types.ObjectId.Companion.minAt
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.experimental.and
import kotlin.time.Instant

/**
 * Small, likely unique, fast to generate, ordered identifier.
 *
 * - **Small**: Each `ObjectId` is 12 bytes long, shorter than a UUID's 16 bytes.
 * - **Likely unique**: Each driver randomly selects a generation range, so conflicts are unlikely.
 * - **Fast to generate**: Depending on the [generation strategy][ObjectIdGenerator], generating a new `ObjectId` may
 * be as simple as querying the time and incrementing a counter.
 * - **Ordered**: All `ObjectId` instances can be sorted by creating date, with a precision of one second.
 * Two `ObjectId` instances created by the same driver are sorted with even more precision.
 *
 * ### Composition
 *
 * An `ObjectId` is composed of:
 * - A 4-byte [timestamp], representing the `ObjectId`'s creation date, with a precision of one second,
 * - A 5-byte random [processId], unique to each machine and process, to decrease conflicts,
 * - A 3-byte incrementing [counter], initialized at a random value.
 *
 * `ObjectId` instances are generally represented as 24-characters hexadecimal strings:
 * ```kotlin
 * ObjectId("699dfad90ca573f85c0eec1c").hex // 699dfad90ca573f85c0eec1c
 * ```
 *
 * ### Generating new ObjectIds
 *
 * There are different strategies to generate new `ObjectId` instances; they are encoded by the [ObjectIdGenerator] interface.
 *
 * The [ObjectIdGenerator] used by each driver is available directly on each collection:
 * ```kotlin
 * users.insert(
 *     User(
 *         _id = users.newId(), // Generate an ObjectId using the configuration specific to that collection
 *         name = "Bob",
 *     )
 * )
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/bson-types/#std-label-objectid)
 *
 * ### Thread safety
 *
 * Instances of this class are immutable and thread-safe.
 */
@Serializable(with = ObjectId.Serializer::class)
class ObjectId : Comparable<ObjectId> {

	/**
	 * The ObjectId creation timestamp, with a resolution of one second.
	 *
	 * This timestamp can represent time from the UNIX epoch (Jan 1 1970) and is stored as 32 unsigned bits
	 * (approximately Feb 2 2106, see [ObjectId.MAX]'s timestamp for an exact value).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * ObjectId("699dfad90ca573f85c0eec1c").timestamp // 2026-02-24T19:24:09Z
	 * ```
	 */
	val timestamp: Instant

	/**
	 * 5-byte random value generated per client-side process.
	 *
	 * This random value is unique to the machine and process.
	 * If the process restarts of the primary node of the process changes, this value is re-regenerated.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * ObjectId("699dfad90ca573f85c0eec1c").processId // 54315448412
	 * ```
	 *
	 * @see ObjectId.PROCESS_ID_BOUND
	 */
	val processId: Long

	/**
	 * A 3-byte incrementing counter per client-side process, initialized to a random value. Always positive.
	 *
	 * The counter resets when a process restarts.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * ObjectId("699dfad90ca573f85c0eec1c").counter // 977948
	 * ```
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
	val hex: String by lazy(LazyThreadSafetyMode.PUBLICATION) { bytes.toHexString(HexFormat.Default) }

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

	/**
	 * Default serializer for [ObjectId].
	 *
	 * `:bson-multiplatform` and `:bson-official` both override this serializer.
	 * This serializer exists so that `@Contextual` is not required.
	 * It may also be used to convert the DTOs to other formats, like JSON.
	 *
	 * Using this serializer, [ObjectId] is represented as if it were a [String].
	 *
	 * Avoid interacting with this type directly.
	 */
	@LowLevelApi
	object Serializer : KSerializer<ObjectId> {
		override val descriptor: SerialDescriptor
			get() = PrimitiveSerialDescriptor("opensavvy.ktmongo.bson.types.ObjectId", PrimitiveKind.STRING)

		@LowLevelApi
		override fun serialize(encoder: Encoder, value: ObjectId) {
			serializeObjectIdPlatformSpecific(encoder, value)
		}

		override fun deserialize(decoder: Decoder): ObjectId =
			deserializeObjectIdPlatformSpecific(decoder)
	}
}

internal fun serializeObjectIdAsString(encoder: Encoder, value: ObjectId) {
	encoder.encodeString(value.hex)
}

internal fun deserializeObjectIdAsString(decoder: Decoder): ObjectId =
	decoder.decodeString().let(::ObjectId)

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [serializeObjectIdAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
internal expect fun serializeObjectIdPlatformSpecific(encoder: Encoder, value: ObjectId)

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [deserializeObjectIdAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
internal expect fun deserializeObjectIdPlatformSpecific(decoder: Decoder): ObjectId

operator fun Instant.compareTo(objectId: ObjectId): Int =
	-objectId.compareTo(this)

/**
 * Converts a range of [Instant] to a range of [ObjectId].
 *
 * @see minAt
 * @see maxAt
 */
fun ClosedRange<Instant>.toObjectIdRange(): ClosedRange<ObjectId> =
	minAt(start)..maxAt(endInclusive)

/**
 * Converts a range of [Instant] to a range of [ObjectId].
 *
 * @see minAt
 * @see maxAt
 */
fun OpenEndRange<Instant>.toObjectIdRange(): OpenEndRange<ObjectId> =
	minAt(start)..<minAt(endExclusive)
