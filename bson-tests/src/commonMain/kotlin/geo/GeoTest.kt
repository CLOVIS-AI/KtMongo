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

@file:OptIn(ExperimentalGeoBsonApi::class, LowLevelApi::class)

package opensavvy.ktmongo.bson.geo

import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.decode
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.serialize
import opensavvy.ktmongo.bson.types.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.bson.types.ExperimentalGeoBsonApi
import opensavvy.ktmongo.bson.types.Geo
import opensavvy.ktmongo.bson.types.testBson
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

fun SuiteDsl.validateGeo(factory: Prepared<BsonFactory>) = suite("Geo") {
	geoPoint(factory)
	geoLineString(factory)
	geoPolygon(factory)
}

private fun SuiteDsl.geoPoint(factory: Prepared<BsonFactory>) = suite("Point") {

	testBson(
		factory,
		"Serialize simple point",
		serialize(Geo.Point(Geo.Longitude(2.0), Geo.Latitude(3.5))),
		document {
			writeString("type", "Point")
			writeArray("coordinates") {
				writeDouble(2.0)
				writeDouble(3.5)
			}
		},
		json("""{"type": "Point", "coordinates": [2.0, 3.5]}"""),
		verify("The longitude is correct") {
			check(decode<Geo.Point>().x == Geo.Longitude(2.0))
		},
		verify("The latitude is correct") {
			check(decode<Geo.Point>().y == Geo.Latitude(3.5))
		},
	)

}

private fun SuiteDsl.geoLineString(factory: Prepared<BsonFactory>) = suite("LineString") {

	testBson(
		factory,
		"Serialize simple linestring",
		serialize(Geo.LineString(Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)), Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)))),
		document {
			writeString("type", "LineString")
			writeArray("coordinates") {
				writeArray {
					writeDouble(40.0)
					writeDouble(5.0)
				}
				writeArray {
					writeDouble(41.0)
					writeDouble(6.0)
				}
			}
		},
		json("""{"type": "LineString", "coordinates": [[40.0, 5.0], [41.0, 6.0]]}"""),
		verify("The coordinates are correct") {
			check(decode<Geo.LineString>().points[0] == Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)))
			check(decode<Geo.LineString>().points[1] == Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)))
		},
		verify("The string is not closed") {
			check(!decode<Geo.LineString>().isClosed)
		},
	)

	testBson(
		factory,
		"Serialize simple linestring with three points",
		serialize(
			Geo.LineString(
				Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)),
				Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(41.5), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)),
			)
		),
		document {
			writeString("type", "LineString")
			writeArray("coordinates") {
				writeArray {
					writeDouble(40.0)
					writeDouble(5.0)
				}
				writeArray {
					writeDouble(41.0)
					writeDouble(6.0)
				}
				writeArray {
					writeDouble(41.5)
					writeDouble(6.0)
				}
				writeArray {
					writeDouble(40.0)
					writeDouble(5.0)
				}
			}
		},
		json("""{"type": "LineString", "coordinates": [[40.0, 5.0], [41.0, 6.0], [41.5, 6.0], [40.0, 5.0]]}"""),
		verify("The coordinates are correct") {
			check(decode<Geo.LineString>().points[0] == Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)))
			check(decode<Geo.LineString>().points[1] == Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)))
			check(decode<Geo.LineString>().points[2] == Geo.Point(Geo.Longitude(41.5), Geo.Latitude(6.0)))
			check(decode<Geo.LineString>().points[3] == Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)))
		},
		verify("The string is closed") {
			check(decode<Geo.LineString>().isClosed)
		},
	)

}

private fun SuiteDsl.geoPolygon(factory: Prepared<BsonFactory>) = suite("Polygon") {

	testBson(
		factory,
		"Serialize simple polygon (triangle)",
		serialize(
			Geo.Polygon(
				Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
				Geo.Point(Geo.Longitude(3.0), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(6.0), Geo.Latitude(1.0)),
				Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
			)
		),
		document {
			writeString("type", "Polygon")
			writeArray("coordinates") {
				writeArray {
					writeArray {
						writeDouble(0.0)
						writeDouble(0.0)
					}
					writeArray {
						writeDouble(3.0)
						writeDouble(6.0)
					}
					writeArray {
						writeDouble(6.0)
						writeDouble(1.0)
					}
					writeArray {
						writeDouble(0.0)
						writeDouble(0.0)
					}
				}
			}
		},
		json("""{"type": "Polygon", "coordinates": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0], [0.0, 0.0]]]}"""),
		verify("The polygon has one ring") {
			check(decode<Geo.Polygon>().rings.size == 1)
		},
		verify("The ring is closed") {
			check(decode<Geo.Polygon>().rings[0].isClosed)
		},
		verify("The coordinates are correct") {
			val polygon = decode<Geo.Polygon>()
			check(polygon.rings[0].points[0] == Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)))
			check(polygon.rings[0].points[1] == Geo.Point(Geo.Longitude(3.0), Geo.Latitude(6.0)))
			check(polygon.rings[0].points[2] == Geo.Point(Geo.Longitude(6.0), Geo.Latitude(1.0)))
			check(polygon.rings[0].points[3] == Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)))
		},
	)

	testBson(
		factory,
		"Serialize polygon with hole (doughnut)",
		serialize(
			Geo.Polygon(
				Geo.LineString(
					Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
					Geo.Point(Geo.Longitude(10.0), Geo.Latitude(0.0)),
					Geo.Point(Geo.Longitude(10.0), Geo.Latitude(10.0)),
					Geo.Point(Geo.Longitude(0.0), Geo.Latitude(10.0)),
					Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(2.0), Geo.Latitude(2.0)),
					Geo.Point(Geo.Longitude(8.0), Geo.Latitude(2.0)),
					Geo.Point(Geo.Longitude(8.0), Geo.Latitude(8.0)),
					Geo.Point(Geo.Longitude(2.0), Geo.Latitude(8.0)),
					Geo.Point(Geo.Longitude(2.0), Geo.Latitude(2.0)),
				),
			)
		),
		document {
			writeString("type", "Polygon")
			writeArray("coordinates") {
				writeArray {
					writeArray {
						writeDouble(0.0)
						writeDouble(0.0)
					}
					writeArray {
						writeDouble(10.0)
						writeDouble(0.0)
					}
					writeArray {
						writeDouble(10.0)
						writeDouble(10.0)
					}
					writeArray {
						writeDouble(0.0)
						writeDouble(10.0)
					}
					writeArray {
						writeDouble(0.0)
						writeDouble(0.0)
					}
				}
				writeArray {
					writeArray {
						writeDouble(2.0)
						writeDouble(2.0)
					}
					writeArray {
						writeDouble(8.0)
						writeDouble(2.0)
					}
					writeArray {
						writeDouble(8.0)
						writeDouble(8.0)
					}
					writeArray {
						writeDouble(2.0)
						writeDouble(8.0)
					}
					writeArray {
						writeDouble(2.0)
						writeDouble(2.0)
					}
				}
			}
		},
		json("""{"type": "Polygon", "coordinates": [[[0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0]], [[2.0, 2.0], [8.0, 2.0], [8.0, 8.0], [2.0, 8.0], [2.0, 2.0]]]}"""),
		verify("The polygon has two rings") {
			check(decode<Geo.Polygon>().rings.size == 2)
		},
		verify("Both rings are closed") {
			check(decode<Geo.Polygon>().rings[0].isClosed)
			check(decode<Geo.Polygon>().rings[1].isClosed)
		},
		verify("The exterior ring coordinates are correct") {
			val polygon = decode<Geo.Polygon>()
			check(polygon.rings[0].points[0] == Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)))
			check(polygon.rings[0].points[1] == Geo.Point(Geo.Longitude(10.0), Geo.Latitude(0.0)))
			check(polygon.rings[0].points[2] == Geo.Point(Geo.Longitude(10.0), Geo.Latitude(10.0)))
			check(polygon.rings[0].points[3] == Geo.Point(Geo.Longitude(0.0), Geo.Latitude(10.0)))
			check(polygon.rings[0].points[4] == Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)))
		},
		verify("The interior ring coordinates are correct") {
			val polygon = decode<Geo.Polygon>()
			check(polygon.rings[1].points[0] == Geo.Point(Geo.Longitude(2.0), Geo.Latitude(2.0)))
			check(polygon.rings[1].points[1] == Geo.Point(Geo.Longitude(8.0), Geo.Latitude(2.0)))
			check(polygon.rings[1].points[2] == Geo.Point(Geo.Longitude(8.0), Geo.Latitude(8.0)))
			check(polygon.rings[1].points[3] == Geo.Point(Geo.Longitude(2.0), Geo.Latitude(8.0)))
			check(polygon.rings[1].points[4] == Geo.Point(Geo.Longitude(2.0), Geo.Latitude(2.0)))
		},
	)

}
