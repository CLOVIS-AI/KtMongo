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

@file:OptIn(ExperimentalTime::class)

package opensavvy.ktmongo.dsl.query.filter

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.time.ExperimentalTime

class BitwiseFilterTest : PreparedSpec({

	suite(bitsAllClear) {
		test("Int mask") {
			filter {
				User::age bitsAllClear 0x22f8u
			} shouldBeBson """
				{
					"age": {
						"$bitsAllClear": 8952
					}
				}
			""".trimIndent()
		}

		test("Complex mask") {
			filter {
				User::age bitsAllClear ObjectId("686d568045632c8726d01634").bytes
			} shouldBeBson """
				{
					"age": {
						"$bitsAllClear": {
							"$binary": {
								"base64": "aG1WgEVjLIcm0BY0",
								"subType": "00"
							}
						}
					}
				}
			""".trimIndent()
		}
	}

	suite(bitsAllSet) {
		test("Int mask") {
			filter {
				User::age bitsAllSet 0x22f8u
			} shouldBeBson """
				{
					"age": {
						"$bitsAllSet": 8952
					}
				}
			""".trimIndent()
		}

		test("Complex mask") {
			filter {
				User::age bitsAllSet ObjectId("686d568045632c8726d01634").bytes
			} shouldBeBson """
				{
					"age": {
						"$bitsAllSet": {
							"$binary": {
								"base64": "aG1WgEVjLIcm0BY0",
								"subType": "00"
							}
						}
					}
				}
			""".trimIndent()
		}
	}

	suite(bitsAnyClear) {
		test("Int mask") {
			filter {
				User::age bitsAnyClear 0x22f8u
			} shouldBeBson """
				{
					"age": {
						"$bitsAnyClear": 8952
					}
				}
			""".trimIndent()
		}

		test("Complex mask") {
			filter {
				User::age bitsAnyClear ObjectId("686d568045632c8726d01634").bytes
			} shouldBeBson """
				{
					"age": {
						"$bitsAnyClear": {
							"$binary": {
								"base64": "aG1WgEVjLIcm0BY0",
								"subType": "00"
							}
						}
					}
				}
			""".trimIndent()
		}
	}

	suite(bitsAnySet) {
		test("Int mask") {
			filter {
				User::age bitsAnySet 0x22f8u
			} shouldBeBson """
				{
					"age": {
						"$bitsAnySet": 8952
					}
				}
			""".trimIndent()
		}

		test("Complex mask") {
			filter {
				User::age bitsAnySet ObjectId("686d568045632c8726d01634").bytes
			} shouldBeBson """
				{
					"age": {
						"$bitsAnySet": {
							"$binary": {
								"base64": "aG1WgEVjLIcm0BY0",
								"subType": "00"
							}
						}
					}
				}
			""".trimIndent()
		}
	}
})
