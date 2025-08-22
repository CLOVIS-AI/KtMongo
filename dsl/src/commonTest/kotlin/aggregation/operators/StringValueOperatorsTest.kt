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

	suite($$"$ltrim") {
		test("Default whitespace trimming from start") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimStart()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$ltrim": {
										"input": "$description"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Custom character trimming from start") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimStart(characters = of("ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$ltrim": {
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

		test("Trimming from start with vararg characters") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimStart('g', 'e')
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$ltrim": {
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

	suite($$"$rtrim") {
		test("Default whitespace trimming from end") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimEnd()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$rtrim": {
										"input": "$description"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Custom character trimming from end") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimEnd(characters = of("ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$rtrim": {
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

		test("Trimming from end with vararg characters") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimEnd('g', 'e')
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$rtrim": {
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

	suite($$"$toLower") {
		test("Convert string to lowercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).lowercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toLower": "$description"
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert literal string to lowercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of("PRODUCT 1").lowercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toLower": {
										"$literal": "PRODUCT 1"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert field reference to lowercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::text).lowercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toLower": "$text"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$toUpper") {
		test("Convert string to uppercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).uppercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toUpper": "$description"
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert literal string to uppercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of("product 1").uppercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toUpper": {
										"$literal": "product 1"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert field reference to uppercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::text).uppercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toUpper": "$text"
								}
							}
						}
					]
				""".trimIndent())
		}
	}
}
