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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.ValueDsl
import opensavvy.ktmongo.dsl.query.FilterOperators

/**
 * Operators to compare two values.
 *
 * To learn more about aggregation operators, view [ValueDsl].
 */
interface ComparisonValueOperators : ValueOperators {

	/**
	 * Compares two aggregation values and returns `true` if they are equivalent.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val name: String,
	 *     val creationDate: Instant,
	 *     val releaseDate: Instant,
	 * )
	 *
	 * val releasedOnCreation = collection.aggregate()
	 *     .match {
	 *         expr {
	 *             of(Product::creationDate) eq of(Product::releaseDate)
	 *         }
	 *     }
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/eq/)
	 * - [Comparison algorithm](https://www.mongodb.com/docs/manual/reference/bson-type-comparison-order/#std-label-bson-types-comparison-order)
	 *
	 * @see ne Negation of this operator.
	 * @see FilterOperators.eq Equivalent operator in regular queries.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any, Result> Value<Context, Result>.eq(other: Value<Context, Result>): Value<Context, Boolean> =
		ComparisonValueOperator(context, this, other, "eq")

	/**
	 * Compares two aggregation values and returns `true` if they are not equivalent.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val name: String,
	 *     val creationDate: Instant,
	 *     val releaseDate: Instant,
	 * )
	 *
	 * val notReleasedOnCreation = collection.aggregate()
	 *     .match {
	 *         expr {
	 *             of(Product::creationDate) eq of(Product::releaseDate)
	 *         }
	 *     }
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/ne/)
	 * - [Comparison algorithm](https://www.mongodb.com/docs/manual/reference/bson-type-comparison-order/#std-label-bson-types-comparison-order)
	 *
	 * @see eq Negation of this operator.
	 * @see FilterOperators.ne Equivalent operator in regular queries.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any, Result> Value<Context, Result>.ne(other: Value<Context, Result>): Value<Context, Boolean> =
		ComparisonValueOperator(context, this, other, "ne")

	// TODO: document the other operators once 'project' is implemented, since the official examples use 'project' to demonstrate them

	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any, Result> Value<Context, Result>.gt(other: Value<Context, Result>): Value<Context, Boolean> =
		ComparisonValueOperator(context, this, other, "gt")

	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any, Result> Value<Context, Result>.gte(other: Value<Context, Result>): Value<Context, Boolean> =
		ComparisonValueOperator(context, this, other, "gte")

	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any, Result> Value<Context, Result>.lt(other: Value<Context, Result>): Value<Context, Boolean> =
		ComparisonValueOperator(context, this, other, "lt")

	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	infix fun <Context : Any, Result> Value<Context, Result>.lte(other: Value<Context, Result>): Value<Context, Boolean> =
		ComparisonValueOperator(context, this, other, "lte")

	@OptIn(LowLevelApi::class)
	private class ComparisonValueOperator<Context : Any, T>(
		context: BsonContext,
		private val operandA: Value<Context, T>,
		private val operandB: Value<Context, T>,
		private val operator: String,
	) : AbstractValue<Context, Boolean>(context) {

		@LowLevelApi
		override fun write(writer: BsonValueWriter) = with(writer) {
			writeDocument {
				writeArray("$$operator") {
					operandA.writeTo(this)
					operandB.writeTo(this)
				}
			}
		}
	}
}
