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

@file:Suppress("PropertyName")

package opensavvy.ktmongo.sync.studies

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.coroutines.toList
import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.time.Instant

@Serializable
data class Movie(
	val _id: ObjectId,
	val runtime: Int,
	val title: String,
	val year: Int,
	val movie_comments: List<Comment> = emptyList(),
)

@Serializable
data class Comment(
	val _id: ObjectId,
	val movieId: ObjectId,
	val name: String,
	val text: String,
	val date: Instant,
)

val AggregationLookup by preparedSuite {

	val movies by testCollection<Movie>("case-lookup-movies")
	val comments by testCollection<Comment>("case-lookup-comments")

	test($$"Perform a Single Equality Join with $lookup") {
		// Ported from: https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#perform-a-single-equality-join-with--lookup

		val movieA = movies().newId()
		val movieB = movies().newId()

		movies().insertMany(
			Movie(
				_id = movieA,
				runtime = 3500,
				title = "A",
				year = 2008,
			),
			Movie(
				_id = movieB,
				runtime = 2700,
				title = "B",
				year = 2017,
			)
		)

		comments().insertMany(
			Comment(
				_id = comments().newId(),
				movieId = movieA,
				name = "Bob",
				text = "A1",
				date = Instant.parse("2023-01-01T00:00:00Z"),
			),
			Comment(
				_id = comments().newId(),
				movieId = movieA,
				name = "Alice",
				text = "A2",
				date = Instant.parse("2023-01-07T00:00:00Z"),
			),
			Comment(
				_id = comments().newId(),
				movieId = movieB,
				name = "Fred",
				text = "B1",
				date = Instant.parse("2025-01-01T00:00:00Z"),
			),
		)

		val allComments = comments().aggregate()

		val moviesWithComments = movies().aggregate()
			// { $match: { runtime: { $gt: 1000 } } },
			.match { Movie::runtime gt 1000 }
			// {
			//    $lookup: {
			//       from: "comments",
			//       localField: "_id",
			//       foreignField: "movie_id",
			//       as: "movie_comments"
			//    }
			// },
			.lookup(Movie::movie_comments) {
				from(allComments)
				on(Movie::_id, Comment::movieId)
			}
			.toList()

		check(moviesWithComments.first { it._id == movieA }.movie_comments.map { it.text }.sorted() == listOf("A1", "A2"))
		check(moviesWithComments.first { it._id == movieB }.movie_comments.map { it.text } == listOf("B1"))
	}

}
