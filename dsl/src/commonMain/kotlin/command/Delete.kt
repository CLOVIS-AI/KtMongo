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
 * Deleting a single document from a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.deleteOne {
 *     User::name eq "Patrick"
 * }
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/delete/)
 */
@KtMongoDsl
class DeleteOne<Document : Any> private constructor(
	context: BsonContext,
	val options: DeleteOneOptions<Document>,
	val filter: FilterQuery<Document>,
) : AbstractBsonNode(context), Command, AvailableInBulkWrite<Document> {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, DeleteOneOptions(context), FilterQuery(context))

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("deletes") {
			writeDocument {
				writeDocument("q") {
					filter.writeTo(this)
				}
				writeInt32("limit", 1)
			}
		}

		options.writeTo(this)
	}
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
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/delete/)
 */
@KtMongoDsl
class DeleteMany<Document : Any> private constructor(
	context: BsonContext,
	val options: DeleteManyOptions<Document>,
	val filter: FilterQuery<Document>,
) : AbstractBsonNode(context), Command, AvailableInBulkWrite<Document> {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, DeleteManyOptions(context), FilterQuery(context))

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("deletes") {
			writeDocument {
				writeDocument("q") {
					filter.writeTo(this)
				}
			}
		}

		options.writeTo(this)
	}
}

/**
 * The options for a [DeleteOne] command.
 */
class DeleteOneOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern

/**
 * The options for a [DeleteMany] command.
 */
class DeleteManyOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern
