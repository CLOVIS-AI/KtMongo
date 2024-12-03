/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.stages.match
import opensavvy.ktmongo.dsl.aggregation.stages.sample
import opensavvy.ktmongo.dsl.expr.filter.eq
import opensavvy.ktmongo.dsl.expr.shouldBeBson
import opensavvy.ktmongo.dsl.expr.testContext
import opensavvy.prepared.runner.kotest.PreparedSpec

val match = "\$match"
val sample = "\$sample"
val set = "\$set"

@OptIn(LowLevelApi::class)
class AggregationStageTest : PreparedSpec({

	class Target(
		val foo: String,
		val bar: Int,
	)

	fun <Type : PipelineType> aggregate(type: Type) =
		Pipeline<Type, Target>(testContext(), type)

	infix fun Pipeline<*, *>.shouldBeBson(expected: String) {
		this.toString() shouldBeBson expected
	}

	test("Empty pipeline") {
		aggregate(PipelineType.Aggregate) shouldBeBson "[]"
	}

	test("Single-stage pipeline") {
		aggregate(PipelineType.Aggregate)
			.match { Target::foo eq "Bob" }
			.shouldBeBson("""
				[
					{
						"$match": {
							"foo": {
								"$eq": "Bob"
							}
						}
					}
				]
			""".trimIndent())
	}

	test(sample) {
		aggregate(PipelineType.Aggregate)
			.sample(5)
			.shouldBeBson("""
				[
					{
						"$sample": {
							"size": 5
						}
					}
				]
			""".trimIndent())
	}

})
