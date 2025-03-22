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
	Double(1),

	String(2),

	Document(3),

	Array(4),

	BinaryData(5),

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	Undefined(6),

	ObjectId(7),

	Boolean(8),

	Datetime(9),

	Null(10),

	RegExp(11),

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	DBPointer(12),

	JavaScript(13),

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	Symbol(14),

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	JavaScriptWithScope(15),

	Int32(16),

	Timestamp(17),

	Int64(18),

	Decimal128(19),

	MinKey(-1),

	MaxKey(127)
}
