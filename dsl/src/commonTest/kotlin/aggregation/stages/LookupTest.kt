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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.multiContextSuite
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.prepared.suite.assertions.checkThrows
import kotlin.time.Instant

val LookupTest by multiContextSuite {

	class Department(
		val _id: ObjectId,
		val name: String,
		val creationDate: Instant,
	)

	class User(
		val _id: ObjectId,
		val name: String,
		val department: ObjectId,
		val departmentResolved: Department? = null,
		val creationDate: Instant,
	)

	test($$"$lookup without 'into' is not allowed") {
		val departments = TestPipeline<Department>("departments")

		checkThrows<IllegalStateException> {
			TestPipeline<User>()
				.lookup {
					from(departments)
					on(User::department, Department::_id)
				}
		}
	}

	test($$"Simple join with $lookup") {
		val departments = TestPipeline<Department>("departments")

		val outputField = Field.unsafe<List<Department>>("departments")

		TestPipeline<User>()
			.lookup {
				into(outputField)
				from(departments)
				on(User::department, Department::_id)
			}
			.shouldBeBson($$"""
				[
					{
						"$lookup": {
							"from": "departments",
							"localField": "department",
							"foreignField": "_id",
							"as": "departments"
						}
					}
				]
			""".trimIndent())
	}

	test($$"Simple join with $lookup with a project to get a single output field") {
		val departments = TestPipeline<Department>("departments")

		val resultDepartments = Field.unsafe<List<Department>>("departments")

		TestPipeline<User>()
			.lookup {
				into(resultDepartments)
				from(departments)
				on(User::department, Department::_id)
			}
			.project {
				User::departmentResolved set resultDepartments[0]
			}
			.shouldBeBson($$"""
				[
					{
						"$lookup": {
							"from": "departments",
							"localField": "department",
							"foreignField": "_id",
							"as": "departments"
						}
					},
					{
						"$project": {
							"departmentResolved": "$departments.0"
						}
					}
				]
			""".trimIndent())
	}


	test($$"Complex join with $lookup and variables") {
		val departments = TestPipeline<Department>("departments")

		val outputField = Field.unsafe<List<Department>>("departments")

		TestPipeline<User>()
			.lookup {
				into(outputField)

				val userCreationDate = let(User::creationDate)

				from(
					departments
						.matchExpr { Department::creationDate gt userCreationDate }
				)

				on(User::department, Department::_id)
			}
			.shouldBeBson($$$"""
				[
					{
						"$lookup": {
							"let": {
								"l1": "$creationDate"
							},
							"from": "departments",
							"pipeline": [
								{
									"$match": {
										"$expr": {
											"$gt": [
												"$creationDate",
												"$$l1"
											]
										}
									}
								}
							],
							"localField": "department",
							"foreignField": "_id",
							"as": "departments"
						}
					}
				]
			""".trimIndent())
	}

	test("Complex join with multiple variables") {
		val departments = TestPipeline<Department>("departments")

		val outputField = Field.unsafe<List<Department>>("departments")

		TestPipeline<User>()
			.lookup {
				into(outputField)

				val userCreationDate = let(User::creationDate)
				val resolved = let(User::departmentResolved / Department::name)

				from(
					departments
						.matchExpr { Department::creationDate gt userCreationDate }
						.matchExpr { resolved ne null }
				)

				on(User::department, Department::_id)
			}
			.shouldBeBson($$$"""
				[
					{
						"$lookup": {
							"let": {
								"l1": "$creationDate",
								"l2": "$departmentResolved.name"
							},
							"from": "departments",
							"pipeline": [
								{
									"$match": {
										"$expr": {
											"$gt": [
												"$creationDate",
												"$$l1"
											]
										}
									}
								},
								{
									"$match": {
										"$expr": {
											"$ne": [
												"$$l2",
												{
													"$literal": null
												}
											]
										}
									}
								}
							],
							"localField": "department",
							"foreignField": "_id",
							"as": "departments"
						}
					}
				]
			""".trimIndent())
	}
}
