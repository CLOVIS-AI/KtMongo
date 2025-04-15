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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.ktmongo.dsl.tree.BsonNode
import org.intellij.lang.annotations.Language

val count = "\$count"
val limit = "\$limit"
val match = "\$match"
val sample = "\$sample"
val skip = "\$skip"
val set = "\$set"
val abs = "\$abs"
val add = "\$add"
val ceil = "\$ceil"
val floor = "\$floor"
val concat = "\$concat"
val sort = "\$sort"
val unset = "\$unset"
val project = "\$project"
val filter = "\$filter"
val map = "\$map"
val firstN = "\$firstN"
val lastN = "\$lastN"
val sortArray = "\$sortArray"

@OptIn(LowLevelApi::class)
class TestPipeline<Document : Any>(
	chain: PipelineChainLink = PipelineChainLink(testContext(), null, null)
) : AbstractPipeline<Document>(
	testContext(),
	chain,
), AggregationPipeline<Document>, UpdatePipeline<Document> {

	@DangerousMongoApi
	@LowLevelApi
	override fun withStage(stage: BsonNode): TestPipeline<Document> =
		TestPipeline(chain.withStage(stage))

	@Suppress("UNCHECKED_CAST")
	@DangerousMongoApi
	@LowLevelApi
	override fun <New : Any> reinterpret(): TestPipeline<New> =
		this as TestPipeline<New>

	override fun embedInUnionWith(writer: BsonFieldWriter) = with(writer) {
		writeString("coll", "other")
		writeArray("pipeline") {
			this@TestPipeline.writeTo(this)
		}
	}

}

infix fun Pipeline<*>.shouldBeBson(@Language("MongoDB-JSON") expected: String) {
	this.toString() shouldBeBson expected
}
