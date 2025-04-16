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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.query.common.AbstractExpression

/**
 * Pipeline implementing the `$sample` stage.
 */
@KtMongoDsl
interface HasSample<Document : Any> : Pipeline<Document> {

	/**
	 * Randomly selects [size] documents.
	 *
	 * ### Pipeline optimizations
	 *
	 * MongoDB is able to perform sampling more efficiently if it is the first stage of the pipeline and [size] is less
	 * than 5% of the collection size.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sample/)
	 *
	 * @see HasLimit.limit Selects the first elements found.
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun sample(size: Int): Pipeline<Document> =
		withStage(SampleStage(size, context))
}

private class SampleStage(
	val size: Int,
	context: BsonContext,
) : AbstractExpression(context) {

	init {
		require(size >= 1) { "The sample size should be at least 1. Found: $size" }
	}

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$sample") {
			writeInt32("size", size)
		}
	}
}
