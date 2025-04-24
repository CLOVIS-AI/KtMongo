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
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.path.*
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import org.intellij.lang.annotations.Language
import kotlin.reflect.KProperty1

/**
 * DSL for MongoDB operators that are used as predicates in conditions.
 *
 * ### Example
 *
 * This expression type is available in multiple operators, most commonly `find`:
 * ```kotlin
 * class User(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * collection.find {
 *     User::age gte 18
 * }
 * ```
 *
 * ### Beware of arrays!
 *
 * MongoDB operators do not discriminate between scalars and arrays.
 * When an array is encountered, all operators attempt to match on the array itself.
 * If the match fails, the operators attempt to match array elements.
 *
 * It is not possible to mimic this behavior in KtMongo while still keeping type-safety,
 * so operators may behave strangely when arrays are encountered.
 *
 * Note that if the collection corresponds to the declared Kotlin type,
 * these situations can never happen, as the Kotlin type system doesn't allow them to.
 *
 * When developers attempt to perform an operator on the entire array,
 * they should use operators as normal:
 * ```kotlin
 * class User(
 *     val name: String,
 *     val favoriteNumbers: List<Int>
 * )
 *
 * collection.find {
 *     User::favoriteNumbers eq listOf(1, 2)
 * }
 * ```
 * Developers should use the request above when they want to match a document similar to:
 * ```json
 * {
 *     favoriteNumbers: [1, 2]
 * }
 * ```
 * The following document will NOT match:
 * ```json
 * {
 *     favoriteNumbers: [3]
 * }
 * ```
 *
 * However, due to MongoDB's behavior when encountering arrays, it should be noted
 * that the following document WILL match:
 * ```json
 * {
 *     favoriteNumbers: [
 *         [3],
 *         [1, 2],
 *         [7, 2]
 *     ]
 * }
 * ```
 *
 * To execute an operator on one of the elements of an array, see [anyValue].
 *
 * ### Operators
 *
 * Comparison query:
 * - [`$eq`][eq]
 * - [`$gt`][gt]
 * - [`$gte`][gte]
 * - [`$in`][isOneOf]
 * - [`$lt`][lt]
 * - [`$lte`][lte]
 * - [`$ne`][ne]
 *
 * Logical query:
 * - [`$and`][and]
 * - [`$not`][not]
 * - [`$or`][or]
 *
 * Element query:
 * - [`$exists`][exists]
 * - [`$type`][hasType]
 * - [`$in`][isOneOf]
 * - [`$nin`][isNotOneOf]
 *
 * Array query:
 * - [`$all`][containsAll]
 * - [`$elemMatch`][any]
 *
 * Text query:
 * - [`$regex`][regex]
 *
 * If you can't find an operator you're searching for, visit the [tracking issue](https://gitlab.com/opensavvy/ktmongo/-/issues/4).
 */
@KtMongoDsl
interface FilterQuery<T> : CompoundBsonNode, FieldDsl {

	// region $and, $or

	/**
	 * Performs a logical `AND` operation on one or more expressions,
	 * and selects the documents that satisfy *all* the expressions.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.findOne {
	 *     and {
	 *         User::name eq "foo"
	 *         User::age eq 18
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/and/)
	 *
	 * @see or Logical `OR` operation.
	 */
	@KtMongoDsl
	fun and(block: FilterQuery<T>.() -> Unit)

	/**
	 * Performs a logical `OR` operation on one or more expressions,
	 * and selects the documents that satisfy *at least one* of the expressions.
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
	 *     or {
	 *         User::name eq "foo"
	 *         User::name eq "bar"
	 *         User::age eq 18
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/or/)
	 *
	 * @see and Logical `AND` operation.
	 */
	@KtMongoDsl
	fun or(block: FilterQuery<T>.() -> Unit)

	// endregion
	// region Predicate access

	/**
	 * Targets a single field to execute a [targeted predicate][FilterQueryPredicate].
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
	 * Note that many operators available this way have a convenience function directly in this class to
	 * shorten this. For this example, see [eq]:
	 *
	 * ```kotlin
	 * collection.find {
	 *     User::name eq "foo"
	 * }
	 * ```
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.invoke(block: FilterQueryPredicate<V>.() -> Unit)

	/**
	 * Targets a single field to execute a [targeted predicate][FilterQueryPredicate].
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
	 * Note that many operators available this way have a convenience function directly in this class to
	 * shorten this. For this example, see [eq]:
	 *
	 * ```kotlin
	 * collection.find {
	 *     User::name eq "foo"
	 * }
	 * ```
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.invoke(block: FilterQueryPredicate<V>.() -> Unit) {
		this.field.invoke(block)
	}

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
	 *     User::age not {
	 *         hasType(BsonType.STRING)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/not/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.not(expression: FilterQueryPredicate<V>.() -> Unit) {
		this { not(expression) }
	}

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
	 *     User::age not {
	 *         hasType(BsonType.STRING)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/not/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.not(expression: FilterQueryPredicate<V>.() -> Unit) {
		this.field.not(expression)
	}

	// endregion
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
	 *     User::name eq "foo"
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.eq(value: V) {
		this { eq(value) }
	}

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
	 *     User::name eq "foo"
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.eq(value: V) {
		this.field.eq(value)
	}

	/**
	 * Matches documents where the value of a field equals [value].
	 *
	 * If [value] is `null`, the operator is not added (all documents are matched).
	 *
	 * ### Example
	 *
	 * This operator is useful to simplify searches when the criteria are optional.
	 * For example, instead of writing:
	 * ```kotlin
	 * collection.find {
	 *     if (criteria.name != null)
	 *         User::name eq criteria.name
	 * }
	 * ```
	 * this operator can be used instead:
	 * ```kotlin
	 * collection.find {
	 *     User::name eqNotNull criteria.name
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 *
	 * @see eq Equality filter.
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.eqNotNull(value: V?) {
		this { eqNotNull(value) }
	}

	/**
	 * Matches documents where the value of a field equals [value].
	 *
	 * If [value] is `null`, the operator is not added (all documents are matched).
	 *
	 * ### Example
	 *
	 * This operator is useful to simplify searches when the criteria are optional.
	 * For example, instead of writing:
	 * ```kotlin
	 * collection.find {
	 *     if (criteria.name != null)
	 *         User::name eq criteria.name
	 * }
	 * ```
	 * this operator can be used instead:
	 * ```kotlin
	 * collection.find {
	 *     User::name eqNotNull criteria.name
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 *
	 * @see eq Equality filter.
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.eqNotNull(value: V?) {
		this.field.eqNotNull(value)
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
	 *     User::name ne "foo"
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/ne/)
	 *
	 * @see eq
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.ne(value: V) {
		this { ne(value) }
	}

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
	 *     User::name ne "foo"
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/ne/)
	 *
	 * @see eq
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.ne(value: V) {
		this.field.ne(value)
	}

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
	 *     User::age.exists()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see doesNotExist Opposite.
	 * @see isNotNull Identical, but does not match elements where the field is `null`.
	 */
	@KtMongoDsl
	fun Field<T, *>.exists() {
		this { exists() }
	}

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
	 *     User::age.exists()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see doesNotExist Opposite.
	 * @see isNotNull Identical, but does not match elements where the field is `null`.
	 */
	@KtMongoDsl
	fun KProperty1<T, *>.exists() {
		this.field.exists()
	}

	/**
	 * Matches documents that do not contain the specified field.
	 * Documents where the field if `null` are not matched.
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
	 *     User::age.doesNotExist()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see exists Opposite.
	 * @see isNull Only matches documents that are specifically `null`.
	 */
	@KtMongoDsl
	fun Field<T, *>.doesNotExist() {
		this { doesNotExist() }
	}

	/**
	 * Matches documents that do not contain the specified field.
	 * Documents where the field if `null` are not matched.
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
	 *     User::age.doesNotExist()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see exists Opposite.
	 * @see isNull Only matches documents that are specifically `null`.
	 */
	@KtMongoDsl
	fun KProperty1<T, *>.doesNotExist() {
		this.field.doesNotExist()
	}

	/**
	 * Matches documents in which an array is empty or absent.
	 *
	 * ### Example
	 *
	 * Return all users that have no grades (either an empty array, or the `grades` field is absent):
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNull
	 * @see isNotEmpty
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun Field<T, Collection<*>>.isEmpty() {
		FieldImpl<T, Any>(path / PathSegment.Indexed(0)).doesNotExist()
	}

	/**
	 * Matches documents in which an array is empty or absent.
	 *
	 * ### Example
	 *
	 * Return all users that have no grades (either an empty array, or the `grades` field is absent):
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNull
	 * @see isNotEmpty
	 */
	@KtMongoDsl
	fun KProperty1<T, Collection<*>>.isEmpty() {
		this.field.isEmpty()
	}

	/**
	 * Matches documents in which a map is empty or absent.
	 *
	 * ### Example
	 *
	 * Return all users that have no grades (either an empty map, or the `grades` field is absent):
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: Map<String, Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isMapEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNull
	 * @see isNotEmpty
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun Field<T, Map<String, *>>.isMapEmpty() {
		or {
			doesNotExist()
			FieldImpl<T, Any>(path) eq context.buildDocument { }
		}
	}

	/**
	 * Matches documents in which a map is empty or absent.
	 *
	 * ### Example
	 *
	 * Return all users that have no grades (either an empty map, or the `grades` field is absent):
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: Map<String, Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isMapEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNull
	 * @see isNotEmpty
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun KProperty1<T, Map<String, *>>.isMapEmpty() {
		this.field.isMapEmpty()
	}

	/**
	 * Matches documents in which an array is not empty.
	 *
	 * ### Example
	 *
	 * Return all users that have one or more grades.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isNotEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNotNull
	 * @see isEmpty
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun Field<T, Collection<*>>.isNotEmpty() {
		FieldImpl<T, Any>(path / PathSegment.Indexed(0)).exists()
	}

	/**
	 * Matches documents in which an array is not empty.
	 *
	 * ### Example
	 *
	 * Return all users that have one or more grades.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isNotEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNotNull
	 * @see isEmpty
	 */
	@KtMongoDsl
	fun KProperty1<T, Collection<*>>.isNotEmpty() {
		this.field.isNotEmpty()
	}

	/**
	 * Matches documents in which a map is not empty.
	 *
	 * ### Example
	 *
	 * Return all users that have one or more grades.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: Map<String, Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isMapNotEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNotNull
	 * @see isEmpty
	 */
	// Spec: https://www.mongodb.com/docs/manual/reference/bson-type-comparison-order/#objects
	//       "An object without [fields] is less than an object with [fields]."
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun Field<T, Map<String, *>>.isMapNotEmpty() {
		FieldImpl<T, Any>(path) gt context.buildDocument { }
	}

	/**
	 * Matches documents in which a map is not empty.
	 *
	 * ### Example
	 *
	 * Return all users that have one or more grades.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val grades: Map<String, Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades.isMapNotEmpty()
	 * }
	 * ```
	 *
	 * @see exists
	 * @see isNotNull
	 * @see isEmpty
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun KProperty1<T, Map<String, *>>.isMapNotEmpty() {
		this.field.isMapNotEmpty()
	}

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
	 *     User::age hasType BsonType.STRING
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/type/)
	 *
	 * @see isNull Checks if a value has the type [BsonType.Null].
	 * @see isUndefined Checks if a value has the type [BsonType.Undefined].
	 */
	@KtMongoDsl
	infix fun Field<T, *>.hasType(type: BsonType) {
		this { hasType(type) }
	}

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
	 *     User::age hasType BsonType.STRING
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/type/)
	 *
	 * @see isNull Checks if a value has the type [BsonType.Null].
	 * @see isUndefined Checks if a value has the type [BsonType.Undefined].
	 */
	@KtMongoDsl
	infix fun KProperty1<T, *>.hasType(type: BsonType) {
		this.field.hasType(type)
	}

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
	 *     User::age.isNull()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see doesNotExist Checks if the value is not set.
	 * @see isNotNull Opposite.
	 */
	@KtMongoDsl
	fun Field<T, *>.isNull() {
		this { isNull() }
	}

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
	 *     User::age.isNull()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see doesNotExist Checks if the value is not set.
	 * @see isNotNull Opposite.
	 */
	@KtMongoDsl
	fun KProperty1<T, *>.isNull() {
		this.field.isNull()
	}

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
	 *     User::age.isNotNull()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see isNull Opposite.
	 */
	@KtMongoDsl
	fun Field<T, *>.isNotNull() {
		this { isNotNull() }
	}

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
	 *     User::age.isNotNull()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see isNull Opposite.
	 */
	@KtMongoDsl
	fun KProperty1<T, *>.isNotNull() {
		this.field.isNotNull()
	}

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
	 *     User::age.isUndefined()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see isNotUndefined Opposite.
	 */
	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@KtMongoDsl
	fun Field<T, *>.isUndefined() {
		this { isUndefined() }
	}

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
	 *     User::age.isUndefined()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see isNotUndefined Opposite.
	 */
	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@KtMongoDsl
	fun KProperty1<T, *>.isUndefined() {
		this.field.isUndefined()
	}

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
	 *     User::age.isNotUndefined()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see isUndefined Opposite.
	 */
	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@KtMongoDsl
	fun Field<T, *>.isNotUndefined() {
		this { isNotUndefined() }
	}

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
	 *     User::age.isNotUndefined()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see isUndefined Opposite.
	 */
	@Suppress("DEPRECATION")
	@Deprecated(DEPRECATED_IN_BSON_SPEC)
	@KtMongoDsl
	fun KProperty1<T, *>.isNotUndefined() {
		this.field.isNotUndefined()
	}

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
	 *     User::age gt 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see gtNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.gt(value: V) {
		this { gt(value) }
	}

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
	 *     User::age gt 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see gtNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.gt(value: V) {
		this.field.gt(value)
	}

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
	 *     User::age gtNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see gt
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.gtNotNull(value: V?) {
		this { gtNotNull(value) }
	}

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
	 *     User::age gtNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see gt
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.gtNotNull(value: V?) {
		this.field.gtNotNull(value)
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
	 *     User::age gte 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see gteNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.gte(value: V) {
		this { gte(value) }
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
	 *     User::age gte 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see gteNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.gte(value: V) {
		this.field.gte(value)
	}

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
	 *     User::age gteNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see gte
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.gteNotNull(value: V?) {
		this { gteNotNull(value) }
	}

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
	 *     User::age gteNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see gte
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.gteNotNull(value: V?) {
		this.field.gteNotNull(value)
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
	 *     User::age lt 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see ltNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.lt(value: V) {
		this { lt(value) }
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
	 *     User::age lt 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see ltNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.lt(value: V) {
		this.field.lt(value)
	}

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
	 *     User::age ltNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see lt
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.ltNotNull(value: V?) {
		this { ltNotNull(value) }
	}

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
	 *     User::age ltNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see lt
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.ltNotNull(value: V?) {
		this.field.ltNotNull(value)
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
	 *     User::age lte 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see lteNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.lte(value: V) {
		this { lte(value) }
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
	 *     User::age lte 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see lteNotNull
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.lte(value: V) {
		this.field.lte(value)
	}

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
	 *     User::age lteNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see lte
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.lteNotNull(value: V?) {
		this { lteNotNull(value) }
	}

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
	 *     User::age lteNotNull 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see lte
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.lteNotNull(value: V?) {
		this.field.lteNotNull(value)
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
	 *     User::name.isOneOf(listOf("Alfred", "Arthur"))
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see or
	 * @see eq
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.isOneOf(values: List<V>) {
		this { isOneOf(values) }
	}

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
	 *     User::name.isOneOf(listOf("Alfred", "Arthur"))
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see or
	 * @see eq
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.isOneOf(values: List<V>) {
		this.field.isOneOf(values)
	}

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
	 *     User::name.isOneOf("Alfred", "Arthur")
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see or
	 * @see eq
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.isOneOf(vararg values: V) {
		isOneOf(values.asList())
	}

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
	 *     User::name.isOneOf("Alfred", "Arthur")
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see or
	 * @see eq
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.isOneOf(vararg values: V) {
		isOneOf(values.asList())
	}

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
	 *     User::name.isNotOneOf(listOf("Alfred", "Arthur"))
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/nin/)
	 *
	 * @see isOneOf
	 * @see ne
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.isNotOneOf(values: List<V>) {
		this { isNotOneOf(values) }
	}

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
	 *     User::name.isNotOneOf(listOf("Alfred", "Arthur"))
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/nin/)
	 *
	 * @see isOneOf
	 * @see ne
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.isNotOneOf(values: List<V>) {
		this.field.isNotOneOf(values)
	}

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
	 *     User::name.isNotOneOf("Alfred", "Arthur")
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/nin/)
	 *
	 * @see isOneOf
	 * @see ne
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.isNotOneOf(vararg values: V) {
		isNotOneOf(values.asList())
	}

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
	 *     User::name.isNotOneOf("Alfred", "Arthur")
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/nin/)
	 *
	 * @see isOneOf
	 * @see ne
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.isNotOneOf(vararg values: V) {
		isNotOneOf(values.asList())
	}

	// endregion
	// region $elemMatch

	/**
	 * Specify operators on array elements.
	 *
	 * ### Example
	 *
	 * Find any user who has 12 as one of their favorite numbers.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val favoriteNumbers: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::favoriteNumbers.any eq 12
	 * }
	 * ```
	 *
	 * ### Repeated usages will match different items
	 *
	 * Note that if `any` is used multiple times, it may test different items.
	 * For example, the following request will match the following document:
	 * ```kotlin
	 * collection.find {
	 *     User::favoriteNumbers.any gt 2
	 *     User::favoriteNumbers.any lte 7
	 * }
	 * ```
	 * ```json
	 * {
	 *     "name": "Nicolas",
	 *     "favoriteNumbers": [ 1, 9 ]
	 * }
	 * ```
	 * Because 1 is less than 7, and 9 is greater than 2, the document is returned.
	 *
	 * If you want to apply multiple filters to the same item, use the [any] function.
	 *
	 * ### Arrays don't exist in finds!
	 *
	 * MongoDB operators do not discriminate between scalars and arrays.
	 * When an array is encountered, all operators attempt to match on the array itself.
	 * If the match fails, the operators attempt to match array elements.
	 *
	 * It is not possible to mimic this behavior in KtMongo while still keeping type-safety,
	 * so KtMongo has different operators to filter a collection itself or its elements.
	 *
	 * As a consequence, the request:
	 * ```kotlin
	 * collection.find {
	 *     User::favoriteNumbers.any eq 5
	 * }
	 * ```
	 * will, as expected, match the following document:
	 * ```json
	 * {
	 *     favoriteNumbers: [1, 4, 5, 10]
	 * }
	 * ```
	 *
	 * It is important to note that it WILL also match this document:
	 * ```json
	 * {
	 *     favoriteNumbers: 5
	 * }
	 * ```
	 *
	 * Since this document doesn't conform to the Kotlin declared type `List<Int>`,
	 * it is unlikely that such an element exists, but developers should keep it in mind.
	 *
	 * ### External resources
	 *
	 * - [Official document](https://www.mongodb.com/docs/manual/tutorial/query-arrays/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <V> Field<T, Collection<V>>.any: Field<T, V>
		get() = FieldImpl<T, V>(path)

	/**
	 * Specify operators on array elements.
	 *
	 * ### Example
	 *
	 * Find any user who has 12 as one of their favorite numbers.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val favoriteNumbers: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::favoriteNumbers.any eq 12
	 * }
	 * ```
	 *
	 * ### Repeated usages will match different items
	 *
	 * Note that if `any` is used multiple times, it may test different items.
	 * For example, the following request will match the following document:
	 * ```kotlin
	 * collection.find {
	 *     User::favoriteNumbers.any gt 2
	 *     User::favoriteNumbers.any lte 7
	 * }
	 * ```
	 * ```json
	 * {
	 *     "name": "Nicolas",
	 *     "favoriteNumbers": [ 1, 9 ]
	 * }
	 * ```
	 * Because 1 is less than 7, and 9 is greater than 2, the document is returned.
	 *
	 * If you want to apply multiple filters to the same item, use the [any] function.
	 *
	 * ### Arrays don't exist in finds!
	 *
	 * MongoDB operators do not discriminate between scalars and arrays.
	 * When an array is encountered, all operators attempt to match on the array itself.
	 * If the match fails, the operators attempt to match array elements.
	 *
	 * It is not possible to mimic this behavior in KtMongo while still keeping type-safety,
	 * so KtMongo has different operators to filter a collection itself or its elements.
	 *
	 * As a consequence, the request:
	 * ```kotlin
	 * collection.find {
	 *     User::favoriteNumbers.any eq 5
	 * }
	 * ```
	 * will, as expected, match the following document:
	 * ```json
	 * {
	 *     favoriteNumbers: [1, 4, 5, 10]
	 * }
	 * ```
	 *
	 * It is important to note that it WILL also match this document:
	 * ```json
	 * {
	 *     favoriteNumbers: 5
	 * }
	 * ```
	 *
	 * Since this document doesn't conform to the Kotlin declared type `List<Int>`,
	 * it is unlikely that such an element exists, but developers should keep it in mind.
	 *
	 * ### External resources
	 *
	 * - [Official document](https://www.mongodb.com/docs/manual/tutorial/query-arrays/)
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	val <V> KProperty1<T, Collection<V>>.any: Field<T, V>
		get() = FieldImpl<T, V>(field.path)

	/**
	 * Combines Kotlin properties into a path usable to point to any item in an array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val grades: List<Grade>
	 * )
	 *
	 * class Grade(
	 *     val name: Int
	 * )
	 *
	 * collection.find {
	 *     User::grades / Grade::name eq 19
	 * }
	 * ```
	 *
	 * This function is a shorthand for [any]:
	 * ```kotlin
	 * collection.find {
	 *     User::grades.any / Gradle::name eq 19
	 * }
	 * ```
	 */
	// DO NOT REIMPLEMENT THIS METHOD, THIS IS A HACK TO AVOID PLATFORM DECLARATION CLASHES,
	// IT WILL NOT WORK IF YOU USE ANY OTHER IMPLEMENTATION THAN THE DEFAULT ONE.
	@KtMongoDsl
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("divAny")
	operator fun <V, V2> Field<T, Collection<V>>.div(other: Field<V, V2>): Field<T, V2> =
		this.any / other

	/**
	 * Combines Kotlin properties into a path usable to point to any item in an array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val grades: List<Grade>
	 * )
	 *
	 * class Grade(
	 *     val name: Int
	 * )
	 *
	 * collection.find {
	 *     User::grades / Grade::name eq 19
	 * }
	 * ```
	 *
	 * This function is a shorthand for [any]:
	 * ```kotlin
	 * collection.find {
	 *     User::grades.any / Gradle::name eq 19
	 * }
	 * ```
	 */
	// DO NOT REIMPLEMENT THIS METHOD, THIS IS A HACK TO AVOID PLATFORM DECLARATION CLASHES,
	// IT WILL NOT WORK IF YOU USE ANY OTHER IMPLEMENTATION THAN THE DEFAULT ONE.
	@KtMongoDsl
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("divAny")
	operator fun <V, V2> KProperty1<T, Collection<V>>.div(other: Field<V, V2>): Field<T, V2> =
		this.any / other

	/**
	 * Combines Kotlin properties into a path usable to point to any item in an array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val grades: List<Grade>
	 * )
	 *
	 * class Grade(
	 *     val name: Int
	 * )
	 *
	 * collection.find {
	 *     User::grades / Grade::name eq 19
	 * }
	 * ```
	 *
	 * This function is a shorthand for [any]:
	 * ```kotlin
	 * collection.find {
	 *     User::grades.any / Gradle::name eq 19
	 * }
	 * ```
	 */
	// DO NOT REIMPLEMENT THIS METHOD, THIS IS A HACK TO AVOID PLATFORM DECLARATION CLASHES,
	// IT WILL NOT WORK IF YOU USE ANY OTHER IMPLEMENTATION THAN THE DEFAULT ONE.
	@KtMongoDsl
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("divAny")
	operator fun <V, V2> Field<T, Collection<V>>.div(other: KProperty1<V, V2>): Field<T, V2> =
		this.any / other

	/**
	 * Combines Kotlin properties into a path usable to point to any item in an array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val grades: List<Grade>
	 * )
	 *
	 * class Grade(
	 *     val name: Int
	 * )
	 *
	 * collection.find {
	 *     User::grades / Grade::name eq 19
	 * }
	 * ```
	 *
	 * This function is a shorthand for [any]:
	 * ```kotlin
	 * collection.find {
	 *     User::grades.any / Gradle::name eq 19
	 * }
	 * ```
	 */
	// DO NOT REIMPLEMENT THIS METHOD, THIS IS A HACK TO AVOID PLATFORM DECLARATION CLASHES,
	// IT WILL NOT WORK IF YOU USE ANY OTHER IMPLEMENTATION THAN THE DEFAULT ONE.
	@KtMongoDsl
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("divAny")
	operator fun <V, V2> KProperty1<T, Collection<V>>.div(other: KProperty1<V, V2>): Field<T, V2> =
		this.any / other

	/**
	 * Specify multiple operators on a single array element.
	 *
	 * ### Example
	 *
	 * Find students with a grade between 8 and 10, that may be eligible to perform
	 * an exam a second time.
	 *
	 * ```kotlin
	 * class Student(
	 *     val name: String,
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     Student::grades.anyValue {
	 *         gte(8)
	 *         lte(10)
	 *     }
	 * }
	 * ```
	 *
	 * The following document will match because the grade 9 is in the interval.
	 * ```json
	 * {
	 *     "name": "John",
	 *     "grades": [9, 3]
	 * }
	 * ```
	 *
	 * The following document will NOT match, because none of the grades are in the interval.
	 * ```json
	 * {
	 *     "name": "Lea",
	 *     "grades": [18, 19]
	 * }
	 * ```
	 *
	 * If you want to perform multiple checks on different elements of an array,
	 * see the [any] property.
	 *
	 * This function only allows specifying operators on array elements directly.
	 * To specify operators on sub-fields of array elements, see [any].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/elemMatch/)
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	fun <V> Field<T, Collection<V>>.anyValue(block: FilterQueryPredicate<V>.() -> Unit)

	/**
	 * Specify multiple operators on a single array element.
	 *
	 * ### Example
	 *
	 * Find students with a grade between 8 and 10, that may be eligible to perform
	 * an exam a second time.
	 *
	 * ```kotlin
	 * class Student(
	 *     val name: String,
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     Student::grades.anyValue {
	 *         gte(8)
	 *         lte(10)
	 *     }
	 * }
	 * ```
	 *
	 * The following document will match because the grade 9 is in the interval.
	 * ```json
	 * {
	 *     "name": "John",
	 *     "grades": [9, 3]
	 * }
	 * ```
	 *
	 * The following document will NOT match, because none of the grades are in the interval.
	 * ```json
	 * {
	 *     "name": "Lea",
	 *     "grades": [18, 19]
	 * }
	 * ```
	 *
	 * If you want to perform multiple checks on different elements of an array,
	 * see the [any] property.
	 *
	 * This function only allows specifying operators on array elements directly.
	 * To specify operators on sub-fields of array elements, see [any].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/elemMatch/)
	 */
	@KtMongoDsl
	fun <V> KProperty1<T, Collection<V>>.anyValue(block: FilterQueryPredicate<V>.() -> Unit) {
		this.field.anyValue(block)
	}

	/**
	 * Specify multiple operators on fields of a single array element.
	 *
	 * ### Example
	 *
	 * Find customers who have a pet that is born this month, as they may be eligible for a discount.
	 *
	 * ```kotlin
	 * class Customer(
	 *     val name: String,
	 *     val pets: List<Pet>,
	 * )
	 *
	 * class Pet(
	 *     val name: String,
	 *     val birthMonth: Int
	 * )
	 *
	 * val currentMonth = 3
	 *
	 * collection.find {
	 *     Customer::pets.any {
	 *         Pet::birthMonth gte currentMonth
	 *         Pet::birthMonth lte (currentMonth + 1)
	 *     }
	 * }
	 * ```
	 *
	 * The following document will match:
	 * ```json
	 * {
	 *     "name": "Fred",
	 *     "pets": [
	 *         {
	 *             "name": "Arthur",
	 *             "birthMonth": 5
	 *         },
	 *         {
	 *             "name": "Gwen",
	 *             "birthMonth": 3
	 *         }
	 *     ]
	 * }
	 * ```
	 * because the pet "Gwen" has a matching birth month.
	 *
	 * If you want to perform operators on the elements directly (not on their fields), use
	 * [anyValue] instead.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/elemMatch/)
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	fun <V> Field<T, Collection<V>>.any(block: FilterQuery<V>.() -> Unit)

	/**
	 * Specify multiple operators on fields of a single array element.
	 *
	 * ### Example
	 *
	 * Find customers who have a pet that is born this month, as they may be eligible for a discount.
	 *
	 * ```kotlin
	 * class Customer(
	 *     val name: String,
	 *     val pets: List<Pet>,
	 * )
	 *
	 * class Pet(
	 *     val name: String,
	 *     val birthMonth: Int
	 * )
	 *
	 * val currentMonth = 3
	 *
	 * collection.find {
	 *     Customer::pets.any {
	 *         Pet::birthMonth gte currentMonth
	 *         Pet::birthMonth lte (currentMonth + 1)
	 *     }
	 * }
	 * ```
	 *
	 * The following document will match:
	 * ```json
	 * {
	 *     "name": "Fred",
	 *     "pets": [
	 *         {
	 *             "name": "Arthur",
	 *             "birthMonth": 5
	 *         },
	 *         {
	 *             "name": "Gwen",
	 *             "birthMonth": 3
	 *         }
	 *     ]
	 * }
	 * ```
	 * because the pet "Gwen" has a matching birth month.
	 *
	 * If you want to perform operators on the elements directly (not on their fields), use
	 * [anyValue] instead.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/elemMatch/)
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	fun <V> KProperty1<T, Collection<V>>.any(block: FilterQuery<V>.() -> Unit) {
		this.field.any(block)
	}

	// endregion
	// region $all

	/**
	 * Selects documents where the value of a field is an array that contains all the specified [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades containsAll listOf(2, 3, 7)
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/all/)
	 */
	@KtMongoDsl
	infix fun <V> Field<T, Collection<V>>.containsAll(values: Collection<V>)

	/**
	 * Selects documents where the value of a field is an array that contains all the specified [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val grades: List<Int>
	 * )
	 *
	 * collection.find {
	 *     User::grades containsAll listOf(2, 3, 7)
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/all/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, Collection<V>>.containsAll(values: Collection<V>) {
		this.field.containsAll(values)
	}

	// endregion
	// region $expr

	/**
	 * Enables the usage of [aggregation values][AggregationOperators] within a regular query.
	 *
	 * Aggregation values are much more powerful than regular query operators (for example, it is possible to compare two
	 * fields of the same document). However, the way they are written is quite different, and the way they are
	 * evaluated by MongoDB is quite different again. Before using aggregation values, be sure to read [AggregationOperators].
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
	 * val anomalies = products.find {
	 *     expr {
	 *         of(Product::creationDate) gt of(Product::releaseDate)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/expr/)
	 */
	@KtMongoDsl
	fun expr(block: AggregationOperators.() -> Value<T & Any, Boolean>)

	// endregion
	// region $regex

	/**
	 * Matches documents where the field corresponds to a given regex expression.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 * )
	 *
	 * collection.find {
	 *     User::name.regex("John .*")
	 * }
	 * ```
	 *
	 * ### Indexing
	 *
	 * If possible, prefer using a `"^"` prefix. For example, if we know that a pattern will only be present
	 * at the start of a string, `"^foo"` will use indexes, whereas `"foo"` will not.
	 *
	 * Avoid using `.*` at the start and end of a pattern. `"foo"` is identical to `"foo.*"`, but the former
	 * can use indexes and the latter cannot.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/regex)
	 * - [Syntax sheet](https://www.pcre.org/current/doc/html/pcre2syntax.html)
	 *
	 * @param caseInsensitive If `true`, the result is matched even if its case doesn't match.
	 * Note that this also disables index usage (even case-insensitive indexes) and ignores collation.
	 * @param dotAll If `true`, the dot character (`.`) can match newlines.
	 * @param extended If `true`, whitespace (except in character classes) is ignored,
	 * and segments starting from an unescaped pound (`#`) until a newline are ignored, similarly to a Python comment.
	 * ```kotlin
	 * User::name.regex(
	 *     pattern = """
	 *         abc # This is a comment, it's not part of the pattern
	 *         123
	 *     """.trimIndent(),
	 *     extended = true,
	 * )
	 * ```
	 * which is identical to the non-extended pattern `"abc123"`.
	 * @param matchEachLine If `true`, the special characters `^` and `$` match the beginning and end
	 * of each line, instead of matching the beginning and end of the entire string.
	 * Therefore, `"^S"` will match `"First line\nSecond line"`, which would not match otherwise.
	 */
	@KtMongoDsl
	fun Field<T, String?>.regex(
		@Language("JSRegexp") pattern: String,
		caseInsensitive: Boolean = false,
		dotAll: Boolean = false,
		extended: Boolean = false,
		matchEachLine: Boolean = false,
	) {
		this { regex(pattern, caseInsensitive, dotAll, extended, matchEachLine) }
	}

	/**
	 * Matches documents where the field corresponds to a given regex expression.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 * )
	 *
	 * collection.find {
	 *     User::name.regex("John .*")
	 * }
	 * ```
	 *
	 * ### Indexing
	 *
	 * If possible, prefer using a `"^"` prefix. For example, if we know that a pattern will only be present
	 * at the start of a string, `"^foo"` will use indexes, whereas `"foo"` will not.
	 *
	 * Avoid using `.*` at the start and end of a pattern. `"foo"` is identical to `"foo.*"`, but the former
	 * can use indexes and the latter cannot.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/regex)
	 * - [Syntax sheet](https://www.pcre.org/current/doc/html/pcre2syntax.html)
	 *
	 * @param caseInsensitive If `true`, the result is matched even if its case doesn't match.
	 * Note that this also disables index usage (even case-insensitive indexes) and ignores collation.
	 * @param dotAll If `true`, the dot character (`.`) can match newlines.
	 * @param extended If `true`, whitespace (except in character classes) is ignored,
	 * and segments starting from an unescaped pound (`#`) until a newline are ignored, similarly to a Python comment.
	 * ```kotlin
	 * User::name.regex(
	 *     pattern = """
	 *         abc # This is a comment, it's not part of the pattern
	 *         123
	 *     """.trimIndent(),
	 *     extended = true,
	 * )
	 * ```
	 * which is identical to the non-extended pattern `"abc123"`.
	 * @param matchEachLine If `true`, the special characters `^` and `$` match the beginning and end
	 * of each line, instead of matching the beginning and end of the entire string.
	 * Therefore, `"^S"` will match `"First line\nSecond line"`, which would not match otherwise.
	 */
	@KtMongoDsl
	fun KProperty1<T, String?>.regex(
		@Language("JSRegexp") pattern: String,
		caseInsensitive: Boolean = false,
		dotAll: Boolean = false,
		extended: Boolean = false,
		matchEachLine: Boolean = false,
	) {
		this { regex(pattern, caseInsensitive, dotAll, extended, matchEachLine) }
	}

	// endregion
}
