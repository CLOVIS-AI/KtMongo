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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.operators.ComparisonValueOperators
import opensavvy.ktmongo.dsl.aggregation.operators.ConditionalOperators
import opensavvy.ktmongo.dsl.aggregation.operators.ValueOperators
import opensavvy.ktmongo.dsl.expr.FilterOperators
import opensavvy.ktmongo.dsl.path.Field
import kotlin.reflect.KProperty1

/**
 * DSL to instantiate aggregation values, usually automatically added into scope by aggregation stages.
 *
 * ### What are aggregation values?
 *
 * In MongoDB, operators targeting regular queries and aggregation pipelines often have the same name but a different
 * syntax. Using KtMongo, operators keep the same name __and syntax__ for both usages, but the way they are used in
 * practice is still quite different. For example, compare [`$eq` (query)][opensavvy.ktmongo.dsl.expr.FilterOperators.eq]
 * and [`$eq` (aggregation)][ComparisonValueOperators.eq].
 *
 * In regular queries, operators do not have a return type, and invoking them immediately adds them to the current query.
 * If multiple operators are called within the operation lambda, each of them is added to the query: operators "bind"
 * themselves on invoking. Operators always take a [field][Field] as first operand, and value as second operand. As an example:
 * ```kotlin
 * users.find {
 *     User::age gt 18
 *     User::scores[0] eq 10
 * }
 * ```
 * which generates:
 * ```json
 * { "$and": [{ "$eq": { "age": 18 } }, { "$eq": { "scores.0": 10 } }] }
 * ```
 *
 * In aggregations, operators have a return type and **only the last value of a block is taken into account**, just like
 * in regular Kotlin code. We can use regular variables to store parts of a more complex expression.
 * Operators accept multiple aggregation values which must conform to some type requirements. As an example:
 * ```kotlin
 * users.find {
 *     expr {
 *         val maxScore = of(User::scores).max()
 *         maxScore lt of(15)
 *     }
 * }
 * ```
 * which generates:
 * ```json
 * { "expr": { "lt": [{ "$max": "$scores" }, { "$literal": 15 }] } }
 * ```
 *
 * As you can see, we use the [of] method to convert from Kotlin values or from field names to aggregation values.
 * Because each side of an operator accepts an aggregation value, we can thus compare multiple fields from the same document,
 * use conditionals or other complex requests.
 *
 * In this example, we used the [`$expr`][FilterOperators.expr] query predicate to write an aggregation value within
 * a regular query. `$expr` requires returning a boolean, so the last operator of the value needed to be a boolean-returning
 * operator, which `$lt` is one of. In other contexts, aggregation values can be typed with any other document type.
 *
 * When writing your first aggregation pipelines, keep in mind that **only the last value of each lambda is used**,
 * just like when calling Kotlin functions that return a value that is unused.
 *
 * ### Operators
 *
 * Access values:
 * - [`$literal`][of]
 *
 * Conditionally compute values:
 * - [`$cond`][cond]
 *
 * Compare values:
 * - [`$eq`][eq]
 * - [`$ne`][ne]
 * - [`$gt`][gt]
 * - [`$lt`][lt]
 * - [`$gte`][gte]
 * - [`$lte`][lte]
 *
 * @see Value Representation of an aggregation value.
 */
@KtMongoDsl
interface ValueDsl : ValueOperators,
	ComparisonValueOperators,
	ConditionalOperators {

	/**
	 * Refers to a [field] within an [aggregation value][ValueDsl].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val acceptanceDate: Instant,
	 *     val publishingDate: Instant,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::publishingDate) lt of(Product::acceptanceDate)
	 *     }
	 * }
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun <Context : Any, Result> of(field: Field<Context, Result>): Value<Context, Result> =
		FieldValue(field, context)

	/**
	 * Refers to a [field] within an [aggregation value][ValueDsl].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val acceptanceDate: Instant,
	 *     val publishingDate: Instant,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::publishingDate) lt of(Product::acceptanceDate)
	 *     }
	 * }
	 * ```
	 */
	fun <Context : Any, Result> of(field: KProperty1<Context, Result>): Value<Context, Result> =
		of(field.field)

	/**
	 * Refers to a Kotlin [value] within an [aggregation value][ValueDsl].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val age: Int,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::age) lt of(15)
	 *     }
	 * }
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun <Result> of(value: Result): Value<Any, Result> =
		LiteralValue(value, context)

}

@OptIn(LowLevelApi::class)
private class FieldValue<Context : Any, Result>(
	val field: Field<Context, Result>,
	context: BsonContext,
) : AbstractValue<Context, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeString("$$field")
	}
}

@OptIn(LowLevelApi::class)
private class LiteralValue<Result>(
	val value: Any?,
	context: BsonContext,
) : AbstractValue<Any, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeDocument {
			writeObjectSafe("\$literal", value, context)
		}
	}
}
