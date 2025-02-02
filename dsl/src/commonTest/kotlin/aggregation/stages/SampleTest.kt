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

import io.kotest.assertions.throwables.shouldThrow
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.sample
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.kotest.PreparedSpec

class SampleTest : PreparedSpec({

	test(sample) {
		TestPipeline<Nothing>()
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

	test("Sample of 0 elements is forbidden") {
		shouldThrow<IllegalArgumentException> {
			TestPipeline<Nothing>()
				.sample(0)
		}
	}

	test("Sample of less than 0 elements is forbidden") {
		shouldThrow<IllegalArgumentException> {
			TestPipeline<Nothing>()
				.sample(-1)
		}
	}

})
