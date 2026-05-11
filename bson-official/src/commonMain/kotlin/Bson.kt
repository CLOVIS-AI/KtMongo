/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.official

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.BsonValue
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.typeOf

/**
 * Implementation of [opensavvy.ktmongo.bson.BsonDocument] based on the official MongoDB drivers.
 *
 * To create instances of this class, see [BsonFactory].
 *
 * > This page describes the BSON implementation based on the official MongoDB drivers.
 * > To learn more about the general BSON operations, see [opensavvy.ktmongo.bson.BsonDocument].
 *
 * ### Navigating BSON types
 *
 * This interface is part of the BSON trinity:
 *
 * - [BsonDocument][opensavvy.ktmongo.bson.official.BsonDocument] represents an entire BSON document.
 * - [BsonArray][opensavvy.ktmongo.bson.official.BsonArray] represents an array of BSON values.
 * - [BsonValue][opensavvy.ktmongo.bson.official.BsonValue] represents a single value in isolation.
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
 */
@Serializable(with = opensavvy.ktmongo.bson.official.BsonDocument.Serializer::class)
expect class BsonDocument : BsonDocument {

	override val factory: opensavvy.ktmongo.bson.official.BsonFactory

	override fun get(field: String): opensavvy.ktmongo.bson.official.BsonValue?

	override fun asValue(): opensavvy.ktmongo.bson.official.BsonValue

	override fun asIterable(): Iterable<Field>

	override fun asMap(): Map<String, opensavvy.ktmongo.bson.official.BsonValue>

	override fun asSequence(): Sequence<Field>

	override fun iterator(): Iterator<Field>

	class Field(
		name: String,
		value: opensavvy.ktmongo.bson.official.BsonValue,
	) : BsonDocument.Field {

		override val value: opensavvy.ktmongo.bson.official.BsonValue

		override fun component2(): opensavvy.ktmongo.bson.official.BsonValue
	}

	/**
	 * Serializer for the official [opensavvy.ktmongo.bson.official.BsonDocument] implementation.
	 *
	 * This serializer only supports the official MongoDB library `org.bson:bson-kotlinx`.
	 *
	 * If support for other formats is important to you, please [comment on the tracking issue](https://gitlab.com/opensavvy/ktmongo/-/work_items/122).
	 */
	object Serializer : KSerializer<opensavvy.ktmongo.bson.official.BsonDocument>
}

/**
 * Implementation of [opensavvy.ktmongo.bson.BsonArray] based on the official MongoDB drivers.
 *
 * To create instances of this class, see [BsonFactory].
 *
 * > This page describes the BSON implementation based on the official MongoDB drivers.
 * > To learn more about the general BSON operations, see [opensavvy.ktmongo.bson.BsonArray].
 *
 * ### Navigating BSON types
 *
 * This interface is part of the BSON trinity:
 *
 * - [BsonDocument][opensavvy.ktmongo.bson.official.BsonDocument] represents an entire BSON document.
 * - [BsonArray][opensavvy.ktmongo.bson.official.BsonArray] represents an array of BSON values.
 * - [BsonValue][opensavvy.ktmongo.bson.official.BsonValue] represents a single value in isolation.
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
 */
@Serializable(with = opensavvy.ktmongo.bson.official.BsonArray.Serializer::class)
expect class BsonArray : BsonArray {

	override val factory: opensavvy.ktmongo.bson.official.BsonFactory

	override fun get(index: Int): opensavvy.ktmongo.bson.official.BsonValue?

	override fun asValue(): opensavvy.ktmongo.bson.official.BsonValue

	override fun iterator(): Iterator<opensavvy.ktmongo.bson.official.BsonValue>

	override fun asIterable(): Iterable<opensavvy.ktmongo.bson.official.BsonValue>

	override fun asList(): List<opensavvy.ktmongo.bson.official.BsonValue>

	override fun asSequence(): Sequence<opensavvy.ktmongo.bson.official.BsonValue>

	override fun withIndex(): Iterable<IndexedValue<opensavvy.ktmongo.bson.official.BsonValue>>

	/**
	 * Serializer for the official [opensavvy.ktmongo.bson.official.BsonArray] implementation.
	 *
	 * This serializer only supports the official MongoDB library `org.bson:bson-kotlinx`.
	 *
	 * If support for other formats is important to you, please [comment on the tracking issue](https://gitlab.com/opensavvy/ktmongo/-/work_items/122).
	 */
	object Serializer : KSerializer<opensavvy.ktmongo.bson.official.BsonArray>
}

/**
 * Implementation of [opensavvy.ktmongo.bson.BsonValue] based on the official MongoDB drivers.
 *
 * The BSON specification only allows root [documents][BsonDocument], so it is not possible
 * to instantiate this interface directly with a complex structure. This interface
 * is used to decode the fields of a [BsonDocument] or the items of a [BsonArray].
 *
 * To instantiate a [BsonDocument] or [BsonArray], see [BsonFactory].
 *
 * > This page describes the BSON implementation based on the official MongoDB drivers.
 * > To learn more about the general BSON operations, see [opensavvy.ktmongo.bson.BsonArray].
 *
 * ### Navigating BSON types
 *
 * This interface is part of the BSON trinity:
 *
 * - [BsonDocument][opensavvy.ktmongo.bson.official.BsonDocument] represents an entire BSON document.
 * - [BsonArray][opensavvy.ktmongo.bson.official.BsonArray] represents an array of BSON values.
 * - [BsonValue][opensavvy.ktmongo.bson.official.BsonValue] represents a single value in isolation.
 *
 * ### Usage
 *
 * If this BSON value is the serialized form of a Kotlin DTO, see [decode].
 *
 * This interface provides methods for decoding the different BSON native types, like [decodeInt32],
 * [decodeObjectId] and [decodeInstant].
 *
 * Some BSON types cannot be represented by a single Kotlin type, so multiple methods are provided to decode
 * their components. For example: [decodeRegularExpressionPattern] and [decodeRegularExpressionOptions].
 */
@Serializable(with = opensavvy.ktmongo.bson.official.BsonValue.Serializer::class)
expect class BsonValue : BsonValue {

	override val factory: opensavvy.ktmongo.bson.official.BsonFactory

	override fun decodeDocument(): opensavvy.ktmongo.bson.official.BsonDocument

	override fun decodeArray(): opensavvy.ktmongo.bson.official.BsonArray

	/**
	 * Serializer for the official [opensavvy.ktmongo.bson.official.BsonValue] implementation.
	 *
	 * This serializer only supports the official MongoDB library `org.bson:bson-kotlinx`.
	 *
	 * If support for other formats is important to you, please [comment on the tracking issue](https://gitlab.com/opensavvy/ktmongo/-/work_items/122).
	 */
	object Serializer : KSerializer<opensavvy.ktmongo.bson.official.BsonValue>
}

/**
 * Writes an arbitrary Kotlin [obj] into a top-level BSON document.
 *
 * A top-level BSON document cannot be `null`, cannot be a primitive, and cannot be a collection.
 * If [obj] is not representable as a document, an exception is thrown.
 *
 * @see BsonDocument.decode The inverse operation.
 */
@OptIn(LowLevelApi::class)
inline fun <reified T : Any> opensavvy.ktmongo.bson.official.BsonFactory.encode(obj: T): opensavvy.ktmongo.bson.official.BsonDocument =
	encode(obj, typeOf<T>())
