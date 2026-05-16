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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonDocument
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldImpl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode

/**
 * Specifies which array elements should be updated by [UpdateQuery.filter].
 *
 * For more information, see [UpdateQuery.filter] and [WithArrayFilters.arrayFilter].
 */
class ArrayFiltersOption internal constructor(
	private val arrayFilters: List<Pair<String, BsonNode>>,
	context: BsonContext,
) : AbstractOption("arrayFilters", context) {

	/**
	 * The different registered array filters.
	 */
	@OptIn(LowLevelApi::class)
	val filters: List<BsonDocument>
		get() = read()
			.decodeArray()
			.asIterable()
			.map { it.decodeDocument() }

	@OptIn(DangerousMongoApi::class)
	@LowLevelApi
	override fun merge(other: Option): Option {
		require(other is ArrayFiltersOption) { "Cannot merge arrayFilters options of different types: ${this::class} and ${other::class}" }

		return ArrayFiltersOption(
			arrayFilters + other.arrayFilters,
			context,
		)
	}

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeArray {
			for ((_, filter) in arrayFilters) {
				writeDocument {
					filter.writeTo(this)
				}
			}
		}
	}
}

/**
 * Specifies which array elements should be updated by [UpdateQuery.filter].
 *
 * See [arrayFilter].
 */
@KtMongoDsl
interface WithArrayFilters : Options {

	/**
	 * Declares an array filter, which can then be passed to [UpdateQuery.filter].
	 *
	 * To learn more about the usage of array filters, see [UpdateQuery.filter].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val score: Int,
	 * )
	 *
	 * class Group(
	 *     val _id: ObjectId,
	 *     val users: List<User>,
	 * )
	 *
	 * groups.updateOne(
	 *     filter = {
	 *         Group::_id eq ObjectId("…")
	 *     },
	 *     options = {
	 *         arrayFilter("adult") {
	 *             it / User::age gte 18
	 *         }
	 *     },
	 *     update = {
	 *         Group::users.filter("adult") / User::score inc 1
	 *     }
	 * )
	 * ```
	 *
	 * Note the `it` parameter in the `arrayFilter` function, which allows declaring a filter
	 * expression on a given element. The `arrayFilter` block accepts the same syntax as `find()`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/positional-filtered)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun <Item> arrayFilter(
		id: String,
		filter: ArrayFiltersOptionDsl<Item>.(it: Field<ArrayFiltersOptionDsl.IteratorType<Item>, Item>) -> Unit,
	) {
		accept(ArrayFiltersOption(listOf(id to ArrayFiltersBlockNode<Item>(context).apply { filter(FieldImpl(Path(id))) }), context))
	}

	@OptIn(LowLevelApi::class)
	private class ArrayFiltersBlockNode<Item>(
		context: BsonContext,
		private val filterQuery: FilterQuery<ArrayFiltersOptionDsl.IteratorType<Item>> = FilterQuery(context),
	) : CompoundBsonNode,
		ArrayFiltersOptionDsl<Item>,
		FilterQuery<ArrayFiltersOptionDsl.IteratorType<Item>> by filterQuery
}

/**
 * DSL to declare array filters.
 *
 * See [WithArrayFilters.arrayFilter].
 */
@KtMongoDsl
interface ArrayFiltersOptionDsl<Document> : CompoundBsonNode,
	FilterQuery<ArrayFiltersOptionDsl.IteratorType<Document>> {

	/**
	 * Special type used by the [WithArrayFilters.arrayFilter] lambda parameter to designate
	 * the current array filter.
	 */
	@KtMongoDsl
	interface IteratorType<@Suppress("unused") Document>
}
