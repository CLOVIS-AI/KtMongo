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
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.aggregation.sort
import opensavvy.prepared.runner.testballoon.preparedSuite

val SortTest by preparedSuite {

	class Target(
		val _id: String,
		val name: String,
		val age: Int,
	)

	test("Sort by ID (ascending)") {
		TestPipeline<Target>()
			.sort {
				ascending(Target::_id)
			}
			.shouldBeBson("""
				[
					{
						"${sort}": {
							"_id": 1
						}
					}
				]
			""".trimIndent())
	}

	test("Sort by ID (ascending)") {
		TestPipeline<Target>()
			.sort {
				descending(Target::_id)
			}
			.shouldBeBson("""
				[
					{
						"${sort}": {
							"_id": -1
						}
					}
				]
			""".trimIndent())
	}

	test("Sort by multiple fields") {
		TestPipeline<Target>()
			.sort {
				ascending(Target::name)
				descending(Target::_id)
				ascending(Target::age)
			}
			.shouldBeBson("""
				[
					{
						"${sort}": {
							"name": 1,
							"_id": -1,
							"age": 1
						}
					}
				]
			""".trimIndent())
	}

	test("Empty sort is simplified away") {
		TestPipeline<Target>()
			.sort {}
			.shouldBeBson("""
				[
				]
			""".trimIndent())
	}

}
