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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KProperty1

/**
 * A pointer to a specific field in a document.
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
 * @param Root The type of the document in which this field is in.
 * @param Type The type of the value stored by this field.
 */
interface Field<@Suppress("unused") Root, @Suppress("unused") out Type> {

	/**
	 * Low-level representation of this field's path.
	 */
	@LowLevelApi
	val path: Path

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
	@KtMongoDsl
	operator fun <Child> div(child: Field<in Type, Child>): Field<Root, Child>

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
	@KtMongoDsl
	operator fun <Child> div(child: KProperty1<in Type, Child>): Field<Root, Child>
}

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
	FieldImpl<Root, Type>(this.path / PathSegment.Indexed(index))

/**
 * DSL to refer to [fields][Field].
 */
interface FieldDsl {

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
		get() = FieldImpl<Root, Type>(Path(this.name))

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
	operator fun <Root, Parent, Child> KProperty1<Root, Parent>.div(child: KProperty1<Parent, Child>): Field<Root, Child> =
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
	operator fun <Root, Type> KProperty1<Root, Collection<Type>>.get(index: Int): Field<Root, Type> =
		this.field[index]

}

@LowLevelApi
internal class FieldImpl<Root, Type>(
	override val path: Path,
) : Field<Root, Type> {

	@OptIn(DangerousMongoApi::class)
	override fun <Child> div(child: Field<in Type, Child>): Field<Root, Child> =
		FieldImpl(path / child.path)

	override fun <Child> div(child: KProperty1<in Type, Child>): Field<Root, Child> =
		FieldImpl(path / PathSegment.Field(child.name))

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
