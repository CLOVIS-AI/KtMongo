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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val ArithmeticValueOperatorsTest by preparedSuite {

	class Target(
		val score: Int,
		val average: Double,
		val name: String,
	)

	suite($$"$abs") {
		test("Usage with a number") {
			TestPipeline<Target>()
				.set {
					Target::average set abs(of(5.2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$abs": {
										"$literal": 5.2
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$add") {
		test("Binary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(Target::score) + of(15))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$add": [
										"$score",
										{
											"$literal": 15
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Binary usage with doubles") {
			TestPipeline<Target>()
				.set {
					Target::average set (of(Target::average) + of(15.0))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$add": [
										"$average",
										{
											"$literal": 15.0
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("N-ary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(1) + of(Target::score) + of(15))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$add": [
										{
											"$literal": 1
										},
										"$score",
										{
											"$literal": 15
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$ceil") {
		test("Usage with a number") {
			TestPipeline<Target>()
				.set {
					Target::average set ceil(of(Target::average))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$ceil": "$average"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$floor") {
		test("Usage with a number") {
			TestPipeline<Target>()
				.set {
					Target::average set floor(of(Target::average))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$floor": "$average"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$multiply") {
		test("Binary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(Target::score) * of(2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$multiply": [
										"$score",
										{
											"$literal": 2
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Binary usage with doubles") {
			TestPipeline<Target>()
				.set {
					Target::average set (of(Target::average) * of(1.5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$multiply": [
										"$average",
										{
											"$literal": 1.5
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("N-ary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(2) * of(Target::score) * of(3))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$multiply": [
										{
											"$literal": 2
										},
										"$score",
										{
											"$literal": 3
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$divide") {
		test("Binary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(Target::score) / of(2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$divide": [
										"$score",
										{
											"$literal": 2
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Binary usage with doubles") {
			TestPipeline<Target>()
				.set {
					Target::average set (of(Target::average) / of(2.5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$divide": [
										"$average",
										{
											"$literal": 2.5
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Literal divided by literal") {
			TestPipeline<Target>()
				.set {
					Target::average set (of(80.0) / of(8.0))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$divide": [
										{
											"$literal": 80.0
										},
										{
											"$literal": 8.0
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$subtract") {
		test("Binary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(Target::score) - of(5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$subtract": [
										"$score",
										{
											"$literal": 5
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Binary usage with doubles") {
			TestPipeline<Target>()
				.set {
					Target::average set (of(Target::average) - of(2.5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"average": {
									"$subtract": [
										"$average",
										{
											"$literal": 2.5
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Literal subtracted from literal") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(100) - of(25))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$subtract": [
										{
											"$literal": 100
										},
										{
											"$literal": 25
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Complex expression with addition and subtraction") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(Target::score) + of(10) - of(3))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"score": {
									"$subtract": [
										{
											"$add": [
												"$score",
												{
													"$literal": 10
												}
											]
										},
										{
											"$literal": 3
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$concat") {
		test("Binary usage") {
			TestPipeline<Target>()
				.set {
					Target::name set (of(Target::name) concat of(" II"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"name": {
									"$concat": [
										"$name",
										{
											"$literal": " II"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("N-ary usage") {
			TestPipeline<Target>()
				.set {
					Target::name set (of("[") concat of(Target::name) concat of("]"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"name": {
									"$concat": [
										{
											"$literal": "["
										},
										"$name",
										{
											"$literal": "]"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

}
