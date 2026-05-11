/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.official.BsonArray
import opensavvy.ktmongo.bson.official.BsonFactory
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.BsonArrayCodec
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

internal class KotlinBsonArrayCodec(
	private val factory: BsonFactory,
) : Codec<BsonArray> {
	private val arrayCodec = BsonArrayCodec()

	override fun encode(writer: BsonWriter, value: BsonArray, encoderContext: EncoderContext) {
		arrayCodec.encode(writer, value.raw, encoderContext)
	}

	override fun getEncoderClass(): Class<BsonArray> =
		BsonArray::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): BsonArray? =
		arrayCodec.decode(reader, decoderContext)?.let { BsonArray(it, factory) }

}

internal class KotlinCommonBsonArrayCodec(
	private val factory: BsonFactory,
) : Codec<opensavvy.ktmongo.bson.BsonArray> {
	private val arrayCodec = BsonArrayCodec()

	override fun encode(writer: BsonWriter, value: opensavvy.ktmongo.bson.BsonArray, encoderContext: EncoderContext) {
		arrayCodec.encode(writer, (value as BsonArray).raw, encoderContext)
	}

	override fun getEncoderClass(): Class<opensavvy.ktmongo.bson.BsonArray> =
		opensavvy.ktmongo.bson.BsonArray::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): opensavvy.ktmongo.bson.BsonArray? =
		arrayCodec.decode(reader, decoderContext)?.let { BsonArray(it, factory) }

}
