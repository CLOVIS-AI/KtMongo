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

package opensavvy.ktmongo.dsl.query.filter

import opensavvy.ktmongo.bson.types.ExperimentalGeoBsonApi
import opensavvy.ktmongo.bson.types.Geo
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.query.shouldBeBson

val GeopositionalFilterTest by multiContextSuite {
	suite($$"$near") {
		test("Unbounded") {
			filter {
				User::home.near(Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)))
			} shouldBeBson $$"""
				{
					"home": {
						"$near": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							}
						}
					}
				}
			""".trimIndent()
		}

		test("With minimum bound") {
			filter {
				User::home.near(
					target = Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)),
					minDistance = 100.0,
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$near": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							},
							"$minDistance": 100.0
						}
					}
				}
			""".trimIndent()
		}

		test("With maximum bound") {
			filter {
				User::home.near(
					target = Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)),
					maxDistance = 100.0,
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$near": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							},
							"$maxDistance": 100.0
						}
					}
				}
			""".trimIndent()
		}

		test("With both bounds") {
			filter {
				User::home.near(
					target = Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)),
					minDistance = 17.50,
					maxDistance = 267.0,
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$near": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							},
							"$minDistance": 17.5,
							"$maxDistance": 267.0
						}
					}
				}
			""".trimIndent()
		}
	}

	suite($$"$nearSphere") {
		test("Unbounded") {
			filter {
				User::home.nearSphere(Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)))
			} shouldBeBson $$"""
				{
					"home": {
						"$nearSphere": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							}
						}
					}
				}
			""".trimIndent()
		}

		test("With minimum bound") {
			filter {
				User::home.nearSphere(
					target = Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)),
					minDistance = 100.0,
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$nearSphere": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							},
							"$minDistance": 100.0
						}
					}
				}
			""".trimIndent()
		}

		test("With maximum bound") {
			filter {
				User::home.nearSphere(
					target = Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)),
					maxDistance = 100.0,
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$nearSphere": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							},
							"$maxDistance": 100.0
						}
					}
				}
			""".trimIndent()
		}

		test("With both bounds") {
			filter {
				User::home.nearSphere(
					target = Geo.Point(Geo.Longitude(0.7269), Geo.Latitude(45.1828)),
					minDistance = 17.50,
					maxDistance = 267.0,
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$nearSphere": {
							"$geometry": {
								"type": "Point",
								"coordinates": [0.7269, 45.1828]
							},
							"$minDistance": 17.5,
							"$maxDistance": 267.0
						}
					}
				}
			""".trimIndent()
		}
	}

	suite($$"$geoWithin") {
		test("Simple") {
			filter {
				User::home.geoWithin(
					Geo.Polygon(
						Geo.Point(Geo.Longitude(-73.95), Geo.Latitude(40.80)),
						Geo.Point(Geo.Longitude(-73.94), Geo.Latitude(40.79)),
						Geo.Point(Geo.Longitude(-73.97), Geo.Latitude(40.79)),
						Geo.Point(Geo.Longitude(-79.98), Geo.Latitude(40.76)),
						Geo.Point(Geo.Longitude(-73.95), Geo.Latitude(40.80)),
					)
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$geoWithin": {
							"$geometry": {
								"type": "Polygon",
								"coordinates": [
									[
										[-73.95, 40.8],
										[-73.94, 40.79],
										[-73.97, 40.79],
										[-79.98, 40.76],
										[-73.95, 40.8]
									]
								]
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Large with CRS") {
			filter {
				User::home.geoWithin(
					Geo.Polygon(
						Geo.Point(Geo.Longitude(-73.95), Geo.Latitude(40.80)),
						Geo.Point(Geo.Longitude(-73.94), Geo.Latitude(40.79)),
						Geo.Point(Geo.Longitude(-73.97), Geo.Latitude(40.79)),
						Geo.Point(Geo.Longitude(-79.98), Geo.Latitude(40.76)),
						Geo.Point(Geo.Longitude(-73.95), Geo.Latitude(40.80)),
					),
					crs = Geo.CoordinateReferenceSystem.MongoDB
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$geoWithin": {
							"$geometry": {
								"type": "Polygon",
								"coordinates": [
									[
										[-73.95, 40.8],
										[-73.94, 40.79],
										[-73.97, 40.79],
										[-79.98, 40.76],
										[-73.95, 40.8]
									]
								],
								"crs": {
									"type": "name",
									"properties": {
										"name": "urn:x-mongodb:crs:strictwinding:EPSG:4326"
									}
								}
							}
						}
					}
				}
			""".trimIndent()
		}

		test("MultiPolygon") {
			filter {
				User::home.geoWithin(
					Geo.MultiPolygon(
						Geo.Polygon(
							Geo.Point(Geo.Longitude(-73.95), Geo.Latitude(40.80)),
							Geo.Point(Geo.Longitude(-73.94), Geo.Latitude(40.79)),
							Geo.Point(Geo.Longitude(-73.97), Geo.Latitude(40.79)),
							Geo.Point(Geo.Longitude(-79.98), Geo.Latitude(40.76)),
							Geo.Point(Geo.Longitude(-73.95), Geo.Latitude(40.80)),
						),
						Geo.Polygon(
							Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
							Geo.Point(Geo.Longitude(10.0), Geo.Latitude(0.0)),
							Geo.Point(Geo.Longitude(10.0), Geo.Latitude(10.0)),
							Geo.Point(Geo.Longitude(0.0), Geo.Latitude(10.0)),
							Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
						)
					)
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$geoWithin": {
							"$geometry": {
								"type": "MultiPolygon",
								"coordinates": [
									[
										[
											[-73.95, 40.8],
											[-73.94, 40.79],
											[-73.97, 40.79],
											[-79.98, 40.76],
											[-73.95, 40.8]
										]
									],
									[
										[
											[0.0, 0.0],
											[10.0, 0.0],
											[10.0, 10.0],
											[0.0, 10.0],
											[0.0, 0.0]
										]
									]
								]
							}
						}
					}
				}
			""".trimIndent()
		}
	}

	suite($$"$geoIntersects") {
		test("Simple") {
			filter {
				User::home.geoIntersects(
					Geo.Polygon(
						Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
						Geo.Point(Geo.Longitude(3.0), Geo.Latitude(6.0)),
						Geo.Point(Geo.Longitude(6.0), Geo.Latitude(1.0)),
						Geo.Point(Geo.Longitude(0.0), Geo.Latitude(0.0)),
					)
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$geoIntersects": {
							"$geometry": {
								"type": "Polygon",
								"coordinates": [
									[
										[0.0, 0.0],
										[3.0, 6.0],
										[6.0, 1.0],
										[0.0, 0.0]
									]
								]
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Large with CRS") {
			filter {
				User::home.geoIntersects(
					Geo.Polygon(
						Geo.Point(Geo.Longitude(-100.0), Geo.Latitude(60.0)),
						Geo.Point(Geo.Longitude(-100.0), Geo.Latitude(-60.0)),
						Geo.Point(Geo.Longitude(100.0), Geo.Latitude(-60.0)),
						Geo.Point(Geo.Longitude(100.0), Geo.Latitude(60.0)),
						Geo.Point(Geo.Longitude(-100.0), Geo.Latitude(60.0)),
					),
					crs = Geo.CoordinateReferenceSystem.MongoDB
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$geoIntersects": {
							"$geometry": {
								"type": "Polygon",
								"coordinates": [
									[
										[-100.0, 60.0],
										[-100.0, -60.0],
										[100.0, -60.0],
										[100.0, 60.0],
										[-100.0, 60.0]
									]
								],
								"crs": {
									"type": "name",
									"properties": {
										"name": "urn:x-mongodb:crs:strictwinding:EPSG:4326"
									}
								}
							}
						}
					}
				}
			""".trimIndent()
		}

		test("LineString") {
			filter {
				User::home.geoIntersects(
					Geo.LineString(
						Geo.Point(Geo.Longitude(-105.82), Geo.Latitude(33.87)),
						Geo.Point(Geo.Longitude(-106.31), Geo.Latitude(35.65)),
						Geo.Point(Geo.Longitude(-107.39), Geo.Latitude(35.98)),
					)
				)
			} shouldBeBson $$"""
				{
					"home": {
						"$geoIntersects": {
							"$geometry": {
								"type": "LineString",
								"coordinates": [
									[-105.82, 33.87],
									[-106.31, 35.65],
									[-107.39, 35.98]
								]
							}
						}
					}
				}
			""".trimIndent()
		}
	}
}
