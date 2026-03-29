/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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
 * A BSON array.
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
 * val bson: BsonArray = …
 *
 * for (element in bson) {
 *     println("Element: $element")
 * }
 * ```
 *
 * ### Equality
 *
 * Different implementations of this interface are considered equal if they represent the same value
 * with the same type. That is, both values would result in the exact same BSON sent over the wire.
 *
 * The methods [BsonArray.Companion.equals] and [BsonArray.Companion.hashCode] are provided
 * as default implementations.
 */
interface BsonArray : List<BsonValue> {

	/**
	 * Decodes this array into an instance of the Kotlin type [T].
	 *
	 * **`T` should be a type that contains elements, such as `List<Int>` or `Set<User>`.**
	 *
	 * To decode this array as a [List] of elements, see [decodeElements] instead.
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
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * val factory: BsonFactory = …
	 *
	 * val bson = factory.buildDocument {
	 *     writeArray("users") {
	 *         writeDocument {
	 *             writeString("name", "Alice")
	 *             writeInt32("age", 13)
	 *         }
	 *
	 *         writeDocument {
	 *             writeString("name", "Bob")
	 *             writeInt32("age", 52)
	 *         }
	 *     }
	 * }
	 *
	 * val users = bson["users"]?.decode<List<User>>()
	 *
	 * println(users[0].name) // Alice
	 * println(users[1].age)  // 13
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
	 * Decodes this array into a [List] of the Kotlin type [T].
	 *
	 * To decode this array into a type other than a [List], see [decode].
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
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * val factory: BsonFactory = …
	 *
	 * val bson = factory.buildDocument {
	 *     writeArray("users") {
	 *         writeDocument {
	 *             writeString("name", "Alice")
	 *             writeInt32("age", 13)
	 *         }
	 *
	 *         writeDocument {
	 *             writeString("name", "Bob")
	 *             writeInt32("age", 52)
	 *         }
	 *     }
	 * }
	 *
	 * val users = bson["users"]?.decodeElements<User>()
	 *
	 * println(users[0].name) // Alice
	 * println(users[1].age)  // 13
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
	fun <T> decodeElements(type: KType): List<T> =
		map { it.decode(type) }

	/**
	 * Returns the [BsonValue] equivalent to this array.
	 *
	 * The returned value has type [BsonType.Array] and its [BsonValue.decodeArray] method returns this array.
	 */
	fun asValue(): BsonValue

	/**
	 * JSON representation of this [BsonArray], as a [String].
	 */
	override fun toString(): String

	companion object {

		/**
		 * Compares two [BsonArray] to verify whether they are equal according to the rules documented in [BsonArray].
		 *
		 * This method may be used in [BsonArray] implementations as a default implementation.
		 */
		@LowLevelApi
		fun equals(a: BsonArray, b: BsonArray): Boolean {
			// Compare fields one by one to keep the lazy properties
			for ((i, aReader) in a.withIndex()) {
				val bReader = b.getOrNull(i)

				if (bReader == null || aReader != bReader)
					return false
			}

			// At this point we have already gone through all items, so this is inexpensive.
			return a.size == b.size
		}

		/**
		 * Generates a hash code for a [BsonArray] instance, that respects the equality rules documented in [BsonArray].
		 *
		 * This method may be used in [BsonArray] implementations as a default implementation.
		 */
		@LowLevelApi
		fun hashCode(a: BsonArray): Int {
			var hashCode = 1
			for (element in a) {
				hashCode = 31 * hashCode + element.hashCode()
			}
			return hashCode
		}

	}
}

/**
 * Decodes this array into an instance of the Kotlin type [T].
 *
 * **`T` should be a type that contains elements, such as `List<Int>` or `Set<User>`.**
 *
 * To decode this array as a [List] of elements, see [decodeElements] instead.
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
 *     val name: String,
 *     val age: Int?,
 * )
 *
 * val factory: BsonFactory = …
 *
 * val bson = factory.buildDocument {
 *     writeArray("users") {
 *         writeDocument {
 *             writeString("name", "Alice")
 *             writeInt32("age", 13)
 *         }
 *
 *         writeDocument {
 *             writeString("name", "Bob")
 *             writeInt32("age", 52)
 *         }
 *     }
 * }
 *
 * val users = bson["users"]?.decode<List<User>>()
 *
 * println(users[0].name) // Alice
 * println(users[1].age)  // 13
 * ```
 *
 * @throws BsonDecodingException If the value cannot be decoded as an instance of [T].
 */
@OptIn(LowLevelApi::class)
inline fun <reified T> BsonArray.decode(): T =
	decode(typeOf<T>())

/**
 * Decodes this array into a [List] of the Kotlin type [T].
 *
 * To decode this array into a type other than a [List], see [decode].
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
 *     val name: String,
 *     val age: Int?,
 * )
 *
 * val factory: BsonFactory = …
 *
 * val bson = factory.buildDocument {
 *     writeArray("users") {
 *         writeDocument {
 *             writeString("name", "Alice")
 *             writeInt32("age", 13)
 *         }
 *
 *         writeDocument {
 *             writeString("name", "Bob")
 *             writeInt32("age", 52)
 *         }
 *     }
 * }
 *
 * val users = bson["users"]?.decodeElements<User>()
 *
 * println(users[0].name) // Alice
 * println(users[1].age)  // 13
 * ```
 *
 * @throws BsonDecodingException If the value cannot be decoded as an instance of [T].
 */
@OptIn(LowLevelApi::class)
inline fun <reified T> BsonArray.decodeElements(): List<T> =
	decodeElements(typeOf<T>())
