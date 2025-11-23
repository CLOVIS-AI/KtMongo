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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.Options
import opensavvy.ktmongo.dsl.options.OptionsHolder
import opensavvy.ktmongo.dsl.options.WithWriteConcern
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode

/**
 * Inserting a single element in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.insertOne(User(name = "Bob", age = 18))
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/insert/)
 *
 * @see InsertMany
 * @see InsertOneOptions Options
 */
@KtMongoDsl
class InsertOne<Document : Any> private constructor(
	context: BsonContext,
	val options: InsertOneOptions<Document>,
	val document: Document,
) : Command, AbstractBsonNode(context), AvailableInBulkWrite<Document> {

	constructor(context: BsonContext, document: Document) : this(context, InsertOneOptions(context), document)

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("documents") {
			writeObjectSafe(document)
		}

		options.writeTo(this)
	}
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
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/insert/)
 *
 * @see InsertOne
 * @see InsertManyOptions Options
 */
@KtMongoDsl
class InsertMany<Document : Any> private constructor(
	context: BsonContext,
	val options: InsertManyOptions<Document>,
	val documents: List<Document>,
) : Command, AbstractBsonNode(context) {

	constructor(context: BsonContext, documents: List<Document>) : this(context, InsertManyOptions(context), documents)

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("documents") {
			for (document in documents) {
				writeObjectSafe(document)
			}
		}

		options.writeTo(this)
	}
}

/**
 * The options for a `collection.insertOne` operation.
 */
class InsertOneOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern

/**
 * The options for a `collection.insertMany` operation.
 */
class InsertManyOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern
