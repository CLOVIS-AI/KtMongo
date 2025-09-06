/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.official.types

import org.bson.BSONException
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.UuidRepresentation
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.UuidCodec
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

// region Codec

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
internal class KotlinUuidCodec : Codec<Uuid> {
	private val standardCodec = UuidCodec(UuidRepresentation.STANDARD)
	private val legacyCodec = UuidCodec(UuidRepresentation.PYTHON_LEGACY)

	override fun encode(writer: BsonWriter?, value: Uuid, encoderContext: EncoderContext?) {
		standardCodec.encode(writer, value.toJavaUuid(), encoderContext)
	}

	override fun getEncoderClass(): Class<Uuid> =
		Uuid::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Uuid? {
		return when (val subtype = reader.peekBinarySubType()) {
			3.toByte() -> legacyCodec.decode(reader, decoderContext)?.toKotlinUuid()
			4.toByte() -> standardCodec.decode(reader, decoderContext)?.toKotlinUuid()
			else -> throw BSONException("Cannot deserialize a Uuid with binary subtype $subtype")
		}
	}
}

// endregion
