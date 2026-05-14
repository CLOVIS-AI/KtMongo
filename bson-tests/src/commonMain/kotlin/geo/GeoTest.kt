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

@file:OptIn(ExperimentalGeoBsonApi::class, LowLevelApi::class, ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.bson.geo

import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.ExperimentalBsonPathApi
import opensavvy.ktmongo.bson.decode
import opensavvy.ktmongo.bson.select
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
	geoMultiPoint(factory)
	geoMultiLineString(factory)
	geoMultiPolygon(factory)
	geoGeometryCollection(factory)
}

private fun SuiteDsl.geoPoint(factory: Prepared<BsonFactory>) = suite("Point") {

	testBson(
		factory,
		"Serialize simple point",
		serialize(Geo.Point(Geo.Longitude(2.0), Geo.Latitude(3.5))),
		serialize(Geo.Point(Geo.Longitude(2.0), Geo.Latitude(3.5)) as Geo),
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
		verify("The longitude is correct (polymorphic)") {
			check(decode<Geo>() == Geo.Point(Geo.Longitude(2.0), Geo.Latitude(3.5)))
		}
	)

}

private fun SuiteDsl.geoLineString(factory: Prepared<BsonFactory>) = suite("LineString") {

	testBson(
		factory,
		"Serialize simple linestring",
		serialize(Geo.LineString(Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)), Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)))),
		serialize(Geo.LineString(Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)), Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0))) as Geo),
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
		verify("The coordinates are correct (polymorphic)") {
			val expected = Geo.LineString(Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)), Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)))
			check(decode<Geo>() == expected)
		}
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
		serialize(
			Geo.LineString(
				Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)),
				Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(41.5), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)),
			) as Geo
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
		verify("The coordinates are correct (polymorphic)") {
			val expected = Geo.LineString(
				Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)),
				Geo.Point(Geo.Longitude(41.0), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(41.5), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(40.0), Geo.Latitude(5.0)),
			)
			check(decode<Geo>() == expected)
		}
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
		serialize(
			Geo.Polygon(
				Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
				Geo.Point(Geo.Longitude(3.0), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(6.0), Geo.Latitude(1.0)),
				Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
			) as Geo
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
		verify("The coordinates are correct (polymorphic)") {
			val expected = Geo.Polygon(
				Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
				Geo.Point(Geo.Longitude(3.0), Geo.Latitude(6.0)),
				Geo.Point(Geo.Longitude(6.0), Geo.Latitude(1.0)),
				Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
			)
			check(decode<Geo>() == expected)
		}
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

private fun SuiteDsl.geoMultiPoint(factory: Prepared<BsonFactory>) = suite("MultiPoint") {

	testBson(
		factory,
		"Serialize multipoint",
		serialize(
			Geo.MultiPoint(
				Geo.Point(Geo.Longitude(-73.9580), Geo.Latitude(40.8003)),
				Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
				Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
				Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
			)
		),
		serialize(
			Geo.MultiPoint(
				Geo.Point(Geo.Longitude(-73.9580), Geo.Latitude(40.8003)),
				Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
				Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
				Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
			) as Geo
		),
		document {
			writeString("type", "MultiPoint")
			writeArray("coordinates") {
				writeArray {
					writeDouble(-73.9580)
					writeDouble(40.8003)
				}
				writeArray {
					writeDouble(-73.9498)
					writeDouble(40.7968)
				}
				writeArray {
					writeDouble(-73.9737)
					writeDouble(40.7648)
				}
				writeArray {
					writeDouble(-73.9814)
					writeDouble(40.7681)
				}
			}
		},
		json("""{"type": "MultiPoint", "coordinates": [[-73.958, 40.8003], [-73.9498, 40.7968], [-73.9737, 40.7648], [-73.9814, 40.7681]]}"""),
		verify("The number of points is correct") {
			check(decode<Geo.MultiPoint>().points.size == 4)
		},
		verify("The coordinates are correct") {
			val multiPoint = decode<Geo.MultiPoint>()
			check(multiPoint.points[0] == Geo.Point(Geo.Longitude(-73.9580), Geo.Latitude(40.8003)))
			check(multiPoint.points[1] == Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)))
			check(multiPoint.points[2] == Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)))
			check(multiPoint.points[3] == Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)))
		},
		verify("The coordinates are correct (polymorphic)") {
			val expected = Geo.MultiPoint(
				Geo.Point(Geo.Longitude(-73.9580), Geo.Latitude(40.8003)),
				Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
				Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
				Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
			)
			check(decode<Geo>() == expected)
		}
	)
}

private fun SuiteDsl.geoMultiLineString(factory: Prepared<BsonFactory>) = suite("MultiLineString") {

	testBson(
		factory,
		"Serialize multilinestring",
		serialize(
			Geo.MultiLineString(
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.96943), Geo.Latitude(40.78519)),
					Geo.Point(Geo.Longitude(-73.96082), Geo.Latitude(40.78095)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.96415), Geo.Latitude(40.79229)),
					Geo.Point(Geo.Longitude(-73.95544), Geo.Latitude(40.78854)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.97162), Geo.Latitude(40.78205)),
					Geo.Point(Geo.Longitude(-73.96374), Geo.Latitude(40.77715)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.97880), Geo.Latitude(40.77247)),
					Geo.Point(Geo.Longitude(-73.97036), Geo.Latitude(40.76811)),
				),
			)
		),
		serialize(
			Geo.MultiLineString(
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.96943), Geo.Latitude(40.78519)),
					Geo.Point(Geo.Longitude(-73.96082), Geo.Latitude(40.78095)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.96415), Geo.Latitude(40.79229)),
					Geo.Point(Geo.Longitude(-73.95544), Geo.Latitude(40.78854)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.97162), Geo.Latitude(40.78205)),
					Geo.Point(Geo.Longitude(-73.96374), Geo.Latitude(40.77715)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.97880), Geo.Latitude(40.77247)),
					Geo.Point(Geo.Longitude(-73.97036), Geo.Latitude(40.76811)),
				),
			) as Geo
		),
		document {
			writeString("type", "MultiLineString")
			writeArray("coordinates") {
				writeArray {
					writeArray {
						writeDouble(-73.96943)
						writeDouble(40.78519)
					}
					writeArray {
						writeDouble(-73.96082)
						writeDouble(40.78095)
					}
				}
				writeArray {
					writeArray {
						writeDouble(-73.96415)
						writeDouble(40.79229)
					}
					writeArray {
						writeDouble(-73.95544)
						writeDouble(40.78854)
					}
				}
				writeArray {
					writeArray {
						writeDouble(-73.97162)
						writeDouble(40.78205)
					}
					writeArray {
						writeDouble(-73.96374)
						writeDouble(40.77715)
					}
				}
				writeArray {
					writeArray {
						writeDouble(-73.97880)
						writeDouble(40.77247)
					}
					writeArray {
						writeDouble(-73.97036)
						writeDouble(40.76811)
					}
				}
			}
		},
		json("""{"type": "MultiLineString", "coordinates": [[[-73.96943, 40.78519], [-73.96082, 40.78095]], [[-73.96415, 40.79229], [-73.95544, 40.78854]], [[-73.97162, 40.78205], [-73.96374, 40.77715]], [[-73.9788, 40.77247], [-73.97036, 40.76811]]]}"""),
		verify("The number of line strings is correct") {
			check(decode<Geo.MultiLineString>().lineStrings.size == 4)
		},
		verify("The coordinates are correct") {
			val multiLineString = decode<Geo.MultiLineString>()
			check(multiLineString.lineStrings[0].points[0] == Geo.Point(Geo.Longitude(-73.96943), Geo.Latitude(40.78519)))
			check(multiLineString.lineStrings[0].points[1] == Geo.Point(Geo.Longitude(-73.96082), Geo.Latitude(40.78095)))
			check(multiLineString.lineStrings[1].points[0] == Geo.Point(Geo.Longitude(-73.96415), Geo.Latitude(40.79229)))
			check(multiLineString.lineStrings[1].points[1] == Geo.Point(Geo.Longitude(-73.95544), Geo.Latitude(40.78854)))
			check(multiLineString.lineStrings[2].points[0] == Geo.Point(Geo.Longitude(-73.97162), Geo.Latitude(40.78205)))
			check(multiLineString.lineStrings[2].points[1] == Geo.Point(Geo.Longitude(-73.96374), Geo.Latitude(40.77715)))
			check(multiLineString.lineStrings[3].points[0] == Geo.Point(Geo.Longitude(-73.97880), Geo.Latitude(40.77247)))
			check(multiLineString.lineStrings[3].points[1] == Geo.Point(Geo.Longitude(-73.97036), Geo.Latitude(40.76811)))
		},
		verify("The coordinates are correct (polymorphic)") {
			val expected = Geo.MultiLineString(
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.96943), Geo.Latitude(40.78519)),
					Geo.Point(Geo.Longitude(-73.96082), Geo.Latitude(40.78095)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.96415), Geo.Latitude(40.79229)),
					Geo.Point(Geo.Longitude(-73.95544), Geo.Latitude(40.78854)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.97162), Geo.Latitude(40.78205)),
					Geo.Point(Geo.Longitude(-73.96374), Geo.Latitude(40.77715)),
				),
				Geo.LineString(
					Geo.Point(Geo.Longitude(-73.97880), Geo.Latitude(40.77247)),
					Geo.Point(Geo.Longitude(-73.97036), Geo.Latitude(40.76811)),
				),
			)
			check(decode<Geo>() == expected)
		}
	)
}

private fun SuiteDsl.geoMultiPolygon(factory: Prepared<BsonFactory>) = suite("MultiPolygon") {

	testBson(
		factory,
		"Serialize multipolygon",
		serialize(
			Geo.MultiPolygon(
				Geo.Polygon(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
						Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
						Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
						Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
					),
				),
				Geo.Polygon(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
						Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
						Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
					),
				),
			)
		),
		serialize(
			Geo.MultiPolygon(
				Geo.Polygon(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
						Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
						Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
						Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
					),
				),
				Geo.Polygon(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
						Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
						Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
					),
				),
			) as Geo
		),
		document {
			writeString("type", "MultiPolygon")
			writeArray("coordinates") {
				writeArray {
					writeArray {
						writeArray {
							writeDouble(-73.958)
							writeDouble(40.8003)
						}
						writeArray {
							writeDouble(-73.9498)
							writeDouble(40.7968)
						}
						writeArray {
							writeDouble(-73.9737)
							writeDouble(40.7648)
						}
						writeArray {
							writeDouble(-73.9814)
							writeDouble(40.7681)
						}
						writeArray {
							writeDouble(-73.958)
							writeDouble(40.8003)
						}
					}
				}
				writeArray {
					writeArray {
						writeArray {
							writeDouble(-73.958)
							writeDouble(40.8003)
						}
						writeArray {
							writeDouble(-73.9498)
							writeDouble(40.7968)
						}
						writeArray {
							writeDouble(-73.9737)
							writeDouble(40.7648)
						}
						writeArray {
							writeDouble(-73.958)
							writeDouble(40.8003)
						}
					}
				}
			}
		},
		json("""{"type": "MultiPolygon", "coordinates": [[[[-73.958, 40.8003], [-73.9498, 40.7968], [-73.9737, 40.7648], [-73.9814, 40.7681], [-73.958, 40.8003]]], [[[-73.958, 40.8003], [-73.9498, 40.7968], [-73.9737, 40.7648], [-73.958, 40.8003]]]]}"""),
		verify("The number of polygons is correct") {
			check(decode<Geo.MultiPolygon>().polygons.size == 2)
		},
		verify("The coordinates are correct") {
			val multiPolygon = decode<Geo.MultiPolygon>()
			check(multiPolygon.polygons[0].rings[0].points[0] == Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)))
			check(multiPolygon.polygons[0].rings[0].points[1] == Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)))
			check(multiPolygon.polygons[0].rings[0].points[2] == Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)))
			check(multiPolygon.polygons[0].rings[0].points[3] == Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)))
			check(multiPolygon.polygons[0].rings[0].points[4] == Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)))
			check(multiPolygon.polygons[1].rings[0].points[0] == Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)))
			check(multiPolygon.polygons[1].rings[0].points[1] == Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)))
			check(multiPolygon.polygons[1].rings[0].points[2] == Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)))
			check(multiPolygon.polygons[1].rings[0].points[3] == Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)))
		},
		verify("The coordinates are correct (polymorphic)") {
			val expected = Geo.MultiPolygon(
				Geo.Polygon(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
						Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
						Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
						Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
					),
				),
				Geo.Polygon(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
						Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
						Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
						Geo.Point(Geo.Longitude(-73.958), Geo.Latitude(40.8003)),
					),
				),
			)
			check(decode<Geo>() == expected)
		}
	)
}

private fun SuiteDsl.geoGeometryCollection(factory: Prepared<BsonFactory>) = suite("GeometryCollection") {

	testBson(
		factory,
		"Serialize geometry collection",
		serialize(
			Geo.GeometryCollection(
				Geo.MultiPoint(
					Geo.Point(Geo.Longitude(-73.9580), Geo.Latitude(40.8003)),
					Geo.Point(Geo.Longitude(-73.9498), Geo.Latitude(40.7968)),
					Geo.Point(Geo.Longitude(-73.9737), Geo.Latitude(40.7648)),
					Geo.Point(Geo.Longitude(-73.9814), Geo.Latitude(40.7681)),
				),
				Geo.MultiLineString(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.96943), Geo.Latitude(40.78519)),
						Geo.Point(Geo.Longitude(-73.96082), Geo.Latitude(40.78095)),
					),
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.96415), Geo.Latitude(40.79229)),
						Geo.Point(Geo.Longitude(-73.95544), Geo.Latitude(40.78854)),
					),
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.97162), Geo.Latitude(40.78205)),
						Geo.Point(Geo.Longitude(-73.96374), Geo.Latitude(40.77715)),
					),
					Geo.LineString(
						Geo.Point(Geo.Longitude(-73.97880), Geo.Latitude(40.77247)),
						Geo.Point(Geo.Longitude(-73.97036), Geo.Latitude(40.76811)),
					),
				)
			)
		),
		json("""{"type": "GeometryCollection", "geometries": [{"type": "MultiPoint", "coordinates": [[-73.958, 40.8003], [-73.9498, 40.7968], [-73.9737, 40.7648], [-73.9814, 40.7681]]}, {"type": "MultiLineString", "coordinates": [[[-73.96943, 40.78519], [-73.96082, 40.78095]], [[-73.96415, 40.79229], [-73.95544, 40.78854]], [[-73.97162, 40.78205], [-73.96374, 40.77715]], [[-73.9788, 40.77247], [-73.97036, 40.76811]]]}]}"""),
		document {
			writeString("type", "GeometryCollection")
			writeArray("geometries") {
				writeDocument {
					writeString("type", "MultiPoint")
					writeArray("coordinates") {
						writeArray {
							writeDouble(-73.958)
							writeDouble(40.8003)
						}
						writeArray {
							writeDouble(-73.9498)
							writeDouble(40.7968)
						}
						writeArray {
							writeDouble(-73.9737)
							writeDouble(40.7648)
						}
						writeArray {
							writeDouble(-73.9814)
							writeDouble(40.7681)
						}
					}
				}
				writeDocument {
					writeString("type", "MultiLineString")
					writeArray("coordinates") {
						writeArray {
							writeArray {
								writeDouble(-73.96943)
								writeDouble(40.78519)
							}
							writeArray {
								writeDouble(-73.96082)
								writeDouble(40.78095)
							}
						}
						writeArray {
							writeArray {
								writeDouble(-73.96415)
								writeDouble(40.79229)
							}
							writeArray {
								writeDouble(-73.95544)
								writeDouble(40.78854)
							}
						}
						writeArray {
							writeArray {
								writeDouble(-73.97162)
								writeDouble(40.78205)
							}
							writeArray {
								writeDouble(-73.96374)
								writeDouble(40.77715)
							}
						}
						writeArray {
							writeArray {
								writeDouble(-73.9788)
								writeDouble(40.77247)
							}
							writeArray {
								writeDouble(-73.97036)
								writeDouble(40.76811)
							}
						}
					}
				}
			}
		},
		verify("Get the coordinates of the MultiPoint") {
			check(this.select<Double>("$.geometries[0].coordinates.*.*").toList() == listOf(-73.958, 40.8003, -73.9498, 40.7968, -73.9737, 40.7648, -73.9814, 40.7681))
		}
	)

}
