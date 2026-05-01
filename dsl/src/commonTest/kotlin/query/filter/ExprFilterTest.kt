/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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
import opensavvy.prepared.runner.testballoon.preparedSuite

val ExprFilterTest by preparedSuite {
	test("Reading a field") {
		filter {
			expr {
				of(User::isAlive)
			}
		} shouldBeBson $$"""
			{
				"$expr": "$isAlive"
			}
		""".trimIndent()
	}

	test("Hardcoded value") {
		filter {
			expr {
				of(true)
			}
		} shouldBeBson $$"""
			{
				"$expr": {
					"$literal": true
				}
			}
		""".trimIndent()
	}

	test("Comparing two fields") {
		filter {
			expr {
				User::grades[0] ne User::grades[1]
			}
		} shouldBeBson $$"""
			{
				"$expr": {
					"$ne": [
						"$grades.0",
						"$grades.1"
					]
				}
			}
		""".trimIndent()
	}

	test("Comparing a field and a hardcoded value") {
		filter {
			expr {
				User::grades[0] ne 12
			}
		} shouldBeBson $$"""
			{
				"$expr": {
					"$ne": [
						"$grades.0",
						{
							"$literal": 12
						}
					]
				}
			}
		""".trimIndent()
	}

	test("Comparing a hardcoded value and a field") {
		filter {
			expr {
				12 ne User::grades[0]
			}
		} shouldBeBson $$"""
			{
				"$expr": {
					"$ne": [
						{
							"$literal": 12
						},
						"$grades.0"
					]
				}
			}
		""".trimIndent()
	}
}
