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
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import kotlin.time.ExperimentalTime

private val isOfficialKotlinSerializationEnabled =
	ClassLoader.getSystemClassLoader().loadClass("org.bson.codecs.kotlinx.BsonEncoder") != null

@OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
internal actual fun serializeObjectIdPlatformSpecific(encoder: Encoder, value: ObjectId) {
	if (isOfficialKotlinSerializationEnabled && encoder is BsonEncoder) {
		encoder.encodeObjectId(org.bson.types.ObjectId(value.bytes))
	} else {
		serializeObjectIdAsString(encoder, value)
	}
}

@OptIn(ExperimentalSerializationApi::class)
@ExperimentalTime
internal actual fun deserializeObjectIdPlatformSpecific(decoder: Decoder): ObjectId =
	if (isOfficialKotlinSerializationEnabled && decoder is BsonDecoder)
		ObjectId(decoder.decodeObjectId().toByteArray())
	else
		deserializeObjectIdAsString(decoder)
