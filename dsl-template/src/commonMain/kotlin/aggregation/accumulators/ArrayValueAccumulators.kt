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

package opensavvy.ktmongo.dsl.aggregation.accumulators

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AccumulationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode

/**
 * Accumulators to create arrays.
 *
 * To learn more about accumulation operators, see [AccumulationOperators].
 */
@KtMongoDsl
interface ArrayValueAccumulators<From : Any, Into : Any> : ValueAccumulators<From, Into> {

	// region $addToSet

	/**
	 * Accumulates [value] into an array of unique values.
	 *
	 * Duplicate values are ignored.
	 *
	 * The order of values in the resulting array is not specified.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: String,
	 *     val age: Int,
	 *     val cityName: String,
	 * )
	 *
	 * class AgesByCity(
	 *     val _id: String, // City name
	 *     val ages: List<Int>,
	 * )
	 *
	 * users.aggregate()
	 *     .group {
	 *         AgesByCity::_id set User::cityName
	 *         AgesByCity::ages addToSet User::age
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/addtoset/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	infix fun <T> Field<Into, Collection<@kotlin.internal.Exact T>>.addToSet(value: Value<From, T>) {
		accept(SimpleValueAccumulator(value, this.path, "addToSet", context))
	}

	// endregion

	@LowLevelApi
	private class SimpleValueAccumulator(
		val value: Value<*, *>,
		val into: Path,
		val operatorName: String,
		context: BsonContext,
	) : AbstractBsonNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(into.toString()) {
				write("$$operatorName") {
					value.writeTo(this)
				}
			}
		}
	}
}
