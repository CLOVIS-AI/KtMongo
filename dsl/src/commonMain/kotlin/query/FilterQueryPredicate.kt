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

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.DEPRECATED_IN_BSON_SPEC
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.query.common.CompoundExpression

/**
 * DSL for MongoDB operators that are used as predicates in conditions in a context where the targeted field is already
 * specified.
 *
 * ### Example
 *
 * ```kotlin
 * class User(
 *     val name: String?,
 *     val age: Int,
 * )
 *
 * collection.find {
 *     User::name { //(1)
 *         eq("foo")
 *     }
 * }
 * ```
 *
 * 1. By referring to a specific property, we obtain a [FilterQueryPredicate] that we can use
 * to declare many operators on that property.
 *
 * If you can't find the operator you're searching for, visit the [tracking issue](https://gitlab.com/opensavvy/ktmongo/-/issues/4).
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/)
 *
 * @param T The type on which this predicate applies.
 * For example, if the selected field is of type `String`, then `T` is `String`.
 */
@KtMongoDsl
interface FilterQueryPredicate<T> : CompoundExpression, FieldDsl {

	// region $eq

	/**
	 * Matches documents where the value of a field equals the [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         eq("foo")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 *
	 * @see FilterQuery.eq Shorthand.
	 */
	@KtMongoDsl
	fun eq(value: T)

	/**
	 * Matches documents where the value of a field equals [value].
	 *
	 * If [value] is `null`, the operator is not added (all documents are matched).
	 *
	 * ### Example
	 *
	 * This operator is useful to simplify searches when the criteria is optional.
	 * For example, instead of writing:
	 * ```kotlin
	 * collection.find {
	 *     User::name {
	 *         if (criteria.name != null)
	 *             eq(criteria.name)
	 *     }
	 * }
	 * ```
	 * this operator can be used instead:
	 * ```kotlin
	 * collection.find {
	 *     User::name {
	 *         eqNotNull(criteria.name)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 *
	 * @see FilterQuery.eqNotNull Shorthand.
	 * @see eq Equality filter.
	 */
	@KtMongoDsl
	fun eqNotNull(value: T?) {
		if (value != null) eq(value)
	}

	// endregion
	// region $ne

	/**
	 * Matches documents where the value of a field does not equal the [value].
	 *
	 * The result includes documents which do not contain the specified field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         ne("foo")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/ne/)
	 *
	 * @see FilterQuery.ne Shorthand.
	 */
	@KtMongoDsl
	fun ne(value: T)

	// endregion
	// region $exists

	/**
	 * Matches documents that contain the specified field, including
	 * values where the field value is `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         exists()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see FilterQuery.exists Shorthand.
	 * @see doesNotExist Opposite.
	 * @see isNotNull Identical, but does not match elements where the field is `null`.
	 */
	@KtMongoDsl
	fun exists()

	/**
	 * Matches documents that do not contain the specified field.
	 * Documents where the field if `null` are counted as existing.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         doesNotExist()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see FilterQuery.doesNotExist Shorthand.
	 * @see exists Opposite.
	 * @see isNull Only matches elements that are specifically `null`.
	 */
	@KtMongoDsl
	fun doesNotExist()

	// endregion
	// region $type

	/**
	 * Selects documents where the value of the field is an instance of the specified BSON [type].
	 *
	 * Querying by data type is useful when dealing with highly unstructured data where data types
	 * are not predictable.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Any,
	 * )
	 *
	 * collection.find {
	 *     User::age {
	 *         type(BsonType.STRING)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/type/)
	 *
	 * @see FilterQuery.hasType Shorthand.
	 * @see isNull Checks if a value has the type [BsonType.Null].
	 * @see isUndefined Checks if a value has the type [BsonType.Undefined].
	 */
	@KtMongoDsl
	fun hasType(type: BsonType)

	// endregion
	// region $not

	/**
	 * Performs a logical `NOT` operation on the specified [expression] and selects the
	 * documents that *do not* match the expression. This includes the elements
	 * that do not contain the field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::age {
	 *         not {
	 *             hasType(BsonType.STRING)
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/not/)
	 *
	 * @see FilterQuery.not Shorthand.
	 */
	@KtMongoDsl
	fun not(expression: FilterQueryPredicate<T>.() -> Unit)

	// endregion
	// region Nullability

	/**
	 * Selects documents for which the field is `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isNull() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterQuery.isNull Shorthand.
	 * @see doesNotExist Checks if the value is not set.
	 * @see isNotNull Opposite.
	 */
	@KtMongoDsl
	fun isNull() =
		hasType(BsonType.Null)

	/**
	 * Selects documents for which the field is not `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isNotNull() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterQuery.isNotNull Shorthand.
	 * @see isNull Opposite.
	 */
	@KtMongoDsl
	fun isNotNull() =
		not { isNull() }

	/**
	 * Selects documents for which the field is `undefined`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isUndefined() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterQuery.isUndefined Shorthand.
	 * @see isNotUndefined Opposite.
	 */
	@KtMongoDsl
	@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun isUndefined() =
		hasType(BsonType.Undefined)

	/**
	 * Selects documents for which the field is not `undefined`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isNotUndefined() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterQuery.isNotUndefined Shorthand.
	 * @see isUndefined Opposite.
	 */
	@KtMongoDsl
	@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	fun isNotUndefined() =
		not { isUndefined() }

	// endregion
	// region $gt, $gte, $lt, $lte

	/**
	 * Selects documents for which this field has a value strictly greater than [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { gt(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see FilterQuery.gt
	 * @see gtNotNull
	 */
	@KtMongoDsl
	fun gt(value: T)

	/**
	 * Selects documents for which this field has a value strictly greater than [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { gtNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see FilterQuery.gtNotNull
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun gtNotNull(value: T?) {
		if (value != null)
			gt(value)
	}

	/**
	 * Selects documents for which this field has a value greater or equal to [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { gte(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see FilterQuery.gte
	 * @see gteNotNull
	 */
	@KtMongoDsl
	fun gte(value: T)

	/**
	 * Selects documents for which this field has a value greater or equal to [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { gteNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see FilterQuery.gteNotNull
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun gteNotNull(value: T?) {
		if (value != null)
			gte(value)
	}

	/**
	 * Selects documents for which this field has a value strictly lesser than [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { lt(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see FilterQuery.lt
	 * @see ltNotNull
	 */
	@KtMongoDsl
	fun lt(value: T)

	/**
	 * Selects documents for which this field has a value strictly lesser than [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { ltNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see FilterQuery.ltNotNull
	 * @see ltNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun ltNotNull(value: T?) {
		if (value != null)
			lt(value)
	}

	/**
	 * Selects documents for which this field has a value lesser or equal to [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { lte(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see FilterQuery.lte
	 * @see lteNotNull
	 */
	@KtMongoDsl
	fun lte(value: T)

	/**
	 * Selects documents for which this field has a value lesser or equal to [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { lteNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see FilterQuery.lteNotNull
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun lteNotNull(value: T?) {
		if (value != null)
			lte(value)
	}

	// endregion
	// region $in

	/**
	 * Selects documents for which this field is equal to one of the given [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         isOneOf(listOf("Alfred", "Arthur"))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see FilterQuery.isOneOf
	 */
	@KtMongoDsl
	fun isOneOf(values: Collection<T>)

	/**
	 * Selects documents for which this field is equal to one of the given [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         isOneOf("Alfred", "Arthur")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see FilterQuery.isOneOf
	 */
	@KtMongoDsl
	fun isOneOf(vararg values: T) {
		isOneOf(values.asList())
	}

	// endregion
	// region $nin

	/**
	 * Selects documents for which this field is not equal to any of the given [values].
	 *
	 * This operator will also select documents for which the field doesn't exist.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         isNotOneOf(listOf("Alfred", "Arthur"))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/nin/)
	 *
	 * @see FilterExpression.isNotOneOf
	 * @see ne
	 */
	@KtMongoDsl
	fun isNotOneOf(values: Collection<T>)

	/**
	 * Selects documents for which this field is not equal to any of the given [values].
	 *
	 * This operator will also select documents for which the field doesn't exist.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         isNotOneOf("Alfred", "Arthur")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/nin/)
	 *
	 * @see FilterExpression.isNotOneOf
	 * @see ne
	 */
	@KtMongoDsl
	fun isNotOneOf(vararg values: T) {
		isNotOneOf(values.asList())
	}

	// endregion

}
