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

import opensavvy.ktmongo.dsl.aggregation.literal
import opensavvy.ktmongo.dsl.options.WriteConcern
import opensavvy.ktmongo.dsl.query.filter.eq
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.ktmongo.dsl.query.update.set
import opensavvy.ktmongo.dsl.query.update.unset
import opensavvy.prepared.runner.testballoon.preparedSuite

val UpdateWithPipelineTest by preparedSuite {

	class Target(
		val name: String,
		val age: Int,
	)

	test("updateOneWithPipeline") {
		UpdateOneWithPipeline<Target>(testContext()).apply {
			filter.apply {
				Target::name eq "foo"
			}
			update.apply {
				set {
					Target::age set 2
				}
				unset {
					exclude(Target::name)
				}
			}
			options.apply {
				writeConcern(WriteConcern.Majority)
			}
		} shouldBeBson """
			{
				"updates": [
					{
						"q": {
							"name": {"$eq": "foo"}
						},
						"u": [
							{
								"$set": {
									"age": {"$literal": 2}
								}
							},
							{
								"$unset": ["name"]
							}
						],
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

	test("upsertOneWithPipeline") {
		UpsertOneWithPipeline<Target>(testContext()).apply {
			filter.apply {
				Target::name eq "foo"
			}
			update.apply {
				set {
					Target::age set 2
				}
				unset {
					exclude(Target::name)
				}
			}
			options.apply {
				writeConcern(WriteConcern.Majority)
			}
		} shouldBeBson """
			{
				"updates": [
					{
						"q": {
							"name": {"$eq": "foo"}
						},
						"u": [
							{
								"$set": {
									"age": {"$literal": 2}
								}
							},
							{
								"$unset": ["name"]
							}
						],
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

	test("updateManyWithPipeline") {
		UpdateManyWithPipeline<Target>(testContext()).apply {
			filter.apply {
				Target::name eq "foo"
			}
			update.apply {
				set {
					Target::age set 2
				}
				unset {
					exclude(Target::name)
				}
			}
			options.apply {
				writeConcern(WriteConcern.Majority)
			}
		} shouldBeBson """
			{
				"updates": [
					{
						"q": {
							"name": {"$eq": "foo"}
						},
						"u": [
							{
								"$set": {
									"age": {"$literal": 2}
								}
							},
							{
								"$unset": ["name"]
							}
						],
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

}
