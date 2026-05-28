/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.UpdateOptions
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.testContext

@OptIn(DangerousMongoApi::class, LowLevelApi::class)
val ArrayFiltersTest by multiContextSuite {

	class Target(
		val name: String,
		val age: Int,
	)

	test("Direct filter") {
		val options = UpdateOptions<Unit>(testContext())

		options.arrayFilter("test") {
			it eq 12.0
		}

		options.toString() shouldBeBson $$"""
			{
				"arrayFilters": [
					{
						"test": {
							"$eq": 12.0
						}
					}
				]
			}
		""".trimIndent()
	}

	test("Filter on field") {
		val options = UpdateOptions<Unit>(testContext())

		options.arrayFilter<Target>("test") {
			it / Target::age gte 18
		}

		options.toString() shouldBeBson $$"""
			{
				"arrayFilters": [
					{
						"test.age": {
							"$gte": 18
						}
					}
				]
			}
		""".trimIndent()
	}

	test("Filter on multiple fields") {
		val options = UpdateOptions<Unit>(testContext())

		options.arrayFilter<Target>("test") {
			it / Target::name eq "John"
			it / Target::age gte 18
		}

		options.toString() shouldBeBson $$"""
			{
				"arrayFilters": [
					{
						"$and": [
							{
								"test.name": {
									"$eq": "John"
								}
							},
							{
								"test.age": {
									"$gte": 18
								}
							}
						]
					}
				]
			}
		""".trimIndent()
	}

	test("Two different filters") {
		val options = UpdateOptions<Unit>(testContext())

		options.arrayFilter<Target>("a") {
			it / Target::name eq "Damien"
		}

		options.arrayFilter("b") {
			it eq 42
		}

		options.toString() shouldBeBson $$"""
			{
				"arrayFilters": [
					{
						"a.name": {
							"$eq": "Damien"
						}
					},
					{
						"b": {
							"$eq": 42
						}
					}
				]
			}
		""".trimIndent()
	}

	test("Logical or") {
		val options = UpdateOptions<Unit>(testContext())

		options.arrayFilter<Target>("a") {
			or {
				it / Target::name eq "John"
				it / Target::age gte 18
			}
		}
	}
}
