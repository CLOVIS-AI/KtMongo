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

package opensavvy.ktmongo.dsl.command

import opensavvy.ktmongo.dsl.options.WriteConcern
import opensavvy.ktmongo.dsl.query.filter.eq
import opensavvy.ktmongo.dsl.query.filter.gt
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.ktmongo.dsl.query.update.inc
import opensavvy.ktmongo.dsl.query.update.setOnInsert
import opensavvy.prepared.runner.kotest.PreparedSpec

class UpdateTest : PreparedSpec({

	class Target(
		val user: String,
		val age: Int,
	)

	test("updateOne") {
		UpdateOne<Target>(testContext()).apply {
			filter.apply {
				Target::user eq "foo"
			}
			update.apply {
				Target::age inc 1
			}
			options.apply {
				writeConcern(WriteConcern.Majority)
			}
		} shouldBeBson """
			{
				"updates": [
					{
						"q": {
							"user": {"$eq": "foo"}
						},
						"u": {
							"$inc": {
								"age": 1
							}
						},
						"upsert": false,
						"multi": false
					}
				],
				"writeConcern": {
					"w": "majority",
					"j": true
				}
			}
		""".trimIndent()
	}

	test("upsertOne") {
		UpsertOne<Target>(testContext()).apply {
			filter.apply {
				Target::user eq "foo"
			}
			update.apply {
				Target::age setOnInsert 3
				Target::age inc 1
			}
			options.apply {
				writeConcern(WriteConcern.Majority)
			}
		} shouldBeBson """
			{
				"updates": [
					{
						"q": {
							"user": {"$eq": "foo"}
						},
						"u": {
							"$setOnInsert": {
								"age": 3
							},
							"$inc": {
								"age": 1
							}
						},
						"upsert": true,
						"multi": false
					}
				],
				"writeConcern": {
					"w": "majority",
					"j": true
				}
			}
		""".trimIndent()
	}

	test("updateMany") {
		UpdateMany<Target>(testContext()).apply {
			filter.apply {
				Target::age gt 5
			}
			update.apply {
				Target::age inc 1
			}
			options.apply {
				writeConcern(WriteConcern.Majority)
			}
		} shouldBeBson """
			{
				"updates": [
					{
						"q": {
							"age": {"$gt": 5}
						},
						"u": {
							"$inc": {
								"age": 1
							}
						},
						"upsert": false,
						"multi": true
					}
				],
				"writeConcern": {
					"w": "majority",
					"j": true
				}
			}
		""".trimIndent()
	}

})
