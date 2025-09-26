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
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.CompletableBsonValueWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.MultiplatformArrayFieldWriter
import opensavvy.ktmongo.bson.multiplatform.impl.write.MultiplatformDocumentFieldWriter
import opensavvy.ktmongo.bson.multiplatform.serialization.encodeToBson
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class BsonContext @OptIn(ExperimentalAtomicApi::class) constructor(
	objectIdGenerator: ObjectIdGenerator = ObjectIdGenerator.Default(),
) : BsonContext, ObjectIdGenerator by objectIdGenerator {

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
	override fun buildDocument(block: BsonFieldWriter.() -> Unit): Bson =
		buildArbitraryTopLevel {
			block(this)
		}.let(::Bson)

	@OptIn(ExperimentalSerializationApi::class)
	@LowLevelApi
	override fun <T : Any> buildDocument(obj: T, type: KType, klass: KClass<T>): Bson =
		encodeToBson(this, obj, serializer(type))

	@LowLevelApi
	@DangerousMongoApi
	internal fun openDocument(): TopCompletableBsonFieldWriter {
		val buffer = Buffer()
		val bsonWriter = openArbitraryTopLevel(buffer)

		return object : TopCompletableBsonFieldWriter, CompletableBsonFieldWriter by MultiplatformDocumentFieldWriter(bsonWriter) {
			override fun build(): Bson =
				Bson(closeArbitraryTopLevel(buffer, bsonWriter))
		}
	}

	@LowLevelApi
	override fun readDocument(bytes: ByteArray): Bson =
		Bson(Bytes(bytes.copyOf()))

	@LowLevelApi
	override fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray =
		buildArbitraryTopLevel {
			block(MultiplatformArrayFieldWriter(this))
		}.let(::BsonArray)

	@LowLevelApi
	@DangerousMongoApi
	internal fun openArray(): TopCompletableBsonValueWriter {
		val buffer = Buffer()
		val bsonWriter = openArbitraryTopLevel(buffer)

		return object : TopCompletableBsonValueWriter, CompletableBsonValueWriter by MultiplatformArrayFieldWriter(MultiplatformDocumentFieldWriter(bsonWriter)) {
			override fun build(): BsonArray =
				BsonArray(closeArbitraryTopLevel(buffer, bsonWriter))
		}
	}

	@LowLevelApi
	override fun readArray(bytes: ByteArray): BsonArray =
		BsonArray(Bytes(bytes.copyOf()))

	@LowLevelApi
	@DangerousMongoApi
	internal interface TopCompletableBsonFieldWriter : CompletableBsonFieldWriter {
		fun build(): Bson
	}

	@LowLevelApi
	@DangerousMongoApi
	internal interface TopCompletableBsonValueWriter : CompletableBsonValueWriter {
		fun build(): BsonArray
	}
}
