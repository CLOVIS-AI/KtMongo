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

package opensavvy.ktmongo.dsl.expr

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.buildBsonDocument
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.stages.SetStageOperators
import opensavvy.ktmongo.dsl.aggregation.stages.createSetStage
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.CompoundExpression

/**
 * Interface describing the DSL when declaring an update with a pipeline.
 */
@KtMongoDsl
interface UpdatePipelineOperators<Document : Any> : CompoundExpression {

	/**
	 * Overrides the value of existing fields or inserts new ones.
	 *
	 * This method is equivalent to the [`$set` stage][opensavvy.ktmongo.dsl.aggregation.stages.HasSet].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::age set 12
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/set/)
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun set(
		block: SetStageOperators<Document>.() -> Unit,
	) {
		accept(createSetStage(context, block))
	}

	// In the future: unset
	// In the future: replaceWith

}

@LowLevelApi
internal class UpdatePipelineExpression<Document : Any> @LowLevelApi constructor(
	context: BsonContext,
) : AbstractCompoundExpression(context), UpdatePipelineOperators<Document> {

	val stages get() = children
		.map { buildBsonDocument { it.writeTo(this) } }

}
