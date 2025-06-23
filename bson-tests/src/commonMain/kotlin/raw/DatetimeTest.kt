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

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Test datetime representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/datetime.json.
 */
@OptIn(ExperimentalEncodingApi::class)
fun SuiteDsl.datetime(context: Prepared<BsonContext>) = suite("Datetime") {
	test("epoch") {
		context().buildDocument {
			writeInstant("a", Instant.fromEpochSeconds(0))
		} shouldBeHex "10000000096100000000000000000000"
	}

	test("positive ms") {
		context().buildDocument {
			writeInstant("a", Instant.parse("2012-12-24T12:15:30.501Z"))
		} shouldBeHex "10000000096100C5D8D6CC3B01000000"
	}

	test("negative") {
		context().buildDocument {
			writeInstant("a", Instant.fromEpochMilliseconds(-284643869501))
		} shouldBeHex "10000000096100C33CE7B9BDFFFFFF00"
	}

	test("Y10K") {
		context().buildDocument {
			writeInstant("a", Instant.fromEpochMilliseconds(253402300800000))
		} shouldBeHex "1000000009610000DC1FD277E6000000"
	}

	test("leading zero ms") {
		context().buildDocument {
			writeInstant("a", Instant.parse("2012-12-24T12:15:30.001Z"))
		} shouldBeHex "10000000096100D1D6D6CC3B01000000"
	}

}
