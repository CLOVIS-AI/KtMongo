/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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
 * Entrypoint for creating [BsonDocument] and [BsonArray] instances.
 *
 * ### Navigating BSON types
 *
 * This interface is part of the BSON trinity:
 *
 * - [BsonDocument] represents an entire BSON document.
 * - [BsonArray] represents an array of BSON values.
 * - [BsonValue] represents a single value in isolation.
 *
 * ### Serialization
 *
 * This interface encapsulates the configuration of the serialization library in use.
 *
 * For example, if you use the official java or Kotlin MongoDB drivers, all BSON values built
 * by this factory will respect the configured `CodecRegistry`.
 *
 * ### Usage
 *
 * To create the following BSON document:
 * ```json
 * {
 *     "name": "Bob",
 *     "isAlive": true,
 *     "children": [
 *         {
 *             "name": "Alice"
 *         },
 *         {
 *             "name": "Charles"
 *         }
 *     ]
 * }
 * ```
 * use the code:
 * ```kotlin
 * val document = factory.buildDocument {
 *     writeString("name", "Alice")
 *     writeBoolean("isAlive", true)
 *     writeArray("children") {
 *         writeDocument {
 *             writeString("name", "Alice")
 *         }
 *         writeDocument {
 *             writeString("name", "Charles")
 *         }
 *     }
 * }
 * ```
 *
 * The value can then be read into Kotlin types:
 * ```kotlin
 * data class User(
 *     val name: String,
 *     val isAlive: Boolean,
 *     val children: List<Child>,
 * )
 *
 * data class Child(
 *     val name: String,
 * )
 *
 * val user = document.decode<User>()
 *
 * println(user.children[0].name)  // Alice
 * ```
 */
@Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
interface BsonFactory {

	/**
	 * Instantiates a new [BSON document][BsonDocument].
	 *
	 * ### Example
	 *
	 * To create the following BSON document:
	 * ```json
	 * {
	 *     "name": "Bob",
	 *     "isAlive": true,
	 *     "children": [
	 *         {
	 *             "name": "Alice"
	 *         },
	 *         {
	 *             "name": "Charles"
	 *         }
	 *     ]
	 * }
	 * ```
	 * use the code:
	 * ```kotlin
	 * factory.buildDocument {
	 *     writeString("name", "Alice")
	 *     writeBoolean("isAlive", true)
	 *     writeArray("children") {
	 *         writeDocument {
	 *             writeString("name", "Alice")
	 *         }
	 *         writeDocument {
	 *             writeString("name", "Charles")
	 *         }
	 *     }
	 * }
	 * ```
	 */
	@LowLevelApi
	@BsonWriterDsl
	fun buildDocument(block: BsonFieldWriter.() -> Unit): BsonDocument

	/**
	 * Instantiates a new [BSON document][BsonDocument] representing the provided [instance].
	 */
	@LowLevelApi
	@BsonWriterDsl
	fun buildDocument(instance: BsonFieldWriteable): BsonDocument =
		buildDocument { instance.writeTo(this) }

	/**
	 * Writes an arbitrary Kotlin [obj] into a top-level BSON document.
	 *
	 * Prefer using the overload with a single parameter.
	 *
	 * A top-level BSON document cannot be `null`, cannot be a primitive, and cannot be a collection.
	 * If [obj] is not representable as a document, an exception is thrown.
	 */
	@LowLevelApi
	@BsonWriterDsl
	fun <T : Any> encode(obj: T, type: KType): BsonDocument

	/**
	 * Instantiates a new [BSON document][BsonDocument] by reading its [bytes] representation.
	 *
	 * The reverse operation is available as [BsonDocument.toByteArray].
	 */
	@LowLevelApi
	fun readDocument(bytes: ByteArray): BsonDocument

	/**
	 * Instantiates a new [BSON array][BsonArray].
	 *
	 * ### Example
	 *
	 * To create the following BSON array:
	 * ```json
	 * [
	 *     12,
	 *     null,
	 *     {
	 *         "name": "Barry"
	 *     }
	 * ]
	 * ```
	 * use the code:
	 * ```kotlin
	 * buildArray {
	 *     writeInt32(12)
	 *     writeNull()
	 *     writeDocument {
	 *         writeString("name", "Barry")
	 *     }
	 * }
	 * ```
	 */
	@LowLevelApi
	@BsonWriterDsl
	fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray

	/**
	 * Instantiates a new [BSON array][BsonArray] representing the provided [instance].
	 */
	@LowLevelApi
	@BsonWriterDsl
	fun buildArray(instance: BsonValueWriteable): BsonArray =
		buildArray { instance.writeTo(this) }

	/**
	 * Instantiates a new [BSON array][BsonArray] by reading its [bytes] representation.
	 *
	 * The reverse operation is available as [BsonArray.toByteArray].
	 */
	@LowLevelApi
	fun readArray(bytes: ByteArray): BsonArray

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
inline fun <reified T : Any> BsonFactory.encode(obj: T): BsonDocument =
	encode(obj, typeOf<T>())
