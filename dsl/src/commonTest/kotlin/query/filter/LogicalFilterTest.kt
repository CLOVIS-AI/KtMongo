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
import org.bson.types.ObjectId

val LogicalFilterTest by preparedSuite {
	suite($$"Operators $and, $or, and $nor") {
		test("And") {
			filter {
				and {
					User::name eq "foo"
					User::age eq null
				}
			} shouldBeBson $$"""
				{
					"$and": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test($$"Empty $and") {
			filter {
				and {}
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}

		test($$"An $and with a single term is removed") {
			filter {
				and {
					User::name eq "foo"
				}
			} shouldBeBson $$"""
				{
					"name": {
						"$eq": "foo"
					}
				}
			""".trimIndent()
		}

		test($$"Combine nested $and") {
			filter {
				and {
					User::name eq "foo"
					and {
						User::age eq 12
						User::id eq ObjectId("507f1f77bcf86cd799439011")
					}
				}
			} shouldBeBson $$"""
				{
					"$and": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": 12
							}
						},
						{
							"id": {
								"$eq": {
									"$oid": "507f1f77bcf86cd799439011"
								}
							}
						}
					]
				}
			""".trimIndent()
		}

		test($$"An automatic $and is generated when multiple filters are given") {
			filter { // same example as the previous, but we didn't write the '$and'
				User::name eq "foo"
				User::age eq null
			} shouldBeBson $$"""
				{
					"$and": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test("Or") {
			filter {
				or {
					User::name eq "foo"
					User::age eq null
				}
			} shouldBeBson $$"""
				{
					"$or": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test($$"Empty $or") {
			filter {
				or {}
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}

		test($$"An $or with a single term is removed") {
			filter {
				or {
					User::name eq "foo"
				}
			} shouldBeBson $$"""
				{
					"name": {
						"$eq": "foo"
					}
				}
			""".trimIndent()
		}

		test("Nor") {
			filter {
				nor {
					User::name eq "foo"
					User::age eq null
				}
			} shouldBeBson $$"""
				{
					"$nor": [
						{
							"name": {
								"$eq": "foo"
							}
						},
						{
							"age": {
								"$eq": null
							}
						}
					]
				}
			""".trimIndent()
		}

		test($$"Empty $nor") {
			filter {
				nor {}
			} shouldBeBson """
				{
				}
			""".trimIndent()
		}

		test($$"An $nor with a single term isn't simplified") {
			// Ideally, it should become a $not, but that isn't trivial to implement currently.
			filter {
				nor {
					User::name eq "foo"
				}
			} shouldBeBson $$"""
				{
					"$nor": [
						{
							"name": {
								"$eq": "foo"
							}
						}
					]
				}
			""".trimIndent()
		}
	}
}
