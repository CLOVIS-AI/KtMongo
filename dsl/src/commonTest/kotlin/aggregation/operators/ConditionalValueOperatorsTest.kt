/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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
import opensavvy.ktmongo.dsl.multiContextSuite

val ConditionalValueOperatorsTest by multiContextSuite {

	class User(
		val name: String,
		val score: Int,
		val multiplier: Int,
		val role: String,
		val bonus: Int?,
	)

	suite($$"$cond") {
		test("Basic condition") {
			TestPipeline<User>()
				.set {
					User::score set cond(
						condition = User::multiplier gt 2,
						ifTrue = User::score + User::multiplier,
						ifFalse = User::score,
					)
				} shouldBeBson $$"""
					[
						{
							"$set": {
								"score": {
									"$cond": {
										"if": {
											"$gt": [
												"$multiplier", 
												{"$literal": 2}
											]
										},
										"then": {
											"$add": [
												"$score",
												"$multiplier"
											]
										},
										"else": "$score"
									}
								}
							}
						}
					]"""
		}
	}

	suite($$"$switch") {
		test("Basic switch with multiple conditions and default") {
			TestPipeline<User>()
				.set {
					User::bonus set switch(
						User::role eq "GUEST" then 5,
						User::role eq "EMPLOYEE" then 6,
						User::role eq "ADMIN" then 7,
						default = -1
					)
				} shouldBeBson $$"""
					[
						{
							"$set": {
								"bonus": {
									"$switch": {
										"branches": [
											{
												"case": {
													"$eq": [
														"$role", 
														{"$literal": "GUEST"}
													]
												}, 
												"then": {"$literal": 5}
											}, 
											{
												"case": {
													"$eq": [
														"$role", 
														{"$literal": "EMPLOYEE"}
													]
												}, 
												"then": {"$literal": 6}
											}, 
											{
												"case": {
													"$eq": [
														"$role", 
														{"$literal": "ADMIN"}
													]
												}, 
												"then": {
													"$literal": 7
												}
											}
										], 
										"default": {"$literal": -1}
									}
								}
							}
						}
					]
				"""
		}

		test("Switch with single condition and no default") {
			TestPipeline<User>()
				.set {
					User::bonus set switch(
						User::role eq "ADMIN" then 100
					)
				} shouldBeBson $$"""
					[
						{
							"$set": {
								"bonus": {
									"$switch": {
										"branches": [
											{
												"case": {
													"$eq": [
														"$role", 
														{"$literal": "ADMIN"}
													]
												}, 
												"then": {"$literal": 100}
											}
										]
									}
								}
							}
						}
					]"""
		}

		test("Switch with empty cases and default") {
			TestPipeline<User>()
				.set {
					User::bonus set switch(
						default = of(0)
					)
				} shouldBeBson $$"""
					[
						{
							"$set": {
								"bonus": {
									"$switch": {
										"branches": [],
										"default": {"$literal": 0}
									}
								}
							}
						}
					]"""
		}
	}
}
