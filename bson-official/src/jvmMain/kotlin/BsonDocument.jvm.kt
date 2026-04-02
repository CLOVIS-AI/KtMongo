/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonBinaryWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.io.BasicOutputBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import org.bson.BsonDocument as OfficialBsonDocument

/**
 * Implementation of a KtMongo [opensavvy.ktmongo.bson.BsonDocument] that wraps a [org.bson.BsonDocument].
 *
 * To create an instance of this class, see [BsonFactory.readDocument].
 */
actual class BsonDocument internal constructor(
	val raw: OfficialBsonDocument,
	private val factory: BsonFactory,
) : opensavvy.ktmongo.bson.BsonDocument {

	@LowLevelApi
	override fun toByteArray(): ByteArray =
		raw.toByteArray(factory)

	@LowLevelApi
	override fun <T> decode(type: KType): T {
		val classifier = type.classifier
		require(classifier is KClass<*>) { "The official Java driver only supports types that can be represented as classes\n\tObject: $raw\n\tType: $type" }

		@Suppress("UNCHECKED_CAST")
		classifier as KClass<T & Any>

		val codec = factory.codecRegistry.get(classifier.java)

		return codec.decode(
			raw.asBsonReader(),
			DecoderContext.builder().build(),
		)
	}

	actual override fun asIterable(): Iterable<Field> =
		object : Iterable<Field> {
			override fun iterator(): Iterator<Field> =
				this@BsonDocument.iterator()

			override fun toString(): String =
				this@BsonDocument.toString()
		}

	actual override fun asMap(): Map<String, BsonValue> =
		BsonDocumentMap()

	actual override fun asSequence(): Sequence<Field> =
		Sequence { this@BsonDocument.iterator() }

	actual override fun asValue(): BsonValue =
		BsonValue(raw, factory)

	override val size: Int
		get() = raw.size

	override val fields: Set<String>
		get() = raw.keys

	private inner class BsonDocumentMap : Map<String, BsonValue> {

		override val size: Int
			get() = raw.size

		override val keys: Set<String>
			get() = raw.keys

		override val values: Collection<BsonValue>
			get() = object : Collection<BsonValue> {
				override val size: Int
					get() = raw.size

				override fun isEmpty(): Boolean =
					raw.isEmpty()

				override fun contains(element: BsonValue): Boolean =
					raw.containsValue(element.raw)

				override fun iterator(): Iterator<BsonValue> =
					object : Iterator<BsonValue> {
						val iter = raw.values.iterator()

						override fun hasNext(): Boolean =
							iter.hasNext()

						override fun next(): BsonValue =
							BsonValue(iter.next(), factory)
					}

				override fun containsAll(elements: Collection<BsonValue>): Boolean =
					elements.all { it in this }
			}

		override val entries: Set<Map.Entry<String, BsonValue>>
			get() = object : Set<Map.Entry<String, BsonValue>> {
				val entries = raw.entries

				override val size: Int
					get() = entries.size

				override fun isEmpty(): Boolean =
					entries.isEmpty()

				override fun contains(element: Map.Entry<String, BsonValue>): Boolean =
					entries.any { it.key == element.key && it.value == element.value.raw }

				override fun iterator(): Iterator<Map.Entry<String, BsonValue>> =
					object : Iterator<Map.Entry<String, BsonValue>> {
						val iter = entries.iterator()

						override fun hasNext(): Boolean =
							iter.hasNext()

						override fun next(): Map.Entry<String, BsonValue> =
							object : Map.Entry<String, BsonValue> {
								val raw = iter.next()

								override val key: String
									get() = raw.key

								override val value: BsonValue
									get() = BsonValue(raw.value, factory)
							}
					}

				override fun containsAll(elements: Collection<Map.Entry<String, BsonValue>>): Boolean =
					elements.all { it in this}
			}

		override fun isEmpty(): Boolean =
			raw.isEmpty()

		override fun containsKey(key: String): Boolean =
			raw.containsKey(key)

		override fun containsValue(value: BsonValue): Boolean =
			raw.containsValue(value.raw) // If it's a multiplatform BsonValue, then it can't be in the BsonDocument from the official driver

		override fun get(key: String): BsonValue? =
			this@BsonDocument[key]
	}

	actual override fun get(field: String): BsonValue? =
		raw[field]?.let { BsonValue(it, factory) }

	actual override fun iterator(): Iterator<Field> =
		object : Iterator<Field> {
			val iter = raw.iterator()

			override fun hasNext(): Boolean =
				iter.hasNext()

			override fun next(): Field {
				val next = iter.next()
				return Field(next.key, BsonValue(next.value, factory))
			}
		}

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		(other is BsonDocument && raw == other.raw) || (other is opensavvy.ktmongo.bson.BsonDocument && opensavvy.ktmongo.bson.BsonDocument.equals(this, other))

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		opensavvy.ktmongo.bson.BsonDocument.hashCode(this)

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		raw.toJson()

	actual class Field actual constructor(
		override val name: String,
		actual override val value: BsonValue
	) : opensavvy.ktmongo.bson.BsonDocument.Field {

		override fun component1(): String = name
		actual override fun component2(): BsonValue = value
	}
}

// Inspired by https://gist.github.com/Koboo/ebd7c6802101e1a941ef31baca04113d
// Inspired by https://stackoverflow.com/questions/49262903
@LowLevelApi
private fun OfficialBsonDocument.toByteArray(factory: BsonFactory): ByteArray {
	val buffer = BasicOutputBuffer()
	val writer = BsonBinaryWriter(buffer)
	val documentCodec = DocumentCodec(factory.codecRegistry)
	documentCodec.encode(
		writer,
		documentCodec.decode(
			this.asBsonReader(),
			DecoderContext.builder().build()
		),
		EncoderContext.builder()
			.isEncodingCollectibleDocument(true)
			.build()
	)
	return buffer.toByteArray()
		.also { buffer.close() }
}
