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

package opensavvy.ktmongo.bson.official.types

import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.BsonValue
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.BsonValueCodec
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

internal class KotlinBsonValueCodec(
	private val factory: BsonFactory,
) : Codec<BsonValue> {
	private val valueCodec = BsonValueCodec()

	override fun encode(writer: BsonWriter, value: BsonValue, encoderContext: EncoderContext) {
		valueCodec.encode(writer, value.raw, encoderContext)
	}

	override fun getEncoderClass(): Class<BsonValue> =
		BsonValue::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): BsonValue? =
		valueCodec.decode(reader, decoderContext)?.let { BsonValue(it, factory) }

}

internal class KotlinCommonBsonValueCodec(
	private val factory: BsonFactory,
) : Codec<opensavvy.ktmongo.bson.BsonValue> {
	private val valueCodec = BsonValueCodec()

	override fun encode(writer: BsonWriter, value: opensavvy.ktmongo.bson.BsonValue, encoderContext: EncoderContext) {
		valueCodec.encode(writer, (value as BsonValue).raw, encoderContext)
	}

	override fun getEncoderClass(): Class<opensavvy.ktmongo.bson.BsonValue> =
		opensavvy.ktmongo.bson.BsonValue::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): opensavvy.ktmongo.bson.BsonValue? =
		valueCodec.decode(reader, decoderContext)?.let { BsonValue(it, factory) }

}
