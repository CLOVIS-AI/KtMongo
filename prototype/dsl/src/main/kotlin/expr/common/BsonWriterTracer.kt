package fr.qsh.ktmongo.dsl.expr.common

import org.bson.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId

private class LoggingBsonWriter(
	private val upstream: BsonWriter,
) : BsonWriter {

	// region Machinery

	private var indent = 0
	private val loggingBuffer = ArrayList<String>()

	private fun addLine(text: String) {
		loggingBuffer += buildString {
			repeat(indent) { append('\t') }
			append(text)
		}
	}

	private inline fun <T> logStateOnException(block: () -> T) {
		try {
			block()
		} catch (e: BsonInvalidOperationException) {
			throw BsonInvalidOperationException("An error occurred while writing the BSON expression.\n$this ^ the exception happened while trying to write this line.", e)
		}
	}

	override fun toString() = buildString {
		appendLine(" *** Expression writer ***")
		appendLine("This is a debugger helper. The data represented here is a pseudo-representation of the current state of the writer. This isn't the real contents of the writer.")

		for (line in loggingBuffer) {
			appendLine(line)
		}
	}

	// endregion
	// region Methods

	override fun flush() = logStateOnException {
		upstream.flush()
	}

	override fun writeBinaryData(binary: BsonBinary?) = logStateOnException {
		addLine("$binary (binary data)")
		upstream.writeBinaryData(binary)
	}

	override fun writeBinaryData(name: String?, binary: BsonBinary?) = logStateOnException {
		addLine("$name: $binary (binary data)")
		upstream.writeBinaryData(name, binary)
	}

	override fun writeBoolean(value: Boolean) = logStateOnException {
		addLine("$value (boolean)")
		upstream.writeBoolean(value)
	}

	override fun writeBoolean(name: String?, value: Boolean) = logStateOnException {
		addLine("$name: $value (boolean)")
		upstream.writeBoolean(name, value)
	}

	override fun writeDateTime(value: Long) = logStateOnException {
		addLine("$value (date time)")
		upstream.writeDateTime(value)
	}

	override fun writeDateTime(name: String?, value: Long) = logStateOnException {
		addLine("$name: $value (date time)")
		upstream.writeDateTime(name, value)
	}

	override fun writeDBPointer(value: BsonDbPointer?) = logStateOnException {
		addLine("$value (db pointer)")
		upstream.writeDBPointer(value)
	}

	override fun writeDBPointer(name: String?, value: BsonDbPointer?) = logStateOnException {
		addLine("$name: $value (db pointer)")
		upstream.writeDBPointer(name, value)
	}

	override fun writeDouble(value: Double) = logStateOnException {
		addLine("$value (double)")
		upstream.writeDouble(value)
	}

	override fun writeDouble(name: String?, value: Double) = logStateOnException {
		addLine("$name: $value (double)")
		upstream.writeDouble(name, value)
	}

	override fun writeEndArray() = logStateOnException {
		indent--
		addLine("]")
		upstream.writeEndArray()
	}

	override fun writeEndDocument() = logStateOnException {
		indent--
		addLine("}")
		upstream.writeEndDocument()
	}

	override fun writeInt32(value: Int) = logStateOnException {
		addLine("$value (int32)")
		upstream.writeInt32(value)
	}

	override fun writeInt32(name: String?, value: Int) = logStateOnException {
		addLine("$name: $value (int32)")
		upstream.writeInt32(name, value)
	}

	override fun writeInt64(value: Long) = logStateOnException {
		addLine("$value (int64)")
		upstream.writeInt64(value)
	}

	override fun writeInt64(name: String?, value: Long) = logStateOnException {
		addLine("$name: $value (int64)")
		upstream.writeInt64(name, value)
	}

	override fun writeDecimal128(value: Decimal128?) = logStateOnException {
		addLine("$value (decimal128)")
		upstream.writeDecimal128(value)
	}

	override fun writeDecimal128(name: String?, value: Decimal128?) = logStateOnException {
		addLine("$name: $value (decimal128)")
		upstream.writeDecimal128(name, value)
	}

	override fun writeJavaScript(code: String?) = logStateOnException {
		addLine("$code (JS)")
		upstream.writeJavaScript(code)
	}

	override fun writeJavaScript(name: String?, code: String?) = logStateOnException {
		addLine("$name: $code (JS)")
		upstream.writeJavaScript(name, code)
	}

	override fun writeJavaScriptWithScope(code: String?) = logStateOnException {
		addLine("$code (JS with scope)")
		upstream.writeJavaScriptWithScope(code)
	}

	override fun writeJavaScriptWithScope(name: String?, code: String?) = logStateOnException {
		addLine("$name: $code (JS with scope)")
		upstream.writeJavaScriptWithScope(name, code)
	}

	override fun writeMaxKey() = logStateOnException {
		addLine("(max key)")
		upstream.writeMaxKey()
	}

	override fun writeMaxKey(name: String?) = logStateOnException {
		addLine("$name: (max key)")
		upstream.writeMaxKey(name)
	}

	override fun writeMinKey() = logStateOnException {
		addLine("(min key)")
		upstream.writeMinKey()
	}

	override fun writeMinKey(name: String?) = logStateOnException {
		addLine("(min key)")
		upstream.writeMinKey(name)
	}

	override fun writeName(name: String?) = logStateOnException {
		addLine("$name:")
		upstream.writeName(name)
	}

	override fun writeNull() = logStateOnException {
		addLine("null")
		upstream.writeNull()
	}

	override fun writeNull(name: String?) = logStateOnException {
		addLine("$name: null")
		upstream.writeNull(name)
	}

	override fun writeObjectId(objectId: ObjectId?) = logStateOnException {
		addLine("$objectId (ObjectID)")
		upstream.writeObjectId(objectId)
	}

	override fun writeObjectId(name: String?, objectId: ObjectId?) = logStateOnException {
		addLine("$name: $objectId (ObjectId)")
		upstream.writeObjectId(name, objectId)
	}

	override fun writeRegularExpression(regularExpression: BsonRegularExpression?) = logStateOnException {
		addLine("$regularExpression (RegExp)")
		upstream.writeRegularExpression(regularExpression)
	}

	override fun writeRegularExpression(name: String?, regularExpression: BsonRegularExpression?) = logStateOnException {
		addLine("$name: $regularExpression (RegExp)")
		upstream.writeRegularExpression(name, regularExpression)
	}

	override fun writeStartArray() = logStateOnException {
		addLine("[")
		indent++
		upstream.writeStartArray()
	}

	override fun writeStartArray(name: String?) = logStateOnException {
		addLine("$name: [")
		indent++
		upstream.writeStartArray(name)
	}

	override fun writeStartDocument() = logStateOnException{
		addLine("{")
		indent++
		upstream.writeStartDocument()
	}

	override fun writeStartDocument(name: String?) = logStateOnException {
		addLine("$name: {")
		indent++
		upstream.writeStartDocument(name)
	}

	override fun writeString(value: String?) = logStateOnException {
		addLine("$value (string)")
		upstream.writeString(value)
	}

	override fun writeString(name: String?, value: String?) = logStateOnException {
		addLine("$name: $value (string)")
		upstream.writeString(name, value)
	}

	override fun writeSymbol(value: String?) = logStateOnException {
		addLine("$value (symbol)")
		upstream.writeSymbol(value)
	}

	override fun writeSymbol(name: String?, value: String?) = logStateOnException {
		addLine("$name: $value (symbol)")
		upstream.writeSymbol(name, value)
	}

	override fun writeTimestamp(value: BsonTimestamp?) = logStateOnException {
		addLine("$value (timestamp)")
		upstream.writeTimestamp(value)
	}

	override fun writeTimestamp(name: String?, value: BsonTimestamp?) = logStateOnException {
		addLine("$name: $value (timestamp)")
		upstream.writeTimestamp(name, value)
	}

	override fun writeUndefined() = logStateOnException {
		addLine("undefined")
		upstream.writeUndefined()
	}

	override fun writeUndefined(name: String?) = logStateOnException {
		addLine("$name: undefined")
		upstream.writeUndefined(name)
	}

	override fun pipe(reader: BsonReader?) = logStateOnException {
		addLine("Piping a reader…")
		upstream.pipe(reader)
	}

	// endregion
}

fun BsonWriter.withLoggedContext(storeLogs: Boolean = true) =
	if (storeLogs) LoggingBsonWriter(this)
	else this
