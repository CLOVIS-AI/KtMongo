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

import opensavvy.ktmongo.bson.types.Vector

/**
 * The different data types supported in BSON documents.
 *
 * Note that the BSON type is available as the property [code].
 * **The property [ordinal] is meaningless on this type, and may change over time.**
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
	 * @see BsonValue.decodeDouble Read this type.
	 * @see BsonFieldWriter.writeDouble Write this type.
	 */
	Double(1),

	/**
	 * A UTF-8 encoded string, represented by the Kotlin class [kotlin.String].
	 *
	 * @see BsonValue.decodeString Read this type.
	 * @see BsonFieldWriter.writeString Write this type.
	 */
	String(2),

	/**
	 * An arbitrary document, represented by the Kotlin interface [BsonDocument].
	 *
	 * @see BsonValue.decodeDocument Read this type.
	 * @see BsonFieldWriter.writeDocument Write this type.
	 */
	Document(3),

	/**
	 * An array of arbitrary values, represented by the Kotlin interface [BsonArray].
	 *
	 * @see BsonValue.decodeArray Read this type.
	 * @see BsonFieldWriter.writeArray Write this type.
	 */
	Array(4),

	/**
	 * An arbitrary binary block.
	 *
	 * The arbitrary block is accompanied by a type as a [UByte].
	 *
	 * The binary data types `0..127` are reserved.
	 * The data types `128..255` are available for custom use.
	 *
	 * The binary data type `0` is a generic subtype that can be used for any usage.
	 *
	 * The KtMongo library provides utilities for some binary data types, like [kotlin.uuid.Uuid] and [Vector].
	 *
	 * @see BsonValue.decodeBinaryData Read the binary blob.
	 * @see BsonValue.decodeBinaryDataType Read the subtype.
	 * @see BsonFieldWriter.writeBinaryData Write this type.
	 */
	BinaryData(5),

	/**
	 * The undefined value.
	 *
	 * @see BsonValue.decodeUndefined Read this type.
	 * @see BsonFieldWriter.writeUndefined Write this type.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	Undefined(6),

	/**
	 * A 12-bytes, time-sorted unique identifier for documents, represented by the Kotlin class [ObjectId][opensavvy.ktmongo.bson.types.ObjectId].
	 *
	 * @see BsonValue.decodeObjectId Read this type.
	 * @see BsonValue.decodeObjectIdBytes Low-level read.
	 * @see BsonFieldWriter.writeObjectId Write this type.
	 */
	ObjectId(7),

	/**
	 * A boolean value, represented by the Kotlin class [kotlin.Boolean].
	 *
	 * @see BsonValue.decodeBoolean Read this type.
	 * @see BsonFieldWriter.writeBoolean Write this type.
	 */
	Boolean(8),

	/**
	 * A date and time in UTC, represented by the Kotlin class [kotlin.time.Instant].
	 *
	 * @see BsonValue.decodeInstant Read this type.
	 * @see BsonValue.decodeDateTime Low-level read as a Unix timestamp.
	 * @see BsonFieldWriter.writeInstant Write this type.
	 * @see BsonFieldWriter.writeDateTime Low-level write as a Unix timestamp.
	 */
	Datetime(9),

	/**
	 * The null value.
	 *
	 * @see BsonValue.decodeNull Read this type.
	 * @see BsonFieldWriter.writeNull Write this type.
	 */
	Null(10),

	/**
	 * A MongoDB regular expression.
	 *
	 * A regular expression is composed of two strings: the pattern, and its options.
	 *
	 * @see BsonValue.decodeRegularExpressionPattern Read the pattern.
	 * @see BsonValue.decodeRegularExpressionOptions Read the options.
	 * @see BsonFieldWriter.writeRegularExpression Write this type.
	 */
	RegExp(11),

	/**
	 * A reference to a document in another collection.
	 *
	 * A DB pointer is composed of two values: a namespace (the name of a collection) and an ID.
	 *
	 * @see BsonValue.decodeDBPointerNamespace Read the namespace.
	 * @see BsonValue.decodeDBPointerId Read the ID.
	 * @see BsonFieldWriter.writeDBPointer Write this type.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	DBPointer(12),

	/**
	 * A string of JavaScript code.
	 *
	 * @see BsonValue.decodeJavaScript Read this type.
	 * @see BsonFieldWriter.writeJavaScript Write this type.
	 */
	JavaScript(13),

	/**
	 * A symbol.
	 *
	 * @see BsonValue.decodeSymbol Read this type.
	 * @see BsonFieldWriter.writeSymbol Write this type.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	Symbol(14),

	/**
	 * A string of JavaScript code, accompanied by a document containing variables.
	 *
	 * @see BsonValue.decodeJavaScript Read the JavaScript code.
	 * @see BsonValue.decodeJavaScriptScope Read the accompanying variables document.
	 * @see BsonFieldWriter.writeJavaScriptWithScope Write this type.
	 */
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	JavaScriptWithScope(15),

	/**
	 * A 32-bit signed integer, represented by the Kotlin class [kotlin.Int].
	 *
	 * @see BsonValue.decodeInt32 Read this type.
	 * @see BsonFieldWriter.writeInt32 Write this type.
	 */
	Int32(16),

	/**
	 * A special type used in MongoDB logs, represented by the Kotlin class [Timestamp][opensavvy.ktmongo.bson.types.Timestamp].
	 * In most situations, users should use [Datetime] instead.
	 *
	 * @see BsonValue.decodeTimestamp Read this type.
	 * @see BsonFieldWriter.writeTimestamp Write this type.
	 */
	Timestamp(17),

	/**
	 * A 64-bit signed integer, represented by the Kotlin class [kotlin.Long].
	 *
	 * @see BsonValue.decodeInt64 Read this type.
	 * @see BsonFieldWriter.writeInt64 Write this type.
	 */
	Int64(18),

	/**
	 * A 128-bit floating-point number.
	 *
	 * @see BsonValue.decodeDecimal128Bytes Read this type.
	 * @see BsonFieldWriter.writeDecimal128 Write this type.
	 */
	Decimal128(19),

	/**
	 * A special value containing the minimum possible value.
	 *
	 * @see BsonValue.decodeMinKey Read this type.
	 * @see BsonFieldWriter.writeMinKey Write this type.
	 */
	MinKey(-1),

	/**
	 * A special value containing the maximum possible value.
	 *
	 * @see BsonValue.decodeMaxKey Read this type.
	 * @see BsonFieldWriter.writeMaxKey Write this type.
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
