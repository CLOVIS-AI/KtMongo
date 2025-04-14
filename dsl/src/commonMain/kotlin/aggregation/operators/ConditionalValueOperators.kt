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
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value

/**
 * Operators to conditionally create a value.
 *
 * To learn more about aggregation operators, view [AggregationOperators].
 */
@KtMongoDsl
interface ConditionalValueOperators : ValueOperators {

	/**
	 * Decides between two [values][Value] depending on the evaluation of a boolean value.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val score: Int,
	 *     val multiplier: Int,
	 * )
	 *
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::score set cond(
	 *             condition = of(User::multiplier) gt of(2),
	 *             ifTrue = of(User::score) * of(User::multiplier),
	 *             ifFalse = of(User::score)
	 *         )
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun <R : Any, T> cond(
		condition: Value<R, Boolean>,
		ifTrue: Value<R, T>,
		ifFalse: Value<R, T>,
	): Value<R, T> =
		ConditionalValue(context, condition, ifTrue, ifFalse)

	@OptIn(LowLevelApi::class)
	private class ConditionalValue<Root: Any, Type>(
		context: BsonContext,
		private val condition: Value<Root, Boolean>,
		private val ifTrue: Value<Root, Type>,
		private val ifFalse: Value<Root, Type>,
	) : Value<Root, Type>, AbstractValue<Root, Type>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeDocument("\$cond") {
					write("if") {
						condition.writeTo(this)
					}

					write("then") {
						ifTrue.writeTo(this)
					}

					write("else") {
						ifFalse.writeTo(this)
					}
				}
			}
		}
	}
}
