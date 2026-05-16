/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.ArrayFiltersOptionDsl
import opensavvy.ktmongo.dsl.options.SortOptionDsl
import opensavvy.ktmongo.dsl.path.*
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import kotlin.jvm.JvmName
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Instant

/**
 * DSL for MongoDB operators that are used to update existing values (does *not* include aggregation operators).
 *
 * ### Example
 *
 * This expression type is available on multiple operations, most commonly `update`:
 * ```kotlin
 * class User(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * collection.update(
 *     filter = {
 *         User::name eq "Bob"
 *     },
 *     update = {
 *         User::age set 18
 *     }
 * )
 * ```
 *
 * ### Operators
 *
 * On regular fields:
 * - [`$inc`][plusAssign]
 * - [`$max`][max]
 * - [`$min`][min]
 * - [`$rename`][renameTo]
 * - [`$set`][set]
 * - [`$setOnInsert`][UpsertQuery.setOnInsert] (only for [upserts][UpsertQuery])
 * - [`$unset`][unset]
 *
 * On arrays:
 * - [`$`][selected]
 * - [`$[]`][all]
 * - [`$[<name>]`][filter]
 * - [`$addToSet`][addToSet]
 * - [`$push`][push]
 *
 * Time management:
 * - [`$currentDate`][setToCurrentDate]
 *
 * If you can't find the operator you're searching for, visit the [tracking issue](https://gitlab.com/opensavvy/ktmongo/-/issues/5).
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/)
 *
 * @see FilterQuery Filters
 */
@KtMongoDsl
interface UpdateQuery<T> : CompoundBsonNode, FieldDsl {

	// region $set

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.updateMany {
	 *     User::age set 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 *
	 * @see UpsertQuery.setOnInsert Only set if a new document is created.
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V, type: KType)

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.updateMany {
	 *     User::age set 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 *
	 * @see UpsertQuery.setOnInsert Only set if a new document is created.
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, V>.set(value: V) {
		set(value, typeOf<V>())
	}

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `true`.
	 *
	 * If [condition] is `false`, this operator does nothing.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.updateMany {
	 *     User::age.setIf(someComplexOperation, 18)
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 *
	 * @see UpsertQuery.setOnInsert Only set if a new document is created.
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, V>.setIf(condition: Boolean, value: V) {
		if (condition)
			this set value
	}

	/**
	 * Replaces the value of a field with the specified [value], if [condition] is `false`.
	 *
	 * If [condition] is `true`, this operator does nothing.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.updateMany {
	 *     User::age.setUnless(someComplexOperation, 18)
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 *
	 * @see UpsertQuery.setOnInsert Only set if a new document is created.
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, V>.setUnless(condition: Boolean, value: V) {
		if (!condition)
			this set value
	}

	// endregion
	// region $inc

	/**
	 * Increments a field by the specified [amount].
	 *
	 * [amount] may be negative, in which case the field is decremented.
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of [amount].
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // It's the new year!
	 * collection.updateMany {
	 *     User::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/inc/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.inc(amount: V, type: KType)

	/**
	 * Increments a field by the specified [amount].
	 *
	 * [amount] may be negative, in which case the field is decremented.
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of [amount].
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // It's the new year!
	 * collection.updateMany {
	 *     User::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/inc/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V : Number> Field<T, V>.inc(amount: V) {
		this.inc(amount, typeOf<V>())
	}

	/**
	 * Increments a field by the specified [amount].
	 *
	 * [amount] may be negative, in which case the field is decremented.
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of [amount].
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // It's the new year!
	 * collection.updateMany {
	 *     User::age += 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/inc/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline operator fun <@kotlin.internal.OnlyInputTypes reified V : Number> Field<T, V>.plusAssign(amount: V): Unit =
		this.inc(amount)

	// endregion
	// region $mul

	/**
	 * Multiplies a field by the specified [amount].
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of 0.
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val price: Double,
	 * )
	 *
	 * collection.updateMany {
	 *     User::price mul 2.0
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/mul/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.mul(amount: V, type: KType)

	/**
	 * Multiplies a field by the specified [amount].
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of 0.
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val price: Double,
	 * )
	 *
	 * collection.updateMany {
	 *     User::price mul 2.0
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/mul/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V : Number> Field<T, V>.mul(amount: V) {
		this.mul(amount, typeOf<V>())
	}

	/**
	 * Multiplies a field by the specified [amount].
	 *
	 * If the field doesn't exist (either the document doesn't have it, or the operation is an upsert and a new document is created),
	 * the field is created with an initial value of 0.
	 *
	 * Use of this operator with a field with a `null` value will generate an error.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val price: Double,
	 * )
	 *
	 * collection.updateMany {
	 *     User::price *= 2.0
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/mul/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline operator fun <@kotlin.internal.OnlyInputTypes reified V : Number> Field<T, V>.timesAssign(amount: V) {
		this mul amount
	}

	// endregion
	// region $unset

	/**
	 * Deletes a field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val alive: Boolean,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "Luke Skywalker"
	 * }.updateOne {
	 *     User::age.unset()
	 *     User::alive set false
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/unset/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.unset()

	// endregion
	// region $min & $max

	/**
	 * Updates the value of a field to the specified [value] only if the specified [value] is less than the current value of the field.
	 *
	 * If the field doesn't exist, the field is set to the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val score: Int,
	 * )
	 *
	 * collection.updateMany {
	 *     User::score min 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/min/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> Field<T, V?>.min(value: V, type: KType)

	/**
	 * Updates the value of a field to the specified [value] only if the specified [value] is less than the current value of the field.
	 *
	 * If the field doesn't exist, the field is set to the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val score: Int,
	 * )
	 *
	 * collection.updateMany {
	 *     User::score min 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/min/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V : Comparable<V>> Field<T, V?>.min(value: V) {
		this.min(value, typeOf<V>())
	}

	/**
	 * Updates the value of a field to the specified [value] only if the specified [value] is greater than the current value of the field.
	 *
	 * If the field doesn't exist, the field is set to the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val score: Int,
	 * )
	 *
	 * collection.updateMany {
	 *     User::score max 100
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/max/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> Field<T, V?>.max(value: V, type: KType)

	/**
	 * Updates the value of a field to the specified [value] only if the specified [value] is greater than the current value of the field.
	 *
	 * If the field doesn't exist, the field is set to the specified [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val score: Int,
	 * )
	 *
	 * collection.updateMany {
	 *     User::score max 100
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/max/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V : Comparable<V>> Field<T, V?>.max(value: V) {
		this.max(value, typeOf<V>())
	}

	// endregion
	// region $rename

	/**
	 * Renames a field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val ageOld: Int,
	 * )
	 *
	 * collection.updateMany {
	 *     User::ageOld renameTo User::age
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/rename/)
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.renameTo(newName: Field<T, V>)

	// endregion
	// region $currentDate

	/**
	 * Sets this field to the current date.
	 *
	 * If the field does not exist, this operator adds the field to the document.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 *     val modificationDate: Instant,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "Bob"
	 * }.updateMany {
	 *     User::age set 18
	 *     User::modificationDate.setToCurrentDate()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/currentDate/)
	 */
	@KtMongoDsl
	fun Field<T, Instant?>.setToCurrentDate()

	/**
	 * Sets this field to the current timestamp.
	 *
	 * If the field does not exist, this operator adds the field to the document.
	 *
	 * The time [Timestamp] is internal. [Instant] should be preferred. For more information, see [Timestamp].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 *     val modificationDate: Timestamp,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "Bob"
	 * }.updateMany {
	 *     User::age set 18
	 *     User::modificationDate.setToCurrentDate()
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/currentDate/)
	 */
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("setToCurrentTimestamp")
	@KtMongoDsl
	fun Field<T, Timestamp?>.setToCurrentDate()

	// endregion
	// region Positional operator: $

	/**
	 * The positional operator: update an array item selected in the filter.
	 *
	 * When we use [any][FilterQuery.any] or [anyValue][FilterQuery.anyValue]
	 * in a filter to select an item, we can use this operator to update whichever item was selected.
	 *
	 * Do not use this operator in an `upsert`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val pets: List<Pet>,
	 * )
	 *
	 * class Pet(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.updateMany(
	 *     filter = {
	 *         User::pets.any / Pet::name eq "Bobby"
	 *     },
	 *     update = {
	 *         User::pets.selected / Pet::age inc 1
	 *     }
	 * )
	 * ```
	 *
	 * This example finds all users who have a pet named "Bobby", and increases its age by 1.
	 * Note that if the users have other pets, they are not impacted.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/positional/)
	 */
	@OptIn(LowLevelApi::class)
	val <E> Field<T, Collection<E>>.selected: Field<T, E>
		get() = FieldImpl<T, E>(path / PathSegment.Positional)

	// endregion
	// region All positional operator: $[]

	/**
	 * The all positional operator: selects all elements of an array.
	 *
	 * This operator is used to declare an update that applies to all items of an array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val pets: List<Pet>,
	 * )
	 *
	 * class Pet(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.updateMany {
	 *     User::pets.all / Pet::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/positional-all/)
	 */
	@OptIn(LowLevelApi::class)
	val <E> Field<T, Collection<E>>.all: Field<T, E>
		get() = FieldImpl<T, E>(path / PathSegment.AllPositional)

	// endregion
	// region $addToSet

	/**
	 * Adds [value] at the end of the array, unless it is already present, in which case it does nothing.
	 *
	 * MongoDB detects equality if the two documents are exactly identical. All the fields must be the same _in the same order_.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val tokens: List<String>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::tokens addToSet "123456789"
	 *     }
	 * )
	 * ```
	 *
	 * This will add `"123456789"` to the user's tokens only if it isn't already present.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/addToSet/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.addToSet(value: V, type: KType)

	/**
	 * Adds [value] at the end of the array, unless it is already present, in which case it does nothing.
	 *
	 * MongoDB detects equality if the two documents are exactly identical. All the fields must be the same _in the same order_.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val tokens: List<String>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::tokens addToSet "123456789"
	 *     }
	 * )
	 * ```
	 *
	 * This will add `"123456789"` to the user's tokens only if it isn't already present.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/addToSet/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, Collection<V>>.addToSet(value: V) {
		this.addToSet(value, typeOf<V>())
	}

	/**
	 * Adds multiple [values] at the end of the array, unless they are already present.
	 *
	 * Each value in [values] is treated independently.
	 * It if it is already present in the array, nothing happens.
	 * If it is absent from the array, it is added at the end.
	 *
	 * MongoDB detects equality if the two documents are exactly identical. All the fields must be the same _in the same order_.
	 *
	 * This is a convenience function for calling [addToSet] multiple times.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val tokens: List<String>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::tokens addEachToSet listOf("123456789", "789456123")
	 *     }
	 * )
	 * ```
	 *
	 * This will add `"123456789"` and `"789465123"` to the user's tokens only if they aren't already present.
	 * If only one of them is present, the other is added.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/addToSet/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.addEachToSet(values: Iterable<V>, type: KType) {
		for (value in values)
			this.addToSet(value, type)
	}

	/**
	 * Adds multiple [values] at the end of the array, unless they are already present.
	 *
	 * Each value in [values] is treated independently.
	 * It if it is already present in the array, nothing happens.
	 * If it is absent from the array, it is added at the end.
	 *
	 * MongoDB detects equality if the two documents are exactly identical. All the fields must be the same _in the same order_.
	 *
	 * This is a convenience function for calling [addToSet] multiple times.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val tokens: List<String>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::tokens addEachToSet listOf("123456789", "789456123")
	 *     }
	 * )
	 * ```
	 *
	 * This will add `"123456789"` and `"789465123"` to the user's tokens only if they aren't already present.
	 * If only one of them is present, the other is added.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/addToSet/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, Collection<V>>.addEachToSet(values: Iterable<V>) {
		this.addEachToSet(values, typeOf<V>())
	}

	// endregion
	// region $push

	/**
	 * Adds [value] at the end of the array.
	 *
	 * Unlike [addToSet], this operator always adds the value, even if it's already present in the array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val scores: List<Int>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::scores push 100
	 *     }
	 * )
	 * ```
	 *
	 * This will add `100` to the user's scores, even if it's already present.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/push/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.push(value: V, type: KType)

	/**
	 * Adds [value] at the end of the array.
	 *
	 * Unlike [addToSet], this operator always adds the value, even if it's already present in the array.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val scores: List<Int>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::scores push 100
	 *     }
	 * )
	 * ```
	 *
	 * This will add `100` to the user's scores, even if it's already present.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/push/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, Collection<V>>.push(value: V) {
		this.push(value, typeOf<V>())
	}

	/**
	 * Adds multiple [values] at the end of the array.
	 *
	 * Unlike [addToSet], this operator always adds all values, even if they're already present in the array.
	 *
	 * This is a convenience function for calling [push] multiple times.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val scores: List<Int>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::scores pushEach listOf(100, 200)
	 *     }
	 * )
	 * ```
	 *
	 * This will add `100` and `200` to the user's scores, even if they're already present.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/push/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.pushEach(values: Iterable<V>, type: KType) {
		for (value in values)
			this.push(value, type)
	}

	/**
	 * Adds multiple [values] at the end of the array.
	 *
	 * Unlike [addToSet], this operator always adds all values, even if they're already present in the array.
	 *
	 * This is a convenience function for calling [push] multiple times.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val scores: List<Int>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::scores pushEach listOf(100, 200)
	 *     }
	 * )
	 * ```
	 *
	 * This will add `100` and `200` to the user's scores, even if they're already present.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/push/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, Collection<V>>.pushEach(values: Iterable<V>) {
		this.pushEach(values, typeOf<V>())
	}

	/**
	 * Adds values to the end of the array with advanced options.
	 *
	 * This method allows using MongoDB's advanced `$push` operators like `$each` and `$slice`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val tokens: List<String>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::tokens push {
	 *             each("123", "456")
	 *             slice(3)
	 *         }
	 *     }
	 * )
	 * ```
	 *
	 * This will add `"123"` and `"456"` to the user's tokens and keep only the last 3 elements.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/push/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.push(builder: PushBuilder<V>.() -> Unit, type: KType)

	/**
	 * Adds values to the end of the array with advanced options.
	 *
	 * This method allows using MongoDB's advanced `$push` operators like `$each` and `$slice`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 *     val tokens: List<String>,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::tokens push {
	 *             each("123", "456")
	 *             slice(3)
	 *         }
	 *     }
	 * )
	 * ```
	 *
	 * This will add `"123"` and `"456"` to the user's tokens and keep only the last 3 elements.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/v7.0/reference/operator/update/push/)
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, Collection<V>>.push(noinline builder: PushBuilder<V>.() -> Unit) {
		push(builder, typeOf<V>())
	}

	// endregion

	/**
	 * DSL builder for advanced `$push` operations.
	 *
	 * See [push].
	 */
	@KtMongoDsl
	interface PushBuilder<V> : BsonNode {

		/**
		 * Specifies the values to push to the array.
		 *
		 * If this function is called multiple times, they are combined (keeping the calling order).
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val tokens: List<String>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tokens push {
		 *             each("123", "456")
		 *         }
		 *     }
		 * )
		 * ```
		 *
		 * You can also call [push] multiple times:
		 *
		 * ```kotlin
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tokens push "123"
		 *         User::tokens push "456"
		 *     }
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/push/)
		 */
		@KtMongoDsl
		fun each(vararg values: V) {
			each(values.asIterable())
		}

		/**
		 * Specifies the values to push to the array.
		 *
		 * If this function is called multiple times, they are combined (keeping the calling order).
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val tokens: List<String>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tokens push {
		 *             each(listOf("123", "456"))
		 *         }
		 *     }
		 * )
		 * ```
		 *
		 * You can also call [push] multiple times:
		 *
		 * ```kotlin
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tokens push "123"
		 *         User::tokens push "456"
		 *     }
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/push/)
		 */
		@KtMongoDsl
		fun each(values: Iterable<V>)

		/**
		 * Limits the number of array elements after the push operation.
		 *
		 * If [count] is **positive**, after adding the elements to the array,
		 * only the **first** [count] elements are kept, and all additional elements are discarded.
		 *
		 * If [count] is **negative**, after adding the elements to the array,
		 * only the **last** [count] elements are kept, and prior elements are discarded.
		 *
		 * If this operator is specified without an [each] operator, the size of the array
		 * is truncated but no elements are added.
		 *
		 * If this function is called multiple times, only the latest value has an effect.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val tokens: List<String>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tokens push {
		 *             each("123", "456")
		 *             slice(3)  // Keep only the first 3 elements
		 *         }
		 *     }
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/slice)
		 */
		@KtMongoDsl
		fun slice(count: Int)

		/**
		 * Specifies the location in the array at which the `$push` operator inserts elements.
		 *
		 * Without the `$position` modifier, the `$push` operator inserts elements to the end of the array.
		 *
		 * A **non-negative** number corresponds to the position in the array, starting from the beginning of the array.
		 * If the value is greater or equal to the length of the array, the `$position` modifier has no effect
		 * and `$push` adds elements to the end of the array.
		 *
		 * A **negative** number corresponds to the position in the array, counting from (but not including)
		 * the last element of the array. For example, -1 indicates the position just before the last element
		 * in the array. If you specify multiple elements in the `$each` array, the last added element is in
		 * the specified position from the end. If the absolute value is greater than or equal to the length
		 * of the array, the `$push` adds elements to the beginning of the array.
		 *
		 * Said otherwise:
		 * - To insert elements at the end of the array, do not specify [position] at all.
		 * - To insert elements at the start of the array, specify `position(0)`.
		 * - To insert elements just after the first value, specify `position(1)`.
		 * - To insert elements just before the last value, specify `position(-1)`.
		 *
		 * If this function is called multiple times, only the last value has an effect.
		 *
		 * If this function is called without an [each] operator, this function does nothing.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val scores: List<Int>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::scores push {
		 *             each(50, 60, 70)
		 *             position(0)  // Insert at the beginning
		 *         }
		 *     }
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/position/)
		 */
		@KtMongoDsl
		fun position(index: Int)

		/**
		 * Orders the elements of an array during a `$push` operation.
		 *
		 * If this function is called multiple times, only the last value has an effect.
		 *
		 * If this function is called without an [each] operator, the existing data is sorted but no elements are added.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class Quiz(
		 *     val id: Int,
		 *     val score: Int,
		 * )
		 *
		 * class User(
		 *     val name: String,
		 *     val quizzes: List<Quiz>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::quizzes push {
		 *             each(Quiz(3, 8), Quiz(4, 7), Quiz(5, 6))
		 *             sort {
		 *                 ascending(Quiz::score)
		 *             }
		 *         }
		 *     }
		 * )
		 * ```
		 *
		 * To sort based on the natural order of the array elements themselves:
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val scores: List<Int>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::scores push {
		 *             each(12, 34)
		 *             sort { ascending() }
		 *         }
		 *     }
		 * )
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/sort/)
		 */
		@KtMongoDsl
		fun sort(block: PushSortDsl<V & Any>.() -> Unit)
	}

	/**
	 * DSL for sorting elements during a `$push` operation.
	 *
	 * See [push] and [PushBuilder.sort].
	 */
	@KtMongoDsl
	interface PushSortDsl<V : Any> : SortOptionDsl<V> {

		/**
		 * Sort array elements in ascending order (for simple values, not documents).
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val tests: List<Int>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tests push {
		 *             each(40, 60)
		 *             sort { ascending() }
		 *         }
		 *     }
		 * )
		 * ```
		 */
		@KtMongoDsl
		fun ascending()

		/**
		 * Sort array elements in descending order (for simple values, not documents).
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val name: String,
		 *     val tests: List<Int>,
		 * )
		 *
		 * collection.updateOne(
		 *     filter = {
		 *         User::name eq "Bob"
		 *     },
		 *     update = {
		 *         User::tests push {
		 *             each(40, 60)
		 *             sort { descending() }
		 *         }
		 *     }
		 * )
		 * ```
		 */
		@KtMongoDsl
		fun descending()
	}

	// region Array filters

	/**
	 * Filters an array and performs the specified update only on the filtered items.
	 *
	 * Unlike [selected], which only updates a single array element, [filter] can update multiple array elements.
	 * [filter] can also be nested.
	 *
	 * ### Usage
	 *
	 * MongoDB doesn't allow specifying the filter within the update itself, it must be specified in the options.
	 * The parameter [id] must match a declared array filter.
	 *
	 * You may prefer using the overload which automatically registers the filter.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val grades: List<Grade>,
	 * )
	 *
	 * class Grade(
	 *     val subject: String,
	 *     val grade: Int,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     options = {
	 *         arrayFilter("maths") {
	 *             Grade::subject eq "Maths"
	 *         }
	 *     },
	 *     update = {
	 *         User::grades.filter("maths") / Grade::grade inc 10
	 *     }
	 * )
	 * ```
	 *
	 * This will increment all grades for the subject `"Maths"` for user `"Bob"`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/positional-filtered)
	 */
	@KtMongoDsl
	@LowLevelApi
	fun <V> Field<T, Collection<V>>.filter(id: String): Field<T, V>

	/**
	 * Filters an array and performs the specified update only on the filtered items.
	 *
	 * Unlike [selected], which only updates a single array element, [filter] can update multiple array elements.
	 * [filter] can also be nested.
	 *
	 * **This overload registers the array filter within the option automatically.**
	 * There is no need to configure [opensavvy.ktmongo.dsl.options.WithArrayFilters.arrayFilter].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val grades: List<Grade>,
	 * )
	 *
	 * class Grade(
	 *     val subject: String,
	 *     val grade: Int,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Bob"
	 *     },
	 *     update = {
	 *         User::grades.filter {
	 *             it / Grade::subject eq "Maths"
	 *         } / Grade::grade inc 10
	 *     }
	 * )
	 * ```
	 *
	 * This will increment all grades for the subject `"Maths"` for user `"Bob"`.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/positional-filtered)
	 *
	 * @param id The identifier of the generated array filter. If `null`, a unique identifier is generated automatically.
	 * @param filter The filter expression to apply to the array element.
	 * The lambda passes a parameter that represents the current item during the iteration.
	 * The operators within this lambda are the same as [FilterQuery].
	 */
	@KtMongoDsl
	fun <V> Field<T, Collection<V>>.filter(
		id: String? = null,
		filter: ArrayFiltersOptionDsl<V>.(it: Field<ArrayFiltersOptionDsl.IteratorType<V>, V>) -> Unit,
	): Field<T, V>

	// endregion

}

/**
 * DSL for MongoDB operators that are used to update existing values, creating new documents if none exist (does *not* include aggregation operators).
 *
 * This interface is a variant of [UpdateQuery] used in upsert operations. See [UpdateQuery] for more information.
 *
 * If you can't find the operator you're searching for, visit the [tracking issue](https://gitlab.com/opensavvy/ktmongo/-/issues/5).
 */
@KtMongoDsl
interface UpsertQuery<T> : UpdateQuery<T> {

	// region $setOnInsert

	/**
	 * If an upsert operation results in an insert of a document,
	 * then this operator assigns the specified [value] to the field.
	 * If the update operation does not result in an insert, this operator does nothing.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.upsertOne {
	 *     User::age setOnInsert 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/setOnInsert/)
	 *
	 * @see set Always set the value.
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setOnInsert(value: V, type: KType)

	/**
	 * If an upsert operation results in an insert of a document,
	 * then this operator assigns the specified [value] to the field.
	 * If the update operation does not result in an insert, this operator does nothing.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.filter {
	 *     User::name eq "foo"
	 * }.upsertOne {
	 *     User::age setOnInsert 18
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/setOnInsert/)
	 *
	 * @see set Always set the value.
	 */
	@Suppress("INVISIBLE_REFERENCE", "WRONG_MODIFIER_CONTAINING_DECLARATION")
	@KtMongoDsl
	final inline infix fun <@kotlin.internal.OnlyInputTypes reified V> Field<T, V>.setOnInsert(value: V) {
		this.setOnInsert(value, typeOf<V>())
	}

	// endregion

}
