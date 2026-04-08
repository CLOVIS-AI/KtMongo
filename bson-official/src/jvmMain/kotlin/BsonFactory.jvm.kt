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

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.official.types.*
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonBinaryReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * [BsonDocument] and [BsonArray] factory that uses the official MongoDB driver's [CodecRegistry] machinery.
 *
 * Documents and arrays created from this class follow your existing configuration of the official MongoDB driver,
 * including any library supported by the official MongoDB driver (KotlinX.Serialization, Jackson…).
 *
 * This factory also adds support for the types specific to KtMongo.
 * If you also use the official driver directly, you will need to configure it to access the KtMongo codecs.
 * To learn more, see [BsonFactory.codecRegistry].
 */
actual class BsonFactory(
	codecRegistry: CodecRegistry,
) : BsonFactory {

	/**
	 * The [CodecRegistry] used by this factory to create and read documents.
	 *
	 * This registry includes codecs for KtMongo-specific types.
	 * When mixing KtMongo with the official driver, it is recommended to configure
	 * the official driver using this registry to ensure types are serialized identically by both libraries.
	 *
	 * ```kotlin
	 * val client = MongoClient.create("mongodb://mongo:27017")
	 * val factory = BsonFactory(client.codecRegistry)
	 * val clientWithKotlin = client.withCodecRegistry(factory.codecRegistry)
	 *
	 * val database = clientWithKotlin.getDatabase("myDatabase")
	 * val collection = database.getCollection<MyType>("myCollection").asKtMongo()
	 * ```
	 *
	 * If you only use the KtMongo DSL to write queries, you do not need this configuration.
	 */
	val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
		CodecRegistries.fromCodecs(
			KotlinBsonDocumentCodec(this),
			KotlinBsonArrayCodec(this),
			KotlinObjectIdCodec(),
			KotlinTimestampCodec(),
			KotlinUuidCodec(),
			KotlinInstantCodec(),
			KotlinVectorCodec(),
			KotlinFloatVectorCodec(),
			KotlinBooleanVectorCodec(),
			KotlinByteVectorCodec(),
		),
		codecRegistry,
	)

	@LowLevelApi
	actual override fun buildDocument(block: BsonFieldWriter.() -> Unit): BsonDocument {
		val document = org.bson.BsonDocument()

		BsonDocumentWriter(document).use { writer ->
			JavaBsonDocumentWriter(this, writer).writeDocument {
				block()
			}
		}

		return readDocument(document)
	}

	@LowLevelApi
	actual override fun buildDocument(instance: BsonFieldWriteable): BsonDocument  =
		buildDocument { instance.writeTo(this) }

	@LowLevelApi
	actual override fun <T : Any> encode(obj: T, type: KType): BsonDocument {
		val classifier = type.classifier
		require(classifier is KClass<*>) { "The official Java driver only supports types that can be represented as classes\n\tObject: $obj\n\tType: $type" }

		@Suppress("UNCHECKED_CAST")
		val codec = codecRegistry.get(classifier.java) as Codec<T>

		val document = org.bson.BsonDocument()

		codec.encode(
			BsonDocumentWriter(document),
			obj,
			EncoderContext.builder().isEncodingCollectibleDocument(true).build(),
		)

		return readDocument(document)
	}

	/**
	 * Wraps a [org.bson.BsonDocument] from the official MongoDB driver into its KtMongo equivalent.
	 */
	fun readDocument(official: org.bson.BsonDocument): BsonDocument =
		BsonDocument(official, this)

	@LowLevelApi
	actual override fun readDocument(bytes: ByteArray): BsonDocument {
		val codec = codecRegistry.get(org.bson.BsonDocument::class.java)
		val buffer = ByteBuffer.wrap(bytes)
		val document = codec.decode(
			BsonBinaryReader(buffer),
			DecoderContext.builder().build(),
		)
		return readDocument(document)
	}

	@LowLevelApi
	actual override fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray {
		val nativeArray = org.bson.BsonArray()

		JavaBsonArrayWriter(this, nativeArray).block()

		return readArray(nativeArray)
	}

	@LowLevelApi
	actual override fun buildArray(instance: BsonValueWriteable): BsonArray =
		buildArray { instance.writeTo(this) }

	@LowLevelApi
	actual override fun readArray(bytes: ByteArray): BsonArray {
		val codec = codecRegistry.get(org.bson.BsonArray::class.java)
		val buffer = ByteBuffer.wrap(bytes)
		val document = codec.decode(
			BsonBinaryReader(buffer),
			DecoderContext.builder().build(),
		)
		return BsonArray(document, this)
	}

	/**
	 * Wraps a [org.bson.BsonArray] from the official MongoDB driver into its KtMongo equivalent.
	 */
	fun readArray(raw: org.bson.BsonArray): BsonArray =
		BsonArray(raw, this)

	/**
	 * Wraps a [org.bson.BsonValue] from the official MongoDB driver into its KtMongo equivalent.
	 */
	fun readValue(raw: org.bson.BsonValue): BsonValue =
		BsonValue(raw, this)

	/**
	 * Returns the instance of [Codec] (from the official MongoDB driver)
	 * that is used to encode or decode the given [type].
	 *
	 * **If [type] and [T] do not match, the behavior is unspecified.**
	 * Prefer using the no-argument of this method.
	 *
	 * @see codecRegistry The codec registry used by this BSON factory.
	 * @throws BsonDecodingException If no matching codec is found for the given type.
	 */
	@LowLevelApi
	fun <T> findCodecForType(type: KType): Codec<T> {
		val classifier = type.classifier

		if (classifier !is KClass<*>) {
			throw BsonDecodingException("The official Java driver only supports types that can be represented as classes; cannot find a codec for type $type (classifier: $classifier)")
		}

		@Suppress("UNCHECKED_CAST")
		classifier as KClass<T & Any>

		val codec = try {
			codecRegistry.get(classifier.java)
		} catch (e: Exception) {
			throw BsonDecodingException(
				"""
					Could not find codec for type $type (${classifier.java})
					If you're using org.bson:bson-kotlin, are you sure your type is a non-private data class?
					If you're using org.bson:bson-kotlinx, did you annotate your type with @Serializable and configured the KotlinX.Serialization plugin?
				""".trimIndent(),
				e,
			)
		}

		return codec
	}

	/**
	 * Returns the instance of [Codec] (from the official MongoDB driver)
	 * that is used to encode or decode the given type [T].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * val client = MongoClient.create("mongodb://mongo:27017")
	 * val factory = BsonFactory(client.codecRegistry)
	 *
	 * val codec = factory.findCodecForType<String>()
	 * ```
	 *
	 * @see codecRegistry The codec registry used by this BSON factory.
	 * @throws BsonDecodingException If no matching codec is found for the given type.
	 */
	@LowLevelApi
	inline fun <reified T> findCodecForType(): Codec<T> =
		findCodecForType(typeOf<T>())
}
