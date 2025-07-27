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

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite

@OptIn(LowLevelApi::class)
val PredicateExpressionTest by preparedSuite {

	@KtMongoDsl
	fun <T> predicate(block: FilterQueryPredicate<T>.() -> Unit): String =
		FilterQueryPredicate<T>(testContext())
			.apply(block)
			.toBson()
			.toString()

	suite($$"Operator $eq") {
		test("Integer") {
			predicate {
				eq(4)
			} shouldBeBson $$"""
				{
					"$eq": 4
				}
			""".trimIndent()
		}

		test("String") {
			predicate {
				eq("foo")
			} shouldBeBson $$"""
				{
					"$eq": "foo"
				}
			""".trimIndent()
		}

		test("Null") {
			predicate {
				eq(null)
			} shouldBeBson $$"""
				{
					"$eq": null
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $exists") {
		test("Does exist") {
			predicate<String> {
				exists()
			} shouldBeBson $$"""
				{
					"$exists": true
				}
			""".trimIndent()
		}

		test("Does not exist") {
			predicate<String> {
				doesNotExist()
			} shouldBeBson $$"""
				{
					"$exists": false
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $type") {
		test("Has a given type") {
			predicate<String> {
				hasType(BsonType.Double)
			} shouldBeBson $$"""
				{
					"$type": 1
				}
			""".trimIndent()
		}

		test("Is null") {
			predicate<String?> {
				isNull()
			} shouldBeBson $$"""
				{
					"$type": 10
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $not") {
		test("Is not null") {
			predicate<String?> {
				isNotNull()
			} shouldBeBson $$"""
				{
					"$not": {
						"$type": 10
					}
				}
			""".trimIndent()
		}

		test($$"Empty $not is no-op and thus removed") {
			predicate<String> {
				not { }
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}
	}

	test("Can specify multiple operators at once") {
		predicate {
			exists()
			gt(15)
			lte(99)
			not {
				isNull()
				eq(17)
			}
		} shouldBeBson $$"""
			{
				"$exists": true,
				"$gt": 15,
				"$lte": 99,
				"$not": {
					"$type": 10,
					"$eq": 17
				}
			}
		""".trimIndent()
	}

}
