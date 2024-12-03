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

import opensavvy.ktmongo.dsl.aggregation.PipelineType
import opensavvy.ktmongo.dsl.aggregation.aggregate
import opensavvy.ktmongo.dsl.aggregation.sample
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.kotest.PreparedSpec

class SampleTest : PreparedSpec({

	test(sample) {
		aggregate<_, Nothing>(PipelineType.Aggregate)
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
