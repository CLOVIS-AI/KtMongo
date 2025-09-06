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

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.typeOf

/**
 * A BSON document.
 *
 * To create instances of this class, see [BsonContext.buildDocument].
 */
interface Bson {

	/**
	 * Low-level byte representation of this BSON document.
	 */
	@LowLevelApi
	fun toByteArray(): ByteArray

	/**
	 * Reads the fields of this document.
	 */
	@LowLevelApi
	fun reader(): BsonDocumentReader

	/**
	 * JSON representation of this [Bson] object, as a [String].
	 */
	override fun toString(): String
}

/**
 * A BSON array.
 *
 * To create instances of this class, see [BsonContext.buildArray].
 */
interface BsonArray {

	/**
	 * Low-level byte representation of this BSON document.
	 */
	fun toByteArray(): ByteArray

	/**
	 * Reads the elements of this array.
	 */
	@LowLevelApi
	fun reader(): BsonArrayReader

	/**
	 * JSON representation of this [BsonArray], as a [String].
	 */
	override fun toString(): String
}

/**
 * Reads this document into an instance of type [T].
 *
 * If it isn't possible to deserialize this BSON to the given type, an exception is thrown.
 *
 * @see BsonContext.write The inverse operation.
 */
@OptIn(LowLevelApi::class)
inline fun <reified T : Any> Bson.read(): T? =
	reader().read(typeOf<T>(), T::class)
