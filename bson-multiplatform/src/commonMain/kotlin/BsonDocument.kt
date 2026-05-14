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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.multiplatform.BsonDocument.Serializer
import opensavvy.ktmongo.bson.multiplatform.impl.read.MultiplatformBsonDocumentMap
import opensavvy.ktmongo.bson.multiplatform.serialization.BsonDecoder
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType

/**
 * Pure Kotlin implementation of [opensavvy.ktmongo.bson.BsonDocument].
 *
 * To create instances of this class, see [BsonFactory].
 *
 * > This page describes the BSON multiplatform implementation.
 * > To learn more about the general BSON operations, see [opensavvy.ktmongo.bson.BsonDocument].
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
 * val bson: BsonDocument = …
 *
 * for ((name, field) in bson) {
 *     println("Field: $name • ${field.type}")
 * }
 * ```
 *
 * The iteration order of a BSON document is preserved: fields appear in the same order as they
 * are written in.
 *
 * ### Equality
 *
 * Different implementations of this interface are considered equal if they represent the same value
 * with the same type. That is, both values would result in the exact same BSON sent over the wire.
 *
 * The methods [BsonDocument.Companion.equals] and [BsonDocument.Companion.hashCode] are provided
 * as default implementations.
 *
 * ### Thread-safety
 *
 * This class is **not thread-safe**.
 * Although it is not possible to mutate its state, this class uses internal mutation to lazily decode the BSON stream.
 */
@Serializable(with = Serializer::class)
class BsonDocument internal constructor(
	override val factory: BsonFactory,
	private val bytesWithHeader: Bytes,
) : BsonDocument {

	@OptIn(LowLevelApi::class)
	private val map = MultiplatformBsonDocumentMap(factory, bytesWithHeader)

	// region Access

	@LowLevelApi
	override fun <T> decode(type: KType): T {
		val decoder = BsonDecoder(this.asValue())
		@Suppress("UNCHECKED_CAST")
		return decoder.decodeSerializableValue(serializer(type) as KSerializer<T>)
	}

	@OptIn(LowLevelApi::class)
	override fun get(field: String): BsonValue? =
		map[field]

	override fun asValue(): BsonValue =
		BsonValue(factory, BsonType.Document, bytesWithHeader)

	// endregion
	// region Iteration

	@OptIn(LowLevelApi::class)
	override fun asIterable(): Iterable<Field> =
		object : Iterable<Field> {
			override fun iterator(): Iterator<Field> =
				this@BsonDocument.iterator()

			override fun toString(): String =
				this@BsonDocument.toString()
		}

	@OptIn(LowLevelApi::class)
	override fun asMap(): Map<String, BsonValue> =
		map

	override fun asSequence(): Sequence<Field> =
		object : Sequence<Field> {
			override fun iterator(): Iterator<Field> =
				this@BsonDocument.iterator()

			override fun toString(): String =
				this@BsonDocument.toString()
		}

	@OptIn(LowLevelApi::class)
	override val size: Int
		get() = map.size

	@OptIn(LowLevelApi::class)
	override fun isEmpty(): Boolean =
		map.isEmpty()

	@OptIn(LowLevelApi::class)
	override val fields: Set<String>
		get() = map.keys

	@OptIn(LowLevelApi::class)
	override fun iterator(): Iterator<Field> =
		map.iterator()

	class Field(
		override val name: String,
		override val value: BsonValue,
	) : BsonDocument.Field {

		override fun component1(): String  = name
		override fun component2(): BsonValue = value

		override fun equals(other: Any?): Boolean =
			other is BsonDocument.Field && name == other.name && value == other.value

		override fun hashCode(): Int =
			name.hashCode() * 31 + value.hashCode()

		override fun toString(): String =
			"($name, $value)"
	}

	override fun toByteArray(): ByteArray =
		bytesWithHeader.toByteArray()

	// endregion
	// region Identity

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		(other is opensavvy.ktmongo.bson.multiplatform.BsonDocument && bytesWithHeader == other.bytesWithHeader) || (other is BsonDocument && BsonDocument.equals(this, other))

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		BsonDocument.hashCode(this)

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		map.toString()

	// endregion

	/**
	 * Serializer for the multiplatform [opensavvy.ktmongo.bson.multiplatform.BsonDocument] implementation.
	 *
	 * This serializer only supports KtMongo's bson-multiplatform serialization. Other formats are not supported.
	 *
	 * If support for other formats is important to you, please [comment on the tracking issue](https://gitlab.com/opensavvy/ktmongo/-/work_items/122).
	 */
	object Serializer : KSerializer<opensavvy.ktmongo.bson.multiplatform.BsonDocument> {
		// Implementation is empty because the bson-multiplatform encoder has a special case for this type

		private const val NAME = "opensavvy.ktmongo.bson.multiplatform.BsonDocument"

		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(NAME, PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.multiplatform.BsonDocument) {
			error("The $NAME type is only serializable with KtMongo's bson-multiplatform serialization. Found encoder: $encoder")
		}

		override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.multiplatform.BsonDocument {
			error("The $NAME type is only deserializable with KtMongo's bson-multiplatform serialization. Found decoder: $decoder")
		}
	}

}
