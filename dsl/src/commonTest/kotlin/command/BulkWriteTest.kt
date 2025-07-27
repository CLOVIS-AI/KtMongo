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
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.time.Duration.Companion.minutes

val BulkWriteTest by preparedSuite {

	class Target(
		val name: String,
		val age: Int,
		val isAdult: Boolean,
	)

	test("bulkWrite") {
		BulkWrite<Target>(testContext(), {}).apply {
			updateOne(
				filter = {
					Target::name eq "Daniel"
				},
				update = {
					Target::age inc 1
				}
			)

			upsertOne(
				filter = {
					Target::name eq "Fred"
				},
				update = {
					Target::age setOnInsert 15
				}
			)

			updateMany(
				filter = {
					Target::age gte 18
				},
				update = {
					Target::isAdult set true
				}
			)

			options.apply {
				writeConcern(WriteConcern(writeTimeout = 2.minutes))
			}
		} shouldBeBson $$"""
			{
				"bulkWrite": 1,
				"ops": [
					{
						"update": 0,
						"filter": {
							"name": {"$eq": "Daniel"}
						},
						"updateMods": {
							"$inc": {
								"age": 1
							}
						},
						"multi": false
					},
					{
						"update": 0,
						"filter": {
							"name": {"$eq": "Fred"}
						},
						"updateMods": {
							"$setOnInsert": {
								"age": 15
							}
						},
						"upsert": true,
						"multi": false
					},
					{
						"update": 0,
						"filter": {
							"age": {"$gte": 18}
						},
						"updateMods": {
							"$set": {
								"isAdult": true
							}
						},
						"multi": true
					}
				],
				"writeConcern": {
					"wtimeout": 120000
				}
			}
		""".trimIndent()
	}

}
