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

import opensavvy.ktmongo.dsl.LowLevelApi
import org.intellij.lang.annotations.Language
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

@RequiresOptIn("This symbol is part of the experimental BsonPath API. It may change or be removed without warnings. Please provide feedback in https://gitlab.com/opensavvy/ktmongo/-/issues/93.")
annotation class ExperimentalBsonPathApi

/**
 * Access specific fields in arbitrary BSON documents using a [JSONPath](https://www.rfc-editor.org/rfc/rfc9535.html)-like API.
 *
 * To access fields of a [BSON document][Bson], use [select] or [at].
 *
 * ### Why BSON paths?
 *
 * Most of the time, users want to deserialize documents, which they can do with [opensavvy.ktmongo.bson.read].
 *
 * However, sometimes, we receive large BSON payloads but only care about a few fields (for example, an explain plan).
 * Writing an entire DTO for such payloads is time-consuming and complex.
 *
 * Deserializing only a few specific fields can be much faster than deserializing the entire payload, as BSON is designed
 * to allow skipping unwanted fields.
 *
 * We may also face a payload that is too dynamic to easily deserialize, or with so much nesting that accessing fields becomes boilerplate.
 *
 * In these situations, it may be easier (and often, more performant) to only deserialize a few specific fields,
 * which is what [BsonPath] is useful for.
 *
 * ### Syntax
 *
 * ```kotlin
 * BsonPath["foo"]    // Refer to the field 'foo': $.foo
 * BsonPath[0]        // Refer to the item at index 0: $[0]
 * BsonPath["foo"][0] // Refer to the item at index 0 in the array named 'foo': $.foo[0]
 * ```
 *
 * ### Accessing data
 *
 * Find the first value for a given BSON path using [at]:
 *
 * ```kotlin
 * val document: Bson = …
 *
 * val name = document at BsonPath["profile"]["name"]
 * ```
 *
 * Find all values for a given BSON path using [select]:
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.select(BsonPath["profile"])
 *     .forEach { println("Found $it") }
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

	@LowLevelApi
	fun findIn(reader: BsonValueReader): Sequence<BsonValueReader>

	private data class Field(val name: String, val parent: BsonPath) : BsonPath {

		init {
			require("'" !in name) { "The character ' (apostrophe) is currently forbidden in BsonPath expressions, found: \"$name\"" }
		}

		@LowLevelApi
		override fun findIn(reader: BsonValueReader): Sequence<BsonValueReader> =
			parent.findIn(reader)
				.mapNotNull {
					if (it.type == BsonType.Document) {
						it.readDocument().read(name)
					} else {
						null
					}
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

		@LowLevelApi
		override fun findIn(reader: BsonValueReader): Sequence<BsonValueReader> =
			parent.findIn(reader)
				.mapNotNull {
					if (it.type == BsonType.Array) {
						it.readArray().read(index)
					} else {
						null
					}
				}

		override fun toString() = "$parent[$index]"
	}

	/**
	 * The root of a [BsonPath] expression.
	 *
	 * All BSON paths start at the root.
	 * For example, `BsonPath["foo"]` refers to the field `"foo"`.
	 *
	 * For more information, see [BsonPath].
	 */
	companion object Root : BsonPath {
		@LowLevelApi
		override fun findIn(reader: BsonValueReader): Sequence<BsonValueReader> =
			sequenceOf(reader)

		override fun toString() = "$"

		/**
		 * Parses an [RFC-9535 compliant](https://www.rfc-editor.org/rfc/rfc9535.html) string expression into a [BsonPath] instance.
		 *
		 * This function is the mirror of the [BsonPath.toString] methods.
		 *
		 * **Warning.** Not everything from the RFC is implemented at the moment.
		 * As a rule of thumb, if [text] can be returned by the [BsonPath.toString] function of a segment,
		 * then it can be parsed by this function.
		 *
		 * | Syntax                | Description                                                  |
		 * | --------------------- | ------------------------------------------------------------ |
		 * | `$`                   | The root identifier. See [BsonPath.Root].                    |
		 * | `['foo']` or `.foo`   | Accessor for a field named `foo`. See [BsonPath.get].        |
		 * | `[0]`                 | Accessor for the first item of an array. See [BsonPath.get]. |
		 *
		 * ### Examples
		 *
		 * ```kotlin
		 * val document: Bson = …
		 *
		 * val id: Uuid = document at BsonPath.parse("$.profile.id")
		 *
		 * for (user in document.select<User>("$.friends")) {
		 *     println("User: $user")
		 * }
		 * ```
		 *
		 * @see BsonPath Learn more about BSON paths.
		 * @see at Access one field by its BSON path.
		 * @see select Access multiple fields by their BSON path.
		 */
		fun parse(@Language("JSONPath") text: String): BsonPath {
			require(text.startsWith("$")) { "A BsonPath expression must start with a dollar sign: $text\nDid you mean to create a BsonPath to access a specific field? If so, see BsonPath[\"foo\"]" }

			var expr: BsonPath = BsonPath

			for (segment in splitSegments(text)) {
				expr = when {
					// .foo
					segment.startsWith(".") ->
						expr[segment.removePrefix(".")]

					// ['foo']
					segment.startsWith("['") && segment.endsWith("']") ->
						expr[segment.removePrefix("['").removeSuffix("']")]

					// ["foo"]
					segment.startsWith("[\"") && segment.endsWith("\"]") ->
						expr[segment.removePrefix("[\"").removeSuffix("\"]")]

					// [0]
					segment.startsWith("[") && segment.endsWith("]") ->
						expr[segment.removePrefix("[").removeSuffix("]").toInt()]

					else -> throw IllegalArgumentException("Could not parse the segment “$segment” in BsonPath expression “$text”.")
				}
			}

			return expr
		}

		private fun splitSegments(text: String): Sequence<String> = sequence {
			val accumulator = StringBuilder()
			var i = 1 // skip the $ sign

			// Helper for nicer error messages
			fun fail(msg: String, cause: Throwable? = null): Nothing {
				val excerpt =
					if (i + 5 > text.length) text.substring(i, text.length)
					else text.substring(i, i + 5) + "…"

				throw IllegalArgumentException("Could not parse the BSON path expression “$text” at index $i (“${excerpt}”): $msg", cause)
			}

			fun accumulate() {
				accumulator.append(text[i])
				i++
			}

			fun accumulateWhile(predicate: (Char) -> Boolean) {
				while (i < text.length && predicate(text[i])) {
					accumulate()
				}
			}

			try {
				while (i < text.length) {
					val c = text[i]

					when (c) {
						'.' if accumulator.isEmpty() -> {
							// .foo
							accumulate()

							if (text[i].isNameFirst()) {
								accumulateWhile { it.isNameChar() }
							} else {
								fail("A name segment should start with a non-digit character, found: ${text[i]}")
							}

							yield(accumulator.toString())
							accumulator.clear()
						}

						'[' if accumulator.isEmpty() -> {
							val c2 = text[i + 1]

							when (c2) {
								'\'', '"' -> {
									accumulate() // opening bracket
									accumulate() // opening quote
									accumulateWhile { it != '\'' && it != '"' }
									accumulate() // closing quote
									accumulate() // closing bracket
									yield(accumulator.toString())
									accumulator.clear()
								}

								in Char(0x30)..Char(0x39) -> {
									accumulate() // opening bracket
									accumulateWhile { it.isDigit() }
									accumulate() // closing bracket
									yield(accumulator.toString())
									accumulator.clear()
								}

								else -> fail("Unrecognized selector")
							}
						}

						else -> {
							fail("Unrecognized syntax")
						}
					}
				}
			} catch (e: Exception) {
				if (e is IllegalArgumentException)
					throw e
				else
					fail("An exception was thrown: ${e.message}", e)
			}
		}

		private inline fun Char.isAlpha() =
			this.code in 0x41..0x51 || this.code in 0x61..0x7A

		private inline fun Char.isDigit() =
			this.code in 0x30..0x39

		private inline fun Char.isNameFirst() =
			isAlpha() || this == '_' || this.code in 0x80..0xD7FF || this.code in 0xE000..0x10FFFF

		private inline fun Char.isNameChar() =
			isNameFirst() || isDigit()
	}
}

/**
 * Finds all values that match [path] in a given [BSON document][Bson].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.select<String>(BsonPath["foo"]["bar"]).firstOrNull()
 * ```
 * will return the value of the field `foo.bar`.
 *
 * @see BsonPath Learn more about BSON paths.
 * @see selectFirst If you're only interested about a single element. See also [at].
 */
@OptIn(LowLevelApi::class)
@ExperimentalBsonPathApi
inline fun <reified T : Any?> Bson.select(path: BsonPath): Sequence<T> {
	val type = typeOf<T>()

	return path.findIn(this.reader().asValue())
		.map {
			@Suppress("UNCHECKED_CAST")
			val result = it.read(type, type.classifier as KClass<T & Any>)

			if (null is T) {
				result as T
			} else {
				result
					?: throw BsonReaderException("Found an unexpected 'null' when reading the path $path in document $this")
			}
		}
}

/**
 * Finds the first value that matches [path] in a given [BSON document][Bson].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.selectFirst<String>(BsonPath["foo"]["bar"])
 * ```
 * will return the value of the field `foo.bar`.
 *
 * ### Alternatives
 *
 * Depending on your situation, you can also use the equivalent function [at]:
 * ```kotlin
 * val document: Bson = …
 *
 * val bar: String = document at BsonPath["foo"]["bar"]
 * ```
 *
 * @see BsonPath Learn more about BSON paths.
 * @see select Select multiple values with a BSON path.
 * @throws NoSuchElementException If no element is found matching the path.
 */
@ExperimentalBsonPathApi
inline fun <reified T : Any?> Bson.selectFirst(path: BsonPath): T {
	val iter = select<T>(path).iterator()

	if (iter.hasNext()) {
		return iter.next()
	} else {
		throw NoSuchElementException("Could not find any value at path $path for document $this")
	}
}

/**
 * Finds the first value that matches [path] in a given [BSON document][Bson].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * val bar: String = document at BsonPath["foo"]["bar"]
 * ```
 * will return the value of the field `foo.bar`.
 *
 * Depending on your situation, you can also use the equivalent function [selectFirst]:
 * ```kotlin
 * val document: Bson = …
 *
 * document.selectFirst<String>(BsonPath["foo"]["bar"])
 * ```
 *
 * @see BsonPath Learn more about BSON paths.
 * @see select Select multiple values with a BSON path.
 * @throws NoSuchElementException If no element is found matching the path.
 */
@ExperimentalBsonPathApi
inline infix fun <reified T : Any?> Bson.at(path: BsonPath): T =
	selectFirst(path)
