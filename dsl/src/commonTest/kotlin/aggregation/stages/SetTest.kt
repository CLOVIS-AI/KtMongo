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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.literal
import opensavvy.ktmongo.dsl.aggregation.set
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.expr.filter.gt
import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.text.Typography.dollar

class SetTest : PreparedSpec({

	class Target(
		val foo: String,
		val deathDate: Int,
		val isAlive: Boolean,
	)

	test("Simple $set") {
		TestPipeline<Target>()
			.set {
				Target::foo set "bar"
				Target::isAlive set (of(Target::deathDate) gt of(18))
			}
			.shouldBeBson("""
				[
					{
						"$set": {
							"foo": {
								"$literal": "bar"
							},
							"isAlive": {
								"$gt": [
									"${dollar}deathDate",
									{
										"$literal": 18
									}
								]
							}
						}
					}
				]
			""".trimIndent())
	}

})
