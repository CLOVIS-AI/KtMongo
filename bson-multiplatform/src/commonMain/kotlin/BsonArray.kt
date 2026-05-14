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

package opensavvy.ktmongo.bson.multiplatform

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.BsonDecodingException
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.BsonArray.Serializer
import opensavvy.ktmongo.bson.multiplatform.impl.read.MultiplatformBsonArrayList
import opensavvy.ktmongo.bson.multiplatform.serialization.BsonDecoderTopLevel
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType

/**
 * Pure Kotlin implementation of [opensavvy.ktmongo.bson.BsonArray].
 *
 * To create instances of this class, see [BsonFactory].
 *
 * > This page describes the BSON multiplatform implementation.
 * > To learn more about the general BSON operations, see [opensavvy.ktmongo.bson.BsonArray].
 *
 * ### Navigating BSON types
 *
 * This class is part of the BSON trinity:
 *
 * - [BsonDocument][opensavvy.ktmongo.bson.multiplatform.BsonDocument] represents an entire BSON document.
 * - [BsonArray][opensavvy.ktmongo.bson.multiplatform.BsonArray] represents an array of BSON values.
 * - [BsonValue][opensavvy.ktmongo.bson.multiplatform.BsonValue] represents a single value in isolation.
 *
 * ### Usage
 *
 * ```kotlin
 * val bson: BsonArray = …
 *
 * for (element in bson) {
 *     println("Element: $element")
 * }
 * ```
 *
 * ### Thread-safety
 *
 * This class is **not thread-safe**.
 * Although it is not possible to mutate its state, this class uses internal mutation to lazily decode the BSON stream.
 */
@Serializable(with = Serializer::class)
class BsonArray internal constructor(
	override val factory: BsonFactory,
	private val bytesWithHeader: Bytes,
) : BsonArray {

	@OptIn(LowLevelApi::class)
	private val list = MultiplatformBsonArrayList(factory, bytesWithHeader)

	// region Access

	@ExperimentalSerializationApi
	@LowLevelApi
	override fun <T> decode(type: KType): T {
		val decoder = BsonDecoderTopLevel(factory, bytesWithHeader)
		@Suppress("UNCHECKED_CAST")
		val serializer = serializer(type) as KSerializer<T>
		return try {
			decoder.decodeSerializableValue(serializer)
		} catch (e: BsonDecodingException) {
			throw BsonDecodingException("Could not decode ${serializer.descriptor}\n\tfrom value $this", e)
		}
	}

	@OptIn(LowLevelApi::class)
	override fun get(index: Int): BsonValue? =
		list.getOrNull(index)

	override fun asValue(): BsonValue =
		BsonValue(factory, BsonType.Array, bytesWithHeader)

	// endregion
	// region Iteration

	@OptIn(LowLevelApi::class)
	override fun asIterable(): Iterable<BsonValue> =
		object : Iterable<BsonValue> {
			override fun iterator(): Iterator<BsonValue> =
				list.iterator()

			override fun toString(): String =
				this@BsonArray.toString()
		}

	@OptIn(LowLevelApi::class)
	override fun asList(): List<BsonValue> =
		list

	@OptIn(LowLevelApi::class)
	override fun asSequence(): Sequence<BsonValue> =
		object : Sequence<BsonValue> {
			override fun iterator(): Iterator<BsonValue> =
				list.iterator()

			override fun toString(): String =
				this@BsonArray.toString()
		}

	override fun withIndex(): Iterable<IndexedValue<BsonValue>> =
		asIterable().withIndex()

	@OptIn(LowLevelApi::class)
	override val size: Int
		get() = list.size

	@OptIn(LowLevelApi::class)
	override fun isEmpty(): Boolean =
		list.isEmpty()

	@OptIn(LowLevelApi::class)
	override fun iterator(): Iterator<BsonValue> =
		list.iterator()

	// endregion
	// region Identity

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		(other is opensavvy.ktmongo.bson.multiplatform.BsonArray && bytesWithHeader == other.bytesWithHeader) || (other is BsonArray && BsonArray.equals(this, other))

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		BsonArray.hashCode(this)

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		list.toString()

	// endregion

	/**
	 * Serializer for the multiplatform [opensavvy.ktmongo.bson.multiplatform.BsonArray] implementation.
	 *
	 * This serializer only supports KtMongo's bson-multiplatform serialization. Other formats are not supported.
	 *
	 * If support for other formats is important to you, please [comment on the tracking issue](https://gitlab.com/opensavvy/ktmongo/-/work_items/122).
	 */
	object Serializer : KSerializer<opensavvy.ktmongo.bson.multiplatform.BsonArray> {
		// Implementation is empty because the bson-multiplatform encoder has a special case for this type

		private const val NAME = "opensavvy.ktmongo.bson.multiplatform.BsonArray"

		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(NAME, PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.multiplatform.BsonArray) {
			error("The $NAME type is only serializable with KtMongo's bson-multiplatform serialization. Found encoder: $encoder")
		}

		override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.multiplatform.BsonArray {
			error("The $NAME type is only deserializable with KtMongo's bson-multiplatform serialization. Found decoder: $decoder")
		}
	}
}
