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

package opensavvy.ktmongo.dsl.aggregation.operators.aggregation.operators

import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.ktmongo.dsl.expr.filter.gt
import opensavvy.prepared.runner.kotest.PreparedSpec

class ArrayValueOperatorsTest : PreparedSpec({

	class Target(
		val numbers: List<Int>,
		val results: List<Any>
	)

	val numbers = "\$numbers"
	val arrayThis = "$\$this"

	suite(filter) {
		test("Usage with a list of integers") {
			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.filter {
							it gt of(3)
						}
				}
				.shouldBeBson("""
					[
						{
							"$set": {
								"results": {
									"$filter": {
										"input": "$numbers",
										"as": "this",
										"cond": {
											"$gt": [
												"$arrayThis",
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
				.shouldBeBson("""
					[
						{
							"$set": {
								"results": {
									"$filter": {
										"input": "$numbers",
										"as": "this",
										"cond": {
											"$gt": [
												"$arrayThis",
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
			val foo = "$\$foo"

			TestPipeline<Target>()
				.set {
					Target::results set Target::numbers
						.filter(variableName = "foo") {
							it gt of(3)
						}
				}
				.shouldBeBson("""
					[
						{
							"$set": {
								"results": {
									"$filter": {
										"input": "$numbers",
										"as": "foo",
										"cond": {
											"$gt": [
												"$foo",
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

})
