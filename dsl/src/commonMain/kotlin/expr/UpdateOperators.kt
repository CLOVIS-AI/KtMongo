/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.expr

import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.CompoundExpression
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import kotlin.reflect.KProperty1

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
 * - [`$inc`][inc]
 * - [`$rename`][renameTo]
 * - [`$set`][set]
 * - [`$setOnInsert`][UpsertOperators.setOnInsert] (only for upserts)
 * - [`$unset`][unset]
 *
 * On arrays:
 * - [`$[]`][FieldDsl.get]
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/)
 *
 * @see FilterExpression Filters
 */
@KtMongoDsl
interface UpdateOperators<T> : CompoundExpression, FieldDsl {

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
	 * @see UpsertOperators.setOnInsert Only set if a new document is created.
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
	 * @see UpsertOperators.setOnInsert Only set if a new document is created.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: V) {
		this.field.set(value)
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

}

/**
 * DSL for MongoDB operators that are used to update existing values, creating new documents if none exist (does *not* include aggregation operators).
 *
 * This interface is a variant of [UpdateOperators] used in upsert operations. See [UpdateOperators] for more information.
 */
@KtMongoDsl
interface UpsertOperators<T> : UpdateOperators<T> {

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