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

package opensavvy.ktmongo.dsl.aggregation.stages

import io.kotest.assertions.throwables.shouldThrow
import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.prepared.runner.kotest.PreparedSpec

class LimitTest : PreparedSpec({

	test("Nominal $limit") {
		aggregate<_, Nothing>(PipelineType.Aggregate)
			.limit(5)
			.shouldBeBson("""
				[
					{
						"$limit": 5
					}
				]
			""".trimIndent())
	}

	test("Limit of 0 is kept") {
		aggregate<_, Nothing>(PipelineType.Aggregate)
			.limit(0)
			.shouldBeBson("""
				[
					{
						"$limit": 0
					}
				]
			""".trimIndent())
	}

	test("Limit less than 0 is forbidden") {
		shouldThrow<IllegalArgumentException> {
			aggregate<_, Nothing>(PipelineType.Aggregate)
				.limit(-1)
		}
	}

})
