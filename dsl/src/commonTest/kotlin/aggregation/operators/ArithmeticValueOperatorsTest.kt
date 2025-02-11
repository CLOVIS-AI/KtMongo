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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.prepared.runner.kotest.PreparedSpec

class ArithmeticValueOperatorsTest : PreparedSpec({

	class Target(
		val score: Int,
		val average: Double,
	)

	val score = "\$score"
	val average = "\$average"

	suite(abs) {
		test("Usage with a number") {
			TestPipeline<Target>()
				.set {
					Target::average set abs(of(5.2))
				}
				.shouldBeBson("""
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

	suite(add) {
		test("Binary usage") {
			TestPipeline<Target>()
				.set {
					Target::score set (of(Target::score) + of(15))
				}
				.shouldBeBson("""
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
				.shouldBeBson("""
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
				.shouldBeBson("""
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

})
