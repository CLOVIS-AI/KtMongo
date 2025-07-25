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

package opensavvy.ktmongo.dsl.query.filter

import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val ComparisonFilterTest by preparedSuite {

	suite("Operator $eq") {
		test("Integer") {
			filter {
				User::age eq 5
			} shouldBeBson """
				{
					"age": {
						"$eq": 5
					}
				}
			""".trimIndent()
		}

		test("Null") {
			filter {
				User::age eq null
			} shouldBeBson """
				{
					"age": {
						"$eq": null
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $ne") {
		test("Integer") {
			filter {
				User::age ne 12
			} shouldBeBson """
				{
					"age": {
						"$ne": 12
					}
				}
			""".trimIndent()
		}

		test("Null") {
			filter {
				User::age ne null
			} shouldBeBson """
				{
					"age": {
						"$ne": null
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $isOneOf") {
		test("With 0 elements") {
			filter {
				User::name.isOneOf()
			} shouldBeBson """
				{
					"name": {
						"$isOneOf": [
						]
					}
				}
			""".trimIndent()
		}

		test("With 1 element") {
			filter {
				User::name.isOneOf("Alfred")
			} shouldBeBson """
				{
					"name": {
						"$isOneOf": [
							"Alfred"
						]
					}
				}
			""".trimIndent()
		}

		test("With multiple elements") {
			filter {
				User::name.isOneOf("Alfred", "Arthur", "Annabelle")
			} shouldBeBson """
				{
					"name": {
						"$isOneOf": [
							"Alfred",
							"Arthur",
							"Annabelle"
						]
					}
				}
			""".trimIndent()
		}
	}

	suite("Operator $isNotOneOf") {
		test("With 0 elements") {
			filter {
				User::name.isNotOneOf()
			} shouldBeBson """
				{
					"name": {
						"$isNotOneOf": [
						]
					}
				}
			""".trimIndent()
		}

		test("With 1 element") {
			filter {
				User::name.isNotOneOf("Alfred")
			} shouldBeBson """
				{
					"name": {
						"$isNotOneOf": [
							"Alfred"
						]
					}
				}
			""".trimIndent()
		}

		test("With multiple elements") {
			filter {
				User::name.isNotOneOf("Alfred", "Arthur", "Annabelle")
			} shouldBeBson """
				{
					"name": {
						"$isNotOneOf": [
							"Alfred",
							"Arthur",
							"Annabelle"
						]
					}
				}
			""".trimIndent()
		}
	}

	suite("Comparison operators") {
		test("int $gt") {
			filter {
				User::age gt 12
			} shouldBeBson """
				{
					"age": {
						"$gt": 12
					}
				}
			""".trimIndent()
		}

		test("int $gte") {
			filter {
				User::age gte 12
			} shouldBeBson """
				{
					"age": {
						"$gte": 12
					}
				}
			""".trimIndent()
		}

		test("int $lt") {
			filter {
				User::age lt 12
			} shouldBeBson """
				{
					"age": {
						"$lt": 12
					}
				}
			""".trimIndent()
		}

		test("int $lte") {
			filter {
				User::age lte 12
			} shouldBeBson """
				{
					"age": {
						"$lte": 12
					}
				}
			""".trimIndent()
		}

		test("int isIn (end inclusive)") {
			filter {
				User::age isIn (12..17)
			} shouldBeBson """
				{
					"$and": [
						{
							"age": {
								"$gte": 12
							}
						},
						{
							"age": {
								"$lte": 17
							}
						}
					]
				}
			""".trimIndent()
		}

		test("int isIn (end exclusive)") {
			filter {
				User::age isIn (12..<17)
				// Kotlin's .. and ..< actually return the same value, so the library sees this as a range 12..16
				// Although the query is slightly different, the behavior is the same
			} shouldBeBson """
				{
					"$and": [
						{
							"age": {
								"$gte": 12
							}
						},
						{
							"age": {
								"$lte": 16
							}
						}
					]
				}
			""".trimIndent()
		}
	}

}
