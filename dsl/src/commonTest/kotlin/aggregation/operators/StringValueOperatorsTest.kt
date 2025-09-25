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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.prepared.runner.testballoon.preparedSuite

val StringValueOperatorsTest by preparedSuite {

	class Target(
		val text: String,
		val description: String?,
	)

	class LengthTarget(
		val text: String,
		val description: String?,
		val length: Int,
	)

	class SplitTarget(
		val text: String,
		val description: String,
		val parts: List<String>,
	)

	suite($$"$trim") {
		test("Default whitespace trimming") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trim()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": "$description"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Custom character trimming") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trim(characters = of("ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming with literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of(" \n good bye \t ").trim()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": {
											"$literal": " \n good bye \t "
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming with custom chars and literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of(" ggggoodbyeeeee").trim(characters = of(" ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": {
											"$literal": " ggggoodbyeeeee"
										},
										"chars": {
											"$literal": " ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming with vararg characters") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trim('g', 'e')
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$trim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$ltrim") {
		test("Default whitespace trimming from start") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimStart()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$ltrim": {
										"input": "$description"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Custom character trimming from start") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimStart(characters = of("ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$ltrim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming from start with vararg characters") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimStart('g', 'e')
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$ltrim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$rtrim") {
		test("Default whitespace trimming from end") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimEnd()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$rtrim": {
										"input": "$description"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Custom character trimming from end") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimEnd(characters = of("ge"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$rtrim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Trimming from end with vararg characters") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).trimEnd('g', 'e')
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$rtrim": {
										"input": "$description",
										"chars": {
											"$literal": "ge"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$toLower") {
		test("Convert string to lowercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).lowercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toLower": "$description"
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert literal string to lowercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of("PRODUCT 1").lowercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toLower": {
										"$literal": "PRODUCT 1"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert field reference to lowercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::text).lowercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toLower": "$text"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$toUpper") {
		test("Convert string to uppercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).uppercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toUpper": "$description"
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert literal string to uppercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of("product 1").uppercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toUpper": {
										"$literal": "product 1"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Convert field reference to uppercase") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::text).uppercase()
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$toUpper": "$text"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$strLenCP") {
		test("Get length of string field") {
			TestPipeline<LengthTarget>()
				.set {
					LengthTarget::length set of(LengthTarget::description).length
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"length": {
									"$strLenCP": "$description"
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Get length of literal string") {
			TestPipeline<LengthTarget>()
				.set {
					LengthTarget::length set of("Hello World!").length
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"length": {
									"$strLenCP": {
										"$literal": "Hello World!"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Get length of field reference") {
			TestPipeline<LengthTarget>()
				.set {
					LengthTarget::length set of(LengthTarget::text).length
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"length": {
									"$strLenCP": "$text"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$strLenBytes") {
		test("Get UTF-8 byte length of string field") {
			TestPipeline<LengthTarget>()
				.set {
					LengthTarget::length set of(LengthTarget::description).lengthUTF8
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"length": {
									"$strLenBytes": "$description"
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Get UTF-8 byte length of literal string") {
			TestPipeline<LengthTarget>()
				.set {
					LengthTarget::length set of("Hello World!").lengthUTF8
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"length": {
									"$strLenBytes": {
										"$literal": "Hello World!"
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Get UTF-8 byte length of field reference") {
			TestPipeline<LengthTarget>()
				.set {
					LengthTarget::length set of(LengthTarget::text).lengthUTF8
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"length": {
									"$strLenBytes": "$text"
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$split") {
		test("Split string field with delimiter") {
			TestPipeline<SplitTarget>()
				.set {
					SplitTarget::parts set of(SplitTarget::description).split(delimiter = of("-"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"parts": {
									"$split": [
										"$description",
										{
											"$literal": "-"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Split literal string with delimiter") {
			TestPipeline<SplitTarget>()
				.set {
					SplitTarget::parts set of("June-15-2013").split(delimiter = of("-"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"parts": {
									"$split": [
										{
											"$literal": "June-15-2013"
										},
										{
											"$literal": "-"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Split with single character delimiter") {
			TestPipeline<SplitTarget>()
				.set {
					SplitTarget::parts set of("banana split").split(delimiter = of("a"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"parts": {
									"$split": [
										{
											"$literal": "banana split"
										},
										{
											"$literal": "a"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Split with space delimiter") {
			TestPipeline<SplitTarget>()
				.set {
					SplitTarget::parts set of("Hello World").split(delimiter = of(" "))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"parts": {
									"$split": [
										{
											"$literal": "Hello World"
										},
										{
											"$literal": " "
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Split with multi-character delimiter") {
			TestPipeline<SplitTarget>()
				.set {
					SplitTarget::parts set of("astronomical").split(delimiter = of("astro"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"parts": {
									"$split": [
										{
											"$literal": "astronomical"
										},
										{
											"$literal": "astro"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Split with field reference as delimiter") {
			TestPipeline<SplitTarget>()
				.set {
					SplitTarget::parts set of(SplitTarget::text).split(delimiter = of(SplitTarget::description))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"parts": {
									"$split": [
										"$text",
										"$description"
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$replaceOne") {
		test("Replace first occurrence in string field") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).replaceFirst(find = of("blue paint"), replacement = of("red paint"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceOne": {
										"input": "$description",
										"find": {
											"$literal": "blue paint"
										},
										"replacement": {
											"$literal": "red paint"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace first occurrence in literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of("blue paint with blue paintbrush").replaceFirst(find = of("blue paint"), replacement = of("red paint"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceOne": {
										"input": {
											"$literal": "blue paint with blue paintbrush"
										},
										"find": {
											"$literal": "blue paint"
										},
										"replacement": {
											"$literal": "red paint"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace with string literals") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).replaceFirst(find = "blue paint", replacement = "red paint")
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceOne": {
										"input": "$description",
										"find": {
											"$literal": "blue paint"
										},
										"replacement": {
											"$literal": "red paint"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace single character") {
			TestPipeline<Target>()
				.set {
					Target::text set of("banana split").replaceFirst(find = of("a"), replacement = of("o"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceOne": {
										"input": {
											"$literal": "banana split"
										},
										"find": {
											"$literal": "a"
										},
										"replacement": {
											"$literal": "o"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace with empty string") {
			TestPipeline<Target>()
				.set {
					Target::text set of("Hello World").replaceFirst(find = of(" "), replacement = of(""))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceOne": {
										"input": {
											"$literal": "Hello World"
										},
										"find": {
											"$literal": " "
										},
										"replacement": {
											"$literal": ""
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace with field references") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::text).replaceFirst(find = of(Target::description), replacement = of("REPLACED"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceOne": {
										"input": "$text",
										"find": "$description",
										"replacement": {
											"$literal": "REPLACED"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$replaceAll") {
		test("Replace all occurrences in string field") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).replace(find = of("blue paint"), replacement = of("red paint"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceAll": {
										"input": "$description",
										"find": {
											"$literal": "blue paint"
										},
										"replacement": {
											"$literal": "red paint"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace all occurrences in literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of("blue paint with blue paintbrush").replace(find = of("blue paint"), replacement = of("red paint"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceAll": {
										"input": {
											"$literal": "blue paint with blue paintbrush"
										},
										"find": {
											"$literal": "blue paint"
										},
										"replacement": {
											"$literal": "red paint"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace all with string literals") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).replace(find = "blue paint", replacement = "red paint")
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceAll": {
										"input": "$description",
										"find": {
											"$literal": "blue paint"
										},
										"replacement": {
											"$literal": "red paint"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace all single character occurrences") {
			TestPipeline<Target>()
				.set {
					Target::text set of("banana split").replace(find = of("a"), replacement = of("o"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceAll": {
										"input": {
											"$literal": "banana split"
										},
										"find": {
											"$literal": "a"
										},
										"replacement": {
											"$literal": "o"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace all with empty string") {
			TestPipeline<Target>()
				.set {
					Target::text set of("Hello World Hello").replace(find = of("Hello"), replacement = of(""))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceAll": {
										"input": {
											"$literal": "Hello World Hello"
										},
										"find": {
											"$literal": "Hello"
										},
										"replacement": {
											"$literal": ""
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Replace all with field references") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::text).replace(find = of(Target::description), replacement = of("REPLACED"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$replaceAll": {
										"input": "$text",
										"find": "$description",
										"replacement": {
											"$literal": "REPLACED"
										}
									}
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$substrCP") {
		test("Extract substring with start index and length") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).substring(startIndex = of(1), length = of(2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrCP": [
										"$description",
										{
											"$literal": 1
										},
										{
											"$literal": 2
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Extract substring with literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of("abcde").substring(startIndex = of(1), length = of(2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrCP": [
										{
											"$literal": "abcde"
										},
										{
											"$literal": 1
										},
										{
											"$literal": 2
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}


		test("Extract substring from field with dynamic indices") {
			TestPipeline<Target>()
				.set {
					Target::text set of("Hello World!").substring(startIndex = of(6), length = of(5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrCP": [
										{
											"$literal": "Hello World!"
										},
										{
											"$literal": 6
										},
										{
											"$literal": 5
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Extract substring with IntRange") {
			TestPipeline<Target>()
				.set {
					Target::text set of("abcde").substring(1..2)
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrCP": [
										{
											"$literal": "abcde"
										},
										{
											"$literal": 1
										},
										{
											"$literal": 1
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$substrBytes") {
		test("Extract substring with start index and byte count") {
			TestPipeline<Target>()
				.set {
					Target::text set of(Target::description).substringUTF8(startIndex = of(1), byteCount = of(2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrBytes": [
										"$description",
										{
											"$literal": 1
										},
										{
											"$literal": 2
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Extract substring with literal string") {
			TestPipeline<Target>()
				.set {
					Target::text set of("abcde").substringUTF8(startIndex = of(1), byteCount = of(2))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrBytes": [
										{
											"$literal": "abcde"
										},
										{
											"$literal": 1
										},
										{
											"$literal": 2
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Extract substring from field with dynamic indices") {
			TestPipeline<Target>()
				.set {
					Target::text set of("Hello World!").substringUTF8(startIndex = of(6), byteCount = of(5))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrBytes": [
										{
											"$literal": "Hello World!"
										},
										{
											"$literal": 6
										},
										{
											"$literal": 5
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Extract substring with IntRange") {
			TestPipeline<Target>()
				.set {
					Target::text set of("abcde").substringUTF8(1..2)
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$substrBytes": [
										{
											"$literal": "abcde"
										},
										{
											"$literal": 1
										},
										{
											"$literal": 1
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}

	suite($$"$concat") {
		test("Concatenate two strings") {
			TestPipeline<Target>()
				.set {
					Target::text set (of(Target::text) concat of(Target::description))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$concat": [
										"$text",
										"$description"
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Concatenate multiple strings") {
			TestPipeline<Target>()
				.set {
					Target::text set concat(of(Target::text), of(" - "), of(Target::description), of("!"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$concat": [
										"$text",
										{
											"$literal": " - "
										},
										"$description",
										{
											"$literal": "!"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Concatenate with literal strings") {
			TestPipeline<Target>()
				.set {
					Target::text set concat(of("Hello"), of(" "), of("World"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$concat": [
										{
											"$literal": "Hello"
										},
										{
											"$literal": " "
										},
										{
											"$literal": "World"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}

		test("Combine nested concatenations") {
			TestPipeline<Target>()
				.set {
					Target::text set (of("Hello") concat of(" ") concat of("World"))
				}
				.shouldBeBson($$"""
					[
						{
							"$set": {
								"text": {
									"$concat": [
										{
											"$literal": "Hello"
										},
										{
											"$literal": " "
										},
										{
											"$literal": "World"
										}
									]
								}
							}
						}
					]
				""".trimIndent())
		}
	}
}
