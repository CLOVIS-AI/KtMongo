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

package opensavvy.ktmongo.api

import opensavvy.ktmongo.dsl.aggregation.AggregationPipeline
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.reflect.full.memberFunctions

val RequiredOverloadsVerifier by preparedSuite {

	test("MongoAggregationPipeline (unified API)") {
		val type = MongoAggregationPipeline::class

		for (method in type.memberFunctions) {
			check(method.returnType.classifier != Pipeline::class) { "The driver API should override all pipeline-returning methods to return a self-type instead" }
			check(method.returnType.classifier != AggregationPipeline::class) { "The driver API should override all pipeline-returning methods to return a self-type instead" }
		}
	}

}
