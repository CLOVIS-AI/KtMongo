/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

@file:OptIn(ExperimentalTime::class, ExperimentalAtomicApi::class)

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.sync.SortUser.Content
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class SortUser(
	val creationDate: Instant? = null,
	val content: Content? = null,
	val numbers: List<Int> = emptyList(),
	val contents: List<Content> = emptyList(),
) {

	data class Content(
		val a: Int,
	)
}

private fun Instant.truncateToMilliseconds(): Instant =
	Instant.fromEpochMilliseconds(this.toEpochMilliseconds())

val SortTest by preparedSuite(preparedConfig = CoroutineTimeout(30.seconds)) {
	val users by testCollection<SortUser>("basic-sorts")

	test("Sort by date") {
		users().insertMany(
			SortUser(Instant.DISTANT_FUTURE),
			SortUser(Instant.DISTANT_PAST),
			SortUser(Instant.parse("2026-03-19T19:24:02Z")),
		)

		val resultsAscending = users().find({
			sort {
				ascending(SortUser::creationDate)
			}
		}) {}

		val expectedAscending = listOf(
			SortUser(Instant.DISTANT_PAST.truncateToMilliseconds()),
			SortUser(Instant.parse("2026-03-19T19:24:02Z").truncateToMilliseconds()),
			SortUser(Instant.DISTANT_FUTURE.truncateToMilliseconds()),
		)

		check(expectedAscending == resultsAscending.toList())

		val resultsDescending = users().find({
			sort {
				descending(SortUser::creationDate)
			}
		}) {}

		check(expectedAscending == resultsDescending.toList().reversed())
	}

	test("Sort by field in document") {
		users().insertMany(
			SortUser(content = Content(7)),
			SortUser(content = Content(2)),
			SortUser(content = Content(4)),
		)

		val resultsAscending = users().find({
			sort {
				ascending(SortUser::content / Content::a)
			}
		}) {}

		val expectedAscending = listOf(
			SortUser(content = Content(2)),
			SortUser(content = Content(4)),
			SortUser(content = Content(7)),
		)

		check(expectedAscending == resultsAscending.toList())

		val resultsDescending = users().find({
			sort {
				descending(SortUser::content / Content::a)
			}
		}) {}

		check(expectedAscending == resultsDescending.toList().reversed())
	}

	test("Sort by an array of numbers") {
		users().insertMany(
			SortUser(numbers = listOf(2)),
			SortUser(numbers = listOf(1, 4)),
			SortUser(numbers = listOf(9, 3)),
		)

		val resultsAscending = users().find({
			sort {
				ascending(SortUser::numbers)
			}
		}) {}

		val expectedAscending = listOf(
			SortUser(numbers = listOf(1, 4)),
			SortUser(numbers = listOf(2)),
			SortUser(numbers = listOf(9, 3)),
		)

		check(expectedAscending == resultsAscending.toList())

		val resultsDescending = users().find({
			sort {
				descending(SortUser::numbers)
			}
		}) {}

		// Note that it is NOT the reverse of expectedAscending
		val expectedDescending = listOf(
			SortUser(numbers = listOf(9, 3)),
			SortUser(numbers = listOf(1, 4)),
			SortUser(numbers = listOf(2)),
		)

		check(expectedDescending == resultsDescending.toList())
	}

	test("Sort by a number in an array") {
		users().insertMany(
			SortUser(numbers = listOf(2)),
			SortUser(numbers = listOf(1, 4)),
			SortUser(numbers = listOf(9, 3)),
		)

		val resultsAscending = users().find({
			sort {
				ascending(SortUser::numbers[1])
			}
		}) {}

		val expectedAscending = listOf(
			SortUser(numbers = listOf(2)),
			SortUser(numbers = listOf(9, 3)),
			SortUser(numbers = listOf(1, 4)),
		)

		check(expectedAscending == resultsAscending.toList())

		val resultsDescending = users().find({
			sort {
				descending(SortUser::numbers[1])
			}
		}) {}

		check(expectedAscending == resultsDescending.toList().reversed())
	}

	test("Sort by an array of documents") {
		users().insertMany(
			SortUser(contents = listOf(Content(2))),
			SortUser(contents = listOf(Content(1), Content(4))),
			SortUser(contents = listOf(Content(9), Content(3))),
		)

		val resultsAscending = users().find({
			sort {
				ascending(SortUser::contents)
			}
		}) {}

		val expectedAscending = listOf(
			SortUser(contents = listOf(Content(1), Content(4))),
			SortUser(contents = listOf(Content(2))),
			SortUser(contents = listOf(Content(9), Content(3))),
		)

		check(expectedAscending == resultsAscending.toList())

		val resultsDescending = users().find({
			sort {
				descending(SortUser::contents)
			}
		}) {}

		// Note that it is NOT the reverse of expectedAscending
		val expectedDescending = listOf(
			SortUser(contents = listOf(Content(9), Content(3))),
			SortUser(contents = listOf(Content(1), Content(4))),
			SortUser(contents = listOf(Content(2))),
		)

		check(expectedDescending == resultsDescending.toList())
	}

	test("Sort by a document in an array") {
		users().insertMany(
			SortUser(contents = listOf(Content(2))),
			SortUser(contents = listOf(Content(1), Content(4))),
			SortUser(contents = listOf(Content(9), Content(3))),
		)

		val resultsAscending = users().find({
			sort {
				ascending(SortUser::contents[1] / Content::a)
			}
		}) {}

		val expectedAscending = listOf(
			SortUser(contents = listOf(Content(2))),
			SortUser(contents = listOf(Content(9), Content(3))),
			SortUser(contents = listOf(Content(1), Content(4))),
		)

		check(expectedAscending == resultsAscending.toList())

		val resultsDescending = users().find({
			sort {
				descending(SortUser::contents[1] / Content::a)
			}
		}) {}

		check(expectedAscending == resultsDescending.toList().reversed())
	}
}
