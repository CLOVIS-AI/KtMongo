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

package opensavvy.ktmongo.dsl.command

import opensavvy.ktmongo.dsl.query.filter.eq
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.time.Duration.Companion.minutes

class FindTest : PreparedSpec({

	class Target(
		val user: String,
	)

	test("find") {
		Find<Target>(testContext()).apply {
			filter.apply {
				Target::user eq "foo"
			}
			options.apply {
				skip(2)
				maxTime(2.minutes)
				sort {
					descending(Target::user)
				}
			}
		} shouldBeBson """
			{
				"filter": {
					"user": {"$eq": "foo"}
				},
				"skip": 2,
				"maxTimeMS": 120000,
				"sort": {
					"user": -1
				}
			}
		""".trimIndent()
	}

})
