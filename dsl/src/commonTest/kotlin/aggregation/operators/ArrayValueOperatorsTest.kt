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
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val ArrayValueOperatorsTest by preparedSuite {

	class User(
		val name: String,
		val age: Int,
	)

	class Target(
		val numbers: List<Int>,
		val users: List<User>,
		val results: List<Any>
	)

	suite($$"$filter") {
		test("Usage with a list of integers") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.filter {
							it gt of(3)
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$filter": {
										"input": "$numbers",
										"as": "this",
										"cond": {
											"$gt": [
												"$$this",
												{"$literal": 3}
											]
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Usage with a limit") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.filter(limit = of(4)) {
							it gt of(3)
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$filter": {
										"input": "$numbers",
										"as": "this",
										"cond": {
											"$gt": [
												"$$this",
												{"$literal": 3}
											]
										},
										"limit": {"$literal": 4}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Usage with another variable name") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.filter(variableName = "foo") {
							it gt of(3)
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$filter": {
										"input": "$numbers",
										"as": "foo",
										"cond": {
											"$gt": [
												"$$foo",
												{"$literal": 3}
											]
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$map") {
		test("Usage with a list of integers") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.map {
							it + of(4)
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$map": {
										"input": "$numbers",
										"as": "this",
										"in": {
											"$add": [
												"$$this",
												{"$literal": 4}
											]
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Usage with a variable name") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.map(variableName = "foo") {
							it + of(4)
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$map": {
										"input": "$numbers",
										"as": "foo",
										"in": {
											"$add": [
												"$$foo",
												{"$literal": 4}
											]
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Usage with a type conversion") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.map {
							it eq of(4)
						}
						.also {
							@Suppress("UnusedVariable", "unused")
							val foo: Value<Target, List<Boolean>> = it // Ensure that the type doesn't change
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$map": {
										"input": "$numbers",
										"as": "this",
										"in": {
											"$eq": [
												"$$this",
												{"$literal": 4}
											]
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Usage with a child field") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::users
						.map {
							it / User::name
						}
						.also {
							@Suppress("UnusedVariable", "unused")
							val foo: Value<Target, List<String>> = it // Ensure that the type doesn't change
						}
				}
				.shouldBeBson($$$"""
					[
						{
							"$set": {
								"results": {
									"$map": {
										"input": "$users",
										"as": "this",
										"in": {
											"$getField": {
												"input": "$$this",
												"field": "name"
											}
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$firstN") {
		test("Usage with a list of integers") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.take(of(5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"results": {
									"$firstN": {
										"input": "$numbers",
										"n": {"$literal": 5}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$lastN") {
		test("Usage with a list of integers") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.takeLast(of(5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"results": {
									"$lastN": {
										"input": "$numbers",
										"n": {"$literal": 5}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$sortArray") {
		test("Sort by field") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::users
						.sortedBy {
							ascending(User::name)
							descending(User::age)
						}
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"results": {
									"$sortArray": {
										"input": "$users",
										"sortBy": {
											"name": 1,
											"age": -1
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Sort by elements (ascending)") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.sorted()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"results": {
									"$sortArray": {
										"input": "$numbers",
										"sortBy": 1
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Sort by elements (descending)") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.sortedDescending()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"results": {
									"$sortArray": {
										"input": "$numbers",
										"sortBy": -1
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

}
