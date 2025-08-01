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
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val UnsetTest by preparedSuite {

	class Target(
		val _id: String,
		val name: String,
		val age: Int,
	)

	test("Exclude a field") {
		TestPipeline<Target>()
			.unset {
				exclude(Target::name)
			}
			.shouldBeBson($$"""
				[
					{
						"$unset": [
							"name"
						]
					}
				]
			""".trimIndent())
	}

	test("Exclude a field's ID") {
		TestPipeline<Target>()
			.unset {
				exclude(Target::_id)
			}
			.shouldBeBson($$"""
				[
					{
						"$unset": [
							"_id"
						]
					}
				]
			""".trimIndent())
	}

	test("Exclude multiple fields") {
		TestPipeline<Target>()
			.unset {
				exclude(Target::_id)
				exclude(Target::age)
			}
			.shouldBeBson($$"""
				[
					{
						"$unset": [
							"_id",
							"age"
						]
					}
				]
			""".trimIndent())
	}

	test("Excluding 0 fields should no-op") {
		TestPipeline<Nothing>()
			.unset {}
			.shouldBeBson("""
				[
				]
			""".trimIndent())
	}

}
