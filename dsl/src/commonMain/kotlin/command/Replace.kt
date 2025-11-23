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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.Options
import opensavvy.ktmongo.dsl.options.OptionsHolder
import opensavvy.ktmongo.dsl.options.WithWriteConcern
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode

/**
 * Replaces a single element in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.replaceOne({ User::name eq "foo" }, User("Bob", 15))
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
 *
 * @see FilterQuery Filter operators
 */
@KtMongoDsl
class ReplaceOne<Document : Any> private constructor(
	context: BsonContext,
	val options: ReplaceOptions<Document>,
	val filter: FilterQuery<Document>,
	val document: Document,
) : AbstractBsonNode(context), Command, AvailableInBulkWrite<Document> {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext, document: Document) : this(context, ReplaceOptions(context), FilterQuery(context), document)

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("updates") {
			writeDocument {
				writeDocument("q") {
					filter.writeTo(this)
				}
				writeObjectSafe("u", document)
				writeBoolean("upsert", false)
				writeBoolean("multi", false)
			}
		}

		options.writeTo(this)
	}
}

/**
 * Replaces a single element in a collection, or inserts it if it doesn't exist.
 *
 * ### Example
 *
 * ```kotlin
 * users.replaceOrInsertOne({ User::name eq "foo" }, User("Bob", 15))
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
 *
 * @see FilterQuery Filter operators
 */
@KtMongoDsl
class RepsertOne<Document : Any> private constructor(
	context: BsonContext,
	val options: ReplaceOptions<Document>,
	val filter: FilterQuery<Document>,
	val document: Document,
) : AbstractBsonNode(context), Command, AvailableInBulkWrite<Document> {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext, document: Document) : this(context, ReplaceOptions(context), FilterQuery(context), document)

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("updates") {
			writeDocument {
				writeDocument("q") {
					filter.writeTo(this)
				}
				writeObjectSafe("u", document)
				writeBoolean("upsert", true)
				writeBoolean("multi", false)
			}
		}

		options.writeTo(this)
	}
}

/**
 * The options for a [ReplaceOne] operation.
 */
class ReplaceOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern
