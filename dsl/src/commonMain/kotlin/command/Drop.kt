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
import opensavvy.ktmongo.dsl.options.WithWriteConcern

/**
 * Deleting an entire collection at once.
 */
@KtMongoDsl
class Drop<Document : Any> private constructor(
	val context: BsonContext,
	val options: DropOptions<Document>,
) {

	constructor(context: BsonContext) : this(context, DropOptions(context))
}

/**
 * The options for a [Drop] command.
 */
@OptIn(LowLevelApi::class)
class DropOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithWriteConcern
