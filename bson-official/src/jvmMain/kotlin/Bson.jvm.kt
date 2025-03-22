/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.*
import org.bson.BsonArray as OfficialBsonArray
import org.bson.BsonDocument as OfficialBsonDocument

actual class Bson(val raw: OfficialBsonDocument): Bson {

	override fun toString(): String =
		raw.toString()
}

actual class BsonArray(val raw: OfficialBsonArray): BsonArray {

	override fun toString(): String {
		// Yes, this is very ugly, and probably inefficient.
		// The Java library doesn't provide a way to serialize arrays to JSON.
		// https://www.mongodb.com/community/forums/t/how-to-convert-a-single-bsonvalue-such-as-bsonarray-to-json-in-the-java-bson-library

		val document = BsonDocument("a", raw).toJson()

		return document.substring(
			document.indexOf('['),
			document.lastIndexOf(']') + 1
		).trim()
	}
}

internal class KotlinBsonCodec : Codec<opensavvy.ktmongo.bson.official.Bson> {
	private val documentCodec = BsonDocumentCodec()

	override fun encode(writer: BsonWriter, value: opensavvy.ktmongo.bson.official.Bson, encoderContext: EncoderContext) {
		documentCodec.encode(writer, value.raw, encoderContext)
	}

	override fun getEncoderClass(): Class<opensavvy.ktmongo.bson.official.Bson>? =
		opensavvy.ktmongo.bson.official.Bson::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): opensavvy.ktmongo.bson.official.Bson? =
		documentCodec.decode(reader, decoderContext)?.let(::Bson)
}

internal class KotlinBsonArrayCodec : Codec<opensavvy.ktmongo.bson.official.BsonArray> {
	private val arrayCodec = BsonArrayCodec()

	override fun encode(writer: BsonWriter, value: opensavvy.ktmongo.bson.official.BsonArray, encoderContext: EncoderContext) {
		arrayCodec.encode(writer, value.raw, encoderContext)
	}

	override fun getEncoderClass(): Class<opensavvy.ktmongo.bson.official.BsonArray>? =
		opensavvy.ktmongo.bson.official.BsonArray::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): opensavvy.ktmongo.bson.official.BsonArray? =
		arrayCodec.decode(reader, decoderContext)?.let(::BsonArray)

}
