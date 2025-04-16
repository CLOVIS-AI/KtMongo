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

import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.ktmongo.dsl.query.filter.gt
import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.text.Typography.dollar

class SetTest : PreparedSpec({

	class Target(
		val foo: String,
		val deathDate: Int,
		val isAlive: Boolean,
	)

	test("Simple $set") {
		TestPipeline<Target>()
			.set {
				Target::foo set "bar"
				Target::isAlive set (of(Target::deathDate) gt of(18))
			}
			.shouldBeBson("""
				[
					{
						"$set": {
							"foo": {
								"$literal": "bar"
							},
							"isAlive": {
								"$gt": [
									"${dollar}deathDate",
									{
										"$literal": 18
									}
								]
							}
						}
					}
				]
			""".trimIndent())
	}

	suite("setIf and setUnless") {

		test("Aggregation values") {
			TestPipeline<Target>()
				.set {
					Target::deathDate.setIf(of(Target::isAlive), of(12))
				}
				.shouldBeBson("""
					[
						{
							"$set": {
								"deathDate": {
									"$cond": {
										"if": "${dollar}isAlive",
										"then": {
											"$literal": 12
										},
										"else": "${dollar}deathDate"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Kotlin values") {
			TestPipeline<Target>()
				.set {
					Target::deathDate.setIf(of(Target::isAlive), 12)
				}
				.shouldBeBson("""
					[
						{
							"$set": {
								"deathDate": {
									"$cond": {
										"if": "${dollar}isAlive",
										"then": {
											"$literal": 12
										},
										"else": "${dollar}deathDate"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Boolean condition") {
			TestPipeline<Target>()
				.set {
					Target::deathDate.setIf(true, of(12))
					Target::deathDate.setIf(false, of(13))
				}
				.shouldBeBson("""
					[
						{
							"$set": {
								"deathDate": {
									"$literal": 12
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Boolean condition and Kotlin values") {
			TestPipeline<Target>()
				.set {
					Target::deathDate.setIf(true, 12)
					Target::deathDate.setIf(false, 13)
				}
				.shouldBeBson("""
					[
						{
							"$set": {
								"deathDate": {
									"$literal": 12
								}
							}
						}
					]
				""".trimIndent())
		}

	}

})
