/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.ReadPreference
import opensavvy.ktmongo.multiplatform.wire.Message
import opensavvy.ktmongo.multiplatform.wire.MessageSection
import kotlin.reflect.KType

// Everything is 'internal' to allow everything to be inlined

@OptIn(LowLevelApi::class)
internal class MessageBuilder(
	internal val factory: BsonFactory,
) {
	internal var document: BsonDocument? = null
	internal var readPreference: ReadPreference? = null
	internal val sequences = ArrayList<MessageSection.DocumentSequence>()

	inline fun document(crossinline block: BsonFieldWriter.() -> Unit) {
		check(document == null) { "Cannot set 'document' multiple times" }
		document = factory.buildDocument {
			with(captureSpecialOptions(this)) {
				block()
			}
		}
	}

	internal fun captureSpecialOptions(writer: BsonFieldWriter): BsonFieldWriter =
		BsonFieldWriterCapturer(writer)

	inline fun sequence(id: String, block: MessageSequenceBuilder.() -> Unit) {
		sequences += MessageSection.DocumentSequence(
			id = id,
			lazyDocuments = MessageSequenceBuilder(factory).apply(block).documents,
		)
	}

	internal inner class BsonFieldWriterCapturer(
		private val writer: BsonFieldWriter,
	) : BsonFieldWriter {

		private inline fun captured(name: String, crossinline block: BsonValueWriter.() -> Unit): Boolean {
			if (name == "readPreference") {
				readPreference = factory.buildArray { block() }[0]
					?.decodeString()
					?.let { str -> ReadPreference.entries.first { it.bsonName == str } }
				return true
			}

			return false
		}

		@LowLevelApi
		override fun write(name: String, block: BsonValueWriter.() -> Unit) {
			if (captured(name, block)) return

			writer.write(name, block)
		}

		@LowLevelApi
		override fun writeBoolean(name: String, value: Boolean) {
			if (captured(name) { writeBoolean(value) }) return

			writer.writeBoolean(name, value)
		}

		@LowLevelApi
		override fun writeDouble(name: String, value: Double) {
			if (captured(name) { writeDouble(value) }) return

			writer.writeDouble(name, value)
		}

		@LowLevelApi
		override fun writeInt32(name: String, value: Int) {
			if (captured(name) { writeInt32(value) }) return

			writer.writeInt32(name, value)
		}

		@LowLevelApi
		override fun writeInt64(name: String, value: Long) {
			if (captured(name) { writeInt64(value) }) return

			writer.writeInt64(name, value)
		}

		@LowLevelApi
		override fun writeDecimal128(name: String, low: Long, high: Long) {
			if (captured(name) { writeDecimal128(low, high) }) return

			writer.writeDecimal128(name, low, high)
		}

		@LowLevelApi
		override fun writeDateTime(name: String, value: Long) {
			if (captured(name) { writeDateTime(value) }) return

			writer.writeDateTime(name, value)
		}

		@LowLevelApi
		override fun writeNull(name: String) {
			if (captured(name) { writeNull() }) return

			writer.writeNull(name)
		}

		@LowLevelApi
		override fun writeObjectId(name: String, id: ByteArray) {
			if (captured(name) { writeObjectId(id) }) return

			writer.writeObjectId(name, id)
		}

		@LowLevelApi
		override fun writeObjectId(name: String, id: ObjectId) {
			if (captured(name) { writeObjectId(id) }) return

			writer.writeObjectId(name, id)
		}

		@LowLevelApi
		override fun writeRegularExpression(name: String, pattern: String, options: String) {
			if (captured(name) { writeRegularExpression(pattern, options) }) return

			writer.writeRegularExpression(name, pattern, options)
		}

		@LowLevelApi
		override fun writeString(name: String, value: String) {
			if (captured(name) { writeString(value) }) return

			writer.writeString(name, value)
		}

		@LowLevelApi
		override fun writeTimestamp(name: String, value: Timestamp) {
			if (captured(name) { writeTimestamp(value) }) return

			writer.writeTimestamp(name, value)
		}

		@Suppress("DEPRECATION")
		@Deprecated(DEPRECATED_IN_BSON_SPEC)
		@LowLevelApi
		override fun writeSymbol(name: String, value: String) {
			if (captured(name) { writeSymbol(value) }) return

			writer.writeSymbol(name, value)
		}

		@Suppress("DEPRECATION")
		@Deprecated(DEPRECATED_IN_BSON_SPEC)
		@LowLevelApi
		override fun writeUndefined(name: String) {
			if (captured(name) { writeUndefined() }) return

			writer.writeUndefined(name)
		}

		@Suppress("DEPRECATION")
		@Deprecated(DEPRECATED_IN_BSON_SPEC)
		@LowLevelApi
		override fun writeDBPointer(name: String, namespace: String, id: ByteArray) {
			if (captured(name) { writeDBPointer(namespace, id) }) return

			writer.writeDBPointer(name, namespace, id)
		}

		@Suppress("DEPRECATION")
		@Deprecated(DEPRECATED_IN_BSON_SPEC)
		@LowLevelApi
		override fun writeJavaScriptWithScope(name: String, code: String) {
			if (captured(name) { writeJavaScriptWithScope(code) }) return

			writer.writeJavaScriptWithScope(name, code)
		}

		@LowLevelApi
		override fun writeBinaryData(name: String, type: UByte, data: ByteArray) {
			if (captured(name) { writeBinaryData(type, data) }) return

			writer.writeBinaryData(name, type, data)
		}

		@LowLevelApi
		override fun writeJavaScript(name: String, code: String) {
			if (captured(name) { writeJavaScript(code) }) return

			writer.writeJavaScript(name, code)
		}

		@LowLevelApi
		override fun writeMinKey(name: String) {
			if (captured(name) { writeMinKey() }) return

			writer.writeMinKey(name)
		}

		@LowLevelApi
		override fun writeMaxKey(name: String) {
			if (captured(name) { writeMaxKey() }) return

			writer.writeMaxKey(name)
		}

		@LowLevelApi
		override fun writeDocument(name: String, block: BsonFieldWriter.() -> Unit) {
			if (captured(name) { writeDocument(block) }) return

			writer.writeDocument(name, block)
		}

		@LowLevelApi
		override fun writeArray(name: String, block: BsonValueWriter.() -> Unit) {
			if (captured(name) { writeArray(block) }) return

			writer.writeArray(name, block)
		}

		@LowLevelApi
		override fun <T> writeSafe(name: String, obj: T, type: KType) {
			if (captured(name) { writeSafe(obj, type) }) return

			writer.writeSafe(name, obj, type)
		}

	}
}

@LowLevelApi
internal class MessageSequenceBuilder(
	internal val factory: BsonFactory,
) {
	internal val documents = ArrayList<Lazy<BsonDocument>>()

	inline fun document(crossinline block: BsonFieldWriter.() -> Unit) {
		documents += eager(
			factory.buildDocument {
				block()
			}
		)
	}
}

internal fun MultiplatformMongoClient.createDriverMessage(
	block: MessageBuilder.() -> Unit,
): DriverMessage {
	val builder = MessageBuilder(factory).apply(block)
	return DriverMessage(
		message = Message.OpMsg(
			body = MessageSection.Body(eager(builder.document!!)),
			sequences = builder.sequences.asSequence(),
		),
		readPreference = builder.readPreference,
	)
}

internal class DriverMessage(
	val message: Message.OpMsg,
	val readPreference: ReadPreference?,
)

/**
 * Instantiates a [Lazy] value that isn't lazy.
 *
 * This allows our API to contain lazy values without forcing us to be lazy everywhere.
 *
 * For example, we often want to be lazy during request sending (so all serialization happens as close as possible to the socket)
 * but not during reception (to extract information as quickly as possible and return the lock).
 */
internal fun <T> eager(value: T): Lazy<T> =
	object : Lazy<T> {
		override val value: T
			get() = value

		override fun isInitialized(): Boolean =
			true

		override fun toString(): String =
			"Lazy($value)"
	}
