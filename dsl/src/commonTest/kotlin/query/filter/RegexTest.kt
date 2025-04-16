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

package opensavvy.ktmongo.dsl.query.filter

import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.prepared.runner.kotest.PreparedSpec

class RegexTest : PreparedSpec({
	suite("Regex") {
		test("Basic usage") {
			filter {
				User::name.regex("foo.*")
			} shouldBeBson """
				{
					"name": {
						"$regex": {
							"$regularExpression": {
								"pattern": "foo.*",
								"options": ""
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Case insensitive") {
			filter {
				User::name.regex("^acme", caseInsensitive = true)
			} shouldBeBson """
				{
					"name": {
						"$regex": {
							"$regularExpression": {
								"pattern": "^acme",
								"options": "i"
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Case insensitive grouping") {
			filter {
				User::name.regex("(?i)a(?-i)cme")
			} shouldBeBson """
				{
					"name": {
						"$regex": {
							"$regularExpression": {
								"pattern": "(?i)a(?-i)cme",
								"options": ""
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Similar to SQL LIKE") {
			filter {
				User::name.regex("789$")
			} shouldBeBson """
				{
					"name": {
						"$regex": {
							"$regularExpression": {
								"pattern": "789$",
								"options": ""
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Multiline match") {
			filter {
				User::name.regex("^S", matchEachLine = true)
			} shouldBeBson """
				{
					"name": {
						"$regex": {
							"$regularExpression": {
								"pattern": "^S",
								"options": "m"
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Pattern with comments") {
			filter {
				User::name.regex(
					pattern = """
						abc # category code
						123 # item number
					""".trimIndent(),
					extended = true,
				)
			} shouldBeBson """
				{
					"name": {
						"$regex": {
							"$regularExpression": {
								"pattern": "abc # category code\n123 # item number",
								"options": "x"
							}
						}
					}
				}
			""".trimIndent()
		}
	}
})
