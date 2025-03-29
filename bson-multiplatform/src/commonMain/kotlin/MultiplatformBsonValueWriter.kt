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

import kotlinx.io.Sink
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal class MultiplatformBsonValueWriter(
	private val sink: Sink,
) : BsonValueWriter {
	@LowLevelApi
	override fun writeBoolean(value: Boolean) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeDouble(value: Double) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeInt32(value: Int) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeInt64(value: Long) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeDecimal128(low: Long, high: Long) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeDateTime(value: Long) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeNull() {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeObjectId(id: ByteArray) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeRegularExpression(pattern: String, options: String) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeString(value: String) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeTimestamp(value: Long) {
		TODO("Not yet implemented")
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeSymbol(value: String) {
		TODO("Not yet implemented")
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeUndefined() {
		TODO("Not yet implemented")
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeDBPointer(namespace: String, id: ByteArray) {
		TODO("Not yet implemented")
	}

	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeJavaScriptWithScope(code: String) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeBinaryData(type: Byte, data: ByteArray) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeJavaScript(code: String) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeDocument(block: BsonFieldWriter.() -> Unit) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun writeArray(block: BsonValueWriter.() -> Unit) {
		TODO("Not yet implemented")
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(obj: T) {
		TODO("Not yet implemented")
	}
}
