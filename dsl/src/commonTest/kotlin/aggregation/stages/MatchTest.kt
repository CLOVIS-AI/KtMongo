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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.match
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.query.filter.eq
import opensavvy.prepared.runner.testballoon.preparedSuite

val MatchTest by preparedSuite {

	class Target(
		val foo: String,
	)

	test("Simple $match") {
		TestPipeline<Target>()
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

}
