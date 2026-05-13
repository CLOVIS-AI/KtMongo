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

@file:OptIn(ExperimentalUuidApi::class)

package opensavvy.ktmongo.bson.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import opensavvy.ktmongo.bson.types.*
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
private data class JsonSerializationObjectId(
	val _id: ObjectId,
)

@Serializable
private data class JsonSerializationInstant @OptIn(ExperimentalTime::class) constructor(
	val at: @Serializable(with = InstantAsBsonDatetimeSerializer::class) Instant,
)

@Serializable
private data class JsonSerializationTimestamp(
	val at: Timestamp,
)

@Serializable
private data class JsonSerializationUuid(
	val id: @Serializable(with = UuidAsBsonBinarySerializer::class) Uuid,
)

@Serializable
private data class JsonSerializationVector(
	val data: Vector,
)

@Serializable
private data class JsonSerializationByteVector(
	val data: ByteVector,
)

@Serializable
private data class JsonSerializationFloatVector(
	val data: FloatVector,
)

@Serializable
private data class JsonSerializationBooleanVector(
	val data: BooleanVector,
)

val JsonSerialization by preparedSuite {

	val json = Json

	test("ObjectId is serialized as string") {
		val obj = JsonSerializationObjectId(ObjectId("69fb13fc606302abb92758a3"))
		val expected = """{"_id":"69fb13fc606302abb92758a3"}"""

		check(json.encodeToString(obj) == expected)
		check(json.decodeFromString<JsonSerializationObjectId>(expected) == obj)
	}

	test("Instant is serialized as string") {
		val obj = JsonSerializationInstant(Instant.fromEpochMilliseconds(1695118271957))
		val expected = """{"at":"2023-09-19T10:11:11.957Z"}"""

		check(json.encodeToString(obj) == expected)
		check(json.decodeFromString<JsonSerializationInstant>(expected) == obj)
	}

	test("Timestamp is serialized as a string") {
		val obj = JsonSerializationTimestamp(Timestamp(Instant.fromEpochSeconds(1695118271), 957u))
		val expected = """{"at":"2023-09-19T10:11:11Z#957"}"""

		check(json.encodeToString(obj) == expected)
		check(json.decodeFromString<JsonSerializationTimestamp>(expected) == obj)
	}

	test("UUID is serialized as a string") {
		val obj = JsonSerializationUuid(Uuid.parse("78d5108d-efb0-4368-82f0-88643792229c"))
		val expected = """{"id":"78d5108d-efb0-4368-82f0-88643792229c"}"""

		check(json.encodeToString(obj) == expected)
		check(json.decodeFromString<JsonSerializationUuid>(expected) == obj)
	}

	suite("Vector is serialized as a base64 string") {
		test("Arbitrary vector") {
			val vector = Vector.fromBinaryData(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
			val obj = JsonSerializationVector(vector)
			val expected = """{"data":"AQIDBAUGBwgJAA=="}"""

			check(json.encodeToString(obj) == expected)
			check(json.decodeFromString<JsonSerializationVector>(expected) == obj)
		}

		test("Byte vector") {
			val vector = ByteVector(1, 2, 3, 4, 5, 6, 7, 8, 9, 127, -128)
			val obj = JsonSerializationByteVector(vector)
			val expected = """{"data":"AwABAgMEBQYHCAl/gA=="}"""

			check(json.encodeToString(obj) == expected)
			check(json.decodeFromString<JsonSerializationByteVector>(expected) == obj)
		}

		test("Float vector") {
			val vector = FloatVector(1.0f, 2.78f, 6156.1f, 0.0f, -0.0f, Float.MAX_VALUE, -Float.MIN_VALUE)
			val obj = JsonSerializationFloatVector(vector)
			val expected = """{"data":"JwAAAIA/hesxQM1gwEUAAAAAAAAAgP//f38BAACA"}"""

			check(json.encodeToString(obj) == expected)
			check(json.decodeFromString<JsonSerializationFloatVector>(expected) == obj)
		}

		test("Boolean vector") {
			val vector = BooleanVector(true, false, true, true, true, false, false, true, true)
			val obj = JsonSerializationBooleanVector(vector)
			val expected = """{"data":"EAGdAQ=="}"""

			check(json.encodeToString(obj) == expected)
			check(json.decodeFromString<JsonSerializationBooleanVector>(expected) == obj)
		}
	}
}
