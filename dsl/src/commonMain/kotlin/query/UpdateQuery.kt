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

import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.*
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import kotlin.reflect.KProperty1
import kotlin.time.ExperimentalTime
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
 * - [`$addToSet`][addToSet]
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
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V)

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
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: V) {
		this.field.set(value)
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
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setIf(condition: Boolean, value: V) {
		if (condition)
			this set value
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
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setIf(condition: Boolean, value: V) =
		this.field.setIf(condition, value)

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
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setUnless(condition: Boolean, value: V) {
		if (!condition)
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
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setUnless(condition: Boolean, value: V) =
		this.field.setUnless(condition, value)

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.inc(amount: V)

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Number> KProperty1<T, V>.inc(amount: V) {
		this.field.inc(amount)
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
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.plusAssign(amount: V): Unit =
		this.inc(amount)

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
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	operator fun <@kotlin.internal.OnlyInputTypes V : Number> KProperty1<T, V>.plusAssign(amount: V): Unit =
		this.field.plusAssign(amount)

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.mul(amount: V)

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Number> KProperty1<T, V>.mul(amount: V) {
		this.field.mul(amount)
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
	fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.unset() {
		this.field.unset()
	}

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> Field<T, V?>.min(value: V)

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> KProperty1<T, V?>.min(value: V) {
		this.field.min(value)
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
	infix fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> Field<T, V?>.max(value: V)

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
	infix fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> KProperty1<T, V?>.max(value: V) {
		this.field.max(value)
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
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.renameTo(newName: Field<T, V>) {
		this.field.renameTo(newName)
	}

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
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.renameTo(newName: KProperty1<T, V>) {
		this.renameTo(newName.field)
	}

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
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.renameTo(newName: KProperty1<T, V>) {
		this.renameTo(newName.field)
	}

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
	@ExperimentalTime
	@KtMongoDsl
	fun Field<T, Instant?>.setToCurrentDate()

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
	@ExperimentalTime
	@KtMongoDsl
	fun KProperty1<T, Instant?>.setToCurrentDate() {
		this.field.setToCurrentDate()
	}

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
	@ExperimentalTime
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("setToCurrentTimestamp")
	@KtMongoDsl
	fun Field<T, Timestamp?>.setToCurrentDate()

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
	@ExperimentalTime
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("setToCurrentTimestamp")
	@KtMongoDsl
	fun KProperty1<T, Timestamp?>.setToCurrentDate() {
		this.field.setToCurrentDate()
	}

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
	val <E> KProperty1<T, Collection<E>>.selected: Field<T, E>
		get() = this.field.selected

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
	val <E> KProperty1<T, Collection<E>>.all: Field<T, E>
		get() = this.field.all

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
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.addToSet(value: V)

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
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, Collection<V>>.addToSet(value: V) {
		this.field.addToSet(value)
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
	 *         User::tokens addToSet listOf("123456789", "789456123")
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
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, Collection<V>>.addToSet(values: Iterable<V>) {
		for (value in values)
			this addToSet value
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
	 *         User::tokens addToSet listOf("123456789", "789456123")
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
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, Collection<V>>.addToSet(values: Iterable<V>) {
		this.field addToSet values
	}

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
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setOnInsert(value: V)

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
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.setOnInsert(value: V) {
		this.field.setOnInsert(value)
	}

	// endregion

}
