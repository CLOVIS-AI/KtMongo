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

import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.aggregation.operators.*
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.query.FilterQuery

/**
 * DSL to instantiate aggregation values, available in most aggregation stages.
 *
 * ### What are aggregation values?
 *
 * In MongoDB, operators targeting regular queries and aggregation pipelines often have the same name but a different
 * syntax. Using KtMongo, operators keep the same name __and syntax__ for both usages, but the way they are used in
 * practice is still quite different. For example, compare [`$eq` (query)][FilterQuery.eq]
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
 *     expr { // Use aggregation values in a regular find() request
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
 * As you can see, we use the [of][ValueOperators.of] method to convert from Kotlin values or from field names to aggregation values.
 * Because each side of an operator accepts an aggregation value, we can thus compare multiple fields from the same document,
 * use conditionals or other complex requests.
 *
 * In this example, we used the [`$expr`][FilterQuery.expr] query predicate to write an aggregation value within
 * a regular query. `$expr` requires returning a boolean, so the last operator of the value needed to be a boolean-returning
 * operator, which `$lt` is one of. In other contexts, aggregation values can be typed with any other document type.
 *
 * When writing your first aggregation pipelines, keep in mind that **only the last value of each lambda is used**,
 * just like when calling Kotlin functions that return a value that is unused.
 *
 * ### Operators
 *
 * Access values:
 * - [`$literal`][ValueOperators.of]
 *
 * Conditionally compute values:
 * - [`$cond`][ConditionalValueOperators.cond]
 * - [`$switch`][ConditionalValueOperators.switch]
 *
 * Compare values:
 * - [`$eq`][ComparisonValueOperators.eq]
 * - [`$ne`][ComparisonValueOperators.ne]
 * - [`$gt`][ComparisonValueOperators.gt]
 * - [`$lt`][ComparisonValueOperators.lt]
 * - [`$gte`][ComparisonValueOperators.gte]
 * - [`$lte`][ComparisonValueOperators.lte]
 *
 * Arithmetic operators:
 * - [`$abs`][ArithmeticValueOperators.abs]
 * - [`$add`][ArithmeticValueOperators.plus]
 * - [`$ceil`][ArithmeticValueOperators.ceil]
 * - [`$concat`][ArithmeticValueOperators.concat]
 *
 * Array operators:
 * - [`$filter`][ArrayValueOperators.filter]
 * - [`$firstN`][ArrayValueOperators.take]
 * - [`$lastN`][ArrayValueOperators.takeLast]
 * - [`$map`][ArrayValueOperators.map]
 * - [`$sortArray`][ArrayValueOperators.sortedBy]
 *
 * Document operators:
 * - [`$getField`][ValueOperators.div]
 *
 * Trigonometric operators and angle management:
 * - [`$acos`][TrigonometryValueOperators.acos]
 * - [`$acosh`][TrigonometryValueOperators.acosh]
 * - [`$asin`][TrigonometryValueOperators.asin]
 * - [`$asinh`][TrigonometryValueOperators.asinh]
 * - [`$atan`][TrigonometryValueOperators.atan]
 * - [`$atanh`][TrigonometryValueOperators.atanh]
 * - [`$cos`][TrigonometryValueOperators.cos]
 * - [`$cosh`][TrigonometryValueOperators.cosh]
 * - [`$sin`][TrigonometryValueOperators.sin]
 * - [`$sinh`][TrigonometryValueOperators.sinh]
 * - [`$tan`][TrigonometryValueOperators.tan]
 * - [`$tanh`][TrigonometryValueOperators.tanh]
 * - [`$degreesToRadians`][TrigonometryValueOperators.toRadians]
 * - [`$radiansToDegrees`][TrigonometryValueOperators.toDegrees]
 *
 * @see Value Representation of an aggregation value.
 */
@KtMongoDsl
interface AggregationOperators : ValueOperators,
	ArrayValueOperators,
	ComparisonValueOperators,
	ConditionalValueOperators,
	ArithmeticValueOperators,
	TrigonometryValueOperators
