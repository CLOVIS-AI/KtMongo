/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.stages.LookupStageOperators
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.testContext
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.prepared.suite.TestDsl
import org.intellij.lang.annotations.Language

class TestPipeline<Document : Any>(
	context: BsonContext,
	chain: PipelineChainLink,
	private val collectionName: String,
) : AbstractPipeline<Document>(
	context,
	chain,
), AggregationPipeline<Document>, UpdatePipeline<Document> {

	@DangerousMongoApi
	@LowLevelApi
	override fun withStage(stage: BsonNode): TestPipeline<Document> =
		TestPipeline(context, chain.withStage(stage), collectionName)

	@Suppress("UNCHECKED_CAST")
	@DangerousMongoApi
	@LowLevelApi
	override fun <New : Any> reinterpret(): TestPipeline<New> =
		this as TestPipeline<New>

	override fun <ForeignDocument : Any> lookup(block: LookupStageOperators<Document, ForeignDocument>.() -> Unit): TestPipeline<Document> =
		super.lookup(block) as TestPipeline<Document>

	override fun match(filter: FilterQuery<Document>.() -> Unit): TestPipeline<Document> =
		super.match(filter) as TestPipeline<Document>

	override fun matchExpr(filter: AggregationOperators.() -> Value<Document, Boolean>): TestPipeline<Document> =
		super.matchExpr(filter) as TestPipeline<Document>

	override fun embedInUnionWith(writer: BsonFieldWriter) = with(writer) {
		writeString("coll", collectionName)
		writeArray("pipeline") {
			this@TestPipeline.writeTo(this)
		}
	}

	@LowLevelApi
	override fun embedInLookup(writer: BsonFieldWriter) = with(writer) {
		writeString("from", collectionName)

		if (chain.isNotEmpty()) {
			writeArray("pipeline") {
				this@TestPipeline.writeTo(this)
			}
		}
	}
}

suspend fun <Document : Any> TestDsl.TestPipeline(collectionName: String = "other"): TestPipeline<Document> =
	TestPipeline(testContext(), PipelineChainLink(testContext(), null, null), collectionName)

infix fun Pipeline<*>.shouldBeBson(@Language("MongoDB-JSON") expected: String) {
	this.toString() shouldBeBson expected
}
