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
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal class MultiplatformBsonSingleFieldWriter(
	private val writer: BsonFieldWriter,
	private val name: String,
) : BsonValueWriter {

	@LowLevelApi
	override fun writeBoolean(value: Boolean) {
		writer.writeBoolean(name, value)
	}

	@LowLevelApi
	override fun writeDouble(value: Double) {
		writer.writeDouble(name, value)
	}

	@LowLevelApi
	override fun writeInt32(value: Int) {
		writer.writeInt32(name, value)
	}

	@LowLevelApi
	override fun writeInt64(value: Long) {
		writer.writeInt64(name, value)
	}

	@LowLevelApi
	override fun writeDecimal128(low: Long, high: Long) {
		writer.writeDecimal128(name, low, high)
	}

	@LowLevelApi
	override fun writeDateTime(value: Long) {
		writer.writeDateTime(name, value)
	}

	@LowLevelApi
	override fun writeNull() {
		writer.writeNull(name)
	}

	@LowLevelApi
	override fun writeObjectId(id: ByteArray) {
		writer.writeObjectId(name, id)
	}

	@LowLevelApi
	override fun writeRegularExpression(pattern: String, options: String) {
		writer.writeRegularExpression(name, pattern, options)
	}

	@LowLevelApi
	override fun writeString(value: String) {
		writer.writeString(name, value)
	}

	@LowLevelApi
	override fun writeTimestamp(value: Long) {
		writer.writeTimestamp(name, value)
	}

	@LowLevelApi
	override fun writeSymbol(value: String) {
		writer.writeSymbol(name, value)
	}

	@LowLevelApi
	override fun writeUndefined() {
		writer.writeUndefined(name)
	}

	@LowLevelApi
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		writer.writeDBPointer(name, namespace, id)
	}

	@LowLevelApi
	override fun writeJavaScriptWithScope(code: String) {
		writer.writeJavaScriptWithScope(name, code)
	}

	@LowLevelApi
	override fun writeBinaryData(type: UByte, data: ByteArray) {
		writer.writeBinaryData(name, type, data)
	}

	@LowLevelApi
	override fun writeJavaScript(code: String) {
		writer.writeJavaScript(name, code)
	}

	@LowLevelApi
	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		writer.writeDocument(name, block)
	}

	@LowLevelApi
	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		writer.writeArray(name, block)
	}

	@LowLevelApi
	override fun writeMinKey() {
		writer.writeMinKey(name)
	}

	@LowLevelApi
	override fun writeMaxKey() {
		writer.writeMaxKey(name)
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(obj: T) {
		writer.writeObjectSafe(name, obj)
	}
}
