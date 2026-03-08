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
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonBinary
import org.bson.BsonBinarySubType
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder

@OptIn(ExperimentalSerializationApi::class, LowLevelApi::class)
internal actual fun serializeVectorPlatformSpecific(encoder: Encoder, value: Vector) {
	if (isOfficialKotlinSerializationEnabled && encoder is BsonEncoder) {
		encoder.encodeBsonValue(BsonBinary(BsonBinarySubType.VECTOR, value.toBinaryData()))
	} else {
		serializeVectorAsString(encoder, value)
	}
}

@OptIn(ExperimentalSerializationApi::class, LowLevelApi::class)
internal actual fun deserializeVectorPlatformSpecific(decoder: Decoder): Vector =
	if (isOfficialKotlinSerializationEnabled && decoder is BsonDecoder) {
		val bsonBinary = decoder.decodeBsonValue() as BsonBinary
		Vector.fromBinaryData(bsonBinary.data)
	} else {
		deserializeVectorAsString(decoder)
	}
