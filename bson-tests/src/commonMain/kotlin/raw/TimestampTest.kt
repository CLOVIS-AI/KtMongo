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
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.document
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.hex
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.json
import opensavvy.ktmongo.bson.raw.BsonDeclaration.Companion.verify
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Test timestamp representations.
 *
 * Adapted from https://github.com/mongodb/specifications/blob/master/source/bson-corpus/tests/timestamp.json.
 */
@OptIn(ExperimentalEncodingApi::class)
fun SuiteDsl.timestamp(context: Prepared<BsonContext>) = suite("Timestamp") {
	testBson(
		context,
		"Timestamp: (123456789, 42)",
		document {
			writeTimestamp("a", Timestamp(Instant.fromEpochSeconds(123456789), 42u))
		},
		hex("100000001161002A00000015CD5B0700"),
		json($$"""{"a": {"$timestamp": {"t": 123456789, "i": 42}}}"""),
		verify("Read the timestamp") {
			read("a")?.readTimestamp()?.instant?.epochSeconds shouldBe 123456789L
		},
		verify("Read the counter") {
			read("a")?.readTimestamp()?.counter shouldBe 42u
		}
	)

	testBson(
		context,
		"Timestamp with high-order bit set on both seconds and increment",
		document {
			writeTimestamp("a", Timestamp(Instant.fromEpochSeconds(4294967295), 4294967295u))
		},
		hex("10000000116100FFFFFFFFFFFFFFFF00"),
		json($$"""{"a": {"$timestamp": {"t": 4294967295, "i": 4294967295}}}"""),
		verify("Read the timestamp") {
			read("a")?.readTimestamp()?.instant?.epochSeconds shouldBe 4294967295L
		},
		verify("Read the counter") {
			read("a")?.readTimestamp()?.counter shouldBe 4294967295u
		}
	)

	testBson(
		context,
		"Timestamp with high-order bit set on both seconds and increment (not UINT32_MAX)",
		document {
			writeTimestamp("a", Timestamp(Instant.fromEpochSeconds(4000000000), 4000000000u))
		},
		hex("1000000011610000286BEE00286BEE00"),
		json($$"""{"a": {"$timestamp": {"t": 4000000000, "i": 4000000000}}}"""),
		verify("Read the timestamp") {
			read("a")?.readTimestamp()?.instant?.epochSeconds shouldBe 4000000000L
		},
		verify("Read the counter") {
			read("a")?.readTimestamp()?.counter shouldBe 4000000000u
		}
	)
}
