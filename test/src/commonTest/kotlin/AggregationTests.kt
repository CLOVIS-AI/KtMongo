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
import opensavvy.ktmongo.coroutines.filter
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

	test("Use an aggregation pipeline to search for data") {
		songs().insertOne(Song(creationDate = 0, editionDate = 1))
		songs().insertOne(Song(creationDate = 14, editionDate = 1))
		songs().insertOne(Song(creationDate = 13, editionDate = 1))

		val results = songs().aggregate()
			.match { Song::creationDate gt 12 }
			.skip(1)
			.limit(5)

		println("Sending aggregation: $results")

		check(results.toList() == listOf(Song(creationDate = 13, editionDate = 1)))
	}

	test("Update with pipeline") {
		songs().insertOne(Song(creationDate = 0, editionDate = 1))
		songs().insertOne(Song(creationDate = 1, editionDate = 1))
		songs().insertOne(Song(creationDate = 2, editionDate = 1))

		val anomalies = songs().filter {
			expr {
				of(Song::creationDate) gt of(Song::editionDate)
			}
		}

		anomalies.updateOneWithPipeline {
			set {
				Song::creationDate set 12
			}
		}

		check(Song(creationDate = 12, editionDate = 1) in anomalies.find().toList())
	}

	test("Update with a condition") {
		songs().insertOne(Song(creationDate = 0, editionDate = 1))
		songs().insertOne(Song(creationDate = 1, editionDate = 1))
		songs().insertOne(Song(creationDate = 2, editionDate = 3))

		songs().updateManyWithPipeline {
			set {
				Song::creationDate set cond(
					of(Song::editionDate) gt of(Song::creationDate),
					of(12),
					of(11)
				)
			}
		}

		check(Song(creationDate = 12, editionDate = 3) in songs().find().toList())
	}
})
