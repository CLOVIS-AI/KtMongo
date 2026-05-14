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
import opensavvy.ktmongo.bson.types.ExperimentalGeoBsonApi
import opensavvy.ktmongo.bson.types.Geo
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

@OptIn(ExperimentalGeoBsonApi::class, LowLevelApi::class)
internal class KotlinGeoPointCodec(
	private val factory: BsonFactory,
) : Codec<Geo.Point> {

	override fun encode(writer: BsonWriter, value: Geo.Point, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "Point")
			writeArray("coordinates") {
				writeDouble(value.x.degrees)
				writeDouble(value.y.degrees)
			}
		}
	}

	override fun getEncoderClass(): Class<Geo.Point> =
		Geo.Point::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Geo.Point {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "Point") { "Expected a GeoJSON Point, but found: $type\n\tData: $doc" }
		require(coordinates?.size == 2) { "A GeoJSON Point should have two coordinates, but found: $coordinates\n\tData: $doc" }

		val x = coordinates[0]?.decodeDouble()
		val y = coordinates[1]?.decodeDouble()

		requireNotNull(x) { "Cannot decode the longitude\n\tData: $doc" }
		requireNotNull(y) { "Cannot decode the latitude\n\tData: $doc" }

		return Geo.Point(
			Geo.Longitude(x),
			Geo.Latitude(y),
		)
	}
}
