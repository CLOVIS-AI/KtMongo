/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

@RequiresOptIn("This symbol is part of the experimental BsonPath API. It may change or be removed without warnings. Please provide feedback in https://gitlab.com/opensavvy/ktmongo/-/issues/93.")
annotation class ExperimentalBsonPathApi

/**
 * Access specific fields in arbitrary BSON documents using a [JSONPath](https://www.rfc-editor.org/rfc/rfc9535.html)-like API.
 *
 * ### Example
 *
 * ```kotlin
 * BsonPath["foo"]    // Refer to the field 'foo': $.foo
 * BsonPath[0]        // Refer to the item at index 0: $[0]
 * BsonPath["foo"][0] // Refer to the item at index 0 in the array named 'foo': $.foo[0]
 * ```
 */
@ExperimentalBsonPathApi
sealed interface BsonPath {

	/**
	 * Points to a [field] in a [Bson] document.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * BsonPath["foo"]         // $.foo
	 * BsonPath["foo"]["bar"]  // $.foo.bar
	 * ```
	 */
	operator fun get(field: String): BsonPath =
		Field(field, this)

	/**
	 * Points to the element at [index] in a [BsonArray].
	 *
	 * BSON indices start at 0.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * BsonPath[0]         // $[0]
	 * BsonPath["foo"][1]  // $.foo[1]
	 * ```
	 */
	operator fun get(index: Int): BsonPath =
		Item(index, this)

	private data class Field(val name: String, val parent: BsonPath) : BsonPath {

		init {
			require("'" !in name) { "The character ' (apostrophe) is currently forbidden in BsonPath expressions, found: \"$name\"" }
		}

		override fun toString() =
			if (name matches legalCharacters) "$parent.$name"
			else "$parent['$name']"

		companion object {
			private val legalCharacters = Regex("[a-zA-Z0-9]*")
		}
	}

	private data class Item(val index: Int, val parent: BsonPath) : BsonPath {
		init {
			require(index >= 0) { "BSON array indices start at 0, found: $index" }
		}

		override fun toString() = "$parent[$index]"
	}

	companion object Root : BsonPath {
		override fun toString() = "$"
	}
}
