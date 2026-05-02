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

import opensavvy.ktmongo.bson.official.BsonDocument
import opensavvy.ktmongo.bson.official.BsonFactory
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

internal class KotlinBsonDocumentCodec(
	private val factory: BsonFactory,
) : Codec<BsonDocument> {
	private val documentCodec = BsonDocumentCodec()

	override fun encode(writer: BsonWriter, value: BsonDocument, encoderContext: EncoderContext) {
		documentCodec.encode(writer, value.raw, encoderContext)
	}

	override fun getEncoderClass(): Class<BsonDocument> =
		BsonDocument::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): BsonDocument? =
		documentCodec.decode(reader, decoderContext)?.let { BsonDocument(it, factory) }

}

internal class KotlinCommonBsonDocumentCodec(
	private val factory: BsonFactory,
) : Codec<opensavvy.ktmongo.bson.BsonDocument> {
	private val documentCodec = BsonDocumentCodec()

	override fun encode(writer: BsonWriter, value: opensavvy.ktmongo.bson.BsonDocument, encoderContext: EncoderContext) {
		documentCodec.encode(writer, (value as BsonDocument).raw, encoderContext)
	}

	override fun getEncoderClass(): Class<opensavvy.ktmongo.bson.BsonDocument> =
		opensavvy.ktmongo.bson.BsonDocument::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): opensavvy.ktmongo.bson.BsonDocument? =
		documentCodec.decode(reader, decoderContext)?.let { BsonDocument(it, factory) }

}
