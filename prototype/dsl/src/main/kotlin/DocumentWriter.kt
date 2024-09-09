package fr.qsh.ktmongo.dsl

import org.bson.BsonWriter
import org.bson.codecs.Encoder
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry

/**
 * Helper to start a document, ensuring it is closed.
 */
@LowLevelApi
@PublishedApi
internal inline fun BsonWriter.writeDocument(name: String, block: () -> Unit) {
	writeStartDocument(name)
	block()
	writeEndDocument()
}

/**
 * Helper to start a document, ensuring it is closed.
 */
@LowLevelApi
@PublishedApi
internal inline fun BsonWriter.writeDocument(block: () -> Unit) {
	writeStartDocument()
	block()
	writeEndDocument()
}

/**
 * Helper to start an array, ensuring it is closed.
 */
@LowLevelApi
@PublishedApi
internal inline fun BsonWriter.writeArray(block: () -> Unit) {
	writeStartArray()
	block()
	writeEndArray()
}

/**
 * Helper to start an array, ensuring it is closed.
 */
@LowLevelApi
@PublishedApi
internal inline fun BsonWriter.writeArray(name: String, block: () -> Unit) {
	writeStartArray(name)
	block()
	writeEndArray()
}

@LowLevelApi
@PublishedApi
internal fun <T> BsonWriter.writeObject(value: T, codec: CodecRegistry) {
	@Suppress("UNCHECKED_CAST") // Kotlin doesn't smart-cast here, but should, this is safe
	(codec.get(value!!::class.java) as Encoder<T>)
		.encode(
			this,
			value,
			EncoderContext.builder()
				.isEncodingCollectibleDocument(true)
				.build()
		)
}
