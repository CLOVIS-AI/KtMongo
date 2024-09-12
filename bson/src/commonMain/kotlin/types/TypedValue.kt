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

package opensavvy.ktmongo.bson.types

import kotlin.reflect.KClass

/**
 * A holder class for a [value] and its [type].
 *
 * To get an instance of this type, see [typed].
 */
sealed class TypedValue<out T> {

	abstract val value: T

	/**
	 * The type of [value].
	 *
	 * If [value] is `null`, this field is `null` too.
	 */
	abstract val type: KClass<out Any>?

	class NonNullable<T : Any>(
		override val value: T,
		override val type: KClass<T>
	) : TypedValue<T>() {

		override fun toString() = "$value of $type"
	}

	object Null : TypedValue<Nothing?>() {
		override val value: Nothing? get() = null
		override val type: Nothing? get() = null

		override fun toString() = "null"
	}
}

fun <T : Any> typed(value: T): TypedValue.NonNullable<T> =
	TypedValue.NonNullable(value, value::class)

fun typed(@Suppress("UNUSED_PARAMETER") value: Nothing? = null): TypedValue.Null =
	TypedValue.Null

fun <T : Any?> typed(value: T) =
	if (value == null) typed()
	else typed(value)
