/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.decode
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.jvm.JvmInline

@RequiresOptIn("This API is part of the experimental GeoJSON implementation. Please provide feedback in https://gitlab.com/opensavvy/ktmongo/-/work_items/76")
annotation class ExperimentalGeoBsonApi

/**
 * GeoJSON types supported by MongoDB.
 *
 * Our goal is to represent the GeoJSON RFC, **as it is implemented by MongoDB**.
 * MongoDB does not support large sections of the RFC, so we won't support them either.
 *
 * GeoJSON operators calculate on a sphere, using the [WGS84 reference system](http://spatialreference.org/ref/epsg/4326/).
 *
 * ### External resources
 *
 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson)
 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946)
 */
@OptIn(LowLevelApi::class)
@ExperimentalGeoBsonApi
@Serializable(with = Geo.Serializer::class)
sealed class Geo {

	/**
	 * A longitude.
	 *
	 * The longitude is measured in degrees, between -180° and 180°, both inclusive.
	 *
	 * Positive values are east of the prime meridian, negative values are west.
	 *
	 * This type is a helper to avoid confusing [Longitude] and [Latitude], it isn't itself a proper GeoJSON
	 * type. For this reason, it isn't serializable by itself (but types that contain it, such as [Point], are).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * val bordeaux = Geo.Point(
	 *     Geo.Longitude(-0.5811),
	 *     Geo.Latitude(44.8416),
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#overview)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1)
	 */
	@JvmInline
	@ExperimentalGeoBsonApi
	value class Longitude(
		val degrees: Double,
	) {

		init {
			require(degrees in -180.0..180.0) { "Valid longitude values are between -180° and 180°, both inclusive, but found: $degrees" }
		}

		override fun toString() = "Longitude($degrees°)"
	}

	/**
	 * A latitude.
	 *
	 * The latitude is measured in degrees, between -90° and 90°, both inclusive.
	 *
	 * Positive values are north of the equator, negative values are south.
	 *
	 * This type is a helper to avoid confusing [Longitude] and [Latitude], it isn't itself a proper GeoJSON
	 * type. For this reason, it isn't serializable by itself (but types that contain it, such as [Point], are).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * val bordeaux = Geo.Point(
	 *     Geo.Longitude(-0.5811),
	 *     Geo.Latitude(44.8416),
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#overview)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1)
	 */
	@JvmInline
	@ExperimentalGeoBsonApi
	value class Latitude(
		val degrees: Double,
	) {

		init {
			require(degrees in -90.0..90.0) { "Valid latitude values are between -90° and 90°, both inclusive, but found: $degrees" }
		}

		override fun toString() = "Latitude($degrees°)"
	}

	/**
	 * A GeoJSON point.
	 *
	 * A point is a basic 2d coordinate.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * val point = Geo.Point(Geo.Longitude(0.2), Geo.Latitude(5.3))
	 * ```
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#point)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.2)
	 */
	@Serializable(with = Point.Serializer::class)
	@ExperimentalGeoBsonApi
	data class Point(
		val x: Longitude,
		val y: Latitude,
	) : Geo() {

		override fun toString() = "Point(${x.degrees}° E, ${y.degrees}° N)"

		@Serializable
		private data class Surrogate(
			val type: String,
			val coordinates: List<Double>,
		)

		@LowLevelApi
		object Serializer : KSerializer<Point> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: Point) {
				surrogateSerializer.serialize(encoder, Surrogate("Point", listOf(value.x.degrees, value.y.degrees)))
			}

			override fun deserialize(decoder: Decoder): Point {
				val surrogate = surrogateSerializer.deserialize(decoder)
				require(surrogate.coordinates.size == 2) {
					"Point coordinates must have exactly 2 elements, got ${surrogate.coordinates.size}"
				}
				return Point(Longitude(surrogate.coordinates[0]), Latitude(surrogate.coordinates[1]))
			}
		}
	}

	/**
	 * A GeoJSON line string.
	 *
	 * The simplest line string is just a line: joining two points together.
	 *
	 * ```kotlin
	 * Geo.LineString(
	 *     Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
	 *     Geo.Point(Geo.Longitude(5.0), Geo.Latitude(0.2)),
	 * )
	 * ```
	 *
	 * A more complex line string represents a path between multiple points, drawing a straight line
	 * between each two points.
	 *
	 * ```kotlin
	 * Geo.LineString(
	 *     Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
	 *     Geo.Point(Geo.Longitude(5.0), Geo.Latitude(0.2)),
	 *     Geo.Point(Geo.Longitude(6.0), Geo.Latitude(0.3)),
	 *     Geo.Point(Geo.Longitude(6.1), Geo.Latitude(0.9)),
	 * )
	 * ```
	 *
	 * A line string can represent the outline of a polygon with any number of points.
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#linestring)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.4)
	 */
	@Serializable(with = LineString.Serializer::class)
	@ExperimentalGeoBsonApi
	data class LineString(
		/**
		 * The points that make up the line string.
		 *
		 * At least 2 points must be present.
		 */
		val points: List<Point>,
	) : Geo() {
		init {
			require(points.size >= 2) { "LineString must have at least 2 points, got ${points.size}" }
		}

		/**
		 * Constructs a [LineString] instance from multiple [Point] instances.
		 */
		constructor(vararg points: Point) : this(points.asList())

		/**
		 * Returns `true` if this line string is closed, i.e. the first and last points are the same.
		 */
		val isClosed: Boolean
			get() = points.first() == points.last()

		override fun toString() = "LineString(${points.joinToString(", ")})"

		@Serializable
		private data class Surrogate(
			val type: String,
			val coordinates: List<List<Double>>,
		)

		@LowLevelApi
		object Serializer : KSerializer<LineString> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: LineString) {
				val coordinates = value.points.map { listOf(it.x.degrees, it.y.degrees) }
				surrogateSerializer.serialize(encoder, Surrogate("LineString", coordinates))
			}

			override fun deserialize(decoder: Decoder): LineString {
				val surrogate = surrogateSerializer.deserialize(decoder)
				require(surrogate.coordinates.size >= 2) {
					"LineString must have at least 2 coordinates, got ${surrogate.coordinates.size}"
				}
				val points = surrogate.coordinates.map { coords ->
					require(coords.size == 2) {
						"Each LineString coordinate must have exactly 2 elements, got ${coords.size}"
					}
					Point(Longitude(coords[0]), Latitude(coords[1]))
				}
				return LineString(points)
			}
		}
	}

	/**
	 * A GeoJSON polygon.
	 *
	 * A polygon is a shape that may have holes.
	 *
	 * A simple polygon has a single ring (e.g., a triangle, a square).
	 * More complex polygons can have multiple rings that represent holes (e.g., a doughnut shape).
	 *
	 * To learn more about the different kinds of polygons, see the constructors.
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#polygon)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.6)
	 */
	@Serializable(with = Polygon.Serializer::class)
	@ExperimentalGeoBsonApi
	data class Polygon(
		/**
		 * The various rings of this polygon.
		 *
		 * A polygon must have at least one ring.
		 *
		 * Each ring must have at least 4 points, be [closed][LineString.isClosed], and must not self-intersect.
		 * For the complete rules on rings, see the constructor that takes a vararg of [LineString].
		 */
		val rings: List<LineString>,
	) : Geo() {

		/**
		 * Constructs a [Polygon] instance from multiple [LineString] instances.
		 *
		 * Each [LineString] instance represents a ring.
		 * - Each ring must have at least 4 points, be [closed][LineString.isClosed], and must not self-intersect.
		 * - The first described ring must be the exterior ring.
		 * - The exterior ring cannot self-intersect.
		 * - Any interior ring must be entirely contained by the outer ring.
		 * - Interior rings cannot intersect or overlap each other. Interior rings cannot share an edge.
		 *
		 * Interior rings represent holes in the external ring.
		 *
		 * ### Example
		 *
		 * A square within another square. Note that the exterior square is described first.
		 * ```kotlin
		 * Geo.Polygon(
		 *     Geo.LineString(
		 *         Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
		 *         Geo.Point(Geo.Longitude(10.0), Geo.Latitude(0.0)),
		 *         Geo.Point(Geo.Longitude(10.0), Geo.Latitude(10.0)),
		 *         Geo.Point(Geo.Longitude(0.0), Geo.Latitude(10.0)),
		 *         Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
		 *     ),
		 *     Geo.LineString(
		 *         Geo.Point(Geo.Longitude(2.0), Geo.Latitude(2.0)),
		 *         Geo.Point(Geo.Longitude(8.0), Geo.Latitude(2.0)),
		 *         Geo.Point(Geo.Longitude(8.0), Geo.Latitude(8.0)),
		 *         Geo.Point(Geo.Longitude(2.0), Geo.Latitude(8.0)),
		 *         Geo.Point(Geo.Longitude(2.0), Geo.Latitude(2.0)),
		 *     ),
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#polygons-with-multiple-rings)
		 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.6)
		 */
		constructor(vararg rings: LineString) : this(rings.asList())

		/**
		 * Constructs a single-ring [Geo.Polygon] instance from multiple [Point] instances.
		 *
		 * The polygon must have at least 4 points, be [closed][LineString.isClosed], and must not self-intersect.
		 *
		 * ### Example
		 *
		 * A square:
		 * ```kotlin
		 * Geo.Polygon(
		 *     Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
		 *     Geo.Point(Geo.Longitude(1.0), Geo.Latitude(0.0)),
		 *     Geo.Point(Geo.Longitude(1.0), Geo.Latitude(1.0)),
		 *     Geo.Point(Geo.Longitude(0.0), Geo.Latitude(1.0)),
		 *     Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#polygons-with-a-single-ring)
		 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.6)
		 */
		constructor(vararg points: Point) : this(listOf(LineString(points.asList())))

		init {
			require(rings.isNotEmpty()) { "A polygon must have at least one ring" }
			for (ring in rings) {
				require(ring.isClosed) { "All rings in a polygon must be closed (start and end at the same point), found: $ring" }
				require(ring.points.size >= 4) { "All rings in a polygon must have at least 4 points, got ${ring.points.size}: $ring" }
			}
		}

		override fun toString() = "Polygon(${rings.joinToString(", ")})"

		@Serializable
		private data class Surrogate(
			val type: String,
			val coordinates: List<List<List<Double>>>,
		)

		@LowLevelApi
		object Serializer : KSerializer<Polygon> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: Polygon) {
				val coordinates = value.rings.map { ring ->
					ring.points.map { point ->
						listOf(point.x.degrees, point.y.degrees)
					}
				}
				surrogateSerializer.serialize(encoder, Surrogate("Polygon", coordinates))
			}

			override fun deserialize(decoder: Decoder): Polygon {
				val surrogate = surrogateSerializer.deserialize(decoder)
				require(surrogate.coordinates.isNotEmpty()) { "Polygon must have at least 1 ring, got ${surrogate.coordinates.size}" }
				val rings = surrogate.coordinates.map { ringCoords ->
					require(ringCoords.size >= 4) { "Each Polygon ring must have at least 4 coordinates, got ${ringCoords.size}" }
					val points = ringCoords.map { coords ->
						require(coords.size == 2) { "Each Polygon coordinate must have exactly 2 elements, got ${coords.size}" }
						Point(Longitude(coords[0]), Latitude(coords[1]))
					}
					LineString(points)
				}
				return Polygon(rings)
			}
		}
	}

	/**
	 * A GeoJSON MultiPoint.
	 *
	 * A MultiPoint is a list of multiple [Point] instances that are grouped together.
	 *
	 * Unlike [LineString], which implies that the points are connected in order,
	 * the points in [Geo.MultiPoint] are in no particular order and are not meant to be connected.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * Geo.MultiPoint(
	 *     Geo.Point(Geo.Longitude(-73.9580), Geo.Latitude(40.8003)),
	 *     Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
	 *     Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
	 *     Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#multipoint)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.3)
	 */
	@Serializable(with = MultiPoint.Serializer::class)
	@ExperimentalGeoBsonApi
	data class MultiPoint(
		/**
		 * The points that make up this [MultiPoint].
		 */
		val points: List<Point>,
	) : Geo() {

		/**
		 * Constructs a [MultiPoint] instance from multiple [Point] instances.
		 */
		constructor(vararg points: Point) : this(points.asList())

		override fun toString() = "MultiPoint(${points.joinToString(", ")})"

		@Serializable
		private data class Surrogate(
			val type: String,
			val coordinates: List<List<Double>>,
		)

		@LowLevelApi
		object Serializer : KSerializer<MultiPoint> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: MultiPoint) {
				val coordinates = value.points.map { listOf(it.x.degrees, it.y.degrees) }
				surrogateSerializer.serialize(encoder, Surrogate("MultiPoint", coordinates))
			}

			override fun deserialize(decoder: Decoder): MultiPoint {
				val surrogate = surrogateSerializer.deserialize(decoder)
				val points = surrogate.coordinates.map { coords ->
					require(coords.size == 2) { "Each MultiPoint coordinate must have exactly 2 elements, got ${coords.size}" }
					Point(Longitude(coords[0]), Latitude(coords[1]))
				}
				return MultiPoint(points)
			}
		}
	}

	/**
	 * A GeoJSON MultiLineString.
	 *
	 * A MultiLineString is a list of multiple [LineString] instances that are grouped together.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * Geo.MultiLineString(
	 *     Geo.LineString(
	 *         Geo.Point(Geo.Longitude(-73.96943), Geo.Latitude(40.78519)),
	 *         Geo.Point(Geo.Longitude(-73.96082), Geo.Latitude(40.78095)),
	 *     ),
	 *     Geo.LineString(
	 *         Geo.Point(Geo.Longitude(-73.96415), Geo.Latitude(40.79229)),
	 *         Geo.Point(Geo.Longitude(-73.95544), Geo.Latitude(40.78854)),
	 *     ),
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#multilinestring)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.5)
	 */
	@Serializable(with = MultiLineString.Serializer::class)
	@ExperimentalGeoBsonApi
	data class MultiLineString(
		/**
		 * The line strings that make up this [MultiLineString].
		 */
		val lineStrings: List<LineString>,
	) : Geo() {

		/**
		 * Constructs a [MultiLineString] instance from multiple [LineString] instances.
		 */
		constructor(vararg lineStrings: LineString) : this(lineStrings.asList())

		override fun toString() = "MultiLineString(${lineStrings.joinToString(", ")})"

		@Serializable
		private data class Surrogate(
			val type: String,
			val coordinates: List<List<List<Double>>>,
		)

		@LowLevelApi
		object Serializer : KSerializer<MultiLineString> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: MultiLineString) {
				val coordinates = value.lineStrings.map { lineString ->
					lineString.points.map { listOf(it.x.degrees, it.y.degrees) }
				}
				surrogateSerializer.serialize(encoder, Surrogate("MultiLineString", coordinates))
			}

			override fun deserialize(decoder: Decoder): MultiLineString {
				val surrogate = surrogateSerializer.deserialize(decoder)
				val lineStrings = surrogate.coordinates.map { line ->
					require(line.size >= 2) { "Each MultiLineString line must have at least 2 coordinates, got ${line.size}" }
					val points = line.map { coords ->
						require(coords.size == 2) { "Each MultiLineString coordinate must have exactly 2 elements, got ${coords.size}" }
						Point(Longitude(coords[0]), Latitude(coords[1]))
					}
					LineString(points)
				}
				return MultiLineString(lineStrings)
			}
		}
	}

	/**
	 * A GeoJSON MultiPolygon.
	 *
	 * A MultiPolygon is a list of multiple [Polygon] instances that are grouped together.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * Geo.MultiPolygon(
	 *     Geo.Polygon(
	 *         Geo.LineString(
	 *             Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
	 *             Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
	 *             Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
	 *             Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
	 *             Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
	 *         ),
	 *     ),
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [MongoDB documentation](https://www.mongodb.com/docs/manual/reference/geojson/#multipolygon)
	 * - [GeoJSON RFC](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.7)
	 */
	@Serializable(with = MultiPolygon.Serializer::class)
	@ExperimentalGeoBsonApi
	data class MultiPolygon(
		/**
		 * The polygons that make up this [MultiPolygon].
		 */
		val polygons: List<Polygon>,
	) : Geo() {

		/**
		 * Constructs a [MultiPolygon] instance from multiple [Polygon] instances.
		 */
		constructor(vararg polygons: Polygon) : this(polygons.asList())

		override fun toString() = "MultiPolygon(${polygons.joinToString(", ")})"

		@Serializable
		private data class Surrogate(
			val type: String,
			val coordinates: List<List<List<List<Double>>>>,
		)

		@LowLevelApi
		object Serializer : KSerializer<MultiPolygon> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: MultiPolygon) {
				val coordinates = value.polygons.map { polygon ->
					polygon.rings.map { ring ->
						ring.points.map { listOf(it.x.degrees, it.y.degrees) }
					}
				}
				surrogateSerializer.serialize(encoder, Surrogate("MultiPolygon", coordinates))
			}

			override fun deserialize(decoder: Decoder): MultiPolygon {
				val surrogate = surrogateSerializer.deserialize(decoder)
				val polygons = surrogate.coordinates.map { polygonCoords ->
					require(polygonCoords.isNotEmpty()) { "Each MultiPolygon polygon must have at least 1 ring, got ${polygonCoords.size}" }
					val rings = polygonCoords.map { ringCoords ->
						require(ringCoords.size >= 4) { "Each MultiPolygon ring must have at least 4 coordinates, got ${ringCoords.size}" }
						val points = ringCoords.map { coords ->
							require(coords.size == 2) { "Each MultiPolygon coordinate must have exactly 2 elements, got ${coords.size}" }
							Point(Longitude(coords[0]), Latitude(coords[1]))
						}
						LineString(points)
					}
					Polygon(rings)
				}
				return MultiPolygon(polygons)
			}
		}
	}

	/**
	 * A GeoJSON geometry collection.
	 *
	 * A GeometryCollection is a list of [Geo] objects.
	 *
	 * It is not recommended to include a [GeometryCollection] inside another.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * Geo.GeometryCollection(
	 *     Geo.MultiPoint(
	 *         Geo.Point(Longitude(-73.9580), Latitude(40.8003)),
	 *         Geo.Point(Longitude(-73.9498), Latitude(40.7968)),
	 *         Geo.Point(Longitude(-73.9737), Latitude(40.7648)),
	 *         Geo.Point(Longitude(-73.9814), Latitude(40.7681)),
	 *     ),
	 *     Geo.MultiLineString(
	 *         Geo.LineString(
	 *             Geo.Point(Longitude(-73.96943), Latitude(40.78519)),
	 *             Geo.Point(Longitude(-73.96082), Latitude(40.78095)),
	 *         ),
	 *         Geo.LineString(
	 *             Geo.Point(Longitude(-73.96415), Latitude(40.79229)),
	 *             Geo.Point(Longitude(-73.95544), Latitude(40.78854)),
	 *         ),
	 *         Geo.LineString(
	 *             Geo.Point(Longitude(-73.97162), Latitude(40.78205)),
	 *             Geo.Point(Longitude(-73.96374), Latitude(40.77715)),
	 *         ),
	 *         Geo.LineString(
	 *             Geo.Point(Longitude(-73.97880), Latitude(40.77247)),
	 *             Geo.Point(Longitude(-73.97036), Latitude(40.76811)),
	 *         ),
	 *     )
	 * )
	 * ```
	 */
	@Serializable(with = GeometryCollection.Serializer::class)
	@ExperimentalGeoBsonApi
	data class GeometryCollection(
		val geometries: List<Geo>,
	) : Geo() {

		constructor(vararg geometries: Geo) : this(geometries.asList())

		override fun toString(): String = "GeometryCollection(${geometries.joinToString(", ")})"

		@Serializable
		private data class Surrogate(
			val type: String,
			val geometries: List<Geo>,
		)

		@LowLevelApi
		object Serializer : KSerializer<GeometryCollection> {
			private val surrogateSerializer = Surrogate.serializer()
			override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

			override fun serialize(encoder: Encoder, value: GeometryCollection) {
				surrogateSerializer.serialize(encoder, Surrogate("GeometryCollection", value.geometries))
			}

			override fun deserialize(decoder: Decoder): GeometryCollection {
				val surrogate = surrogateSerializer.deserialize(decoder)
				require(surrogate.type == "GeometryCollection") { "Invalid geometry type: ${surrogate.type}" }
				return GeometryCollection(surrogate.geometries)
			}
		}
	}

	@LowLevelApi
	object Serializer : KSerializer<Geo> {
		@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
		override val descriptor: SerialDescriptor = buildSerialDescriptor("opensavvy.ktmongo.bson.types.Geo", PolymorphicKind.SEALED) {
			element("Point", Point.serializer().descriptor)
			element("LineString", LineString.serializer().descriptor)
			element("Polygon", Polygon.serializer().descriptor)
			element("MultiPoint", MultiPoint.serializer().descriptor)
			element("MultiLineString", MultiLineString.serializer().descriptor)
			element("MultiPolygon", MultiPolygon.serializer().descriptor)
		}

		override fun serialize(encoder: Encoder, value: Geo) {
			when (value) {
				is Point -> encoder.encodeSerializableValue(Point.serializer(), value)
				is LineString -> encoder.encodeSerializableValue(LineString.serializer(), value)
				is Polygon -> encoder.encodeSerializableValue(Polygon.serializer(), value)
				is MultiPoint -> encoder.encodeSerializableValue(MultiPoint.serializer(), value)
				is MultiLineString -> encoder.encodeSerializableValue(MultiLineString.serializer(), value)
				is MultiPolygon -> encoder.encodeSerializableValue(MultiPolygon.serializer(), value)
				is GeometryCollection -> encoder.encodeSerializableValue(GeometryCollection.serializer(), value)
			}
		}

		override fun deserialize(decoder: Decoder): Geo {
			val document = BsonDocument.serializer().deserialize(decoder)

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

}
