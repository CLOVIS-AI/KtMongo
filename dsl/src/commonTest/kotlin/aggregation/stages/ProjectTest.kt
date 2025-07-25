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

import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.literal
import opensavvy.ktmongo.dsl.aggregation.project
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val ProjectTest by preparedSuite {

	class Target(
		val _id: String,
		val name: String,
		val age: Int,
	)

	test("Include a field") {
		TestPipeline<Target>()
			.project {
				include(Target::name)
			}
			.shouldBeBson("""
				[
					{
						"$project": {
							"name": 1
						}
					}
				]
			""".trimIndent())
	}

	test("Exclude the _id field") {
		TestPipeline<Target>()
			.project {
				excludeId()
			}
			.shouldBeBson("""
				[
					{
						"$project": {
							"_id": 0
						}
					}
				]
			""".trimIndent())
	}

	test("Set a field") {
		TestPipeline<Target>()
			.project {
				excludeId()
				include(Target::name)
				Target::age set 12
			}
			.shouldBeBson("""
				[
					{
						"$project": {
							"_id": 0,
							"name": 1,
							"age": {
								"$literal": 12
							}
						}
					}
				]
			""".trimIndent())
	}

}
