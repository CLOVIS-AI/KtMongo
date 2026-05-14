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

package opensavvy.ktmongo.bson.official.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.plus
import opensavvy.ktmongo.bson.official.BsonArray
import opensavvy.ktmongo.bson.official.BsonDocument
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.BsonValue
import org.bson.codecs.kotlinx.defaultSerializersModule as bsonKotlinxDefaultSerializersModule

// Contextual serializers for the common interfaces, delegating to the concrete JVM serializers.
// The common interfaces are annotated with @Serializable(with = ContextualSerializer::class), so the
// kotlinx.serialization plugin looks these up at runtime from this module rather than generating
// polymorphic (discriminator-wrapped) code.

private object CommonBsonDocumentSerializer : KSerializer<opensavvy.ktmongo.bson.BsonDocument> {
	override val descriptor: SerialDescriptor = BsonDocument.Serializer.descriptor

	override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.BsonDocument) =
		BsonDocument.Serializer.serialize(encoder, value as BsonDocument)

	override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.BsonDocument =
		BsonDocument.Serializer.deserialize(decoder)
}

private object CommonBsonArraySerializer : KSerializer<opensavvy.ktmongo.bson.BsonArray> {
	override val descriptor: SerialDescriptor = BsonArray.Serializer.descriptor

	override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.BsonArray) =
		BsonArray.Serializer.serialize(encoder, value as BsonArray)

	override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.BsonArray =
		BsonArray.Serializer.deserialize(decoder)
}

private object CommonBsonValueSerializer : KSerializer<opensavvy.ktmongo.bson.BsonValue> {
	override val descriptor: SerialDescriptor = BsonValue.Serializer.descriptor

	override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.BsonValue) =
		BsonValue.Serializer.serialize(encoder, value as BsonValue)

	override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.BsonValue =
		BsonValue.Serializer.deserialize(decoder)
}

/**
 * A [SerializersModule] that registers KtMongo BSON types for KotlinX.Serialization.
 *
 * Use this when building a [BsonFactory] with [org.bson.codecs.kotlinx.KotlinSerializerCodecProvider]
 * so that fields typed as [opensavvy.ktmongo.bson.BsonDocument],
 * [opensavvy.ktmongo.bson.BsonArray], or [opensavvy.ktmongo.bson.BsonValue] are serialized correctly:
 *
 * ```kotlin
 * val factory = BsonFactory(CodecRegistries.fromProviders(
 *     KotlinSerializerCodecProvider(ktMongoSerializersModule),
 *     ...
 * ))
 * ```
 *
 * Fields typed as the common interfaces are serialized as-is (no type discriminator):
 *
 * ```kotlin
 * @Serializable
 * data class MyDoc(
 *     val nested: BsonDocument,
 *     val array: BsonArray,
 *     val value: BsonValue,
 * )
 * ```
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
internal val ktMongoSerializersModule: SerializersModule = bsonKotlinxDefaultSerializersModule + SerializersModule {
	contextual(CommonBsonDocumentSerializer)
	contextual(CommonBsonArraySerializer)
	contextual(CommonBsonValueSerializer)
}
