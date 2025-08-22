/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val StringValueOperatorsTest by preparedSuite {

	class Target(
		val text: String,
		val description: String?,
	)

	suite($$"$trim") {
		test("Default whitespace trimming") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trim()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": "$description"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Custom character trimming") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trim(characters = of("ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming with literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of(" \n good bye \t ").trim()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": {
											"$literal": " \n good bye \t "
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming with custom chars and literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of(" ggggoodbyeeeee").trim(characters = of(" ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": {
											"$literal": " ggggoodbyeeeee"
										},
										"chars": {
											"$literal": " ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming with vararg characters") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trim('g', 'e')
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}
}
