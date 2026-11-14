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

	test("Enum") {
		filter {
			schema("Test") {
				type(BsonType.Document)
				properties {
					property(User::isAlive) {
						type(BsonType.Boolean)
						enum(true, false)
					}
				}
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"bsonType": "object",
					"required": ["isAlive"],
					"properties": {
						"isAlive": {
							"title": "isAlive",
							"bsonType": "bool",
							"enum": [true, false]
						}
					},
					"additionalProperties": false
				}
			}
		""".trimIndent()
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

	suite("String length") {
		test("maxLength") {
			filter {
				schema("Test") {
					type(BsonType.String)
					maxLength(5)
				}
			} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"bsonType": "string",
					"maxLength": 5
				}
			}
		""".trimIndent()
		}

		test("minLength") {
			filter {
				schema("Test") {
					type(BsonType.String)
					minLength(2)
				}
			} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"bsonType": "string",
					"minLength": 2
				}
			}
		""".trimIndent()
		}

		test("Exact") {
			filter {
				schema("Test") {
					length(5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minLength": 5,
						"maxLength": 5
					}
				}
			""".trimIndent()
		}

		test("IntRange") {
			filter {
				schema("Test") {
					length(2..5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minLength": 2,
						"maxLength": 5
					}
				}
			""".trimIndent()
		}

		test("UIntRange") {
			filter {
				schema("Test") {
					length(2u..5u)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minLength": 2,
						"maxLength": 5
					}
				}
			""".trimIndent()
		}
	}

	test("pattern") {
		filter {
			schema("Test") {
				pattern("^a.*b$")
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"pattern": "^a.*b$"
				}
			}
		""".trimIndent()
	}

	suite("Array items") {
		test("Homogeneous") {
			filter {
				schema("Test") {
					items<String>("Item") {
						type(BsonType.String)
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"items": {
							"title": "Item",
							"bsonType": "string"
						}
					}
				}
			""".trimIndent()
		}

		test("In order (without additional items)") {
			filter {
				schema("Test") {
					itemsInOrder<Any?> {
						schema("First") {
							type(BsonType.String)
						}
						schema("Second") {
							type(BsonType.Int32)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"items": [
							{
								"title": "First",
								"bsonType": "string"
							},
							{
								"title": "Second",
								"bsonType": "int"
							}
						],
						"additionalItems": false
					}
				}
			""".trimIndent()
		}

		test("In order (with additional items)") {
			filter {
				schema("Test") {
					itemsInOrder<Any?> {
						schema("First") {
							type(BsonType.String)
						}
						schema("Second") {
							type(BsonType.Int32)
						}
						additionalItems("Rest") {
							type(BsonType.Double)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"items": [
							{
								"title": "First",
								"bsonType": "string"
							},
							{
								"title": "Second",
								"bsonType": "int"
							}
						],
						"additionalItems": {
							"title": "Rest",
							"bsonType": "double"
						}
					}
				}
			""".trimIndent()
		}
	}

	suite("Array size") {
		test("maxItems") {
			filter {
				schema("Test") {
					type(BsonType.Array)
					maxItems(5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"bsonType": "array",
						"maxItems": 5
					}
				}
			""".trimIndent()
		}

		test("minItems") {
			filter {
				schema("Test") {
					type(BsonType.Array)
					minItems(2)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"bsonType": "array",
						"minItems": 2
					}
				}
			""".trimIndent()
		}

		test("Exact") {
			filter {
				schema("Test") {
					size(5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minItems": 5,
						"maxItems": 5
					}
				}
			""".trimIndent()
		}

		test("IntRange") {
			filter {
				schema("Test") {
					size(2..5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minItems": 2,
						"maxItems": 5
					}
				}
			""".trimIndent()
		}

		test("UIntRange") {
			filter {
				schema("Test") {
					size(2u..5u)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minItems": 2,
						"maxItems": 5
					}
				}
			""".trimIndent()
		}
	}

	suite("Array unique items") {
		test("True") {
			filter {
				schema("Test") {
					type(BsonType.Array)
					uniqueItems()
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"bsonType": "array",
						"uniqueItems": true
					}
				}
			""".trimIndent()
		}

		test("False") {
			filter {
				schema("Test") {
					uniqueItems(false)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"uniqueItems": false
					}
				}
			""".trimIndent()
		}
	}

	suite("Object size") {
		test("maxProperties") {
			filter {
				schema("Test") {
					type(BsonType.Document)
					maxProperties(5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"bsonType": "object",
						"maxProperties": 5
					}
				}
			""".trimIndent()
		}

		test("minProperties") {
			filter {
				schema("Test") {
					type(BsonType.Document)
					minProperties(2)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"bsonType": "object",
						"minProperties": 2
					}
				}
			""".trimIndent()
		}

		test("Exact") {
			filter {
				schema("Test") {
					properties(5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minProperties": 5,
						"maxProperties": 5
					}
				}
			""".trimIndent()
		}

		test("IntRange") {
			filter {
				schema("Test") {
					properties(2..5)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minProperties": 2,
						"maxProperties": 5
					}
				}
			""".trimIndent()
		}

		test("UIntRange") {
			filter {
				schema("Test") {
					properties(2u..5u)
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"minProperties": 2,
						"maxProperties": 5
					}
				}
			""".trimIndent()
		}
	}

	suite("Object properties") {
		test("Properties by name and property reference") {
			filter {
				schema("Test") {
					properties {
						property<String>("custom") {
							type(BsonType.String)
						}
						property(User::name) {
							type(BsonType.String)
						}
						property(User::age) {
							type(BsonType.Int32)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"required": [
							"custom",
							"name",
							"age"
						],
						"properties": {
							"custom": {
								"title": "custom",
								"bsonType": "string"
							},
							"name": {
								"title": "name",
								"bsonType": "string"
							},
							"age": {
								"title": "age",
								"bsonType": "int"
							}
						},
						"additionalProperties": false
					}
				}
			""".trimIndent()
		}

		test("Pattern properties") {
			filter {
				schema("Test") {
					properties {
						properties<String>("^str_.*$") {
							type(BsonType.String)
						}
						properties<Int>("^num_.*$") {
							type(BsonType.Int32)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"patternProperties": {
							"^str_.*$": {
								"title": "^str_.*$",
								"bsonType": "string"
							},
							"^num_.*$": {
								"title": "^num_.*$",
								"bsonType": "int"
							}
						},
						"additionalProperties": false
					}
				}
			""".trimIndent()
		}

		test("Additional properties (schema)") {
			filter {
				schema("Test") {
					properties {
						property(User::name) {
							type(BsonType.String)
						}
						additionalProperties("Extra") {
							type(BsonType.String)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"required": [
							"name"
						],
						"properties": {
							"name": {
								"title": "name",
								"bsonType": "string"
							}
						},
						"additionalProperties": {
							"title": "Extra",
							"bsonType": "string"
						}
					}
				}
			""".trimIndent()
		}

		test("Properties, patternProperties, and additionalProperties combined") {
			filter {
				schema("Test") {
					properties {
						property(User::name) {
							type(BsonType.String)
						}
						properties<Int>("^extra_.*$") {
							type(BsonType.Int32)
						}
						additionalProperties("Extra") {
							type(BsonType.Boolean)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"required": [
							"name"
						],
						"properties": {
							"name": {
								"title": "name",
								"bsonType": "string"
							}
						},
						"patternProperties": {
							"^extra_.*$": {
								"title": "^extra_.*$",
								"bsonType": "int"
							}
						},
						"additionalProperties": {
							"title": "Extra",
							"bsonType": "bool"
						}
					}
				}
			""".trimIndent()
		}

		test("Optional properties") {
			filter {
				schema("Test") {
					properties {
						property(User::name, required = false) {
							type(BsonType.String)
						}
						property(User::age, required = false) {
							type(BsonType.Int32)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"properties": {
							"name": {
								"title": "name",
								"bsonType": "string"
							},
							"age": {
								"title": "age",
								"bsonType": "int"
							}
						},
						"additionalProperties": false
					}
				}
			""".trimIndent()
		}

		test("Mixed required and optional properties") {
			filter {
				schema("Test") {
					properties {
						property(User::name) {
							type(BsonType.String)
						}
						property(User::age, required = false) {
							type(BsonType.Int32)
						}
						property<String>("custom", required = true) {
							type(BsonType.String)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"required": [
							"name",
							"custom"
						],
						"properties": {
							"name": {
								"title": "name",
								"bsonType": "string"
							},
							"age": {
								"title": "age",
								"bsonType": "int"
							},
							"custom": {
								"title": "custom",
								"bsonType": "string"
							}
						},
						"additionalProperties": false
					}
				}
			""".trimIndent()
		}

		test("Multiple property dependencies") {
			filter {
				schema("Test") {
					properties {
						property(User::name, required = false) {
							type(BsonType.String)
						}
						val city = property<String>("city", required = false) {
							type(BsonType.String)
						}
						property(User::age, required = false) {
							type(BsonType.Int32)
							dependsOn("name")
							dependsOn(city)
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"properties": {
							"name": {
								"title": "name",
								"bsonType": "string"
							},
							"city": {
								"title": "city",
								"bsonType": "string"
							},
							"age": {
								"title": "age",
								"bsonType": "int"
							}
						},
						"additionalProperties": false,
						"dependencies": {
							"age": [
								"name",
								"city"
							]
						}
					}
				}
			""".trimIndent()
		}

		test("Schema dependency") {
			filter {
				schema("Test") {
					properties {
						property(User::age, required = false) {
							type(BsonType.Int32)
							dependsOn {
								properties {
									property(User::name) {
										type(BsonType.String)
									}
								}
							}
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"properties": {
							"age": {
								"title": "age",
								"bsonType": "int"
							}
						},
						"additionalProperties": false,
						"dependencies": {
							"age": {
								"title": "age",
								"required": [
									"name"
								],
								"properties": {
									"name": {
										"title": "name",
										"bsonType": "string"
									}
								},
								"additionalProperties": false
							}
						}
					}
				}
			""".trimIndent()
		}

		test("Mixed dependencies") {
			filter {
				schema("Test") {
					properties {
						val name = property(User::name, required = false) {
							type(BsonType.String)
						}
						property(User::age, required = false) {
							type(BsonType.Int32)
							dependsOn(name)
							dependsOn {
								properties {
									property<String>("city") {
										type(BsonType.String)
									}
								}
							}
						}
					}
				}
			} shouldBeBson $$"""
				{
					"$jsonSchema": {
						"title": "Test",
						"properties": {
							"name": {
								"title": "name",
								"bsonType": "string"
							},
							"age": {
								"title": "age",
								"bsonType": "int"
							}
						},
						"additionalProperties": false,
						"dependencies": {
							"age": {
								"title": "age",
								"required": [
									"city",
									"name"
								],
								"properties": {
									"city": {
										"title": "city",
										"bsonType": "string"
									},
									"name": {}
								},
								"additionalProperties": false
							}
						}
					}
				}
			""".trimIndent()
		}
	}

	test("not") {
		filter {
			schema("Test") {
				not("NotYoung") {
					properties {
						property(User::age) {
							maximum(18)
						}
					}
				}
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"not": {
						"title": "NotYoung",
						"required": ["age"],
						"properties": {
							"age": {
								"title": "age",
								"maximum": 18,
								"exclusiveMaximum": false
							}
						},
						"additionalProperties": false
					}
				}
			}
		""".trimIndent()
	}

	test("allOf") {
		filter {
			schema("Test") {
				allOf {
					schema("HasAge") {
						properties {
							property(User::age) {
								minimum(18)
							}
						}
					}
					schema("HasName") {
						properties {
							property(User::name) {
								minLength(1)
							}
						}
					}
				}
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"allOf": [
						{
							"title": "HasAge",
							"required": ["age"],
							"properties": {
								"age": {
									"title": "age",
									"minimum": 18,
									"exclusiveMinimum": false
								}
							},
							"additionalProperties": false
						},
						{
							"title": "HasName",
							"required": ["name"],
							"properties": {
								"name": {
									"title": "name",
									"minLength": 1
								}
							},
							"additionalProperties": false
						}
					]
				}
			}
		""".trimIndent()
	}

	test("anyOf") {
		filter {
			schema("Test") {
				anyOf {
					schema("Young") {
						properties {
							property(User::age) {
								maximum(17)
							}
						}
					}
					schema("Named") {
						properties {
							property(User::name) {
								minLength(1)
							}
						}
					}
				}
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"anyOf": [
						{
							"title": "Young",
							"required": ["age"],
							"properties": {
								"age": {
									"title": "age",
									"maximum": 17,
									"exclusiveMaximum": false
								}
							},
							"additionalProperties": false
						},
						{
							"title": "Named",
							"required": ["name"],
							"properties": {
								"name": {
									"title": "name",
									"minLength": 1
								}
							},
							"additionalProperties": false
						}
					]
				}
			}
		""".trimIndent()
	}

	test("oneOf") {
		filter {
			schema("Test") {
				oneOf {
					schema("Young") {
						properties {
							property(User::age) {
								maximum(17)
							}
						}
					}
					schema("Adult") {
						properties {
							property(User::age) {
								minimum(18)
							}
						}
					}
				}
			}
		} shouldBeBson $$"""
			{
				"$jsonSchema": {
					"title": "Test",
					"oneOf": [
						{
							"title": "Young",
							"required": ["age"],
							"properties": {
								"age": {
									"title": "age",
									"maximum": 17,
									"exclusiveMaximum": false
								}
							},
							"additionalProperties": false
						},
						{
							"title": "Adult",
							"required": ["age"],
							"properties": {
								"age": {
									"title": "age",
									"minimum": 18,
									"exclusiveMinimum": false
								}
							},
							"additionalProperties": false
						}
					]
				}
			}
		""".trimIndent()
	}

}
