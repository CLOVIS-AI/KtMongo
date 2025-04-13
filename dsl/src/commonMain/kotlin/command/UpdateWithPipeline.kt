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

package opensavvy.ktmongo.dsl.command

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery
import opensavvy.ktmongo.dsl.query.UpdateWithPipelineQueryImpl
import opensavvy.ktmongo.dsl.tree.ImmutableNode
import opensavvy.ktmongo.dsl.tree.Node

/**
 * Updating a single element in a collection, using a pipeline.
 *
 * ### Example
 *
 * ```kotlin
 * users.updateOneWithPipeline({ User::name eq "foo" }) {
 *     set {
 *         User::age set 18
 *     }
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see UpdateQuery Update operators
 */
@KtMongoDsl
class UpdateOneWithPipeline<Document : Any> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterQuery<Document>,
	val update: UpdateWithPipelineQuery<Document>,
) : Node by ImmutableNode {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterQuery(context), UpdateWithPipelineQuery(context))

	@LowLevelApi
	val updates get() = (update as UpdateWithPipelineQueryImpl<*>).stages
}

/**
 * Updating a single element in a collection, creating it if it doesn't exist, using a pipeline.
 *
 * ### Example
 *
 * ```kotlin
 * users.upsertOneWithPipeline({ User::name eq "foo" }) {
 *     set {
 *         User::age set 18
 *     }
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see UpdateQuery Update operators
 */
@KtMongoDsl
class UpsertOneWithPipeline<Document : Any> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterQuery<Document>,
	val update: UpdateWithPipelineQuery<Document>,
) : Node by ImmutableNode {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterQuery(context), UpdateWithPipelineQuery(context))

	@LowLevelApi
	val updates get() = (update as UpdateWithPipelineQueryImpl<*>).stages
}

/**
 * Updating multiple elements in a collection, using a pipeline.
 *
 * ### Example
 *
 * ```kotlin
 * users.updateManyWithPipeline({ User::name eq "foo" }) {
 *     set {
 *         User::age set 18
 *     }
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see UpdateQuery Update operators
 */
@KtMongoDsl
class UpdateManyWithPipeline<Document : Any> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterQuery<Document>,
	val update: UpdateWithPipelineQuery<Document>,
) : Node by ImmutableNode {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterQuery(context), UpdateWithPipelineQuery(context))

	@LowLevelApi
	val updates get() = (update as UpdateWithPipelineQueryImpl<*>).stages
}
