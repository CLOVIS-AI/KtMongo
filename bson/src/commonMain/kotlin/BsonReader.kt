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

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Utilities for decomposing a [Bson] document into its fields.
 *
 * To obtain an instance of this interface, see [Bson.reader].
 *
 * ### Example
 *
 * ```kotlin
 * val bson: Bson = …
 *
 * for ((name, field) in bson.read().entries) {
 *     println("Field: $name • ${field.type}")
 * }
 * ```
 */
@LowLevelApi
interface BsonDocumentReader {

	/**
	 * Attempts to read a field named [name].
	 *
	 * If such a field exists, an instance of [BsonValueReader] is returned.
	 * If no such a field exists, `null` is returned.
	 *
	 * Note that if a field exists and is `null`, a instance of [BsonValueReader] with a [type][BsonValueReader.type] of `null`
	 * is returned.
	 */
	fun read(name: String): BsonValueReader?

	/**
	 * A map allowing to go through all key-value pairs in the document.
	 *
	 * Keys of this map are names of fields.
	 * Value of this map are the result of calling [read] for the given name.
	 */
	val entries: Map<String, BsonValueReader>

	/**
	 * Reads this document into a [Bson] instance.
	 */
	fun toBson(): Bson

	/**
	 * Reads this entire document as a [BsonValueReader].
	 */
	fun asValue(): BsonValueReader

	/**
	 * JSON representation of the document this [BsonDocumentReader] is reading, as a [String].
	 */
	override fun toString(): String
}

/**
 * Utilities for decomposing a [BsonArray] into its elements.
 *
 * To obtain an instance of this interface, see [BsonArray.reader].
 *
 * ### Example
 *
 * ```kotlin
 * val bson: BsonArray = …
 *
 * for ((index, field) in bson.read().elements.withIndex()) {
 *     println("[$index] • ${field.type}")
 * }
 * ```
 */
@LowLevelApi
interface BsonArrayReader {

	/**
	 * Attempts to read an element at index [index].
	 *
	 * If such an element exists, an instance of [BsonValueReader] is returned.
	 * If no such element exists, `null` is returned.
	 *
	 * Note that if an element exists and is `null`, a instance of [BsonValueReader] with a [type][BsonValueReader.type] of `null`
	 * is returned.
	 */
	fun read(index: Int): BsonValueReader?

	/**
	 * A list of all elements in this reader.
	 *
	 * Values of this map are the elements contained by this array.
	 * To go through this list with its indices, see [Iterable.withIndex] or [Collection.indices].
	 */
	val elements: List<BsonValueReader>

	/**
	 * Reads this document into a [BsonArray] instance.
	 */
	fun toBson(): BsonArray

	/**
	 * Reads this entire array as a [BsonValueReader].
	 */
	fun asValue(): BsonValueReader

	/**
	 * JSON representation of the array this [BsonArrayReader] is reading, as a [String].
	 */
	override fun toString(): String
}

/**
 * Representation of a BSON value.
 *
 * See the [type] to know which accessor to use. All other accessors will fail with a [BsonReaderException].
 *
 * To obtain instances of this interface, see [BsonDocumentReader.read] and [BsonArrayReader.read].
 */
@LowLevelApi
interface BsonValueReader {

	/**
	 * The type of this value.
	 *
	 * To access its value, see the accessor function corresponding to this type.
	 * All other accessors will fail with [BsonReaderException].
	 */
	val type: BsonType

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readBoolean(): Boolean

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readDouble(): Double

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readInt32(): Int

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readInt64(): Long

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readDecimal128(): ByteArray

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readDateTime(): Long

	/**
	 * Reads an [Instant]. Conversion function on top of [readDateTime].
	 */
	@ExperimentalTime
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readInstant(): Instant =
		Instant.fromEpochMilliseconds(readDateTime())

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readNull()

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readObjectId(): ByteArray

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readRegularExpressionPattern(): String

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readRegularExpressionOptions(): String

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readString(): String

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readTimestamp(): Timestamp

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readSymbol(): String

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readUndefined()

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readDBPointerNamespace(): String

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readDBPointerId(): ByteArray

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readJavaScriptWithScope(): String

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readBinaryDataType(): UByte

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readBinaryData(): ByteArray

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readJavaScript(): String

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readMinKey()

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readMaxKey()

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readDocument(): BsonDocumentReader

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readArray(): BsonArrayReader

	/**
	 * JSON representation of this value.
	 */
	override fun toString(): String
}

/**
 * Exception thrown when BSON reading fails.
 *
 * @see BsonDocumentReader
 * @see BsonArrayReader
 * @see BsonValueReader
 */
class BsonReaderException(
	message: String,
	cause: Throwable? = null,
) : IllegalStateException(message, cause)
