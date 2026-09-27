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

import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.*
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import kotlin.uuid.Uuid

/**
 * Lists collections.
 *
 * ### Example
 *
 * ```kotlin
 * database.collections()
 *     .forEach {
 *         println("${it.name}")
 *     }
 * ```
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/listcollections)
 */
class ListCollections private constructor(
	context: BsonContext,
	val options: ListCollectionsOptions,
	val filter: FilterQuery<CollectionInfo>,
) : Command, AbstractBsonNode(context) {

	@OptIn(LowLevelApi::class)
	constructor(context: BsonContext) : this(context, ListCollectionsOptions(context), FilterQuery(context))

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		options.writeTo(this)

		writeDocument("filter") {
			filter.writeTo(this)
		}
	}
}

/**
 * The options for the [ListCollections] command.
 */
class ListCollectionsOptions(context: BsonContext) :
	Options by OptionsHolder(context),
	HasAuthorizedCollections,
	HasComment,
	HasNameOnly

/**
 * Information about a collection, as returned by the [ListCollections] command.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/listcollections/#output)
 */
interface CollectionInfo {

	/**
	 * The name of the collection.
	 */
	val name: String

	/**
	 * Whether it's a regular collection, a view, or a time series.
	 *
	 * Note: if `nameOnly` is specified, the official driver does not support this field.
	 */
	val type: Type

	/**
	 * The options that were passed when creating the collection.
	 *
	 * Should match [CreateCollectionOptions].
	 *
	 * If `nameOnly` was specified, contains an empty document.
	 */
	val options: BsonDocument

	/**
	 * `true` if this collection is read-only.
	 *
	 * If `nameOnly` was specified, contains `null`.
	 */
	val readOnly: Boolean?

	/**
	 * The UUID of the collection.
	 *
	 * If `nameOnly` was specified, contains `null`.
	 */
	val uuid: Uuid?

	/**
	 * Information on the index on the `_id` field.
	 *
	 * If `nameOnly` was specified, contains `null`.
	 */
	val idIndex: BsonDocument?

	/**
	 * The kind of collection.
	 *
	 * See [CollectionInfo.type].
	 */
	enum class Type(val bsonName: String) {
		Collection("collection"),
		View("view"),
		TimeSeries("timeseries"),
	}
}
