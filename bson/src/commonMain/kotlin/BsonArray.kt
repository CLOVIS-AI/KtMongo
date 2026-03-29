/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

/**
 * A BSON array.
 *
 * To create instances of this class, see [BsonFactory].
 *
 * ### Implementation constraints
 *
 * [equals] and [hashCode] should follow the same constraints as [BsonArrayReader]'s implementations.
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
