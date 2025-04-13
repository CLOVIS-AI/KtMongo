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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.CountOptions
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.kotest.PreparedSpec

@LowLevelApi
class OptionTest : PreparedSpec({

	test("Validate the system") {
		val countOptions = CountOptions<String>(testContext())
		countOptions.toString() shouldBeBson "{}"

		countOptions.limit(99)
		countOptions.toString() shouldBeBson "{\"limit\": 99}"

		check(countOptions.option<LimitOption, _>() == 99L)
	}

})
