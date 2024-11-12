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

package opensavvy.ktmongo.dsl.models

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.expr.*
import opensavvy.ktmongo.dsl.options.UpdateOptions
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
 * @see FilterOperators Filter operators
 * @see UpdateOperators Update operators
 */
@KtMongoDsl
class UpdateOne<Document> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterOperators<Document>,
	val update: UpdateOperators<Document>,
) : AvailableInBulkWrite<Document>, Node by ImmutableNode {

	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterExpression(context), UpdateExpression(context))
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
 * @see FilterOperators Filter operators
 * @see UpdateOperators Update operators
 */
@KtMongoDsl
class UpsertOne<Document> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterOperators<Document>,
	val update: UpsertOperators<Document>,
) : AvailableInBulkWrite<Document>, Node by ImmutableNode {

	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterExpression(context), UpdateExpression(context))
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
 * @see FilterOperators Filter operators
 * @see UpdateOperators Update operators
 */
@KtMongoDsl
class UpdateMany<Document> private constructor(
	val context: BsonContext,
	val options: UpdateOptions<Document>,
	val filter: FilterOperators<Document>,
	val update: UpdateOperators<Document>,
) : AvailableInBulkWrite<Document>, Node by ImmutableNode {

	constructor(context: BsonContext) : this(context, UpdateOptions(context), FilterExpression(context), UpdateExpression(context))
}
