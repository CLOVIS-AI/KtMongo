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
import opensavvy.prepared.suite.random.Random
import opensavvy.prepared.suite.random.nextLong
import opensavvy.prepared.suite.random.random
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class TimestampTest : PreparedSpec({

	suite("Creation and formatting") {
		val examples = listOf(
			0L to (Instant.parse("1970-01-01T00:00:00Z") to 0.toUInt()),
			1L to (Instant.parse("1970-01-01T00:00:01Z") to 0.toUInt()),
			67L to (Instant.parse("1970-01-01T00:01:07Z") to 0.toUInt()),
			(1L shl 32) to (Instant.parse("1970-01-01T00:00:00Z") to 1.toUInt()),
			(4L shl 32) to (Instant.parse("1970-01-01T00:00:00Z") to 4.toUInt()),
			14588972749L to (Instant.parse("2024-01-01T01:01:01Z") to 3.toUInt()),
			-1L to (Timestamp.MAX_INSTANT to Timestamp.MAX_COUNTER),
			Timestamp.MAX.value to (Timestamp.MAX_INSTANT to Timestamp.MAX_COUNTER),
		)

		for ((value, fields) in examples) {
			val (instant, counter) = fields
			test("A timestamp at $instant + $counter can be round-tripped to itself") {
				check(Timestamp(instant, counter).instant == instant)
				check(Timestamp(instant, counter).counter == counter)
			}

			test("A timestamp at $instant + $counter has value $value") {
				check(Timestamp(instant, counter).value == value)
			}

			test("A timestamp with value $value has the components $instant + $counter") {
				check(Timestamp(value).instant == instant)
				check(Timestamp(value).counter == counter)
			}
		}
	}

	suite("Chronological order") {
		test("The maximum timestamp is greater than all others") {
			val random = random.accessUnsafe()
			repeat(1000) {
				val timestamp = generateSequence { Timestamp(random.nextLong()) }
					.first { it != Timestamp.MAX }

				check(timestamp < Timestamp.MAX)
			}
		}

		test("The minimum timestamp is lesser than all others") {
			val random = random.accessUnsafe()
			repeat(1000) {
				val timestamp = generateSequence { Timestamp(random.nextLong()) }
					.first { it != Timestamp.MIN }

				check(timestamp > Timestamp.MIN)
			}
		}

		test("Timestamps are ordered by instant, ignoring counters") {
			val instant = random.nextInstant(until = Timestamp.MAX_INSTANT.epochSeconds - 1)

			repeat(1000) {
				val largerInstant = random.nextInstant(from = instant.epochSeconds + 1)
				val counter1 = random.nextCounter()
				val counter2 = random.nextCounter()

				check(Timestamp(instant, counter1) < Timestamp(largerInstant, counter2))
			}
		}

		test("For a given instant, timestamps are ordered by counter") {
			repeat(1000) {
				val instant = random.nextInstant()
				val counter = random.nextCounter(until = Timestamp.MAX_COUNTER - 1u)
				val largerCounter = random.nextCounter(from = counter + 1u)

				check(Timestamp(instant, counter) < Timestamp(instant, largerCounter))
			}
		}

		test("Total orders should be anti-symmetric") {
			repeat(1000) {
				val instant1 = Timestamp(random.nextInstant(), random.nextCounter())
				val instant2 = Timestamp(random.nextInstant(), random.nextCounter())

				check(instant1.compareTo(instant2) == -instant2.compareTo(instant1))
			}
		}
	}

})

private suspend fun Random.nextInstant(from: Long = 0, until: Long = Timestamp.MAX_INSTANT.epochSeconds) =
	Instant.fromEpochSeconds(nextLong(from, until))

private suspend fun Random.nextCounter(from: UInt = 0u, until: UInt = Timestamp.MAX_COUNTER) =
	nextLong(from.toULong().toLong(), until.toULong().toLong()).toUInt()
