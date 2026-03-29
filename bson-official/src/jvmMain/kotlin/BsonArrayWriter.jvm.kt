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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.bson.official.types.toOfficial
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonArray
import org.bson.BsonBinary
import org.bson.BsonBoolean
import org.bson.BsonDateTime
import org.bson.BsonDbPointer
import org.bson.BsonDecimal128
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.BsonDouble
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonJavaScript
import org.bson.BsonMaxKey
import org.bson.BsonMinKey
import org.bson.BsonNull
import org.bson.BsonObjectId
import org.bson.BsonRegularExpression
import org.bson.BsonString
import org.bson.BsonSymbol
import org.bson.BsonUndefined
import org.bson.types.Decimal128
import org.bson.types.ObjectId

@LowLevelApi
internal class JavaBsonArrayWriter(
	private val factory: BsonFactory,
	private val array: BsonArray,
) : BsonValueWriter {
	@LowLevelApi
	override fun writeBoolean(value: Boolean) {
		array.add(BsonBoolean(value))
	}

	@LowLevelApi
	override fun writeDouble(value: Double) {
		array.add(BsonDouble(value))
	}

	@LowLevelApi
	override fun writeInt32(value: Int) {
		array.add(BsonInt32(value))
	}

	@LowLevelApi
	override fun writeInt64(value: Long) {
		array.add(BsonInt64(value))
	}

	@LowLevelApi
	override fun writeDecimal128(low: Long, high: Long) {
		array.add(BsonDecimal128(Decimal128.fromIEEE754BIDEncoding(high, low)))
	}

	@LowLevelApi
	override fun writeDateTime(value: Long) {
		array.add(BsonDateTime(value))
	}

	@LowLevelApi
	override fun writeNull() {
		array.add(BsonNull())
	}

	@LowLevelApi
	override fun writeObjectId(id: ByteArray) {
		array.add(BsonObjectId(ObjectId(id)))
	}

	@LowLevelApi
	override fun writeRegularExpression(pattern: String, options: String) {
		array.add(BsonRegularExpression(pattern, options))
	}

	@LowLevelApi
	override fun writeString(value: String) {
		array.add(BsonString(value))
	}

	@LowLevelApi
	override fun writeTimestamp(value: Timestamp) {
		array.add(value.toOfficial())
	}

	@LowLevelApi
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	override fun writeSymbol(value: String) {
		array.add(BsonSymbol(value))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeUndefined() {
		array.add(BsonUndefined())
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		array.add(BsonDbPointer(namespace, ObjectId(id)))
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeJavaScriptWithScope(code: String) {
		array.add(BsonJavaScript(code))
	}

	@LowLevelApi
	override fun writeBinaryData(type: UByte, data: ByteArray) {
		array.add(BsonBinary(type.toByte(), data))
	}

	@LowLevelApi
	override fun writeJavaScript(code: String) {
		array.add(BsonJavaScript(code))
	}

	@LowLevelApi
	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		array.add(factory.buildDocument(block).raw)
	}

	@LowLevelApi
	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		array.add(factory.buildArray(block).raw)
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(obj: T) {
		val document = BsonDocument()

		BsonDocumentWriter(document).use { writer ->
			JavaBsonDocumentWriter(factory, writer).writeObjectSafe(obj)
		}

		array.add(document)
	}

	@LowLevelApi
	override fun writeMinKey() {
		array.add(BsonMinKey())
	}

	@LowLevelApi
	override fun writeMaxKey() {
		array.add(BsonMaxKey())
	}

}
