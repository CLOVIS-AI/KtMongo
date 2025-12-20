/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.testContext
import opensavvy.prepared.suite.TestDsl

@LowLevelApi
val ValueTest by multiContextSuite {

	val dollar = "$"

	class Profile(
		val age: Int,
	)

	class User(
		val name: String,
		val profile: Profile,
	)

	suspend fun TestDsl.value(block: AggregationOperators.() -> Value<User, *>): String {
		val context = testContext()
		return object : AggregationOperators {
			override val context: BsonContext
				get() = context
		}.block().toString()
	}

	suite("Referring to fields with of") {
		test("Referring to a top-level field") {
			value {
				of(User::name)
			} shouldBeBson """
				[
					"${dollar}name"
				]
			""".trimIndent()
		}

		test("Referring to a second-level field") {
			value {
				of(User::profile / Profile::age)
			} shouldBeBson """
				[
					"${dollar}profile.age"
				]
			""".trimIndent()
		}
	}

	suite("Embedding Kotlin values with of") {
		test("Embedding a primitive integer") {
			value {
				of(5)
			} shouldBeBson $$"""
				[
					{
						"$literal": 5
					}
				]
			""".trimIndent()
		}

		test("Embedding null") {
			value {
				of(null)
			} shouldBeBson $$"""
				[
					{
						"$literal": null
					}
				]
			""".trimIndent()
		}
	}

	test("Foo") {
		value {
			5 eq User::profile / Profile::age
		} shouldBeBson $$"""
			[
				{
					"$eq": [
						{
							"$literal": 5
						},
						"$profile.age"
					]
				}
			]
		""".trimIndent()
	}
}
