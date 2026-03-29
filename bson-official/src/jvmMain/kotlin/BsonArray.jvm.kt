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

import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.BsonDecodingException
import opensavvy.ktmongo.bson.BsonValue
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonDocument
import kotlin.reflect.KClass
import kotlin.reflect.KType
import org.bson.BsonArray as OfficialBsonArray

/**
 * Implementation of a KtMongo [opensavvy.ktmongo.bson.BsonArray] that wraps a [org.bson.BsonArray].
 *
 * To create an instance of this class, see [BsonFactory.readArray].
 */
actual class BsonArray internal constructor(
	val raw: OfficialBsonArray,
	private val factory: BsonFactory,
) : BsonArray {

	@LowLevelApi
	override fun <T> decode(type: KType): T {
		val classifier = type.classifier
		require(classifier is KClass<*>) { "The official Java driver only supports types that can be represented as classes\n\tObject: $raw\n\tType: $type" }

		@Suppress("UNCHECKED_CAST")
		classifier as KClass<T & Any>

		// Special-case Kotlin collections/arrays so we can preserve generic element type information
		// (the Java codec erases generics and would otherwise decode subdocuments as org.bson.Document).
		val typeArg = type.arguments.firstOrNull()?.type
		val elementKClass = typeArg?.classifier as? KClass<*>
		val elementIsNullable = typeArg?.isMarkedNullable == true

		fun decodeElement(element: org.bson.BsonValue): Any? {
			if (element.isNull) {
				if (elementIsNullable) return null
				else throw BsonDecodingException("Cannot decode null element for non-nullable element type in $type")
			}

			require(elementKClass is KClass<*>) { "The official Java driver only supports types that can be represented as classes, but this array has an unsupported parameter type\n\tObject: $raw\n\tType: $typeArg" }

			return decodeValue(element, elementKClass, factory.codecRegistry)
		}

		@Suppress("UNCHECKED_CAST")
		return when {
			classifier == List::class || classifier == MutableList::class || classifier == Collection::class || classifier == MutableCollection::class -> {
				val out = ArrayList<Any?>()
				for (v in raw) out.add(decodeElement(v))
				out as T
			}

			classifier == Set::class || classifier == MutableSet::class -> {
				val out = LinkedHashSet<Any?>()
				for (v in raw) out.add(decodeElement(v))
				out as T
			}

			classifier.java.isArray && typeArg != null && elementKClass != null && !classifier.java.componentType.isPrimitive -> {
				val size = raw.size
				val componentClass = elementKClass.java
				val arrayObj = java.lang.reflect.Array.newInstance(componentClass, size)
				for (i in 0 until size) {
					val v = raw[i]
					val decoded = decodeElement(v)
					java.lang.reflect.Array.set(arrayObj, i, decoded)
				}
				arrayObj as T
			}

			else -> decodeValue(raw, classifier, factory.codecRegistry)
		}
	}

	actual override fun asValue(): opensavvy.ktmongo.bson.official.BsonValue =
		BsonValue(raw, factory)

	override val size: Int
		get() = raw.size

	actual override fun get(index: Int): opensavvy.ktmongo.bson.official.BsonValue =
		BsonValue(raw[index], factory)

	override fun isEmpty(): Boolean =
		raw.isEmpty()

	override fun contains(element: BsonValue): Boolean =
		if (element is opensavvy.ktmongo.bson.official.BsonValue) raw.contains(element.raw)
		else false

	actual override fun iterator(): Iterator<opensavvy.ktmongo.bson.official.BsonValue> =
		object : Iterator<opensavvy.ktmongo.bson.official.BsonValue> {
			val iter = raw.iterator()

			override fun hasNext(): Boolean =
				iter.hasNext()

			override fun next(): opensavvy.ktmongo.bson.official.BsonValue =
				BsonValue(iter.next(), factory)
		}

	override fun containsAll(elements: Collection<BsonValue>): Boolean =
		elements.all { it in this }

	override fun indexOf(element: BsonValue): Int =
		if (element is opensavvy.ktmongo.bson.official.BsonValue) raw.indexOf(element.raw)
		else -1

	override fun lastIndexOf(element: BsonValue): Int =
		if (element is opensavvy.ktmongo.bson.official.BsonValue) raw.lastIndexOf(element.raw)
		else -1

	actual override fun listIterator(): ListIterator<opensavvy.ktmongo.bson.official.BsonValue> =
		listIterator(0)

	actual override fun listIterator(index: Int): ListIterator<opensavvy.ktmongo.bson.official.BsonValue> =
		object : ListIterator<opensavvy.ktmongo.bson.official.BsonValue> {
			val iter = raw.listIterator(index)

			override fun next(): opensavvy.ktmongo.bson.official.BsonValue =
				BsonValue(iter.next(), factory)

			override fun hasNext(): Boolean =
				iter.hasNext()

			override fun hasPrevious(): Boolean =
				iter.hasPrevious()

			override fun previous(): opensavvy.ktmongo.bson.official.BsonValue =
				BsonValue(iter.previous(), factory)

			override fun nextIndex(): Int =
				iter.nextIndex()

			override fun previousIndex(): Int =
				iter.previousIndex()
		}

	actual override fun subList(fromIndex: Int, toIndex: Int): List<opensavvy.ktmongo.bson.official.BsonValue> =
		// Very poor implementation, tell us if you need this!
		this.iterator().asSequence().toList().subList(fromIndex, toIndex)

	override fun toString(): String {
		// This is very ugly.
		// The Java library doesn't provide a way to serialize arrays to JSON.
		// https://www.mongodb.com/community/forums/t/how-to-convert-a-single-bsonvalue-such-as-bsonarray-to-json-in-the-java-bson-library

		val document = BsonDocument("a", raw).toJson()

		return document.substring(
			document.indexOf('['),
			document.lastIndexOf(']') + 1
		).trim()
	}

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		(other is opensavvy.ktmongo.bson.official.BsonArray && raw == other.raw) || (other is BsonArray && BsonArray.equals(this, other))

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		BsonArray.hashCode(this)

}
