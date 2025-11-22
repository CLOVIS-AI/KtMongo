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

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val ElementQueryFilterTest by preparedSuite {

	suite($$"Operator $exists") {
		test("Exists") {
			filter {
				User::age.exists()
			} shouldBeBson $$"""
				{
					"age": {
						"$exists": true
					}
				}
			""".trimIndent()
		}

		test("Does not exist") {
			filter {
				User::age.doesNotExist()
			} shouldBeBson $$"""
				{
					"age": {
						"$exists": false
					}
				}
			""".trimIndent()
		}
	}

	suite($$"Operator $type") {
		test("String") {
			filter {
				User::age hasType BsonType.String
			} shouldBeBson $$"""
				{
					"age": {
						"$type": 2
					}
				}
			""".trimIndent()
		}

		test("Null") {
			filter {
				User::age hasType BsonType.Null
			} shouldBeBson $$"""
				{
					"age": {
						"$type": 10
					}
				}
			""".trimIndent()
		}

		test("Is null") {
			filter {
				User::name.isNull()
			} shouldBeBson $$"""
				{
					"name": {
						"$type": 10
					}
				}
			""".trimIndent()
		}

		test("Is undefined") {
			filter {
				User::name.isUndefined()
			} shouldBeBson $$"""
				{
					"name": {
						"$type": 6
					}
				}
			""".trimIndent()
		}

		test("Is not null") {
			filter {
				User::name.isNotNull()
			} shouldBeBson $$"""
				{
					"name": {
						"$not": {
							"$type": 10
						}
					}
				}
			""".trimIndent()
		}

		test("Is not undefined") {
			filter {
				User::name.isNotUndefined()
			} shouldBeBson $$"""
				{
					"name": {
						"$not": {
							"$type": 6
						}
					}
				}
			""".trimIndent()
		}
	}

}
