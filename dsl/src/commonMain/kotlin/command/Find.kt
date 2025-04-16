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
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.FindOptions
import opensavvy.ktmongo.dsl.query.FilterExpression
import opensavvy.ktmongo.dsl.query.FilterOperators
import opensavvy.ktmongo.dsl.query.common.AbstractExpression

/**
 * Searching for documents in a collection.
 *
 * ### Example
 *
 * ```kotlin
 * users.find(options = { limit(12) }) {
 *     User::age lt 18
 * }
 * ```
 *
 * @see FilterOperators Filter operators
 * @see FindOptions Options
 */
@KtMongoDsl
class Find<Document : Any> private constructor(
	context: BsonContext,
	val options: FindOptions<Document>,
	val filter: FilterOperators<Document>,
) : AbstractExpression(context) {

	constructor(context: BsonContext) : this(context, FindOptions(context), FilterExpression(context))

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("options") {
			options.writeTo(this)
		}
		writeDocument("filter") {
			filter.writeTo(this)
		}
	}
}
