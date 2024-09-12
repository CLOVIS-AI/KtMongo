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

typealias NullableValue<T> = TypedValue<T, KClass<out T & Any>?>
typealias NonNullValue<T> = TypedValue<T & Any, KClass<out T & Any>>
typealias NullValue = TypedValue<Nothing?, Nothing?>

/**
 * A holder class for a [value] and its [type].
 */
class TypedValue<T : Any?, out K : KClass<out T & Any>?> internal constructor(
	val value: T,

	/**
	 * The type of [value].
	 *
	 * If [value] is `null`, this field is `null` too.
	 */
	val type: K
) {

	init {
		require(value != null || type == null) { "The type can only be null if the value is null too. Value: $value. Type: $type." }
	}

	/**
	 * Extracts the `null` case out of the value.
	 *
	 * Useful to use smart-casting.
	 */
	fun orNull(): NonNullValue<T & Any>? =
		if (value == null) null
		else NonNullValue(value, type!!)
}

@Suppress("UNNECESSARY_NOT_NULL_ASSERTION") // the compiler is confused
fun <T> typed(value: T): NullableValue<T> =
	if (value != null) TypedValue(value, value!!::class)
	else TypedValue(value, null)
