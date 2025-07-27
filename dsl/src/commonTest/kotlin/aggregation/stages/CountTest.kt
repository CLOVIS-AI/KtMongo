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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val CountTest by preparedSuite {

	class Score(
		val score: Int,
	)

	class Results(
		val passingScores: Int,
	)

	test($$"Nominal $count") {
		TestPipeline<Score>()
			.countTo(Results::passingScores)
			.also {
				@Suppress("UnusedVariable", "unused")
				val foo: Pipeline<Results> = it // Won't compile if the 'countTo' pipeline stops changing the pipeline type
			}
			.shouldBeBson($$"""
				[
					{
						"$count": "passingScores"
					}
				]
			""".trimIndent())
	}

}
