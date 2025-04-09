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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.BsonArrayReader
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.BsonValueReader
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonDocument
import org.bson.BsonValue
import java.nio.ByteBuffer
import org.bson.BsonArray as OfficialBsonArray
import org.bson.BsonDocument as OfficialBsonDocument

@LowLevelApi
internal class BsonDocumentReader(
	private val raw: OfficialBsonDocument,
) : BsonDocumentReader {
	override fun read(name: String): BsonValueReader? {
		return BsonValueReader(raw[name] ?: return null)
	}

	override val entries: Map<String, BsonValueReader>
		get() = raw.mapValues { (_, value) -> BsonValueReader(value) }

	override fun toString(): String =
		raw.toString()
}

@LowLevelApi
internal class BsonArrayReader(
	private val raw: OfficialBsonArray,
) : BsonArrayReader {
	override fun read(index: Int): BsonValueReader? {
		return BsonValueReader(raw.getOrNull(index) ?: return null)
	}

	override val elements: List<BsonValueReader>
		get() = raw.map { BsonValueReader(it) }

	override fun toString(): String {
		// Yes, this is very ugly, and probably inefficient.
		// The Java library doesn't provide a way to serialize arrays to JSON.
		// https://www.mongodb.com/community/forums/t/how-to-convert-a-single-bsonvalue-such-as-bsonarray-to-json-in-the-java-bson-library

		val document = BsonDocument("a", raw).toJson()

		return document.substring(
			document.indexOf('['),
			document.lastIndexOf(']') + 1
		).trim()
	}
}

@LowLevelApi
private class BsonValueReader(
	private val value: BsonValue,
) : BsonValueReader {

	override val type: BsonType
		get() = BsonType.fromCode(value.bsonType.value.toByte())

	private inline fun ensureType(requestedType: BsonType, predicate: () -> Boolean) {
		if (!predicate()) {
			throw BsonReaderException("Could not read value ‘$value’ as a $requestedType, because it is a $type")
		}
	}

	@LowLevelApi
	override fun readBoolean(): Boolean {
		ensureType(BsonType.Boolean) { value.isBoolean }
		return value.asBoolean().value
	}

	@LowLevelApi
	override fun readDouble(): Double {
		ensureType(BsonType.Double) { value.isDouble }
		return value.asDouble().value
	}

	@LowLevelApi
	override fun readInt32(): Int {
		ensureType(BsonType.Int32) { value.isInt32 }
		return value.asInt32().value
	}

	@LowLevelApi
	override fun readInt64(): Long {
		ensureType(BsonType.Int64) { value.isInt64 }
		return value.asInt64().value
	}

	@LowLevelApi
	override fun readDecimal128(): ByteArray {
		ensureType(BsonType.Decimal128) { value.isDecimal128 }
		val decimal = value.asDecimal128().value
		return ByteBuffer.allocate(Long.SIZE_BYTES * 2).apply {
			putLong(decimal.high)
			putLong(decimal.low)
		}.array()
	}

	@LowLevelApi
	override fun readDateTime(): Long {
		ensureType(BsonType.Datetime) { value.isDateTime }
		return value.asDateTime().value
	}

	@LowLevelApi
	override fun readNull() {
		ensureType(BsonType.Null) { value.isNull }
	}

	@LowLevelApi
	override fun readObjectId(): ByteArray {
		ensureType(BsonType.ObjectId) { value.isObjectId }
		return value.asObjectId().value.toByteArray()
	}

	@LowLevelApi
	override fun readRegularExpressionPattern(): String {
		ensureType(BsonType.RegExp) { value.isRegularExpression }
		return value.asRegularExpression().pattern
	}

	@LowLevelApi
	override fun readRegularExpressionOptions(): String {
		ensureType(BsonType.RegExp) { value.isRegularExpression }
		return value.asRegularExpression().options
	}

	@LowLevelApi
	override fun readString(): String {
		ensureType(BsonType.String) { value.isString }
		return value.asString().value
	}

	@LowLevelApi
	override fun readTimestamp(): Long {
		ensureType(BsonType.Timestamp) { value.isTimestamp }
		return value.asTimestamp().value
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readSymbol(): String {
		ensureType(BsonType.Symbol) { value.isSymbol }
		return value.asSymbol().symbol
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readUndefined() {
		ensureType(BsonType.Undefined) { value.bsonType == org.bson.BsonType.UNDEFINED }
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readDBPointerNamespace(): String {
		ensureType(BsonType.DBPointer) { value.isDBPointer }
		return value.asDBPointer().namespace
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readDBPointerId(): ByteArray {
		ensureType(BsonType.DBPointer) { value.isDBPointer }
		return value.asDBPointer().id.toByteArray()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun readJavaScriptWithScope(): String {
		ensureType(BsonType.JavaScriptWithScope) { value.isJavaScriptWithScope }
		return value.asJavaScriptWithScope().code
	}

	@LowLevelApi
	override fun readBinaryDataType(): UByte {
		ensureType(BsonType.BinaryData) { value.isBinary }
		return value.asBinary().type.toUByte()
	}

	@LowLevelApi
	override fun readBinaryData(): ByteArray {
		ensureType(BsonType.BinaryData) { value.isBinary }
		return value.asBinary().data
	}

	@LowLevelApi
	override fun readJavaScript(): String {
		ensureType(BsonType.JavaScript) { value.isJavaScript }
		return value.asJavaScript().code
	}

	@LowLevelApi
	override fun readDocument(): BsonDocumentReader {
		ensureType(BsonType.Document) { value.isDocument }
		return BsonDocumentReader(value.asDocument())
	}

	@LowLevelApi
	override fun readArray(): BsonArrayReader {
		ensureType(BsonType.Array) { value.isArray }
		return BsonArrayReader(value.asArray())
	}

	override fun toString(): String =
		value.toString()
}
