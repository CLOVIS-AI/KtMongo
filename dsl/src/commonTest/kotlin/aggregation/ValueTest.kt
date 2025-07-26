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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.filter.eq
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.testballoon.preparedSuite

const val literal = "\$literal"
const val cond = "\$cond"

@LowLevelApi
val ValueTest by preparedSuite {

	val dollar = "$"

	class AggregationOperatorsImpl : AggregationOperators {
		override val context: BsonContext = testContext()
	}

	class Profile(
		val age: Int,
	)

	class User(
		val name: String,
		val profile: Profile,
	)

	fun value(block: AggregationOperators.() -> Value<User, *>) =
		AggregationOperatorsImpl().block().toString()

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
			} shouldBeBson """
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
			} shouldBeBson """
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
			of(5) eq of(User::profile / Profile::age)
		} shouldBeBson """
			[
				{
					"$eq": [
						{
							"$literal": 5
						},
						"${dollar}profile.age"
					]
				}
			]
		""".trimIndent()
	}
}
