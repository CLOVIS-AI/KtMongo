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

import opensavvy.ktmongo.bson.decode
import opensavvy.ktmongo.bson.official.BsonArray
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.types.ExperimentalGeoBsonApi
import opensavvy.ktmongo.bson.types.Geo
import opensavvy.ktmongo.bson.types.Geo.*
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

private fun BsonArray.decodeAsPoint(): Point {
	require(size == 2) { "A GeoJSON Point should have two coordinates, but found: $this" }

	val x = this[0]?.decodeDouble()
	val y = this[1]?.decodeDouble()

	requireNotNull(x) { "Cannot decode the longitude\n\tCoordinates: $this" }
	requireNotNull(y) { "Cannot decode the latitude\n\tCoordinates: $this" }

	return Point(
		Longitude(x),
		Latitude(y),
	)
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoPointCodec(
	private val factory: BsonFactory,
) : Codec<Point> {

	override fun encode(writer: BsonWriter, value: Point, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "Point")
			writeArray("coordinates") {
				writeDouble(value.x.degrees)
				writeDouble(value.y.degrees)
			}
		}
	}

	override fun getEncoderClass(): Class<Point> =
		Point::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Point {
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
) : Codec<LineString> {

	override fun encode(writer: BsonWriter, value: LineString, encoderContext: EncoderContext) {
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

	override fun getEncoderClass(): Class<LineString> =
		LineString::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): LineString {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "LineString") { "Expected a GeoJSON LineString, but found: $type\n\tData: $doc" }
		require(coordinates != null && coordinates.size >= 2) { "A GeoJSON LineString should have at least two coordinate pairs, but found: $coordinates\n\tData: $doc" }

		val points = coordinates
			.asIterable()
			.map { it.decodeArray().decodeAsPoint() }

		return LineString(points)
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoMultiPointCodec(
	private val factory: BsonFactory,
) : Codec<MultiPoint> {

	override fun encode(writer: BsonWriter, value: MultiPoint, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "MultiPoint")
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

	override fun getEncoderClass(): Class<MultiPoint> =
		MultiPoint::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): MultiPoint {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "MultiPoint") { "Expected a GeoJSON MultiPoint, but found: $type\n\tData: $doc" }
		requireNotNull(coordinates) { "Missing coordinates field in GeoJSON MultiPoint\n\tData: $doc" }

		val points = coordinates
			.asIterable()
			.map { it.decodeArray().decodeAsPoint() }

		return MultiPoint(points)
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoPolygonCodec(
	private val factory: BsonFactory,
) : Codec<Polygon> {

	override fun encode(writer: BsonWriter, value: Polygon, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "Polygon")
			writeArray("coordinates") {
				for (ring in value.rings) {
					writeArray {
						for (point in ring.points) {
							writeArray {
								writeDouble(point.x.degrees)
								writeDouble(point.y.degrees)
							}
						}
					}
				}
			}
		}
	}

	override fun getEncoderClass(): Class<Polygon> =
		Polygon::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Polygon {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "Polygon") { "Expected a GeoJSON Polygon, but found: $type\n\tData: $doc" }
		require(coordinates != null && coordinates.size >= 1) { "A GeoJSON Polygon should have at least one ring, but found: $coordinates\n\tData: $doc" }

		val rings = coordinates
			.asIterable()
			.map { ringArray ->
				val pointsArray = ringArray.decodeArray()
				val points = pointsArray.asIterable().map { it.decodeArray().decodeAsPoint() }
				LineString(points)
			}

		return Polygon(rings)
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoMultiLineStringCodec(
	private val factory: BsonFactory,
) : Codec<MultiLineString> {

	override fun encode(writer: BsonWriter, value: MultiLineString, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "MultiLineString")
			writeArray("coordinates") {
				for (lineString in value.lineStrings) {
					writeArray {
						for (point in lineString.points) {
							writeArray {
								writeDouble(point.x.degrees)
								writeDouble(point.y.degrees)
							}
						}
					}
				}
			}
		}
	}

	override fun getEncoderClass(): Class<MultiLineString> =
		MultiLineString::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): MultiLineString {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "MultiLineString") { "Expected a GeoJSON MultiLineString, but found: $type\n\tData: $doc" }
		requireNotNull(coordinates) { "Missing coordinates field in GeoJSON MultiLineString\n\tData: $doc" }

		val lineStrings = coordinates
			.asIterable()
			.map { lineStringArray ->
				val pointsArray = lineStringArray.decodeArray()
				val points = pointsArray.asIterable().map { it.decodeArray().decodeAsPoint() }
				LineString(points)
			}

		return MultiLineString(lineStrings)
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoMultiPolygonCodec(
	private val factory: BsonFactory,
) : Codec<MultiPolygon> {

	override fun encode(writer: BsonWriter, value: MultiPolygon, encoderContext: EncoderContext) {
		factory.writeDocumentTo(writer) {
			writeString("type", "MultiPolygon")
			writeArray("coordinates") {
				for (polygon in value.polygons) {
					writeArray {
						for (ring in polygon.rings) {
							writeArray {
								for (point in ring.points) {
									writeArray {
										writeDouble(point.x.degrees)
										writeDouble(point.y.degrees)
									}
								}
							}
						}
					}
				}
			}
		}
	}

	override fun getEncoderClass(): Class<MultiPolygon> =
		MultiPolygon::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): MultiPolygon {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val coordinates = doc["coordinates"]?.decodeArray()

		require(type == "MultiPolygon") { "Expected a GeoJSON MultiPolygon, but found: $type\n\tData: $doc" }
		requireNotNull(coordinates) { "Missing coordinates field in GeoJSON MultiPolygon\n\tData: $doc" }

		val polygons = coordinates
			.asIterable()
			.map { polygonArray ->
				val ringsArray = polygonArray.decodeArray()
				val rings = ringsArray.asIterable().map { ringArray ->
					val pointsArray = ringArray.decodeArray()
					val points = pointsArray.asIterable().map { it.decodeArray().decodeAsPoint() }
					LineString(points)
				}
				Polygon(rings)
			}

		return MultiPolygon(polygons)
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoGeometryCollectionCodec(
	private val factory: BsonFactory,
) : Codec<GeometryCollection> {

	override fun encode(writer: BsonWriter, value: GeometryCollection, encoderContext: EncoderContext) {
		writer.writeStartDocument()
		writer.writeString("type", "GeometryCollection")
		writer.writeStartArray("geometries")
		for (geometry in value.geometries) {
			KotlinGeoCodec(factory).encode(writer, geometry, encoderContext)
		}
		writer.writeEndArray()
		writer.writeEndDocument()
	}

	override fun getEncoderClass(): Class<GeometryCollection> =
		GeometryCollection::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): GeometryCollection {
		val doc = factory.readDocument(reader, decoderContext)

		val type = doc["type"]?.decodeString()
		val geometries = doc["geometries"]?.decodeArray()

		require(type == "GeometryCollection") { "Expected a GeoJSON GeometryCollection, but found: $type\n\tData: $doc" }
		requireNotNull(geometries) { "Missing geometries field in GeoJSON GeometryCollection\n\tData: $doc" }

		return GeometryCollection(geometries.asIterable().map { it.decode<Geo>() })
	}
}

@OptIn(LowLevelApi::class)
internal class KotlinGeoCodec(
	private val factory: BsonFactory,
) : Codec<Geo> {
	override fun encode(writer: BsonWriter, value: Geo, encoderContext: EncoderContext) {
		when (value) {
			is Point -> KotlinGeoPointCodec(factory).encode(writer, value, encoderContext)
			is MultiPoint -> KotlinGeoMultiPointCodec(factory).encode(writer, value, encoderContext)
			is LineString -> KotlinGeoLineStringCodec(factory).encode(writer, value, encoderContext)
			is MultiLineString -> KotlinGeoMultiLineStringCodec(factory).encode(writer, value, encoderContext)
			is Polygon -> KotlinGeoPolygonCodec(factory).encode(writer, value, encoderContext)
			is MultiPolygon -> KotlinGeoMultiPolygonCodec(factory).encode(writer, value, encoderContext)
			is GeometryCollection -> KotlinGeoGeometryCollectionCodec(factory).encode(writer, value, encoderContext)
		}
	}

	override fun getEncoderClass(): Class<Geo> =
		Geo::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Geo {
		val document = factory.readDocument(reader, decoderContext)

		return when (val type = document["type"]?.decodeString()) {
			null -> throw IllegalArgumentException("Cannot deserialize Geo: missing 'type' field\n\tData: $document")
			"Point" -> document.decode<Point>()
			"LineString" -> document.decode<LineString>()
			"Polygon" -> document.decode<Polygon>()
			"MultiPoint" -> document.decode<MultiPoint>()
			"MultiLineString" -> document.decode<MultiLineString>()
			"MultiPolygon" -> document.decode<MultiPolygon>()
			"GeometryCollection" -> document.decode<GeometryCollection>()
			else -> throw IllegalArgumentException("Cannot deserialize Geo: unknown type '$type'\n\tData: $document")
		}
	}
}
