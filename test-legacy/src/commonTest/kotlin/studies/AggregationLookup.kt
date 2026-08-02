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
	val reboot_of: RebootInformation? = null,
	val post_release_comments: List<Comment> = emptyList(),
)

@Serializable
data class RebootInformation(
	val movie_id: ObjectId? = null,
	val movie_info: List<Movie> = emptyList(),
)

@Serializable
data class Comment(
	val _id: ObjectId,
	val movieId: ObjectId,
	val name: String,
	val text: String,
	val date: Instant,
	val year: Int = 0,
)

@Serializable
data class SchoolClass(
	val _id: ObjectId,
	val title: String,
	val students: List<ObjectId>,
	val studentsData: List<SchoolStudent> = emptyList(),
)

@Serializable
data class SchoolStudent(
	val _id: ObjectId,
	val name: String,
	val school: Int,
	val age: Int,
)

val AggregationLookup by preparedSuite {

	val movies by testCollection<Movie>("case-lookup-movies")
	val comments by testCollection<Comment>("case-lookup-comments")
	val classes by testCollection<SchoolClass>("case-lookup-classes")
	val students by testCollection<SchoolStudent>("case-lookup-students")

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
			.lookup {
				into(Movie::movie_comments)
				from(allComments)
				on(Movie::_id, Comment::movieId)
			}
			.toList()

		check(moviesWithComments.first { it._id == movieA }.movie_comments.map { it.text }.sorted() == listOf("A1", "A2"))
		check(moviesWithComments.first { it._id == movieB }.movie_comments.map { it.text } == listOf("B1"))
	}


	test($$"Use $lookup with an Array") {
		// Ported from: https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#use--lookup-with-an-array

		val hermine = students().newId()
		val malcom = students().newId()
		val sherlock = students().newId()
		val madame = students().newId()

		students().insertMany(
			SchoolStudent(_id = hermine, name = "Hermine Gin", school = 1, age = 18),
			SchoolStudent(_id = malcom, name = "Malcom Fern", school = 2, age = 17),
			SchoolStudent(_id = sherlock, name = "Sherlock Sym", school = 2, age = 17),
			SchoolStudent(_id = madame, name = "Madame Rig", school = 1, age = 17),
		)

		val reading = classes().newId()
		val writing = classes().newId()

		classes().insertMany(
			SchoolClass(_id = reading, title = "Reading is ...", students = listOf(hermine, malcom, sherlock)),
			SchoolClass(_id = writing, title = "But Writing ...", students = listOf(sherlock, madame)),
		)

		val allStudents = students().aggregate()

		val result = classes().aggregate()
			.lookup {
				into(SchoolClass::studentsData)
				from(allStudents)
				onEach(SchoolClass::students, SchoolStudent::_id)
			}
			.toList()

		val readingClass = result.first { it._id == reading }
		check(readingClass.studentsData.map { it.name }.sorted() == listOf("Hermine Gin", "Malcom Fern", "Sherlock Sym"))

		val writingClass = result.first { it._id == writing }
		check(writingClass.studentsData.map { it.name }.sorted() == listOf("Madame Rig", "Sherlock Sym"))
	}

	test($$"$lookup into a nested document") {
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
				reboot_of = RebootInformation(movieA),
			)
		)

		val allMovies = movies().aggregate()

		val moviesWithSequel = movies().aggregate()
			.lookup {
				into(Movie::reboot_of / RebootInformation::movie_info)
				from(allMovies)
				on(Movie::reboot_of / RebootInformation::movie_id, Movie::_id)
			}
			.toList()

		check(moviesWithSequel.first { it._id == movieA }.reboot_of?.movie_id == null)
		check(moviesWithSequel.first { it._id == movieB }.reboot_of?.movie_info?.firstOrNull()?.title == "A")
	}

	test($$"Use Multiple Join Conditions and a Correlated Subquery") {
		// Ported from: https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#use-multiple-join-conditions-and-a-correlated-subquery

		val classAction = movies().newId()
		val kafka = movies().newId()
		val corpseBride = movies().newId()
		val otherMovie = movies().newId()

		movies().insertMany(
			Movie(_id = classAction, runtime = 100, title = "Class Action", year = 1991),
			Movie(_id = kafka, runtime = 100, title = "Kafka", year = 1991),
			Movie(_id = corpseBride, runtime = 100, title = "Corpse Bride", year = 2005),
			Movie(_id = otherMovie, runtime = 100, title = "Other Movie", year = 1980),
		)

		comments().insertMany(
			// pre-release comment for Class Action -> must be excluded
			Comment(_id = comments().newId(), movieId = classAction, name = "Bob", text = "...", date = Instant.parse("1990-01-01T00:00:00Z"), year = 1990),
			// post-release comment for Class Action -> must be included
			Comment(_id = comments().newId(), movieId = classAction, name = "Khal Drogo", text = "...", date = Instant.parse("2016-12-06T07:17:03Z"), year = 2016),
			// post-release comment for Kafka -> must be included
			Comment(_id = comments().newId(), movieId = kafka, name = "Khal Drogo", text = "...", date = Instant.parse("1998-05-10T03:10:20Z"), year = 1998),
			// pre-release comment for Corpse Bride -> must be excluded (empty result)
			Comment(_id = comments().newId(), movieId = corpseBride, name = "Ygritte", text = "...", date = Instant.parse("2004-01-01T00:00:00Z"), year = 2004),
		)

		val allComments = comments().aggregate()

		val result = movies().aggregate()
			// { $match: { title: { $in: [ "Class Action", "Kafka", "Corpse Bride" ] } } },
			.match { Movie::title isOneOf listOf("Class Action", "Kafka", "Corpse Bride") }
			// {
			//    $lookup: {
			//       from: "comments",
			//       localField: "_id",
			//       foreignField: "movie_id",
			//       let: { movie_year: "$year" },
			//       pipeline: [
			//          { $match: { $expr: { $gt: [ { $year: "$date" }, "$$movie_year" ] } } },
			//       ],
			//       as: "post_release_comments"
			//    }
			// },
			.lookup {
				into(Movie::post_release_comments)

				val movieYear = let(Movie::year)

				from(
					allComments
						.matchExpr { Comment::year gt movieYear }
				)

				on(Movie::_id, Comment::movieId)
			}
			.toList()

		check(result.none { it._id == otherMovie })

		check(result.first { it._id == classAction }.post_release_comments.map { it.name } == listOf("Khal Drogo"))
		check(result.first { it._id == kafka }.post_release_comments.map { it.name } == listOf("Khal Drogo"))
		check(result.first { it._id == corpseBride }.post_release_comments.isEmpty())
	}
}
