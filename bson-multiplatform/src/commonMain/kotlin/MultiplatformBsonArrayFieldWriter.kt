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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.CompletableBsonFieldWriter
import opensavvy.ktmongo.bson.CompletableBsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal class MultiplatformBsonArrayFieldWriter(
	private val writer: BsonFieldWriter,
) : BsonValueWriter {
	private var size = 0

	private fun nextIndex(): String =
		(size++).toString()

	@LowLevelApi
	override fun writeBoolean(value: Boolean) {
		writer.writeBoolean(nextIndex(), value)
	}

	@LowLevelApi
	override fun writeDouble(value: Double) {
		writer.writeDouble(nextIndex(), value)
	}

	@LowLevelApi
	override fun writeInt32(value: Int) {
		writer.writeInt32(nextIndex(), value)
	}

	@LowLevelApi
	override fun writeInt64(value: Long) {
		writer.writeInt64(nextIndex(), value)
	}

	@LowLevelApi
	override fun writeDecimal128(low: Long, high: Long) {
		writer.writeDecimal128(nextIndex(), low, high)
	}

	@LowLevelApi
	override fun writeDateTime(value: Long) {
		writer.writeDateTime(nextIndex(), value)
	}

	@LowLevelApi
	override fun writeNull() {
		writer.writeNull(nextIndex())
	}

	@LowLevelApi
	override fun writeObjectId(id: ByteArray) {
		writer.writeObjectId(nextIndex(), id)
	}

	@LowLevelApi
	override fun writeRegularExpression(pattern: String, options: String) {
		writer.writeRegularExpression(nextIndex(), pattern, options)
	}

	@LowLevelApi
	override fun writeString(value: String) {
		writer.writeString(nextIndex(), value)
	}

	@LowLevelApi
	override fun writeTimestamp(value: Timestamp) {
		writer.writeTimestamp(nextIndex(), value)
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeSymbol(value: String) {
		writer.writeSymbol(nextIndex(), value)
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeUndefined() {
		writer.writeUndefined(nextIndex())
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		writer.writeDBPointer(nextIndex(), namespace, id)
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeJavaScriptWithScope(code: String) {
		writer.writeJavaScript(nextIndex(), code)
	}

	@LowLevelApi
	override fun writeBinaryData(type: UByte, data: ByteArray) {
		writer.writeBinaryData(nextIndex(), type, data)
	}

	@LowLevelApi
	override fun writeJavaScript(code: String) {
		writer.writeJavaScript(nextIndex(), code)
	}

	@LowLevelApi
	override fun writeMinKey() {
		writer.writeMinKey(nextIndex())
	}

	@LowLevelApi
	override fun writeMaxKey() {
		writer.writeMaxKey(nextIndex())
	}

	@LowLevelApi
	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		writer.writeDocument(nextIndex(), block)
	}

	@LowLevelApi
	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		writer.writeArray(nextIndex(), block)
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openDocument(): CompletableBsonFieldWriter<Unit> {
		return writer.openDocument(nextIndex())
	}

	@LowLevelApi
	@DangerousMongoApi
	override fun openArray(): CompletableBsonValueWriter<Unit> {
		return writer.openArray(nextIndex())
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(obj: T) {
		writer.writeObjectSafe(nextIndex(), obj)
	}
}
