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
}
