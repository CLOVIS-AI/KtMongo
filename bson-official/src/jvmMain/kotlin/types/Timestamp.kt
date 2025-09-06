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

import opensavvy.ktmongo.bson.types.Timestamp
import org.bson.BsonReader
import org.bson.BsonTimestamp
import org.bson.BsonWriter
import org.bson.codecs.BsonTimestampCodec
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.time.ExperimentalTime

// region Conversions

fun Timestamp.toOfficial(): BsonTimestamp =
	BsonTimestamp(value.toLong())

fun BsonTimestamp.toKtMongo(): Timestamp =
	Timestamp(value.toULong())

// endregion
// region Codec

@OptIn(ExperimentalTime::class)
internal class KotlinTimestampCodec : Codec<Timestamp> {
	private val objCodec = BsonTimestampCodec()

	override fun encode(writer: BsonWriter?, value: Timestamp, encoderContext: EncoderContext?) {
		objCodec.encode(writer, value.toOfficial(), encoderContext)
	}

	override fun getEncoderClass(): Class<Timestamp> =
		Timestamp::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Timestamp? =
		objCodec.decode(reader, decoderContext)?.toKtMongo()
}

// endregion
