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
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val SkipTest by preparedSuite {

	test("Skip 5 elements") {
		TestPipeline<Nothing>()
			.skip(5)
			.shouldBeBson($$"""
				[
					{
						"$skip": 5
					}
				]
			""".trimIndent())
	}

	test("Skip of 0 elements should no-op") {
		TestPipeline<Nothing>()
			.skip(0)
			.shouldBeBson("""
				[
				]
			""".trimIndent())
	}

	test("Skip of less than 0 elements is forbidden") {
		shouldThrow<IllegalArgumentException> {
			TestPipeline<Nothing>()
				.skip(-1)
		}
	}

}
