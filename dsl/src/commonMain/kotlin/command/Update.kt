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
import opensavvy.ktmongo.dsl.options.Options
import opensavvy.ktmongo.dsl.options.OptionsHolder
import opensavvy.ktmongo.dsl.options.WithWriteConcern
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import opensavvy.ktmongo.dsl.tree.ImmutableNode
import opensavvy.ktmongo.dsl.tree.Node

/**
 * Updating a single element in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.updateOne({ User::name eq "foo" }) {
 *     User::age set 18
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see UpdateQuery Update operators
 */
@KtMongoDsl
class UpdateOne<Document : Any> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterQuery<Document>,
	val update: UpdateQuery<Document>,
) : AvailableInBulkWrite<Document>, Node by ImmutableNode {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterQuery(context), UpdateQuery(context))
}

/**
 * Updating a single element in a collection, creating it if it doesn't exist.
 *
 * ### Example
 *
 * ```kotlin
 * users.upsertOne({ User::name eq "foo" }) {
 *     User::age set 18
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see UpdateQuery Update operators
 */
@KtMongoDsl
class UpsertOne<Document : Any> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterQuery<Document>,
	val update: UpsertQuery<Document>,
) : AvailableInBulkWrite<Document>, Node by ImmutableNode {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterQuery(context), UpsertQuery(context))
}

/**
 * Updating multiple elements in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.updateMany({ User::name eq "foo" }) {
 *     User::age set 18
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see UpdateQuery Update operators
 */
@KtMongoDsl
class UpdateMany<Document : Any> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterQuery<Document>,
	val update: UpdateQuery<Document>,
) : AvailableInBulkWrite<Document>, Node by ImmutableNode {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterQuery(context), UpdateQuery(context))
}

/**
 * The options for a [UpdateOne], [UpsertOne], [UpdateMany] operation.
 */
class UpdateOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern
