/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KClass
import kotlin.reflect.KType
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
 *
 * ### Implementation constraints
 *
 * Different implementations of [BsonDocumentReader] should be considered equal if they have the same fields
 * which each have the same values. The methods [BsonDocumentReader.Companion.equals] and
 * [BsonDocumentReader.Companion.hashCode] are provided to facilitate implementation.
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
	 * A set of the field names in the document.
	 */
	val names: Set<String>
		get() = entries.keys

	/**
	 * Reads this document into a [Bson] instance.
	 */
	fun toBson(): Bson

	/**
	 * Reads this entire document as a [BsonValueReader].
	 */
	fun asValue(): BsonValueReader

	/**
	 * Reads this document into an instance of [type] [T].
	 *
	 * If it isn't possible to deserialize this BSON to the given type, an exception is thrown.
	 *
	 * This function is a low-level implementation detail.
	 * Prefer using the extension function of the same name, that takes no arguments.
	 * If [type] and [klass] refer to different types, the behavior is unspecified.
	 */
	fun <T : Any> read(type: KType, klass: KClass<T>): T?

	/**
	 * JSON representation of the document this [BsonDocumentReader] is reading, as a [String].
	 */
	override fun toString(): String

	companion object {

		@LowLevelApi
		fun equals(a: BsonDocumentReader, b: BsonDocumentReader): Boolean {
			// Start by comparing fields one by one to keep the lazy initialization property of
			// some implementations.
			for ((name, aReader) in a.entries) {
				val bReader = b.read(name)

				if (bReader == null || aReader != bReader)
					return false
			}

			// At this point we know that we have accessed all fields at least once, so this should be inexpensive.
			return a.names == b.names
		}

		@LowLevelApi
		fun hashCode(a: BsonDocumentReader): Int {
			var hashCode = 1
			for ((name, reader) in a.entries) {
				hashCode = 31 * hashCode + name.hashCode()
				hashCode = 31 * hashCode + reader.hashCode()
			}
			return hashCode
		}
	}
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
 *
 * ### Implementation constraints
 *
 * Different implementations of [BsonArrayReader] should be considered equal if they have the same elements
 * in the same order. The methods [BsonArrayReader.Companion.equals] and
 * [BsonArrayReader.Companion.hashCode] are provided to facilitate implementation.
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
	 * The number of elements in this array.
	 *
	 * To iterate over the elements by index, see [indices].
	 */
	val size: Int
		get() = elements.size

	/**
	 * A range of the valid indices in this array.
	 */
	val indices: IntRange
		get() = elements.indices

	/**
	 * Reads this document into a [BsonArray] instance.
	 */
	fun toBson(): BsonArray

	/**
	 * Reads this entire array as a [BsonValueReader].
	 */
	fun asValue(): BsonValueReader

	/**
	 * Reads this document into an instance of [type] [T].
	 *
	 * [T] should be a type that can contain elements, such as `List<Int>` or `Set<User>`.
	 *
	 * If it isn't possible to deserialize this BSON to the given type, an exception is thrown.
	 */
	fun <T : Any> read(type: KType, klass: KClass<T>): T?

	/**
	 * JSON representation of the array this [BsonArrayReader] is reading, as a [String].
	 */
	override fun toString(): String

	companion object {

		@LowLevelApi
		fun equals(a: BsonArrayReader, b: BsonArrayReader): Boolean {
			// Compare fields one by one to keep the lazy properties
			for ((i, aReader) in a.elements.withIndex()) {
				val bReader = b.read(i)

				if (bReader == null || aReader != bReader)
					return false
			}

			// At this point we have already gone through all items, so this is inexpensive.
			return a.elements.size == b.elements.size
		}

		@LowLevelApi
		fun hashCode(a: BsonArrayReader): Int {
			var hashCode = 1
			for (reader in a.elements) {
				hashCode = 31 * hashCode + reader.hashCode()
			}
			return hashCode
		}
	}
}

/**
 * Representation of a BSON value.
 *
 * See the [type] to know which accessor to use. All other accessors will fail with a [BsonReaderException].
 *
 * To obtain instances of this interface, see [BsonDocumentReader.read] and [BsonArrayReader.read].
 *
 * ### Implementation constraints
 *
 * Different implementations of [BsonValueReader] should be considered equal if they represent the same value,
 * with the same type. That is, both values would result in the exact same BSON sent over the wire.
 * The methods [BsonValueReader.Companion.equals] and
 * [BsonValueReader.Companion.hashCode] are provided to facilitate implementation.
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

	@ExperimentalTime
	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readObjectId(): ObjectId

	@LowLevelApi
	@Throws(BsonReaderException::class)
	fun readObjectIdBytes(): ByteArray

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
	 * Reads this reader into an instance of [type] [T].
	 *
	 * If it isn't possible to deserialize this BSON to the given type, an exception is thrown.
	 */
	fun <T : Any> read(type: KType, klass: KClass<T>): T?

	/**
	 * JSON representation of this value.
	 */
	override fun toString(): String

	companion object {

		@OptIn(ExperimentalTime::class)
		@LowLevelApi
		fun equals(a: BsonValueReader, b: BsonValueReader): Boolean {
			if (a.type != b.type)
				return false

			return when (a.type) {
				BsonType.Double -> a.readDouble() == b.readDouble()
				BsonType.String -> a.readString() == b.readString()
				BsonType.Document -> a.readDocument() == b.readDocument()
				BsonType.Array -> a.readArray() == b.readArray()
				BsonType.BinaryData -> a.readBinaryDataType() == b.readBinaryDataType() &&
					a.readBinaryData().contentEquals(b.readBinaryData())

				BsonType.Undefined -> a.readUndefined() == b.readUndefined()
				BsonType.ObjectId -> a.readObjectId() == b.readObjectId()
				BsonType.Boolean -> a.readBoolean() == b.readBoolean()
				BsonType.Datetime -> a.readDateTime() == b.readDateTime()
				BsonType.Null -> a.readNull() == b.readNull()
				BsonType.RegExp -> a.readRegularExpressionOptions() == b.readRegularExpressionOptions() &&
					a.readRegularExpressionPattern() == b.readRegularExpressionPattern()

				BsonType.DBPointer -> a.readDBPointerNamespace() == b.readDBPointerNamespace() &&
					a.readDBPointerId().contentEquals(b.readDBPointerId())

				BsonType.JavaScript -> a.readJavaScript() == b.readJavaScript()
				BsonType.Symbol -> a.readSymbol() == b.readSymbol()
				BsonType.JavaScriptWithScope -> a.readJavaScriptWithScope() == b.readJavaScriptWithScope()
				BsonType.Int32 -> a.readInt32() == b.readInt32()
				BsonType.Timestamp -> a.readTimestamp() == b.readTimestamp()
				BsonType.Int64 -> a.readInt64() == b.readInt64()
				BsonType.Decimal128 -> a.readDecimal128().contentEquals(b.readDecimal128())
				BsonType.MinKey -> a.readMinKey() == b.readMinKey()
				BsonType.MaxKey -> a.readMaxKey() == b.readMaxKey()
			}
		}

		@OptIn(ExperimentalTime::class)
		@LowLevelApi
		fun hashCode(a: BsonValueReader): Int {
			return when (a.type) {
				BsonType.Double -> a.readDouble().hashCode()
				BsonType.String -> a.readString().hashCode()
				BsonType.Document -> a.readDocument().hashCode()
				BsonType.Array -> a.readArray().hashCode()
				BsonType.BinaryData -> {
					var hashCode = 1
					hashCode = 31 * hashCode + a.readBinaryDataType().toInt()
					for (byte in a.readBinaryData()) {
						hashCode = 31 * hashCode + byte.toInt()
					}
					hashCode
				}

				BsonType.Undefined -> a.readUndefined().hashCode()
				BsonType.ObjectId -> a.readObjectId().hashCode()
				BsonType.Boolean -> a.readBoolean().hashCode()
				BsonType.Datetime -> a.readDateTime().hashCode()
				BsonType.Null -> a.readNull().hashCode()
				BsonType.RegExp -> a.readRegularExpressionOptions().hashCode() * 31 + a.readRegularExpressionPattern().hashCode()
				BsonType.DBPointer -> a.readDBPointerNamespace().hashCode() * 31 + a.readDBPointerId().hashCode()
				BsonType.JavaScript -> a.readJavaScript().hashCode()
				BsonType.Symbol -> a.readSymbol().hashCode()
				BsonType.JavaScriptWithScope -> a.readJavaScriptWithScope().hashCode()
				BsonType.Int32 -> a.readInt32().hashCode()
				BsonType.Timestamp -> a.readTimestamp().hashCode()
				BsonType.Int64 -> a.readInt64().hashCode()
				BsonType.Decimal128 -> {
					var hashCode = 1
					for (byte in a.readDecimal128()) {
						hashCode = 31 * hashCode + byte.toInt()
					}
					hashCode
				}

				BsonType.MinKey -> a.readMinKey().hashCode()
				BsonType.MaxKey -> a.readMaxKey().hashCode()
			}
		}
	}
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
