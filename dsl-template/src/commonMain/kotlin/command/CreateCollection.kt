/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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
import opensavvy.ktmongo.dsl.options.*
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode

/**
 * Creates a collection.
 *
 * MongoDB always automatically creates a non-existing collection on the first insert.
 * This method is useful to customize the options of the collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.create {}
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/create/)
 */
@KtMongoDsl
class CreateCollection<Document : Any> private constructor(
	context: BsonContext,
	val options: CreateCollectionOptions<Document>,
) : Command, AbstractBsonNode(context) {

	constructor(context: BsonContext) : this(context, CreateCollectionOptions(context))

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		options.writeTo(this)
	}
}

/**
 * The options for a [CreateCollection] command.
 */
class CreateCollectionOptions<Document : Any>(context: BsonContext) :
	Options by OptionsHolder(context),
	HasCapped,
	HasValidation<Document>,
	HasWriteConcern,
	HasComment
