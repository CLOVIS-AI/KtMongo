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

import kotlinx.io.Buffer
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal class MultiplatformBsonFieldWriter(
	private val writer: RawBsonWriter,
) : BsonFieldWriter {

	@LowLevelApi
	private fun writeType(type: BsonType) {
		writer.writeSignedByte(type.code)
	}

	@LowLevelApi
	private fun writeName(name: String) {
		writer.writeCString(name)
	}

	@LowLevelApi
	override fun write(name: String, block: BsonValueWriter.() -> Unit) {
		TODO()
	}

	@LowLevelApi
	override fun writeBoolean(name: String, value: Boolean) {
		writeType(BsonType.Boolean)
		writeName(name)
		writer.writeUnsignedByte(if (value) 1u else 0u)
	}

	@LowLevelApi
	override fun writeDouble(name: String, value: Double) {
		writeType(BsonType.Double)
		writeName(name)
		writer.writeDouble(value)
	}

	@LowLevelApi
	override fun writeInt32(name: String, value: Int) {
		writeType(BsonType.Int32)
		writeName(name)
		writer.writeInt32(value)
	}

	@LowLevelApi
	override fun writeInt64(name: String, value: Long) {
		writeType(BsonType.Int64)
		writeName(name)
		writer.writeInt64(value)
	}

	@LowLevelApi
	override fun writeDecimal128(name: String, low: Long, high: Long) {
		TODO()
	}

	@LowLevelApi
	override fun writeDateTime(name: String, value: Long) {
		TODO()
	}

	@LowLevelApi
	override fun writeNull(name: String) {
		TODO()
	}

	@LowLevelApi
	override fun writeObjectId(name: String, id: ByteArray) {
		TODO()
	}

	@LowLevelApi
	override fun writeRegularExpression(name: String, pattern: String, options: String) {
		TODO()
	}

	@LowLevelApi
	override fun writeString(name: String, value: String) {
		writeType(BsonType.String)
		writeName(name)
		writer.writeString(value)
	}

	@LowLevelApi
	override fun writeTimestamp(name: String, value: Long) {
		TODO()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeSymbol(name: String, value: String) {
		TODO()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeUndefined(name: String) {
		TODO()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeDBPointer(name: String, namespace: String, id: ByteArray) {
		TODO()
	}

	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@LowLevelApi
	override fun writeJavaScriptWithScope(name: String, code: String) {
		TODO()
	}

	@LowLevelApi
	override fun writeBinaryData(name: String, type: Byte, data: ByteArray) {
		TODO()
	}

	@LowLevelApi
	override fun writeJavaScript(name: String, code: String) {
		TODO()
	}

	private inline fun writeArbitraryDocument(writeTo: (BsonFieldWriter) -> Unit) {
		// We create the entire document in a child buffer so we can measure the size.
		// Once we know the size, we can write it entirely to the real buffer.
		val childBuffer = Buffer()
		val childWriter = RawBsonWriter(childBuffer)
		val childFieldWriter = MultiplatformBsonFieldWriter(childWriter)

		writeTo(childFieldWriter)
		childWriter.writeUnsignedByte(0u)

		// We now have an intermediate buffer, we can measure the size then transfer it the real writer
		check(childBuffer.size <= Int.MAX_VALUE) { "A BSON document cannot be larger than 16MiB. Found ${childBuffer.size} bytes." }
		writer.writeInt32(childBuffer.size.toInt() + 4)
		writer.sink.write(childBuffer, childBuffer.size)
	}

	@LowLevelApi
	override fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit) {
		writeType(BsonType.Document)
		writeName(name)
		writeArbitraryDocument(block)
	}

	@LowLevelApi
	override fun writeArray(name: String, block: BsonValueWriter.() -> Unit) {
		writeType(BsonType.Array)
		writeName(name)
		writeArbitraryDocument {
			val writer = MultiplatformBsonArrayFieldWriter(it)
			writer.block()
		}
	}

	@LowLevelApi
	override fun <T> writeObjectSafe(name: String, obj: T) {
		TODO()
	}

}
