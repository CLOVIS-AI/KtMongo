/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

import kotlinx.io.Buffer
import kotlinx.io.readTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.CompletableBsonValueWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.MultiplatformArrayFieldWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.MultiplatformDocumentFieldWriter
import opensavvy.ktmongo.bson.multiplatform.serialization.encodeToBson
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime

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
 * This interface encapsulates methods to serialize and serialize BSON documents with KotlinX.Serialization.
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
 * @Serializable
 * data class User(
 *     val name: String,
 *     val isAlive: Boolean,
 *     val children: List<Child>,
 * )
 *
 * @Serializable
 * data class Child(
 *     val name: String,
 * )
 *
 * val user = document.decode<User>()
 *
 * println(user.children[0].name)  // Alice
 * ```
 */
@OptIn(ExperimentalTime::class)
class BsonFactory : BsonFactory {

	@Suppress("NOTHING_TO_INLINE")
	private inline fun openArbitraryTopLevel(
		buffer: Buffer,
	): RawBsonWriter {
		val bsonWriter = RawBsonWriter(buffer)
		bsonWriter.writeInt32(0) // Document size. 0 for now, will be overwritten later.
		return bsonWriter
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun closeArbitraryTopLevel(
		buffer: Buffer,
		bsonWriter: RawBsonWriter,
	): Bytes {
		bsonWriter.writeUnsignedByte(0u)

		check(buffer.size <= Int.MAX_VALUE) { "A BSON document cannot be larger than 16MiB. Found ${buffer.size} bytes." }
		val size = buffer.size.toInt()
		val bytes = ByteArray(size)
		buffer.readTo(bytes)
		// 'buffer' is now empty

		// Overwrite the size at the very start of the document
		bsonWriter.writeInt32(size)
		buffer.readTo(bytes, 0, 4)

		return Bytes(bytes)
	}

	@LowLevelApi
	private inline fun buildArbitraryTopLevel(
		block: MultiplatformDocumentFieldWriter.() -> Unit,
	): Bytes {
		val buffer = Buffer()
		val bsonWriter = openArbitraryTopLevel(buffer)
		MultiplatformDocumentFieldWriter(bsonWriter).block()
		return closeArbitraryTopLevel(buffer, bsonWriter)
	}

	@LowLevelApi
	override fun buildDocument(block: BsonFieldWriter.() -> Unit): BsonDocument =
		buildArbitraryTopLevel {
			block(this)
		}.let { BsonDocument(this, it) }

	@ExperimentalSerializationApi
	@LowLevelApi
	override fun <T : Any> encode(obj: T, type: KType): BsonDocument =
		encodeToBson(this, obj, serializer(type))

	@LowLevelApi
	@DangerousMongoApi
	internal fun openDocument(): TopCompletableBsonFieldWriter {
		val buffer = Buffer()
		val bsonWriter = openArbitraryTopLevel(buffer)

		return object : TopCompletableBsonFieldWriter, CompletableBsonFieldWriter by MultiplatformDocumentFieldWriter(bsonWriter) {
			override fun build(): BsonDocument =
				BsonDocument(this@BsonFactory, closeArbitraryTopLevel(buffer, bsonWriter))
		}
	}

	@LowLevelApi
	override fun readDocument(bytes: ByteArray): BsonDocument =
		BsonDocument(this, Bytes(bytes.copyOf()))

	@LowLevelApi
	override fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray =
		buildArbitraryTopLevel {
			block(MultiplatformArrayFieldWriter(this))
		}.let { BsonArray(this, it) }

	@LowLevelApi
	@DangerousMongoApi
	internal fun openArray(): TopCompletableBsonValueWriter {
		val buffer = Buffer()
		val bsonWriter = openArbitraryTopLevel(buffer)

		return object : TopCompletableBsonValueWriter, CompletableBsonValueWriter by MultiplatformArrayFieldWriter(MultiplatformDocumentFieldWriter(bsonWriter)) {
			override fun build(): BsonArray =
				BsonArray(this@BsonFactory, closeArbitraryTopLevel(buffer, bsonWriter))
		}
	}

	@LowLevelApi
	override fun readArray(bytes: ByteArray): BsonArray =
		BsonArray(this, Bytes(bytes.copyOf()))

	@LowLevelApi
	@DangerousMongoApi
	internal interface TopCompletableBsonFieldWriter : CompletableBsonFieldWriter {
		fun build(): BsonDocument
	}

	@LowLevelApi
	@DangerousMongoApi
	internal interface TopCompletableBsonValueWriter : CompletableBsonValueWriter {
		fun build(): BsonArray
	}
}

/**
 * Writes an arbitrary Kotlin [obj] into a top-level BSON document.
 *
 * A top-level BSON document cannot be `null`, cannot be a primitive, and cannot be a collection.
 * If [obj] is not representable as a document, an exception is thrown.
 *
 * @see BsonDocument.decode The inverse operation.
 */
@ExperimentalSerializationApi
@OptIn(LowLevelApi::class)
inline fun <reified T : Any> opensavvy.ktmongo.bson.multiplatform.BsonFactory.encode(obj: T): BsonDocument =
	encode(obj, typeOf<T>())
