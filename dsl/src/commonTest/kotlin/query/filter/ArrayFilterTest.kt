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
import opensavvy.prepared.runner.kotest.PreparedSpec

class ArrayFilterTest : PreparedSpec({
	test("Test on an array element") {
		filter {
			User::grades.any eq 12
		} shouldBeBson """
				{
					"grades": {
						"$eq": 12
					}
				}
			""".trimIndent()
	}

	test("Test on different array elements") {
		filter {
			User::grades.any gt 12
			User::grades.any lte 15
		} shouldBeBson """
				{
					"$and": [
						{
							"grades": {
								"$gt": 12
							}
						},
						{
							"grades": {
								"$lte": 15
							}
						}
					]
				}
			""".trimIndent()
	}

	test("Test on a single array element") {
		filter {
			User::grades.anyValue {
				gt(12)
				lte(15)
			}
		} shouldBeBson """
				{
					"grades": {
						"$elemMatch": {
							"$gt": 12,
							"$lte": 15
						}
					}
				}
			""".trimIndent()
	}

	test("Test on subfields of different array elements") {
		filter {
			User::pets.any / Pet::age gt 15
			User::pets / Pet::age lte 18  // without 'any', the / does the same thing
		} shouldBeBson """
				{
					"$and": [
						{
							"pets.age": {
								"$gt": 15
							}
						},
						{
							"pets.age": {
								"$lte": 18
							}
						}
					]
				}
			""".trimIndent()
	}

	test("Test on subfields of a single array element") {
		filter {
			User::pets.any {
				Pet::age gt 15
				Pet::age lte 18
			}
		} shouldBeBson """
				{
					"pets": {
						"$elemMatch": {
							"$and": [
								{
									"age": {
										"$gt": 15
									}
								},
								{
									"age": {
										"$lte": 18
									}
								}
							]
						}
					}
				}
			""".trimIndent()
	}

	test("Test on a single subfield of a single array element") {
		filter {
			User::pets.any {
				Pet::age {
					gt(15)
					lte(18)
				}
			}
		} shouldBeBson """
				{
					"pets": {
						"$elemMatch": {
							"age": {
								"$gt": 15,
								"$lte": 18
							}
						}
					}
				}
			""".trimIndent()
	}

	test("Everything combined") {
		filter {
			User::pets / Pet::age gt 3
			User::pets.any {
				Pet::age gte 1
				Pet::name eq "Chocolat"
			}
		} shouldBeBson """
				{
					"$and": [
						{
							"pets.age": {"$gt": 3}
						},
						{
							"pets": {
								"$elemMatch": {
									"$and": [
										{
											"age": {"$gte": 1}
										},
										{
											"name": {"$eq": "Chocolat"}
										}
									]
								}
							}
						}
					]
				}
			""".trimIndent()
	}

	test("Operator $all") {
		filter {
			User::grades containsAll listOf(1, 2, 3)
		} shouldBeBson """
			{
				"grades": {
					"$all": [1, 2, 3]
				}
			}
		""".trimIndent()
	}

	test("isEmpty") {
		filter {
			User::grades.isEmpty()
		} shouldBeBson """
			{
				"grades.0": {
					"$exists": false
				}
			}
		""".trimIndent()
	}

	test("isNotEmpty") {
		filter {
			User::grades.isNotEmpty()
		} shouldBeBson """
			{
				"grades.0": {
					"$exists": true
				}
			}
		""".trimIndent()
	}
})
