/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.prepared.suite.assertions.checkThrows

@Serializable
enum class AgeRange {
	Child,
	Adult,
	Elder,
}

val GroupTest by multiContextSuite {

	class Score(
		val topic: String,
		val score: Int,
	)

	class Results(
		val _id: String,
		val topic: String,
		val average: Int,
		val max: Int,
		val total: Int,
		val totals: List<Double>,
	)

	test($$"Simple $group without _id") {
		TestPipeline<Score>()
			.group {
				Results::total sum of(Score::score)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": null,
							"total": {
								"$sum": "$score"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"Simple $group with $avg") {
		TestPipeline<Score>()
			.group {
				Results::total average of(Score::score)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": null,
							"total": {
								"$avg": "$score"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"Simple $group with $median") {
		TestPipeline<Score>()
			.group {
				Results::total median of(Score::score)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": null,
							"total": {
								"$median": {
									"input": "$score",
									"method": "approximate"
								}
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"Simple $group with $percentile") {
		TestPipeline<Score>()
			.group {
				Results::totals.percentiles(of(Score::score), 0.5, 0.99)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": null,
							"totals": {
								"$percentile": {
									"input": "$score",
									"method": "approximate",
									"p": [
										0.5,
										0.99
									]
								}
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"Simple $group with _id") {
		TestPipeline<Score>()
			.group {
				Results::_id set Score::topic
				Results::total sum of(Score::score)
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<Results> = it // Won't compile if 'group' stops changing the type automatically to Results
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": "$topic",
							"total": {
								"$sum": "$score"
							}
						}
					}
				]
			""".trimIndent())
	}

	test("Cannot set a non-_id field") {
		checkThrows<IllegalArgumentException> {
			TestPipeline<Score>()
				.group {
					Results::total set Score::topic
				}
		}
	}

	class NestedGroup(
		val _id: Results,
	)

	test($$"$group with a nested _id field") {
		TestPipeline<Score>()
			.group {
				NestedGroup::_id / Results::topic set Score::topic
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<NestedGroup> = it // Won't compile if 'group' stops changing the type automatically
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": {
								"topic": "$topic"
							}
						}
					}
				]
			""".trimIndent())
	}

	class CompoundGroupId(
		val topic: String,
		val score: Int,
	)

	class CompoundGroup(
		val _id: CompoundGroupId,
		val average: Double,
	)

	test($$"$group with a nested _id field") {
		TestPipeline<Score>()
			.group {
				CompoundGroup::_id / CompoundGroupId::topic set Score::topic
				CompoundGroup::_id / CompoundGroupId::score set ((of(Score::score) / 10).toInt()) * 10
				CompoundGroup::average average Score::score
			}
			.also {
				@Suppress("unused")
				val foo: Pipeline<CompoundGroup> = it // Won't compile if 'group' stops changing the type automatically
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": {
								"topic": "$topic",
								"score": {
									"$multiply": [
										{
											"$toInt": {
												"$divide": [
													"$score",
													{
														"$literal": 10
													}
												]
											}
										},
										{
											"$literal": 10
										}
									]
								}
							},
							"average": {
								"$avg": "$score"
							}
						}
					}
				]
			""".trimIndent())
	}

	class User(
		val _id: ObjectId,
		val name: String,
		val age: Int,
		val city: String,
	)

	class AgePerCityAndRangeId(
		val city: String,
		val ageRange: AgeRange,
	)

	class AgePerCityAndRange(
		val _id: AgePerCityAndRangeId,
		val averageAge: Double,
		val medianAge: Int,
	)

	test($$"$group by city and age range with switch, average and median") {
		TestPipeline<User>()
			.group {
				AgePerCityAndRange::_id / AgePerCityAndRangeId::city set User::city
				AgePerCityAndRange::_id / AgePerCityAndRangeId::ageRange set switch(
					User::age lt 18 then AgeRange.Child,
					User::age gte 65 then AgeRange.Elder,
					default = AgeRange.Adult,
				)
				AgePerCityAndRange::averageAge average User::age
				AgePerCityAndRange::medianAge median User::age
			}
			.shouldBeBson($$"""
				[
					{
						"$group": {
							"_id": {
								"city": "$city",
								"ageRange": {
									"$switch": {
										"branches": [
											{
												"case": {
													"$lt": [
														"$age",
														{
															"$literal": 18
														}
													]
												},
												"then": {
													"$literal": "Child"
												}
											},
											{
												"case": {
													"$gte": [
														"$age",
														{
															"$literal": 65
														}
													]
												},
												"then": {
													"$literal": "Elder"
												}
											}
										],
										"default": {
											"$literal": "Adult"
										}
									}
								}
							},
							"averageAge": {
								"$avg": "$age"
							},
							"medianAge": {
								"$median": {
									"input": "$age",
									"method": "approximate"
								}
							}
						}
					}
				]
			""".trimIndent())
	}
}
