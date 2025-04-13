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
import opensavvy.ktmongo.dsl.options.WithLimit
import opensavvy.ktmongo.dsl.query.FilterQuery

/**
 * Counting a number of documents in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.count({ limit(99) }) {
 *     User::age lt 18
 * }
 * ```
 *
 * @see FilterQuery Filter operators
 * @see CountOptions Options
 */
@KtMongoDsl
class Count<Document : Any> private constructor(
	val context: BsonContext,
	val options: CountOptions<Document>,
	val filter: FilterQuery<Document>,
) {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, CountOptions(context), FilterQuery(context))

}

/**
 * The options for a [Count] command.
 */
@OptIn(LowLevelApi::class)
class CountOptions<Document>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithLimit
