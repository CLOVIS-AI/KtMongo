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

package opensavvy.ktmongo.bson.official.types

import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.random.nextLong
import opensavvy.prepared.suite.random.random
import kotlin.time.ExperimentalTime

class OfficialTimestampTest : PreparedSpec({

	test("Round-trip between official Timestamp and KtMongo Timestamp") {
		repeat(1000) {
			val timestamp = Timestamp(random.nextLong().toULong())

			check(timestamp.toOfficial().toKtMongo() == timestamp)
			check(timestamp.toOfficial().time == timestamp.instant.epochSeconds.toInt())
			check(timestamp.toOfficial().inc == timestamp.counter.toInt())
		}
	}

})
