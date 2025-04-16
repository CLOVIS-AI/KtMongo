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

package opensavvy.ktmongo.dsl.options.common

import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.query.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.query.common.AbstractExpression
import opensavvy.ktmongo.dsl.query.common.CompoundExpression
import opensavvy.ktmongo.dsl.query.common.Expression
import kotlin.reflect.KProperty1

/**
 * Describes in which order elements should be returned.
 *
 * For more information, see [WithSort.sort].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/cursor.sort)
 */
class SortOption<Document : Any>(
	context: BsonContext,
) : AbstractCompoundExpression(context), Option<Bson>, SortOptionDsl<Document> {

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun ascending(field: Field<Document, *>) {
		accept(SortExpression(field.path, 1, context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun descending(field: Field<Document, *>) {
		accept(SortExpression(field.path, -1, context))
	}

	@OptIn(LowLevelApi::class)
	override val value: Bson
		get() = context.buildDocument {
			for (child in children) {
				child.writeTo(this)
			}
		}

	@LowLevelApi
	override fun simplify(children: List<Expression>): AbstractExpression? =
		if (children.isNotEmpty()) this
		else null

	@LowLevelApi
	override fun write(writer: BsonFieldWriter, children: List<Expression>) = with(writer) {
		writeDocument("sort") {
			super.write(this, children)
		}
	}

	@LowLevelApi
	private class SortExpression(
		val path: Path,
		val value: Int,
		context: BsonContext,
	) : AbstractExpression(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeInt32(path.toString(), value)
		}
	}
}

/**
 * Describes in which order elements should be returned by the query.
 *
 * See [sort].
 */
@KtMongoDsl
interface WithSort<Document : Any> : Options {

	/**
	 * The order in which documents should be returned.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collection.find(
	 *     options = {
	 *         ascending(User::age)
	 *     },
	 *     filter = {
	 *         User::age.exists()
	 *     }
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/cursor.sort)
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun sort(
		block: SortOptionDsl<Document>.() -> Unit
	) {
		accept(SortOption<Document>(context).apply(block))
	}

}

/**
 * DSL to describe a sort order.
 *
 * This DSL declares the methods [ascending] and [descending] which describe in which order elements are sorted.
 *
 * For example,
 * ```kotlin
 * ascending(User::name)
 * descending(User::age)
 * ```
 * will sort users in alphabetical order of their name, and when users have the same name, will sort them in decreasing
 * order of their age.
 *
 * See [WithSort.sort].
 */
@KtMongoDsl
interface SortOptionDsl<Document : Any> : CompoundExpression, FieldDsl {

	/**
	 * If two documents have a different value of [field], the one with lesser value will be returned first.
	 *
	 * If this function is called multiple times, the first call takes precedence: subsequent calls determine the order
	 * of documents when they are equal according to the first call.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.find(
	 *     options = {
	 *         sort {
	 *             ascending(User::age)
	 *             ascending(User::name)
	 *         }
	 *     },
	 *     filter = {}
	 * )
	 * ```
	 *
	 * This will return users from the youngest to the oldest. If multiple users have the same age, they are returned
	 * in the alphabetical order of their name.
	 *
	 * @see descending To return documents in descending order.
	 */
	@KtMongoDsl
	fun ascending(field: Field<Document, *>)

	/**
	 * If two documents have a different value of [field], the one with lesser value will be returned first.
	 *
	 * If this function is called multiple times, the first call takes precedence: subsequent calls determine the order
	 * of documents when they are equal according to the first call.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.find(
	 *     options = {
	 *         sort {
	 *             ascending(User::age)
	 *             ascending(User::name)
	 *         }
	 *     },
	 *     filter = {}
	 * )
	 * ```
	 *
	 * This will return users from the youngest to the oldest. If multiple users have the same age, they are returned
	 * in the alphabetical order of their name.
	 *
	 * @see descending To return documents in descending order.
	 */
	@KtMongoDsl
	fun ascending(field: KProperty1<Document, *>) {
		ascending(field.field)
	}

	/**
	 * If two documents have a different value of [field], the one with greater value will be returned first.
	 *
	 * If this function is called multiple times, the first call takes precedence: subsequent calls determine the order
	 * of documents when they are equal according to the first call.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.find(
	 *     options = {
	 *         sort {
	 *             descending(User::age)
	 *             ascending(User::name)
	 *         }
	 *     },
	 *     filter = {}
	 * )
	 * ```
	 *
	 * This will return users from the oldest to the youngest. If multiple users have the same age, they are returned
	 * in the alphabetical order of their name.
	 *
	 * @see ascending To return documents in ascending order.
	 */
	@KtMongoDsl
	fun descending(field: Field<Document, *>)

	/**
	 * If two documents have a different value of [field], the one with greater value will be returned first.
	 *
	 * If this function is called multiple times, the first call takes precedence: subsequent calls determine the order
	 * of documents when they are equal according to the first call.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.find(
	 *     options = {
	 *         sort {
	 *             descending(User::age)
	 *             ascending(User::name)
	 *         }
	 *     },
	 *     filter = {}
	 * )
	 * ```
	 *
	 * This will return users from the oldest to the youngest. If multiple users have the same age, they are returned
	 * in the alphabetical order of their name.
	 *
	 * @see ascending To return documents in ascending order.
	 */
	@KtMongoDsl
	fun descending(field: KProperty1<Document, *>) {
		descending(field.field)
	}

}
