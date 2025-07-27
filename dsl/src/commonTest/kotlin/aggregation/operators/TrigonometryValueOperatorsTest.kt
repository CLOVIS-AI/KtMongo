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

val TrigonometryValueOperatorsTest by preparedSuite {
	class Target(
		val a: Double,
		val b: Double,
		val c: Double,
	)

	test($$"$cos") {
		TestPipeline<Target>()
			.set {
				Target::b set cos(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$cos": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$cosh") {
		TestPipeline<Target>()
			.set {
				Target::b set cosh(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$cosh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$acos") {
		TestPipeline<Target>()
			.set {
				Target::c set acos(of(Target::a) + of(Target::b))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"c": {
									"$acos": {
										"$add": [
											"$a",
											"$b"
										]
									}
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$acosh") {
		TestPipeline<Target>()
			.set {
				Target::c set acosh(of(2.0))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"c": {
									"$acosh": {
										"$literal": 2.0
									}
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$sin") {
		TestPipeline<Target>()
			.set {
				Target::b set sin(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$sin": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$sinh") {
		TestPipeline<Target>()
			.set {
				Target::b set sinh(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$sinh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$asin") {
		TestPipeline<Target>()
			.set {
				Target::b set asin(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$asin": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$asinh") {
		TestPipeline<Target>()
			.set {
				Target::b set asinh(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$asinh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$tan") {
		TestPipeline<Target>()
			.set {
				Target::b set tan(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$tan": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$tanh") {
		TestPipeline<Target>()
			.set {
				Target::b set tanh(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$tanh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$atan") {
		TestPipeline<Target>()
			.set {
				Target::b set atan(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$atan": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$atanh") {
		TestPipeline<Target>()
			.set {
				Target::b set atanh(of(Target::c))
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$atanh": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$degreesToRadians") {
		TestPipeline<Target>()
			.set {
				Target::b set of(Target::c).toRadians()
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$degreesToRadians": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

	test($$"$radiansToDegrees") {
		TestPipeline<Target>()
			.set {
				Target::b set of(Target::c).toDegrees()
			}
			.shouldBeBson($$"""
					[
						{
							"$set": {
								"b": {
									"$radiansToDegrees": "$c"
								}
							}
						}
					]
				""".trimIndent())
	}

}
