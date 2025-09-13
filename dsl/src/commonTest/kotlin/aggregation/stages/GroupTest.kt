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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val GroupTest by preparedSuite {

	class Score(
		val score: Int,
	)

	class Results(
		val average: Int,
		val max: Int,
		val total: Int,
	)

	test($$"Simple $group without _id") {
		TestPipeline<Score>()
			.group {
				Results::total sum of(Score::score)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": null,
							"total": {
								"$sum": "$score"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"Simple $group with $avg") {
		TestPipeline<Score>()
			.group {
				Results::total average of(Score::score)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": null,
							"total": {
								"$avg": "$score"
							}
						}
					}
				]
			""".trimIndent())
	}

}
