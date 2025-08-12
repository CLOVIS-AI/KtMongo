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

import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Configuration for the BSON serialization.
 *
 * Instances of this class are platform-specific and are used to create BSON documents.
 * Platforms can thus parameterize the behavior of writers and readers.
 *
 * For example, a platform may store its serialization configuration in this class.
 */
interface BsonContext : ObjectIdGenerator {

	/**
	 * Instantiates a new [BSON document][Bson].
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
	 * buildDocument {
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
	fun buildDocument(block: BsonFieldWriter.() -> Unit): Bson

	/**
	 * Instantiates a new [BSON document][Bson] representing the provided [instance].
	 */
	@LowLevelApi
	@BsonWriterDsl
	fun buildDocument(instance: BsonFieldWriteable): Bson =
		buildDocument { instance.writeTo(this) }

	/**
	 * Instantiates a new [BSON document][Bson] by reading its [bytes] representation.
	 *
	 * The reverse operation is available as [Bson.toByteArray].
	 */
	@LowLevelApi
	fun readDocument(bytes: ByteArray): Bson

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


	@LowLevelApi
	@DangerousMongoApi
	fun openDocument(): CompletableBsonFieldWriter<Bson>

	@LowLevelApi
	@DangerousMongoApi
	fun openArray(): CompletableBsonValueWriter<BsonArray>
}
