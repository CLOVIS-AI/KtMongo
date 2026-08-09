/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.multiContextSuite
import kotlin.time.Instant

val DateTimeValueOperatorsTest by multiContextSuite {

	class Target(
		val date: Instant,
		val year: Int,
		val week: Int,
		val second: Int,
		val month: Int,
		val minute: Int,
		val millisecond: Int,
		val weekIso: Int,
		val yearOfIsoWeek: Int,
		val dayOfWeekIso: Int,
		val hour: Int,
		val dayOfMonth: Int,
	)

	test($$"$year") {
		TestPipeline<Target>()
			.set {
				Target::year set Target::date.year
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"year": {
								"$year": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$week") {
		TestPipeline<Target>()
			.set {
				Target::week set Target::date.week
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"week": {
								"$week": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$second") {
		TestPipeline<Target>()
			.set {
				Target::second set Target::date.second
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"second": {
								"$second": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$month") {
		TestPipeline<Target>()
			.set {
				Target::month set Target::date.month
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"month": {
								"$month": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$minute") {
		TestPipeline<Target>()
			.set {
				Target::minute set Target::date.minute
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"minute": {
								"$minute": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$millisecond") {
		TestPipeline<Target>()
			.set {
				Target::millisecond set Target::date.millisecond
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"millisecond": {
								"$millisecond": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$isoWeek") {
		TestPipeline<Target>()
			.set {
				Target::weekIso set Target::date.weekIso
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"weekIso": {
								"$isoWeek": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$isoWeekYear") {
		TestPipeline<Target>()
			.set {
				Target::yearOfIsoWeek set Target::date.yearOfIsoWeek
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"yearOfIsoWeek": {
								"$isoWeekYear": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$isoDayOfWeek") {
		TestPipeline<Target>()
			.set {
				Target::dayOfWeekIso set Target::date.dayOfWeekIso
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"dayOfWeekIso": {
								"$isoDayOfWeek": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$hour") {
		TestPipeline<Target>()
			.set {
				Target::hour set Target::date.hour
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"hour": {
								"$hour": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

	test($$"$dayOfMonth") {
		TestPipeline<Target>()
			.set {
				Target::dayOfMonth set Target::date.dayOfMonth
			}
			.shouldBeBson($$"""
				[
					{
						"$set": {
							"dayOfMonth": {
								"$dayOfMonth": "$date"
							}
						}
					}
				]
			""".trimIndent())
	}

}
