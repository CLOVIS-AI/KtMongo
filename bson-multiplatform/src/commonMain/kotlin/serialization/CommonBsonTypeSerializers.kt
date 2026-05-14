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

package opensavvy.ktmongo.bson.multiplatform.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opensavvy.ktmongo.bson.multiplatform.BsonArray
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonValue

// Contextual serializers for the common interfaces (BsonDocument, BsonArray and BsonValue).
// Each interface directly delegates to the matching Multiplatform serializer.

internal object CommonBsonDocumentSerializer : KSerializer<opensavvy.ktmongo.bson.BsonDocument> {
	override val descriptor: SerialDescriptor = BsonDocument.Serializer.descriptor

	override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.BsonDocument) =
		encoder.encodeSerializableValue(BsonDocument.Serializer, value as BsonDocument)

	override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.BsonDocument =
		decoder.decodeSerializableValue(BsonDocument.Serializer)
}

internal object CommonBsonArraySerializer : KSerializer<opensavvy.ktmongo.bson.BsonArray> {
	override val descriptor: SerialDescriptor = BsonArray.Serializer.descriptor

	override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.BsonArray) =
		encoder.encodeSerializableValue(BsonArray.Serializer, value as BsonArray)

	override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.BsonArray =
		decoder.decodeSerializableValue(BsonArray.Serializer)
}

internal object CommonBsonValueSerializer : KSerializer<opensavvy.ktmongo.bson.BsonValue> {
	override val descriptor: SerialDescriptor = BsonValue.Serializer.descriptor

	override fun serialize(encoder: Encoder, value: opensavvy.ktmongo.bson.BsonValue) =
		encoder.encodeSerializableValue(BsonValue.Serializer, value as BsonValue)

	override fun deserialize(decoder: Decoder): opensavvy.ktmongo.bson.BsonValue =
		decoder.decodeSerializableValue(BsonValue.Serializer)
}
