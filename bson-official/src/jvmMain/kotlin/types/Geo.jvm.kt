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

@file:OptIn(ExperimentalGeoBsonApi::class)

package opensavvy.ktmongo.bson.official.types

import opensavvy.ktmongo.bson.official.BsonArray
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.types.ExperimentalGeoBsonApi
import opensavvy.ktmongo.bson.types.Geo
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

private fun BsonArray.decodeAsPoint(): Geo.Point {
	require(size == 2) { "A GeoJSON Point should have two coordinates, but found: $this" }

	val x = this[0]?.decodeDouble()
	val y = this[1]?.decodeDouble()

	requireNotNull(x) { "Cannot decode the longitude\n\tCoordinates: $this" }
	requireNotNull(y) { "Cannot decode the latitude\n\tCoordinates: $this" }

	return Geo.Point(
		Geo.Longitude(x),
		Geo.Latitude(y),
	)
}

@OptIn(LowLevelApi::class)
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
		requireNotNull(coordinates) { "Missing coordinates field in GeoJSON Point\n\tData: $doc" }

		return coordinates.decodeAsPoint()
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoLineStringCodec(
	private val factory: BsonFactory,
) : Codec<Geo.LineString> {

	override fun encode(writer: BsonWriter, value: Geo.LineString, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "LineString")
			writeArray("coordinates") {
				for (point in value.points) {
					writeArray {
						writeDouble(point.x.degrees)
						writeDouble(point.y.degrees)
					}
				}
			}
		}
	}

	override fun getEncoderClass(): Class<Geo.LineString> =
		Geo.LineString::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Geo.LineString {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "LineString") { "Expected a GeoJSON LineString, but found: $type\n\tData: $doc" }
		require(coordinates != null && coordinates.size >= 2) { "A GeoJSON LineString should have at least two coordinate pairs, but found: $coordinates\n\tData: $doc" }

		val points = coordinates
			.asIterable()
			.map { it.decodeArray().decodeAsPoint() }

		return Geo.LineString(points)
	}
}
