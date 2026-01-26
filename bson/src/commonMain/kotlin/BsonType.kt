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

/**
 * The different data types supported in BSON documents.
 *
 * ### External resources
 *
 * - [BSON spec](https://bsonspec.org/spec.html)
 */
enum class BsonType(
	/**
	 * The byte identifier for this particular type.
	 *
	 * Guaranteed to be in the range `-1` ([MinKey]) to `127` ([MaxKey]).
	 */
	val code: Byte,
) {
	/**
	 * A 64-bit floating-point number, represented by the Kotlin class [kotlin.Double].
	 *
	 * @see BsonValueReader.readDouble Low-level read.
	 * @see BsonFieldWriter.writeDouble Low-level write.
	 */
	Double(1),

	/**
	 * A UTF-8 encoded string, represented by the Kotlin class [kotlin.String].
	 *
	 * @see BsonValueReader.readString Low-level read.
	 * @see BsonFieldWriter.writeString Low-level write.
	 */
	String(2),

	/**
	 * An arbitrary document, represented by the Kotlin interface [Bson].
	 *
	 * @see BsonValueReader.readDocument Low-level read.
	 * @see BsonFieldWriter.writeDocument Low-level write.
	 */
	Document(3),

	/**
	 * An array of arbitrary values, represented by the Kotlin interface [BsonArray].
	 *
	 * @see BsonValueReader.readArray Low-level read.
	 * @see BsonFieldWriter.writeArray Low-level write.
	 */
	Array(4),

	/**
	 * An arbitrary binary block.
	 *
	 * The arbitrary block is accompanied by a type.
	 *
	 * @see BsonValueReader.readBinaryData Low-level read: binary data.
	 * @see BsonValueReader.readBinaryDataType Low-level read: binary type.
	 * @see BsonFieldWriter.writeBinaryData Low-level write.
	 */
	BinaryData(5),

	/**
	 * The undefined value.
	 *
	 * @see BsonValueReader.readUndefined Low-level read.
	 * @see BsonFieldWriter.writeUndefined Low-level write.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	Undefined(6),

	/**
	 * A 12-bytes, time-sorted unique identifier for documents, represented by the Kotlin class [ObjectId][opensavvy.ktmongo.bson.types.ObjectId].
	 *
	 * @see BsonValueReader.readObjectId Low-level read.
	 * @see BsonFieldWriter.writeObjectId Low-level write.
	 */
	ObjectId(7),

	/**
	 * A boolean value, represented by the Kotlin class [kotlin.Boolean].
	 *
	 * @see BsonValueReader.readBoolean Low-level read.
	 * @see BsonFieldWriter.writeBoolean Low-level write.
	 */
	Boolean(8),

	/**
	 * A date and time in UTC, represented by the Kotlin class [kotlin.time.Instant].
	 *
	 * @see BsonValueReader.readInstant Low-level read.
	 * @see BsonValueReader.readDateTime Low-level read as a UNIX timestamp.
	 * @see BsonFieldWriter.writeInstant Low-level write.
	 * @see BsonFieldWriter.writeDateTime Low-level write as a UNIX timestamp.
	 */
	Datetime(9),

	/**
	 * The null value.
	 *
	 * @see BsonValueReader.readNull Low-level read.
	 * @see BsonFieldWriter.writeNull Low-level write.
	 */
	Null(10),

	/**
	 * A MongoDB regular expression.
	 *
	 * @see BsonValueReader.readRegularExpressionPattern Low-level read: pattern.
	 * @see BsonValueReader.readRegularExpressionOptions Low-level read: options.
	 * @see BsonFieldWriter.writeRegularExpression Low-level write.
	 */
	RegExp(11),

	/**
	 * A reference to a document in another collection.
	 *
	 * @see BsonValueReader.readDBPointerId Low-level read: ID.
	 * @see BsonValueReader.readDBPointerNamespace Low-level read: namespace.
	 * @see BsonFieldWriter.writeDBPointer Low-level write.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	DBPointer(12),

	/**
	 * A string of JavaScript code.
	 *
	 * @see BsonValueReader.readJavaScript Low-level read.
	 * @see BsonFieldWriter.writeJavaScript Low-level write.
	 */
	JavaScript(13),

	/**
	 * @see BsonValueReader.readSymbol Low-level read.
	 * @see BsonFieldWriter.writeSymbol Low-level write.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	Symbol(14),

	/**
	 * A string of JavaScript code, accompanied by a document containing variables.
	 *
	 * @see BsonValueReader.readJavaScriptWithScope Low-level read.
	 * @see BsonFieldWriter.writeJavaScriptWithScope Low-level write.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	JavaScriptWithScope(15),

	/**
	 * A 32-bit signed integer, represented by the Kotlin class [kotlin.Int].
	 *
	 * @see BsonValueReader.readInt32 Low-level read.
	 * @see BsonFieldWriter.writeInt32 Low-level write.
	 */
	Int32(16),

	/**
	 * A special type used in MongoDB logs, represented by the Kotlin class [Timestamp][opensavvy.ktmongo.bson.types.Timestamp].
	 * In most situations, users should use [Datetime] instead.
	 *
	 * @see BsonValueReader.readInstant Low-level read.
	 * @see BsonFieldWriter.writeInstant Low-level write.
	 */
	Timestamp(17),

	/**
	 * A 64-bit signed integer, represented by the Kotlin class [kotlin.Long].
	 *
	 * @see BsonValueReader.readInt64 Low-level read.
	 * @see BsonFieldWriter.writeInt64 Low-level write.
	 */
	Int64(18),

	/**
	 * A 128-bit floating-point number.
	 *
	 * @see BsonValueReader.readDecimal128 Low-level read.
	 * @see BsonFieldWriter.writeDecimal128 Low-level write.
	 */
	Decimal128(19),

	/**
	 * A special value containing the minimum possible value.
	 *
	 * @see BsonValueReader.readMinKey Low-level read.
	 * @see BsonFieldWriter.writeMinKey Low-level write.
	 */
	MinKey(-1),

	/**
	 * A special value containing the maximum possible value.
	 *
	 * @see BsonValueReader.readMaxKey Low-level read.
	 * @see BsonFieldWriter.writeMaxKey Low-level write.
	 */
	MaxKey(127),
	;

	companion object {

		/**
		 * Finds a [BsonType] instance which has a [BsonType.code] equal to [code].
		 *
		 * @throws NoSuchElementException If the provided [code] matches no [BsonType] instance.
		 */
		fun fromCode(code: Byte): BsonType =
			entries.first { it.code == code }

	}
}
