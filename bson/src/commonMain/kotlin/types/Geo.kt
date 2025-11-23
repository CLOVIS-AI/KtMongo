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
}
