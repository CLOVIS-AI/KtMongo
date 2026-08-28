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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.aggregation.unsafeNonNull
import opensavvy.ktmongo.dsl.multiContextSuite

val UnwindTest by multiContextSuite {

	class Pet(
		val name: String,
		val age: Int,
	)

	class User(
		val _id: ObjectId,
		val name: String,
		val pets: List<Pet>,
	)

	class UserUnwound(
		val _id: ObjectId,
		val name: String,
		val pet: Pet?,
		val petIndex: Int,
	)

	test("Without projection") {
		TestPipeline<User>()
			.unwind<_, User> {
				array(User::pets)
			}
			.shouldBeBson($$"""
				[
					{
						"$unwind": "$pets"
					}
				]
			""".trimIndent())
	}

	test("Nominal") {
		TestPipeline<User>()
			.unwind<_, UserUnwound> {
				array(User::pets)
				set {
					UserUnwound::pet set it.unsafeNonNull()
				}
			}
			.shouldBeBson($$"""
				[
					{
						"$unwind": "$pets"
					},
					{
						"$set": {
							"pet": "$pets"
						}
					}
				]
			""".trimIndent())
	}

	test("Include array index") {
		TestPipeline<User>()
			.unwind {
				array(User::pets)
				writeArrayIndexTo(UserUnwound::petIndex)
				project {
					UserUnwound::pet set it
					excludeId()
				}
			}
			.shouldBeBson($$"""
				[
					{
						"$unwind": {
							"path": "$pets",
							"includeArrayIndex": "petIndex"
						}
					},
					{
						"$project": {
							"pet": "$pets",
							"_id": false
						}
					}
				]
			""".trimIndent())
	}

	test("Preserve null & empty arrays") {
		TestPipeline<User>()
			.unwind {
				array(User::pets)
				preserveNullAndEmptyArrays()
				set {
					UserUnwound::pet set it
				}
			}
			.shouldBeBson($$"""
				[
					{
						"$unwind": {
							"path": "$pets",
							"preserveNullAndEmptyArrays": true
						}
					},
					{
						"$set": {
							"pet": "$pets"
						}
					}
				]
			""".trimIndent())
	}

}
