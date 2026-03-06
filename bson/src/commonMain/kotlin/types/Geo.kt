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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
}
