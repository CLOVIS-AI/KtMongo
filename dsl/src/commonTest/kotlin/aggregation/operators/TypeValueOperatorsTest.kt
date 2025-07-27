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

@file:OptIn(LowLevelApi::class, ExperimentalTime::class, ExperimentalUuidApi::class)

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val TypeValueOperatorsTest by preparedSuite {

	class Target(
		val name: String,
		val foo: Int,
	)

	test($$"$type") {
		TestPipeline<Target>()
			.project {
				Field.unsafe<Boolean>("isInt32") set (of(Target::foo).type eq of(BsonType.Int32))
			} shouldBeBson $$"""
				[
					{
						"$project": {
							"isInt32": {
								"$eq": [
									{
										"$type": "$foo"
									},
									{
										"$literal": 16
									}
								]
							}
						}
					}
				]
			""".trimIndent()
	}

	test($$"$isArray") {
		TestPipeline<Target>()
			.project {
				Field.unsafe<Boolean>("r") set of(Target::foo).isArray
			} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$isArray": [
									"$foo"
								]
							}
						}
					}
				]
			""".trimIndent()
	}

	test($$"$isNumber") {
		TestPipeline<Target>()
			.project {
				Field.unsafe<Boolean>("r") set of(Target::foo).isNumber
			} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$isNumber": "$foo"
							}
						}
					}
				]
			""".trimIndent()
	}

	suite("Simple conversions") {
		test($$"$toBool") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<Boolean>("r") set of(Target::foo).toBoolean()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toBool": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toDate") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<Instant>("r") set of(Target::foo).toInstant()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toDate": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toDouble") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<Double>("r") set of(Target::foo).toDouble()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toDouble": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toInt") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<Int>("r") set of(Target::foo).toInt()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toInt": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toLong") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<Long>("r") set of(Target::foo).toLong()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toLong": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toObjectId") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<ObjectId>("r") set of(Target::foo).toObjectId()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toObjectId": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toString") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<String>("r") set of(Target::foo).toText()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toString": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}

		test($$"$toUUID") {
			TestPipeline<Target>()
				.project {
					Field.unsafe<Uuid>("r") set of(Target::foo).toUuid()
				} shouldBeBson $$"""
				[
					{
						"$project": {
							"r": {
								"$toUUID": "$foo"
							}
						}
					}
				]
			""".trimIndent()
		}
	}

}
