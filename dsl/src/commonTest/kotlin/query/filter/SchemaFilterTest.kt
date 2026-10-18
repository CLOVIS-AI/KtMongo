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

package opensavvy.ktmongo.dsl.query.filter

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.query.shouldBeBson

@Suppress("DEPRECATION")
val SchemaTest by multiContextSuite {

	suite("bsonType") {
		fun type(
			type: BsonType,
			alias: String,
		) {
			test(type.toString()) {
				filter {
					schema("Test") {
						type(type)
					}
				} shouldBeBson $$"""
					{
						"$jsonSchema": {
							"title": "Test",
							"bsonType": "$$alias"
						}
					}
				""".trimIndent()
			}
		}

		type(BsonType.Double, "double")
		type(BsonType.String, "string")
		type(BsonType.Document, "object")
		type(BsonType.Array, "array")
		type(BsonType.BinaryData, "binData")
		type(BsonType.Undefined, "undefined")
		type(BsonType.ObjectId, "objectId")
		type(BsonType.Boolean, "bool")
		type(BsonType.Datetime, "date")
		type(BsonType.Null, "null")
		type(BsonType.RegExp, "regex")
		type(BsonType.DBPointer, "dbPointer")
		type(BsonType.JavaScript, "javascript")
		type(BsonType.Symbol, "symbol")
		type(BsonType.JavaScriptWithScope, "javascriptWithScope")
		type(BsonType.Int32, "int")
		type(BsonType.Timestamp, "timestamp")
		type(BsonType.Int64, "long")
		type(BsonType.Decimal128, "decimal")
		type(BsonType.MinKey, "minKey")
		type(BsonType.MaxKey, "maxKey")

		test("Number") {
			filter {
				schema("Test") {
					type(BsonType.Number)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"bsonType": "number"
					}
				}
			""".trimIndent()
		}
	}

	test("Description") {
		filter {
			schema("Test") {
				description("Hello world")
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"description": "Hello world"
				}
			}
		""".trimIndent()
	}

	suite("multipleOf") {
		test("Int") {
			filter {
				schema("Test") {
					multipleOf(5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"multipleOf": 5
					}
				}
			""".trimIndent()
		}

		test("Long") {
			filter {
				schema("Test") {
					multipleOf(5L)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"multipleOf": 5
					}
				}
			""".trimIndent()
		}

		test("Double") {
			filter {
				schema("Test") {
					multipleOf(5.5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"multipleOf": 5.5
					}
				}
			""".trimIndent()
		}
	}

	suite("maximum") {
		test("Int (inclusive)") {
			filter {
				schema("Test") {
					maximum(10)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"maximum": 10,
						"exclusiveMaximum": false
					}
				}
			""".trimIndent()
		}

		test("Int (exclusive)") {
			filter {
				schema("Test") {
					maximum(10, exclusive = true)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"maximum": 10,
						"exclusiveMaximum": true
					}
				}
			""".trimIndent()
		}

		test("Long (inclusive)") {
			filter {
				schema("Test") {
					maximum(10L)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"maximum": 10,
						"exclusiveMaximum": false
					}
				}
			""".trimIndent()
		}

		test("Long (exclusive)") {
			filter {
				schema("Test") {
					maximum(10L, exclusive = true)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"maximum": 10,
						"exclusiveMaximum": true
					}
				}
			""".trimIndent()
		}

		test("Double (inclusive)") {
			filter {
				schema("Test") {
					maximum(10.5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"maximum": 10.5,
						"exclusiveMaximum": false
					}
				}
			""".trimIndent()
		}

		test("Double (exclusive)") {
			filter {
				schema("Test") {
					maximum(10.5, exclusive = true)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"maximum": 10.5,
						"exclusiveMaximum": true
					}
				}
			""".trimIndent()
		}
	}

	suite("minimum") {
		test("Int (inclusive)") {
			filter {
				schema("Test") {
					minimum(10)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minimum": 10,
						"exclusiveMinimum": false
					}
				}
			""".trimIndent()
		}

		test("Int (exclusive)") {
			filter {
				schema("Test") {
					minimum(10, exclusive = true)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minimum": 10,
						"exclusiveMinimum": true
					}
				}
			""".trimIndent()
		}

		test("Long (inclusive)") {
			filter {
				schema("Test") {
					minimum(10L)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minimum": 10,
						"exclusiveMinimum": false
					}
				}
			""".trimIndent()
		}

		test("Long (exclusive)") {
			filter {
				schema("Test") {
					minimum(10L, exclusive = true)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minimum": 10,
						"exclusiveMinimum": true
					}
				}
			""".trimIndent()
		}

		test("Double (inclusive)") {
			filter {
				schema("Test") {
					minimum(10.5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minimum": 10.5,
						"exclusiveMinimum": false
					}
				}
			""".trimIndent()
		}

		test("Double (exclusive)") {
			filter {
				schema("Test") {
					minimum(10.5, exclusive = true)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minimum": 10.5,
						"exclusiveMinimum": true
					}
				}
			""".trimIndent()
		}
	}

}
