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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field.Companion.unsafe
import kotlin.reflect.KProperty1

/**
 * High-level, typesafe pointer to a specific field in a document.
 *
 * This type may refer to fields nested in children documents or arrays.
 * Instances are generally used to refer to a MongoDB field we want to read or update, when
 * writing requests.
 *
 * ### Type safety
 *
 * This type is responsible for ensuring type safety:
 * ```kotlin
 * class User(
 *     val _id: ObjectId,
 *     val profile: Profile,
 *     val friends: List<Friend>,
 * )
 *
 * class Profile(
 *     val name: String,
 * )
 *
 * class Friend(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * // Refer to the user's id
 * println(User::_id.field)
 *
 * // Refer to the user's name
 * println(User::profile / Profile::name)
 *
 * // Refer to the name of the second friend
 * println(User::friends[1] / Friend::name)
 * ```
 *
 * Some of the functions of the DSL may be available only when [FieldDsl] is in scope.
 * All operator scopes provided by this library should bring it into scope automatically.
 *
 * For example, when writing a filter, methods from this interface are automatically available:
 * ```kotlin
 * collection.find {
 *     User::profile / Profile::name eq "Thibault Lognaise"
 * }
 * ```
 *
 * To bypass type-safety, and refer to arbitrary fields without declaring them first, see [unsafe].
 *
 * @param Root The type of the document in which this field is in.
 * @param Type The type of the value stored by this field.
 */
interface Field<in Root, out Type> {

	/**
	 * Low-level representation of this field's path.
	 */
	@LowLevelApi
	val path: Path

	companion object {

		/**
		 * Refers to a field [child] with no compile-time safety.
		 *
		 * Sometimes, we must refer to a field that we don't want to add in the DTO representation.
		 * For example, when writing complex aggregation queries that use intermediary fields that are removed
		 * before the data is sent to the server.
		 *
		 * We recommend preferring the type-safe syntax when possible (see [Field]).
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * println(Field.unsafe<Int>("age")) // 'age'
		 * ```
		 *
		 * @see Field.unsafe Similar, but for accessing a child of a document.
		 */
		@OptIn(LowLevelApi::class)
		fun <T> unsafe(child: String): Field<Any, T> =
			FieldImpl(Path(child))
	}
}

/**
 * DSL to refer to [fields][Field], usually automatically added into scope by operators.
 */
interface FieldDsl {

	/**
	 * The strategy used when converting from [KProperty1] to [Field].
	 */
	@LowLevelApi
	val context: PropertyNameStrategy

	/**
	 * Converts a Kotlin property into a [Field].
	 *
	 * The KtMongo DSL is built on top of the [Field] interface, which represents a specific
	 * field in a document (possibly nested).
	 *
	 * To help with writing requests, we provide utilities to use Kotlin property references as well:
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * println(User::name)       // KProperty1…
	 * println(User::name.field) // Field 'name'
	 * ```
	 *
	 * Most functions of the DSL have an overload that accepts a [KProperty1] and calls
	 * [field] before calling the real operator implementation.
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class)
	val <Root, Type> KProperty1<Root, Type>.field: Field<Root, Type>
		get() = FieldImpl(context.pathOf(this))

	/**
	 * Refers to [child] as a nested field of the current field.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val id: Int,
	 *     val profile: Profile,
	 * )
	 *
	 * class Profile(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // Refer to the name
	 * println(User::profile / Profile::name)
	 * // → 'profile.name'
	 *
	 * // Refer to the age
	 * println(User::profile / Profile::age)
	 * // → 'profile.age'
	 * ```
	 *
	 * @see get Access a specific element of an array
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	operator fun <Root, Type, Child> Field<Root, Type>.div(child: Field<Type, Child>): Field<Root, Child> =
		FieldImpl(path / child.path)

	/**
	 * Refers to [child] as a nested field of the current field.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val id: Int,
	 *     val profile: Profile,
	 * )
	 *
	 * class Profile(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // Refer to the name
	 * println(User::profile / Profile::name)
	 * // → 'profile.name'
	 *
	 * // Refer to the age
	 * println(User::profile / Profile::age)
	 * // → 'profile.age'
	 * ```
	 *
	 * @see get Access a specific element of an array
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	operator fun <Root, Type, Child> Field<Root, Type>.div(child: KProperty1<in Type & Any, Child>): Field<Root, Child> =
		FieldImpl(path / context.pathOf(child))

	/**
	 * Refers to a field [child] of the current field, with no compile-time safety.
	 *
	 * Sometimes, we must refer to a field that we don't want to add in the DTO representation.
	 * For example, when writing complex aggregation queries that use intermediary fields that are removed
	 * before the data is sent to the server.
	 *
	 * We recommend preferring the type-safe syntax when possible (see [Field]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * println(User::profile.unsafe<Int>("age")) // 'profile.age'
	 * ```
	 *
	 * @see Field.Companion.unsafe Similar, but for accessing a field of the root document.
	 */
	@OptIn(LowLevelApi::class)
	infix fun <Root, Type, Child> Field<Root, Type>.unsafe(child: String): Field<Root, Child> =
		FieldImpl(path / PathSegment.Field(child))

	/**
	 * Refers to a field [child] of the current field, with no compile-time safety.
	 *
	 * Sometimes, we must refer to a field that we don't want to add in the DTO representation.
	 * For example, when writing complex aggregation queries that use intermediary fields that are removed
	 * before the data is sent to the server.
	 *
	 * We recommend preferring the type-safe syntax when possible (see [Field]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * println(User::profile.unsafe<Int>("age")) // 'profile.age'
	 * ```
	 *
	 * @see Field.Companion.unsafe Similar, but for accessing a field of the root document.
	 */
	@OptIn(LowLevelApi::class)
	infix fun <Root, Child> KProperty1<Root, *>.unsafe(child: String): Field<Root, Child> =
		this.field.unsafe(child)

	/**
	 * Refers to a field [child] of the current field, without checking that it is a field available on the current object.
	 *
	 * We recommend preferring the type-safe syntax when possible (see [Field]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * println(User::profile / Pet::name)       // ⚠ compilation error: 'profile' doesn't have the type 'Pet'
	 * println(User::profile unsafe Pet::name)  // 'profile.name'
	 * ```
	 *
	 * @see div The recommended type-safe accessor.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	infix fun <Root, Child> Field<Root, *>.unsafe(child: Field<*, Child>): Field<Root, Child> =
		FieldImpl(this.path / child.path)

	/**
	 * Refers to a field [child] of the current field, without checking that it is a field available on the current object.
	 *
	 * We recommend preferring the type-safe syntax when possible (see [Field]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * println(User::profile / Pet::name)       // ⚠ compilation error: 'profile' doesn't have the type 'Pet'
	 * println(User::profile unsafe Pet::name)  // 'profile.name'
	 * ```
	 *
	 * @see div The recommended type-safe accessor.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	infix fun <Root, Child> Field<Root, *>.unsafe(child: KProperty1<*, Child>): Field<Root, Child> =
		this.unsafe(child.field)

	/**
	 * Refers to a field [child] of the current field, without checking that it is a field available on the current object.
	 *
	 * We recommend preferring the type-safe syntax when possible (see [Field]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * println(User::profile / Pet::name)       // ⚠ compilation error: 'profile' doesn't have the type 'Pet'
	 * println(User::profile unsafe Pet::name)  // 'profile.name'
	 * ```
	 *
	 * @see div The recommended type-safe accessor.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	infix fun <Root, Child> KProperty1<Root, *>.unsafe(child: Field<*, Child>): Field<Root, Child> =
		this.field.unsafe(child)

	/**
	 * Refers to a field [child] of the current field, without checking that it is a field available on the current object.
	 *
	 * We recommend preferring the type-safe syntax when possible (see [Field]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * println(User::profile / Pet::name)       // ⚠ compilation error: 'profile' doesn't have the type 'Pet'
	 * println(User::profile unsafe Pet::name)  // 'profile.name'
	 * ```
	 *
	 * @see div The recommended type-safe accessor.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	infix fun <Root, Child> KProperty1<Root, *>.unsafe(child: KProperty1<*, Child>): Field<Root, Child> =
		this.field.unsafe(child)

	/**
	 * Refers to [child] as a nested field of the current field.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val id: Int,
	 *     val profile: Profile,
	 * )
	 *
	 * class Profile(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * // Refer to the id
	 * println(User::id)
	 * // → 'id'
	 *
	 * // Refer to the name
	 * println(User::profile / Profile::name)
	 * // → 'profile.name'
	 *
	 * // Refer to the age
	 * println(User::profile / Profile::age)
	 * // → 'profile.age'
	 * ```
	 *
	 * @see get Access a specific element of an array
	 */
	@KtMongoDsl
	operator fun <Root, Parent, Child> KProperty1<Root, Parent>.div(child: KProperty1<Parent & Any, Child>): Field<Root, Child> =
		this.field / child

	/**
	 * Refers to a specific item in an array, by its index.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val friends: List<Friend>,
	 * )
	 *
	 * class Friend(
	 *     val name: String,
	 * )
	 *
	 * // Refer to the first friend
	 * println(User::friends[0])
	 * // → 'friends.$0'
	 *
	 * // Refer to the third friend's name
	 * println(User::friends[2] / Friend::name)
	 * // → 'friends.$2.name'
	 * ```
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class)
	operator fun <Root, Type> Field<Root, Collection<Type>>.get(index: Int): Field<Root, Type> =
		FieldImpl(this.path / PathSegment.Indexed(index))

	/**
	 * Refers to a specific item in an array, by its index.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val friends: List<Friend>,
	 * )
	 *
	 * class Friend(
	 *     val name: String,
	 * )
	 *
	 * // Refer to the first friend
	 * println(User::friends[0])
	 * // → 'friends.$0'
	 *
	 * // Refer to the third friend's name
	 * println(User::friends[2] / Friend::name)
	 * // → 'friends.$2.name'
	 * ```
	 */
	@KtMongoDsl
	operator fun <Root, Type> KProperty1<Root, Collection<Type>>.get(index: Int): Field<Root, Type> =
		this.field[index]

	/**
	 * Refers to a specific item in a map, by its name.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val friends: Map<String, Friend>,
	 * )
	 *
	 * class Friend(
	 *     val name: String,
	 * )
	 *
	 * // Refer to the friend Bob
	 * println(User::friends["bob"])
	 * // → 'friends.bob'
	 *
	 * // Refer to Bob's name
	 * println(User::friends["bob"] / Friend::name)
	 * // → 'friends.bob.name'
	 * ```
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class)
	operator fun <Root, Type> Field<Root, Map<String, Type>>.get(key: String): Field<Root, Type> =
		FieldImpl(this.path / PathSegment.Field(key))

	/**
	 * Refers to a specific item in a map, by its name.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val friends: Map<String, Friend>,
	 * )
	 *
	 * class Friend(
	 *     val name: String,
	 * )
	 *
	 * // Refer to the friend Bob
	 * println(User::friends["bob"])
	 * // → 'friends.bob'
	 *
	 * // Refer to Bob's name
	 * println(User::friends["bob"] / Friend::name)
	 * // → 'friends.bob.name'
	 * ```
	 */
	@KtMongoDsl
	operator fun <Root, Type> KProperty1<Root, Map<String, Type>>.get(index: String): Field<Root, Type> =
		this.field[index]

}

@OptIn(LowLevelApi::class)
internal class FieldDslImpl(
	override val context: BsonContext,
) : FieldDsl

@OptIn(LowLevelApi::class)
internal class FieldImpl<Root, Type>(
	override val path: Path,
) : Field<Root, Type> {

	override fun toString() = path.toString()

	// region Identity

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is FieldImpl<*, *>) return false

		if (path != other.path) return false

		return true
	}

	override fun hashCode(): Int {
		return path.hashCode()
	}

	// endregion
}
