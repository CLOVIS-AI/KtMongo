/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.shouldBeBson
import opensavvy.ktmongo.dsl.expr.testContext

val limit = "\$limit"
val match = "\$match"
val sample = "\$sample"
val skip = "\$skip"
val set = "\$set"

@OptIn(LowLevelApi::class)
fun <Type : PipelineType, Document : Any> aggregate(type: Type) =
	Pipeline<Type, Document>(testContext(), type)

infix fun Pipeline<*, *>.shouldBeBson(expected: String) {
	this.toString() shouldBeBson expected
}
