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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonBinary
import org.bson.UuidRepresentation
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

private val isOfficialKotlinSerializationEnabled =
	ClassLoader.getSystemClassLoader().loadClass("org.bson.codecs.kotlinx.BsonEncoder") != null

@OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)
internal actual fun serializeUuidPlatformSpecific(encoder: Encoder, value: Uuid) {
	if (isOfficialKotlinSerializationEnabled && encoder is BsonEncoder) {
		encoder.encodeBsonValue(BsonBinary(value.toJavaUuid(), UuidRepresentation.STANDARD))
	} else {
		serializeUuidAsString(encoder, value)
	}
}

@OptIn(ExperimentalSerializationApi::class)
@ExperimentalUuidApi
internal actual fun deserializeUuidPlatformSpecific(decoder: Decoder): Uuid =
	if (isOfficialKotlinSerializationEnabled && decoder is BsonDecoder) {
		val bsonBinary = decoder.decodeBsonValue() as BsonBinary
		bsonBinary.asUuid(UuidRepresentation.STANDARD).toKotlinUuid()
	} else {
		deserializeUuidAsString(decoder)
	}
