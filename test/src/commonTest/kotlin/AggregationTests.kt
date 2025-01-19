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

package opensavvy.ktmongo.sync

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.kotest.PreparedSpec

class AggregationTests : PreparedSpec({
	@Serializable
	data class Song(
		val creationDate: Int,
		val editionDate: Int,
	)

	val songs by testCollection<Song>("aggregation-songs")

	test("Use \$expr to compare two fields in a document") {
		songs().insertOne(Song(creationDate = 0, editionDate = 1))
		songs().insertOne(Song(creationDate = 1, editionDate = 1))
		songs().insertOne(Song(creationDate = 2, editionDate = 1))

		val anomalies = songs().find {
			expr {
				of(Song::creationDate) gt of(Song::editionDate)
			}
		}

		check(anomalies.toList() == listOf(Song(creationDate = 2, editionDate = 1)))
	}
})
