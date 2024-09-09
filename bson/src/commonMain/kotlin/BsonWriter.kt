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

import opensavvy.ktmongo.bson.types.Decimal128
import opensavvy.ktmongo.bson.types.ObjectId

@DslMarker
annotation class BsonWriterDsl

/**
 * Generator of BSON values.
 *
 * This interface is used to write a generic BSON value.
 * For example, the root BSON value, or a specific value of a field.
 *
 * To write fields in a BSON document, see [BsonFieldWriter].
 *
 * Instances of this interface are commonly obtained by calling the [buildBsonDocument] function.
 */
@BsonWriterDsl
interface BsonValueWriter {
	fun writeBoolean(value: Boolean)
	fun writeDouble(value: Double)
	fun writeInt32(value: Int)
	fun writeInt64(value: Long)
	fun writeDecimal128(value: Decimal128)
	fun writeDateTime(value: Long)
	fun writeNull()
	fun writeObjectId(value: ObjectId)
	fun writeRegularExpression(pattern: String, options: String)
	fun writeString(value: String)
	fun writeTimestamp(value: Long)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeSymbol(value: String)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeUndefined()

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeDBPointer(namespace: String, id: ObjectId)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeJavaScriptWithScope(code: String)

	fun writeBinaryData(type: Byte, data: ByteArray)
	fun writeJavaScript(code: String)

	fun writeDocument(block: BsonFieldWriter.() -> Unit)
	fun writeArray(block: BsonValueWriter.() -> Unit)
}

/**
 * Generator of BSON values.
 *
 * This interface is used to write fields in a BSON document.
 *
 * To write generic values, see [BsonValueWriter].
 *
 * Instances of this interface are commonly obtained by calling the [buildBsonDocument] function.
 */
@BsonWriterDsl
interface BsonFieldWriter {
	fun write(name: String, block: BsonValueWriter.() -> Unit)

	fun writeBoolean(name: String, value: Boolean)
	fun writeDouble(name: String, value: Double)
	fun writeInt32(name: String, value: Int)
	fun writeInt64(name: String, value: Long)
	fun writeDecimal128(name: String, value: Decimal128)
	fun writeDateTime(name: String, value: Long)
	fun writeNull(name: String)
	fun writeObjectId(name: String, value: ObjectId)
	fun writeRegularExpression(name: String, pattern: String, options: String)
	fun writeString(name: String, value: String)
	fun writeTimestamp(name: String, value: Long)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeSymbol(name: String, value: String)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeUndefined(name: String)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeDBPointer(name: String, namespace: String, id: ObjectId)

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun writeJavaScriptWithScope(name: String, code: String)

	fun writeBinaryData(name: String, type: Byte, data: ByteArray)
	fun writeJavaScript(name: String, code: String)

	fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit)
	fun writeArray(name: String, block: BsonValueWriter.() -> Unit)
}

/**
 * Instantiates a new [Bson] document.
 *
 * ### Example
 *
 * To create the following BSON document:
 * ```bson
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
 * buildBsonDocument {
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
expect fun buildBsonDocument(block: BsonFieldWriter.() -> Unit): Bson
