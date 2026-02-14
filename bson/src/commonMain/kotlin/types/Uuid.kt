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

package opensavvy.ktmongo.bson.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opensavvy.ktmongo.bson.BsonType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Serializer for [kotlin.uuid.Uuid] that serializes as a [BsonType.BinaryData] with subtype 4 (UUID).
 *
 * The MongoDB official driver's KotlinX.Serialization support doesn't yet support [kotlin.uuid.Uuid]
 * (see [JAVA-6083](https://jira.mongodb.org/browse/JAVA-6083)). This serializer adds this support.
 * Once this ticket is fixed, this serializer will be deprecated.
 *
 * When KtMongo Multiplatform's BSON implementation is used, this serializer does nothing, as the default serializer
 * already uses [BsonType.BinaryData].
 *
 * ### Behavior with non-BSON formats
 *
 * When serializing to non-BSON formats (e.g., JSON, gRPC…), the UUID is encoded as a string in its standard
 * hyphenated format (e.g., `550e8400-e29b-41d4-a716-446655440000`).
 *
 * ### Example
 *
 * The annotation [UseSerializers] can be used to configure all [Uuid] fields in that file to use this serializer:
 *
 * ```kotlin
 * @file:UseSerializers(UuidAsBsonBinarySerializer::class)
 *
 * @Serializable
 * data class Foo(
 *     val _id: ObjectId,
 *     val correlationId: Uuid,
 * )
 * ```
 *
 * Alternatively, [Serializable] can be used to configure a specific [Uuid] field to use this serializer:
 *
 * ```kotlin
 * @Serializable
 * data class Foo(
 *     val _id: ObjectId,
 *     val correlationId: @Serializable(with = UuidAsBsonBinarySerializer::class) Uuid,
 * )
 * ```
 */
@ExperimentalUuidApi
object UuidAsBsonBinarySerializer : KSerializer<Uuid> {
	override val descriptor: SerialDescriptor
		get() = Uuid.serializer().descriptor

	override fun serialize(encoder: Encoder, value: Uuid) {
		serializeUuidPlatformSpecific(encoder, value)
	}

	override fun deserialize(decoder: Decoder): Uuid =
		deserializeUuidPlatformSpecific(decoder)
}

@ExperimentalUuidApi
internal fun serializeUuidAsString(encoder: Encoder, value: Uuid) {
	encoder.encodeString(value.toString())
}

@ExperimentalUuidApi
internal fun deserializeUuidAsString(decoder: Decoder): Uuid =
	Uuid.parse(decoder.decodeString())

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [serializeUuidAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
@ExperimentalUuidApi
internal expect fun serializeUuidPlatformSpecific(encoder: Encoder, value: Uuid)

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [deserializeUuidAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
@ExperimentalUuidApi
internal expect fun deserializeUuidPlatformSpecific(decoder: Decoder): Uuid
