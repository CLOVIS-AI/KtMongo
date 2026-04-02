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

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A BSON document.
 *
 * To create instances of this class, see [BsonFactory].
 *
 * ### Navigating BSON types
 *
 * This interface is part of the BSON trinity:
 *
 * - [BsonDocument] represents an entire BSON document.
 * - [BsonArray] represents an array of BSON values.
 * - [BsonValue] represents a single value in isolation.
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
 */
interface BsonDocument {

	/**
	 * Generates a [ByteArray] of the raw BSON representation of this value.
	 */
	fun toByteArray(): ByteArray

	/**
	 * Decodes this document into an instance of the Kotlin type [T].
	 *
	 * ### Serialization configuration
	 *
	 * This method uses the serialization methods configured in the [BsonFactory] that created this instance.
	 *
	 * For example, if you use the official Java or Kotlin MongoDB drivers,
	 * this method will use your configured `CodecRegistry`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * data class User(
	 *     val _id: ObjectId,
	 *     val profile: Profile,
	 * )
	 *
	 * data class Profile(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * val factory: BsonFactory = …
	 *
	 * val bson = factory.buildDocument {
	 *     writeObjectId("_id", ObjectId("69c93e17b96e83b72d11b734"))
	 *     writeDocument("profile") {
	 *         writeString("name", "Bob")
	 *         writeInt32("age", 30)
	 *     }
	 * }
	 *
	 * val user = bson.decode<User>()
	 *
	 * println(user._id)          // ObjectId(69c93e17b96e83b72d11b734)
	 * println(user.profile.name) // Bob
	 * println(user.profile.age)  // 30
	 * ```
	 *
	 * ### Overloads
	 *
	 * Prefer using the parameter-less overload.
	 *
	 * If [type] doesn't match [T], the behavior is unspecified.
	 *
	 * @throws BsonDecodingException If the value cannot be decoded as an instance of [T].
	 */
	@LowLevelApi
	fun <T> decode(type: KType): T

	/**
	 * Creates an [Iterable] that wraps this document.
	 *
	 * Each pair is the name of a field and its value.
	 *
	 * The iteration order of a BSON document is preserved: fields appear in the same order as they
	 * are written in.
	 */
	fun asIterable(): Iterable<Field>

	/**
	 * Creates a [Map] view of this document.
	 *
	 * Each entry associates a field and its value.
	 *
	 * The iteration order of a BSON document is preserved: fields appear in the same order as they
	 * are written in.
	 */
	fun asMap(): Map<String, BsonValue>

	/**
	 * Creates a [Sequence] of the fields in this document.
	 *
	 * Each pair is the name of a field and its value.
	 *
	 * The iteration order of a BSON document is preserved: fields appear in the same order as they
	 * are written in.
	 */
	fun asSequence(): Sequence<Field>

	/**
	 * The number of fields in this document.
	 */
	val size: Int

	/**
	 * Returns `true` if this document has no fields.
	 */
	fun isEmpty(): Boolean =
		size == 0

	/**
	 * Returns `true` if this document has at least one field.
	 */
	fun isNotEmpty(): Boolean =
		!isEmpty()

	/**
	 * Returns the set of fields in this document.
	 *
	 * The iteration order of this set is the same as the iteration order as the document.
	 */
	val fields: Set<String>

	/**
	 * Returns the element with named [field].
	 *
	 * This method returns `null` if there is no element named [field].
	 * Note that if there **is** an element named [field], and it has the value `null`, then a [BsonValue]
	 * with [type][BsonValue.type] [BsonType.Null] is returned.
	 */
	operator fun get(field: String): BsonValue?

	/**
	 * Iterates over the fields in this document.
	 *
	 * Each pair is the name of a field and its value.
	 *
	 * The iteration order of a BSON document is preserved: fields appear in the same order as they
	 * are written in.
	 */
	operator fun iterator(): Iterator<Field>

	/**
	 * JSON representation of this [BsonDocument] object, as a [String].
	 */
	override fun toString(): String

	/**
	 * Returns the [BsonValue] equivalent to this document.
	 *
	 * The returned value has type [BsonType.Document] and its [BsonValue.decodeDocument] method returns this document.
	 */
	fun asValue(): BsonValue

	interface Field {
		val name: String
		val value: BsonValue

		operator fun component1(): String = name
		operator fun component2(): BsonValue = value
	}

	companion object {

		/**
		 * Compares two [BsonDocument] to verify whether they are equal according to the rules documented in [BsonDocument].
		 *
		 * This method may be used in [BsonDocument] implementations as a default implementation.
		 */
		@LowLevelApi
		fun equals(a: BsonDocument, b: BsonDocument): Boolean {
			// Start by comparing fields one by one to keep the lazy initialization property of
			// some implementations.
			for ((name, valueInA) in a) {
				val valueInB = b[name]

				if (valueInB == null || valueInA != valueInB)
					return false
			}

			// At this point we know that we have accessed all fields at least once, so this should be inexpensive.
			return a.fields == b.fields
		}

		/**
		 * Generates a hash code for a [BsonDocument] instance, that respects the equality rules documented in [BsonDocument].
		 *
		 * This method may be used in [BsonDocument] implementations as a default implementation.
		 */
		@LowLevelApi
		fun hashCode(a: BsonDocument): Int {
			var hashCode = 1
			for ((name, value) in a) {
				hashCode = 31 * hashCode + name.hashCode()
				hashCode = 31 * hashCode + value.hashCode()
			}
			return hashCode
		}
	}
}

/**
 * Decodes this document into an instance of the Kotlin type [T].
 *
 * ### Serialization configuration
 *
 * This method uses the serialization methods configured in the [BsonFactory] that created this instance.
 *
 * For example, if you use the official Java or Kotlin MongoDB drivers,
 * this method will use your configured `CodecRegistry`.
 *
 * ### Example
 *
 * ```kotlin
 * data class User(
 *     val _id: ObjectId,
 *     val profile: Profile,
 * )
 *
 * data class Profile(
 *     val name: String,
 *     val age: Int?,
 * )
 *
 * val factory: BsonFactory = …
 *
 * val bson = factory.buildDocument {
 *     writeObjectId("_id", ObjectId("69c93e17b96e83b72d11b734"))
 *     writeDocument("profile") {
 *         writeString("name", "Bob")
 *         writeInt32("age", 30)
 *     }
 * }
 *
 * val user = bson.decode<User>()
 *
 * println(user._id)          // ObjectId(69c93e17b96e83b72d11b734)
 * println(user.profile.name) // Bob
 * println(user.profile.age)  // 30
 * ```
 *
 * @throws BsonDecodingException If the value cannot be decoded as an instance of [T].
 */
@OptIn(LowLevelApi::class)
inline fun <reified T> BsonDocument.decode(): T =
	decode(typeOf<T>())
