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

@file:OptIn(ExperimentalTime::class)

package opensavvy.ktmongo.bson.types

import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ObjectIdTest : PreparedSpec({

	test("Construct from parts") {
		val timestamp = Instant.parse("2021-01-01T00:00:00Z")
		val processId = 123456789012L
		val counter = 123

		val id = ObjectId(timestamp, processId, counter)

		check(id.timestamp == timestamp)
		check(id.processId == processId)
		check(id.counter == counter)
	}

	test("Construct from string") {
		val id = ObjectId.fromHex("5fee66001cbe991a1400007b")
		check(id.toString() == "ObjectId(5fee66001cbe991a1400007b)")
	}

	suite("Equality") {
		test("Two ObjectId with the same data are equal") {
			val timestamp = Instant.parse("2021-01-01T00:00:00Z")
			val processId = 123456789012L
			val counter = 123

			val id1 = ObjectId(timestamp, processId, counter)
			val id2 = ObjectId(timestamp, processId, counter)

			check(id1 == id2)
			check(id1.hashCode() == id2.hashCode())
			check(id1.compareTo(id2) == 0)
		}

		test("Two ObjectId with a different timestamp are different") {
			val timestamp1 = Instant.parse("2021-01-01T00:00:00Z")
			val timestamp2 = Instant.parse("2021-01-01T00:00:01Z")
			val processId = 123456789012L
			val counter = 123

			val id1 = ObjectId(timestamp1, processId, counter)
			val id2 = ObjectId(timestamp2, processId, counter)

			check(id1 != id2)
			check(id1.hashCode() != id2.hashCode())
			check(id1 < id2)
		}

		test("Two ObjectId with a different processId are different") {
			val timestamp = Instant.parse("2021-01-01T00:00:00Z")
			val processId1 = 123456789012L
			val processId2 = 123456789013L
			val counter = 123

			val id1 = ObjectId(timestamp, processId1, counter)
			val id2 = ObjectId(timestamp, processId2, counter)

			check(id1 != id2)
			check(id1.hashCode() != id2.hashCode())
			check(id1 < id2)
		}

		test("Two ObjectId with a different counter are different") {
			val timestamp = Instant.parse("2021-01-01T00:00:00Z")
			val processId = 123456789012L
			val counter1 = 123
			val counter2 = 124

			val id1 = ObjectId(timestamp, processId, counter1)
			val id2 = ObjectId(timestamp, processId, counter2)

			check(id1 != id2)
			check(id1.hashCode() != id2.hashCode())
			check(id1 < id2)
		}

		test("Two ObjectId with a different timestamp in the same second are identical") {
			val timestamp1 = Instant.parse("2021-01-01T00:00:00.000Z")
			val timestamp2 = Instant.parse("2021-01-01T00:00:00.999Z")
			val processId = 123456789012L
			val counter = 123

			val id1 = ObjectId(timestamp1, processId, counter)
			val id2 = ObjectId(timestamp2, processId, counter)

			check(id1 == id2)
			check(id1.hashCode() == id2.hashCode())
			check(id1.compareTo(id2) == 0)
		}
	}

	suite("Working with the timestamp") {
		test("Can compare ObjectId with timestamp") {
			val timestamp = Instant.parse("2021-01-01T00:00:00Z")
			val processId = 123456789012L
			val counter = 123

			val id = ObjectId(timestamp, processId, counter)

			// Equality
			check(id <= Instant.parse("2021-01-01T00:00:00Z"))
			check(id >= Instant.parse("2021-01-01T00:00:00Z"))

			// Inequality
			check(id < Instant.parse("2021-01-01T00:00:01Z"))
			check(id > Instant.parse("2020-12-31T23:59:59Z"))

			// Symmetry
			check(Instant.parse("2021-01-01T00:00:00Z") >= id)
			check(Instant.parse("2021-01-01T00:00:00Z") <= id)
			check(Instant.parse("2021-01-01T00:00:01Z") > id)
			check(Instant.parse("2020-12-31T23:59:59Z") < id)
		}

		test("Can create an ObjectId closed range from a timestamp range") {
			val closedRange = (Instant.parse("2020-12-31T23:59:59Z")..Instant.parse("2021-01-01T00:00:01Z"))
				.toObjectIdRange()

			// Middle instant is fully in
			check(ObjectId.minAt(Instant.parse("2021-01-01T00:00:00Z")) in closedRange)
			check(ObjectId.maxAt(Instant.parse("2021-01-01T00:00:00Z")) in closedRange)

			// Start instant is fully in
			check(ObjectId.minAt(Instant.parse("2020-12-31T23:59:59Z")) in closedRange)
			check(ObjectId.maxAt(Instant.parse("2020-12-31T23:59:59Z")) in closedRange)

			// End instant is fully in
			check(ObjectId.minAt(Instant.parse("2021-01-01T00:00:01Z")) in closedRange)
			check(ObjectId.maxAt(Instant.parse("2021-01-01T00:00:01Z")) in closedRange)
		}

		test("Can create an ObjectId open ended range from a timestamp range") {
			val openEndRange = (Instant.parse("2020-12-31T23:59:59Z")..<Instant.parse("2021-01-01T00:00:01Z"))
				.toObjectIdRange()

			// Middle instant is fully in
			check(ObjectId.minAt(Instant.parse("2021-01-01T00:00:00Z")) in openEndRange)
			check(ObjectId.maxAt(Instant.parse("2021-01-01T00:00:00Z")) in openEndRange)

			// Start instant is fully in
			check(ObjectId.minAt(Instant.parse("2020-12-31T23:59:59Z")) in openEndRange)
			check(ObjectId.maxAt(Instant.parse("2020-12-31T23:59:59Z")) in openEndRange)

			// End instant is excluded
			check(ObjectId.minAt(Instant.parse("2021-01-01T00:00:01Z")) !in openEndRange)
			check(ObjectId.maxAt(Instant.parse("2021-01-01T00:00:01Z")) !in openEndRange)
		}
	}

})
