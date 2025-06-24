/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

@file:OptIn(LowLevelApi::class, ExperimentalTime::class)

package opensavvy.ktmongo.bson.raw

import io.kotest.matchers.shouldBe
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Test datetime representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/datetime.json.
 */
fun SuiteDsl.datetime(context: Prepared<BsonContext>) = suite("Datetime") {
	testBson(
		context,
		"epoch"
	) {
		document { writeInstant("a", Instant.fromEpochSeconds(0)) }
		expectedBinaryHex = "10000000096100000000000000000000"
		expectedJson = $$"""{"a": {"$date": "1970-01-01T00:00:00Z"}}"""
		verify("Read value") { read("a")?.readInstant() shouldBe Instant.fromEpochSeconds(0) }
	}

	testBson(
		context,
		"positive ms"
	) {
		document { writeInstant("a", Instant.parse("2012-12-24T12:15:30.501Z")) }
		expectedBinaryHex = "10000000096100C5D8D6CC3B01000000"
		expectedJson = $$"""{"a": {"$date": "2012-12-24T12:15:30.501Z"}}"""
		verify("Read value") { read("a")?.readInstant() shouldBe Instant.parse("2012-12-24T12:15:30.501Z") }
	}

	testBson(
		context,
		"negative"
	) {
		document { writeInstant("a", Instant.fromEpochMilliseconds(-284643869501)) }
		expectedBinaryHex = "10000000096100C33CE7B9BDFFFFFF00"
		expectedJson = $$"""{"a": {"$date": {"$numberLong": "-284643869501"}}}"""
		verify("Read value") { read("a")?.readInstant() shouldBe Instant.fromEpochMilliseconds(-284643869501) }
	}

	testBson(
		context,
		"Y10K"
	) {
		document { writeInstant("a", Instant.fromEpochMilliseconds(253402300800000)) }
		expectedBinaryHex = "1000000009610000DC1FD277E6000000"
		expectedJson = $$"""{"a": {"$date": {"$numberLong": "253402300800000"}}}"""
		verify("Read value") { read("a")?.readInstant() shouldBe Instant.fromEpochMilliseconds(253402300800000) }
	}

	testBson(
		context,
		"leading zero ms"
	) {
		document { writeInstant("a", Instant.parse("2012-12-24T12:15:30.001Z")) }
		expectedBinaryHex = "10000000096100D1D6D6CC3B01000000"
		expectedJson = $$"""{"a": {"$date": "2012-12-24T12:15:30.001Z"}}"""
		verify("Read value") { read("a")?.readInstant() shouldBe Instant.parse("2012-12-24T12:15:30.001Z") }
	}
}
