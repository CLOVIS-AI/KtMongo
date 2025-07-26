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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.dsl.command

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.WriteConcern
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.kotest.PreparedSpec

class InsertTest : PreparedSpec({

	test("insertOne") {
		val context = testContext()
		val daniel = context.buildDocument {
			writeString("name", "Daniel")
		} as opensavvy.ktmongo.bson.official.Bson

		InsertOne(testContext(), daniel.raw).apply {
			options.apply {
				writeConcern(WriteConcern.FireAndForget)
			}
		} shouldBeBson """
			{
				"documents": [
					{
						"name": "Daniel"
					}
				],
				"writeConcern": {
					"w": 0,
					"j": false
				}
			}
		""".trimIndent()
	}

	test("insertMany") {
		val context = testContext()

		val daniel = context.buildDocument {
			writeString("name", "Daniel")
		} as opensavvy.ktmongo.bson.official.Bson

		val fred = context.buildDocument {
			writeString("name", "Fred")
			writeInt32("age", 24)
		} as opensavvy.ktmongo.bson.official.Bson

		val alice = context.buildDocument {
			writeString("name", "Alice")
		} as opensavvy.ktmongo.bson.official.Bson

		InsertMany(testContext(), listOf(daniel.raw, fred.raw, alice.raw)).apply {
			options.apply {
				writeConcern(WriteConcern.Primary)
			}
		} shouldBeBson """
			{
				"documents": [
					{
						"name": "Daniel"
					},
					{
						"name": "Fred",
						"age": 24
					},
					{
						"name": "Alice"
					}
				],
				"writeConcern": {
					"w": 1,
					"j": false
				}
			}
		""".trimIndent()
	}

})
