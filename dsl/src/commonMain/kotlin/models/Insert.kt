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
import opensavvy.ktmongo.dsl.options.InsertManyOptions
import opensavvy.ktmongo.dsl.options.InsertOneOptions
import opensavvy.ktmongo.dsl.tree.ImmutableNode
import opensavvy.ktmongo.dsl.tree.Node

/**
 * Inserting a single element in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.insertOne(User(name = "Bob", age = 18))
 * ```
 *
 * @see InsertMany
 * @see InsertOneOptions Options
 */
@KtMongoDsl
class InsertOne<Document : Any> private constructor(
	val context: BsonContext,
	val options: InsertOneOptions<Document>,
	val document: Document,
) : Node by ImmutableNode, AvailableInBulkWrite<Document> {

	constructor(context: BsonContext, document: Document) : this(context, InsertOneOptions(context), document)
}

/**
 * Inserting multiple elements in a collection in a single operation.
 *
 * ### Example
 *
 * ```kotlin
 * users.insertMany(User(name = "Bob", age = 18), User(name = "Fred", age = 19), User(name = "Arthur", age = 22))
 * ```
 *
 * @see InsertOne
 * @see InsertManyOptions Options
 */
@KtMongoDsl
class InsertMany<Document : Any> private constructor(
	val context: BsonContext,
	val options: InsertManyOptions<Document>,
	val documents: List<Document>,
) : Node by ImmutableNode {

	constructor(context: BsonContext, documents: List<Document>) : this(context, InsertManyOptions(context), documents)
}
