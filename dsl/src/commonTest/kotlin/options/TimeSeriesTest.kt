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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.command.CreateCollectionOptions
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.testContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

val TimeSeriesTest by multiContextSuite {

	class Target(
		val date: Instant,
		val sensorId: ObjectId,
	)

	test("Simple time series") {
		val options = CreateCollectionOptions<Target>(testContext())

		options.timeSeries {
			timeField(Target::date)
			metaField(Target::sensorId)
		}

		options.shouldBeBson("""
			{
				"timeseries": {
					"timeField": "date",
					"metaField": "sensorId"
				}
			}
		""".trimIndent())
	}

	test("With expiration") {
		val options = CreateCollectionOptions<Target>(testContext())

		options.timeSeries {
			timeField(Target::date)
			expiresAfter(1.minutes + 37.seconds)
		}

		options.shouldBeBson("""
			{
				"expireAfterSeconds": 97,
				"timeseries": {
					"timeField": "date"
				}
			}
		""".trimIndent())
	}

	test("Bucket control") {
		val options = CreateCollectionOptions<Target>(testContext())

		options.timeSeries {
			timeField(Target::date)
			metaField(Target::sensorId)

			// These fields can't be used together, but it doesn't matter for this test
			granularity(TimeSeriesDsl.Granularity.Hours)
			bucketMaxSpan(2.hours + 30.minutes)
		}

		options.shouldBeBson("""
			{
				"timeseries": {
					"timeField": "date",
					"metaField": "sensorId",
					"granularity": "hours",
					"bucketMaxSpanSeconds": 9000,
					"bucketRoundingSeconds": 9000
				}
			}
		""".trimIndent())
	}

}
