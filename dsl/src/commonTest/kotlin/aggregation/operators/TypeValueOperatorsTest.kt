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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.TestPipeline
import opensavvy.ktmongo.dsl.aggregation.literal
import opensavvy.ktmongo.dsl.aggregation.project
import opensavvy.ktmongo.dsl.aggregation.shouldBeBson
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.filter.eq
import opensavvy.prepared.runner.kotest.PreparedSpec

val type = "\$type"

class TypeValueOperatorsTest : PreparedSpec({

	class Target(
		val name: String,
		val foo: Int,
	)

	val foo = "\$foo"

	test(type) {
		TestPipeline<Target>()
			.project {
				Field.unsafe<Boolean>("isInt32") set (of(Target::foo).type eq of(BsonType.Int32))
			} shouldBeBson """
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

})
