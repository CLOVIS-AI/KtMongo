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

package opensavvy.ktmongo.dsl.command

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.Options
import opensavvy.ktmongo.dsl.options.OptionsHolder
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.ImmutableNode
import opensavvy.ktmongo.dsl.tree.Node

/**
 * Deleting a single document from a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.deleteOne {
 *     User::name eq "Patrick"
 * }
 * ```
 */
@KtMongoDsl
class DeleteOne<Document : Any> private constructor(
	val context: BsonContext,
	val options: DeleteOneOptions<Document>,
	val filter: FilterQuery<Document>,
) : Node by ImmutableNode, AvailableInBulkWrite<Document> {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, DeleteOneOptions(context), FilterQuery(context))
}

/**
 * Deleting multiple documents from a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.deleteMany {
 *     User::age gte 200
 * }
 * ```
 */
@KtMongoDsl
class DeleteMany<Document : Any> private constructor(
	val context: BsonContext,
	val options: DeleteManyOptions<Document>,
	val filter: FilterQuery<Document>,
) : Node by ImmutableNode, AvailableInBulkWrite<Document> {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, DeleteManyOptions(context), FilterQuery(context))
}

/**
 * The options for a [DeleteOne] command.
 */
class DeleteOneOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context)

/**
 * The options for a [DeleteMany] command.
 */
class DeleteManyOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context)
