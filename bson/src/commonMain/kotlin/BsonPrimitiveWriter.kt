/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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
 * Low-level API to write BSON values.
 *
 * Each function corresponds to a different [BSON type](https://bsonspec.org/spec.html).
 *
 * Each function has an overload that takes a `name: String` as first parameter,
 * which is sugar for calling [writeName] before calling the function.
 *
 * ### Example
 *
 * To produce the BSON document:
 * ```bson
 * {
 *     "_id": ObjectId("507f1f77bcf86cd799439011"),
 *     "name": "Test"
 * }
 * ```
 * use the following code:
 * ```kotlin
 * val writer: BsonPrimitiveWriter = // …
 *
 * writer.writeDocument {
 *     writer.writeObjectId("_id", ObjectId("507f1f77bcf86cd799439011"))
 *     writer.writeString("name", "Test")
 * }
 * ```
 */
interface BsonPrimitiveWriter {

	fun flush()

	fun writeName(name: String)

	// region Primitives

	fun writeBoolean(
		value: Boolean
	)

	fun writeBoolean(
		name: String,
		value: Boolean,
	) {
		writeName(name)
		writeBoolean(value)
	}

	fun writeDouble(
		value: Double
	)

	fun writeDouble(
		name: String,
		value: Double
	) {
		writeName(name)
		writeDouble(value)
	}

	fun writeInt32(
		value: Int,
	)

	fun writeInt32(
		name: String,
		value: Int,
	) {
		writeName(name)
		writeInt32(value)
	}

	fun writeInt64(
		value: Long,
	)

	fun writeInt64(
		name: String,
		value: Long,
	) {
		writeName(name)
		writeInt64(value)
	}

	fun writeDecimal128(
		value: Decimal128
	)

	fun writeDecimal128(
		name: String,
		value: Decimal128,
	) {
		writeName(name)
		writeDecimal128(value)
	}

	fun writeDateTime(
		value: Long,
	)

	fun writeDateTime(
		name: String,
		value: Long,
	) {
		writeName(name)
		writeDateTime(value)
	}

	fun writeNull()

	fun writeNull(
		name: String,
	) {
		writeName(name)
		writeNull()
	}

	fun writeObjectId(
		value: ObjectId,
	)

	fun writeObjectId(
		name: String,
		value: ObjectId,
	) {
		writeName(name)
		writeObjectId(value)
	}

	fun writeRegularExpression(
		// language=jsregexp
		pattern: String,
		options: String,
	)

	fun writeRegularExpression(
		name: String,
		// language=jsregexp
		pattern: String,
		options: String,
	) {
		writeName(name)
		writeRegularExpression(pattern, options)
	}

	fun writeString(
		value: String,
	)

	fun writeString(
		name: String,
		value: String,
	) {
		writeName(name)
		writeString(value)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeSymbol(
		value: String,
	)

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeSymbol(
		name: String,
		value: String,
	) {
		writeName(name)
		writeSymbol(value)
	}

	fun writeTimestamp(
		value: Long,
	)

	fun writeTimestamp(
		name: String,
		value: Long,
	) {
		writeName(name)
		writeTimestamp(value)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeUndefined()

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeUndefined(
		name: String,
	) {
		writeName(name)
		writeUndefined()
	}

	// endregion
	// region Complex types

	fun writeBinaryData(
		type: Byte,
		data: ByteArray,
	)

	fun writeBinaryData(
		name: String,
		type: Byte,
		data: ByteArray,
	) {
		writeName(name)
		writeBinaryData(type, data)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeDBPointer(
		namespace: String,
		id: ObjectId,
	)

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeDBPointer(
		name: String,
		namespace: String,
		id: ObjectId,
	) {
		writeName(name)
		writeDBPointer(namespace, id)
	}

	fun writeJavaScript(
		// language=javascript
		code: String,
	)

	fun writeJavaScript(
		name: String,
		// language=javascript
		code: String,
	) {
		writeName(name)
		writeJavaScript(code)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeJavaScriptWithScope(
		// language=javascript
		code: String,
	)

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeJavaScriptWithScope(
		name: String,
		// language=javascript
		code: String,
	) {
		writeName(name)
		writeJavaScriptWithScope(code)
	}

	// endregion
	// region Documents & arrays

	fun writeStartDocument()

	fun writeEndDocument()

	fun writeStartArray()

	fun writeEndArray()

	// endregion

}

inline fun BsonPrimitiveWriter.writeArray(
	name: String? = null,
	block: BsonPrimitiveWriter.() -> Unit
) {
	if (name != null)
		writeName(name)

	writeStartArray()
	block()
	writeEndArray()
}

inline fun BsonPrimitiveWriter.writeDocument(
	name: String? = null,
	block: BsonPrimitiveWriter.() -> Unit
) {
	if (name != null)
		writeName(name)

	writeStartDocument()
	block()
	writeEndDocument()
}
