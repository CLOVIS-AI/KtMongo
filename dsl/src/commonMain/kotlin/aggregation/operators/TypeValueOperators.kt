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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value

/**
 * Operators to interact with type information.
 *
 * To learn more about aggregation operators, view [AggregationOperators].
 */
@KtMongoDsl
interface TypeValueOperators : ValueOperators {

	/**
	 * Gets the [BsonType] of the current value.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.aggregate()
	 *     .project {
	 *         Field.unsafe<Boolean>("nameIsString") set (of(User::name).type eq of(BsonType.String))
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/type/)
	 *
	 * @see BsonType List of possible types.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <R : Any> Value<R, *>.type: Value<R, BsonType>
		get() = TypeValue(context, this)

	@OptIn(LowLevelApi::class)
	private class TypeValue<Root : Any>(
		context: BsonContext,
		private val value: Value<Root, *>,
	) : Value<Root, BsonType>, AbstractValue<Root, BsonType>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				write("\$type") {
					value.writeTo(this)
				}
			}
		}
	}
}
