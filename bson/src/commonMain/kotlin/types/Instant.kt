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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Serializer for [kotlin.time.Instant] that serializes as a [BsonType.Datetime].
 *
 * The MongoDB official driver's KotlinX.Serialization support doesn't yet support [kotlin.time.Instant]
 * (see [JAVA-6084](https://jira.mongodb.org/browse/JAVA-6084)). This serializer adds this support.
 * Once this ticket is fixed, this serializer will be deprecated.
 *
 * When KtMongo Multiplatform's BSON implementation is used, this serializer does nothing, as the default serializer
 * already uses [BsonType.Datetime].
 *
 * ### Behavior with non-BSON formats
 *
 * When serializing to non-BSON formats (e.g., JSON, gRPC…), the instant is encoded as a string in ISO-8601 format.
 *
 * ### Example
 *
 * The annotation [UseSerializers] can be used to configure all [Instant] fields in that file to use this serializer:
 *
 * ```kotlin
 * @file:UseSerializers(InstantAsBsonDatetimeSerializer::class)
 *
 * @Serializable
 * data class Foo(
 *     val _id: ObjectId,
 *     val createdAt: Instant,
 * )
 * ```
 *
 * Alternatively, [Serializable] can be used to configure a specific [Instant] field to use this serializer:
 *
 * ```kotlin
 * @Serializable
 * data class Foo(
 *     val _id: ObjectId,
 *     val createdAt: @Serializable(with = InstantAsBsonDatetimeSerializer::class) Instant,
 * )
 * ```
 */
@ExperimentalTime
object InstantAsBsonDatetimeSerializer : KSerializer<Instant> {
	override val descriptor: SerialDescriptor
		get() = Instant.serializer().descriptor

	override fun serialize(encoder: Encoder, value: Instant) {
		serializeInstantPlatformSpecific(encoder, value)
	}

	override fun deserialize(decoder: Decoder): Instant =
		deserializeInstantPlatformSpecific(decoder)
}

@ExperimentalTime
internal fun serializeInstantAsString(encoder: Encoder, value: Instant) {
	encoder.encodeString(value.toString())
}

@ExperimentalTime
internal fun deserializeInstantAsString(decoder: Decoder): Instant =
	Instant.parse(decoder.decodeString())

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [serializeInstantAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
@ExperimentalTime
internal expect fun serializeInstantPlatformSpecific(encoder: Encoder, value: Instant)

/**
 * On the JVM, when using KotlinX.Serialization with the official driver, we must hard-code a different behavior.
 *
 * All non-JVM platforms implement this function by calling [deserializeInstantAsString].
 * This could be simplified with [KT-20427](https://youtrack.jetbrains.com/projects/KT/issues/KT-20427).
 */
@ExperimentalTime
internal expect fun deserializeInstantPlatformSpecific(decoder: Decoder): Instant
