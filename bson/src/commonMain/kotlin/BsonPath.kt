/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonPath.PathOrSelector
import opensavvy.ktmongo.bson.BsonPath.Root.findIn
import opensavvy.ktmongo.bson.BsonPath.Root.parent
import opensavvy.ktmongo.bson.BsonPath.Root.reversed
import opensavvy.ktmongo.bson.BsonPath.Root.sliced
import opensavvy.ktmongo.bson.BsonPath.Root.toString
import opensavvy.ktmongo.bson.BsonPath.Selector
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
	 * Applies the filters described by this path on the [reader].
	 *
	 * ### Implementation notes
	 *
	 * The [reader] is the root data.
	 * The path should recursively search by applying the [parent]'s [findIn] first.
	 */
	@LowLevelApi
	fun findIn(reader: BsonValueReader): Sequence<BsonValueReader>

	/**
	 * The parent path of this path: the same path without the last segment.
	 *
	 * For example, the path `$.foo.bar` has the parent `$.foo`.
	 *
	 * The root path has the parent `null`.
	 * All other paths have a non-null parent.
	 */
	@ExperimentalBsonPathApi
	val parent: BsonPath?

	/**
	 * Represents a unique selector in a [multi-selector segment][BsonPath.any].
	 *
	 * Selectors are generally obtained using the methods on [BsonPath.Root] that return a [PathOrSelector].
	 */
	@ExperimentalBsonPathApi
	sealed interface Selector {

		/**
		 * Applies the filters described by this path on the [reader].
		 *
		 * ### Implementation notes
		 *
		 * The [reader] is the **parent node**: one of the results of matching the parent path to the root node.
		 * It is not the root node itself, unlike with [findIn].
		 */
		@LowLevelApi
		fun findInParent(reader: BsonValueReader): Sequence<BsonValueReader>

	}

	/**
	 * Marker interface for types that implement both [BsonPath] and [Selector].
	 *
	 * These types are mainly found as return types in the [BsonPath.Root] object.
	 */
	@ExperimentalBsonPathApi
	sealed interface PathOrSelector : BsonPath, Selector

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
		SingleSelectorSegment(FieldSelector(field), this)

	/**
	 * Points to the element at [index] in a [BsonArray].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * BsonPath[0]         // $[0]
	 * BsonPath["foo"][1]  // $.foo[1], the first element
	 * BsonPath["foo"][-1] // $.foo[-1], the very least element
	 * ```
	 */
	operator fun get(index: Int): BsonPath =
		SingleSelectorSegment(IndexSelector(index), this)

	/**
	 * Points to all children of a document.
	 *
	 * - The children of a [BsonArray] are its elements.
	 * - The children of a [Bson] document are the values of its fields.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * BsonPath["foo"].all   // $.foo.*
	 * ```
	 */
	val all: BsonPath
		get() = SingleSelectorSegment(AllSelector, this)

	/**
	 * Points to the elements of a [BsonArray] at the indices selected by [range].
	 *
	 * If the node is not an array, nothing is returned.
	 *
	 * To create an open-ended range, use the overload that accepts integers.
	 *
	 * To reverse an array, see [reversed].
	 *
	 * ### Range normalization
	 *
	 * When a range has a step, the [IntProgression] class can reduce the closing bound
	 * if the step does not reach it. For example, `1 .. 6 step 2` becomes `1 .. 5 step 2`
	 * because both ranges cover the values `[1, 3, 5]` and `6` could not have been included.
	 * This doesn't impact the outputs, but may impact the [toString] representation of this path.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * BsonPath.sliced(1..<5)         // $[1:5]: Items at indices 1 (inclusive) to 5 (exclusive)
	 * BsonPath.sliced(1..5]          // $[1:6]: Items at indices 1 (inclusive) to 5 (inclusive)
	 * BsonPath.sliced(1..5 step 2)   // $[1:6:2]: Items at indices 1 (inclusive) to 5 (inclusive), skipping every other item
	 * BsonPath.sliced(5 downTo 1)    // $[1:6:2]: Items at indices 1 (inclusive) to 5 (inclusive), in reverse order
	 * ```
	 *
	 * @see reversed Shorthand to reverse an array.
	 */
	fun sliced(range: IntProgression): BsonPath =
		SingleSelectorSegment(SliceSelector.of(range), this)

	/**
	 * Points to the elements of a [BsonArray] at the indices selected by [start] and [end], with an optional [step].
	 *
	 * If the node is not an array, nothing is returned.
	 *
	 * Elements can be iterated in the reversed order by having a [start] greater than the [end] and having a negative [step].
	 *
	 * If the smaller bound is `null`, it means "the first element of the array, inclusive".
	 * If the larger bound is `null`, it means "the last element of the array, **inclusive**".
	 *
	 * ### Examples
	 *
	 * ```json
	 * [0, 1, 2, 3, 4, 5, 6]
	 * ```
	 *
	 * - `BsonPath.sliced(1, 3)` returns `[1, 2]`
	 * - `BsonPath.sliced(start = 5)` returns `[5, 6]`
	 * - `BsonPath.sliced(1, 5, 2)` returns `[1, 3]`
	 * - `BsonPath.sliced(5, 1, -2)` returns `[5, 3]`
	 * - `BsonPath.sliced(step = -1)` returns `[6, 5, 4, 3, 2, 1, 0]`
	 *
	 * @param start The index at which the slice should start, **inclusive**.
	 * @param end The index at which the slice should end, **exclusive**.
	 * @param step How many elements should be skipped between found elements.
	 * - With [step] of 1, all elements are returned.
	 * - With [step] of 2, every other element is returned.
	 * - With [step] of 0, no elements are returned at all.
	 *
	 * @see reversed Shorthand for reversing an array.
	 */
	fun sliced(start: Int? = null, end: Int? = null, step: Int = 1): BsonPath =
		SingleSelectorSegment(SliceSelector.of(start, end, step), this)

	/**
	 * Iterates a [BsonArray] in the reversed order, starting from the end.
	 *
	 * If the node is not an array, nothing is returned.
	 *
	 * This is a shorthand syntax for a [slice][sliced] of `[::-1]`.
	 */
	fun reversed(): BsonPath =
		SingleSelectorSegment(SliceSelector.reversed(), this)

	/**
	 * Allows specifying multiple [selectors].
	 *
	 * All elements that match a selector are returned, in the order of the selectors.
	 * For example, the path `$[0, 3]` returns the elements at index 0 and at index 3.
	 *
	 * The same element may be returned multiple times if it matches multiple [selectors].
	 *
	 * The [Selector] type is obtained by the top-level functions on [BsonPath.Root].
	 * Non-top-level paths are not allowed by the JSONPath specification.
	 *
	 * ### Examples
	 *
	 * ```json
	 * ["a", "b", "c", "d", "e", "f", "g"]
	 * ```
	 *
	 * - `BsonPath.any(BsonPath[0], BsonPath[3])` returns `["a", "d"]`
	 * - `BsonPath.any(BsonPath.sliced(0, 2), BsonPath[5])` returns `["a", "b", "f"]`
	 * - `BsonPath.any(BsonPath[0], BsonPath[0])` returns `["a", "a"]`
	 */
	fun any(vararg selectors: Selector): BsonPath =
		MultiSelectorSegment(
			selectors.map {
				if (it is SingleSelectorSegment) it.selector
				else it
			},
			this,
		)

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

		@Deprecated("This function has been renamed to BsonPath(text).", ReplaceWith("BsonPath(text)", "opensavvy.ktmongo.bson.BsonPath"))
		fun parse(text: String): BsonPath =
			BsonPath(text)

		override fun get(field: String): PathOrSelector =
			SingleSelectorSegment(FieldSelector(field), this)

		override fun get(index: Int): PathOrSelector =
			SingleSelectorSegment(IndexSelector(index), this)

		override fun sliced(start: Int?, end: Int?, step: Int): PathOrSelector =
			SingleSelectorSegment(SliceSelector.of(start, end, step), this)

		override fun sliced(range: IntProgression): PathOrSelector =
			SingleSelectorSegment(SliceSelector.of(range), this)

		override fun reversed(): PathOrSelector =
			SingleSelectorSegment(SliceSelector.reversed(), this)

		override val all: PathOrSelector
			get() = SingleSelectorSegment(AllSelector, this)

		/**
		 * Always returns `null`.
		 */
		override val parent: Nothing? get() = null

		override fun toString() = "$"
	}
}

// region Segments

@ExperimentalBsonPathApi
private data class SingleSelectorSegment(
	val selector: Selector,
	override val parent: BsonPath,
) : BsonPath,
	Selector by selector,
	PathOrSelector {

	// This class implements Selector to power the BsonPath.any(BsonPath["foo"], BsonPath["bar"]) syntax.
	// It doesn't otherwise make sense to use as a selector.

	init {
		require(selector !is SingleSelectorSegment) { "A segment cannot contain a selector that is another ${SingleSelectorSegment::class}. Please report this to the maintainers of BSONPath: $selector" }
	}

	@LowLevelApi
	override fun findIn(reader: BsonValueReader): Sequence<BsonValueReader> =
		parent.findIn(reader)
			.flatMap { selector.findInParent(it) }

	override fun toString() =
		when (selector) {
			is FieldSelector if selector.name matches FieldSelector.legalDotNotationCharacters -> "$parent.${selector.name}"
			is AllSelector -> "$parent.*"
			else -> "$parent[$selector]"
		}
}

@ExperimentalBsonPathApi
private data class MultiSelectorSegment(
	val selectors: List<Selector>,
	override val parent: BsonPath,
) : BsonPath {

	init {
		require(selectors.size > 1) { "BsonPath.any() should have at least two options, found: $selectors" }
		for (selector in selectors) {
			require(selector !is SingleSelectorSegment) { "A multi-selector segment cannot contain a ${SingleSelectorSegment::class} selector. Please report this to the maintainers of BSONPath: $selector" }
		}
	}

	@LowLevelApi
	override fun findIn(reader: BsonValueReader): Sequence<BsonValueReader> {
		val values = parent.findIn(reader).toList()

		return selectors
			.asSequence()
			.flatMap { selector ->
				values.asSequence().flatMap { value ->
					selector.findInParent(value)
				}
			}
	}

	override fun toString() = buildString {
		append(parent)
		append('[')
		for (selector in selectors) {
			append(selector)
			append(", ")
		}
		setLength(length - 2)
		append(']')
	}
}

// endregion
// region Selectors

@ExperimentalBsonPathApi
private data class FieldSelector(val name: String) : Selector {

	init {
		require("'" !in name) { "The character ' (apostrophe) is currently forbidden in BsonPath expressions, found: \"$name\"" }
	}

	@LowLevelApi
	override fun findInParent(reader: BsonValueReader): Sequence<BsonValueReader> {
		return if (reader.type == BsonType.Document) {
			val value = reader.readDocument().read(name)

			if (value != null)
				sequenceOf(value)
			else
				emptySequence()
		} else
			emptySequence()
	}

	override fun toString() = "'$name'"

	companion object {
		val legalDotNotationCharacters = Regex("[a-zA-Z0-9]*")
	}
}

@ExperimentalBsonPathApi
private data class IndexSelector(val index: Int) : Selector {
	@LowLevelApi
	override fun findInParent(reader: BsonValueReader): Sequence<BsonValueReader> {
		return if (reader.type == BsonType.Array) {
			val array = reader.readArray()
			val readingIndex = if (index >= 0) index else array.elements.size + index
			val value = array.read(readingIndex)
			if (value != null)
				sequenceOf(value)
			else
				emptySequence()
		} else {
			emptySequence()
		}
	}

	override fun toString() = "$index"
}

@ExperimentalBsonPathApi
private object AllSelector : Selector {
	@LowLevelApi
	override fun findInParent(reader: BsonValueReader): Sequence<BsonValueReader> =
		when (reader.type) {
			BsonType.Document -> reader.readDocument().entries.values.asSequence()
			BsonType.Array -> reader.readArray().elements.asSequence()
			else -> emptySequence()
		}

	override fun toString() = "*"
}

@ExperimentalBsonPathApi
private data class SliceSelector(
	val start: Int, // inclusive, -1 for open-ended
	val end: Int, // exclusive, -1 for open-ended
	val step: Int,
) : Selector {

	companion object {
		fun of(start: Int? = null, end: Int? = null, step: Int = 1): SliceSelector {
			require(start == null || start >= 0) { "BsonPath.sliced(start=) cannot be negative, found: $start" }
			require(end == null || end >= 0) { "BsonPath.sliced(end=) cannot be negative, found: $end" }

			if (step >= 0) {
				require((start ?: 0) <= (end
					?: Int.MAX_VALUE)) { "BsonPath.sliced()'s start should be lesser than its end when the step is positive. Found start=$start, end=$end and step=$step" }
			} else {
				require((start ?: Int.MAX_VALUE) >= (end
					?: 0)) { "BsonPath.sliced()'s start should be greater than its end when the step is negative. Found start=$start, end=$end and step=$step" }
			}

			return SliceSelector(start ?: -1, end ?: -1, step)
		}

		fun of(range: IntProgression): SliceSelector {
			require(range.first >= 0) { "BsonPath.sliced() only supports positive indices, found: $range" }
			require(range.last >= 0) { "BsonPath.sliced() only supports positive indices, found: $range" }
			return SliceSelector(
				start = if (range.step >= 0 && range.first == 0) -1 else range.first,
				end = when {
					range.step >= 0 && range.last == Int.MAX_VALUE -> -1
					range.step >= 0 -> range.last + 1
					range.step < 0 && range.last == 0 -> -1
					else -> range.last - 1
				},
				step = range.step,
			)
		}

		fun reversed(): SliceSelector =
			SliceSelector(-1, -1, -1)
	}

	@LowLevelApi
	override fun findInParent(reader: BsonValueReader): Sequence<BsonValueReader> = when {
		step == 0 -> emptySequence()
		reader.type == BsonType.Array -> sequence {
			val elements = reader.readArray().elements

			if (step >= 0) {
				val start = if (start == -1) 0 else start
				val end = if (end == -1 || end > elements.size) elements.size else end
				for (index in start..<end step step) {
					yield(elements[index])
				}
			} else {
				val start = if (start == -1 || start > elements.lastIndex) elements.lastIndex else start
				val end = if (end == -1) 0 else end + 1
				for (index in start downTo end step -step) {
					yield(elements[index])
				}
			}
		}

		else -> emptySequence()
	}

	override fun toString(): String = buildString {
		if (start != -1)
			append(start)

		append(':')

		if (end != -1)
			append(end)

		if (step != 1) {
			append(':')
			append(step)
		}
	}
}

// endregion
// region Parsing

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
 * | `.*` or `[*]`         | Accessor for all direct children. See [BsonPath.all].        |
 * | `[1:3]`               | Accessor for elements at index 1..<3. See [BsonPath.sliced]. |
 *
 * Multiple selectors can be defined in the same brackets.
 * When this is the case, all nodes that match any of the selectors are returned.
 * For example, `['foo', 'bar', 'baz']` will return the values of the fields `foo`, `bar` and `baz`.
 * See [BsonPath.any].
 *
 * ### Examples
 *
 * ```kotlin
 * val document: Bson = …
 *
 * val id: Uuid = document at BsonPath("$.profile.id")
 *
 * for (user in document.select<User>(BsonPath("$.friends"))) {
 *     println("User: $user")
 * }
 * ```
 *
 * @see BsonPath Learn more about BSON paths.
 * @see at Access one field by its BSON path.
 * @see select Access multiple fields by their BSON path.
 */
@ExperimentalBsonPathApi
fun BsonPath(@Language("JSONPath") text: String): BsonPath {
	require(text.startsWith("$")) { "A BsonPath expression must start with a dollar sign: $text\nDid you mean to create a BsonPath to access a specific field? If so, see BsonPath[\"foo\"]" }

	var expr: BsonPath = BsonPath
	val parser = BsonPathParserUtils(text, startIndex = 1) // skip the '$'

	while (parser.hasNext()) {
		when (val segmentStart = parser.peek()) {
			'.' -> {
				parser.skip() // '.'
				expr = parseDotSegment(expr, parser)
			}

			'[' -> {
				parser.skip('[')
				expr = parseBracketSegment(expr, parser)
				parser.skip(']')
			}

			else -> {
				parser.fail("Unrecognized segment start: '$segmentStart'. Expected '.' or '['.")
			}
		}
	}

	return expr
}

/**
 * Parses the `.*` or `.field-name` notations.
 */
@ExperimentalBsonPathApi
private fun parseDotSegment(
	parent: BsonPath,
	parser: BsonPathParserUtils,
): BsonPath {
	when (val selectorType = parser.peek()) {
		'*' -> {
			// .*
			parser.skip() // '*'
			return parent.all
		}

		else if selectorType.isNameChar() -> {
			// .foo
			parser.accumulateWhile { it.isNameChar() }
			return parent[parser.extract()]
		}

		else -> parser.fail("Unexpected character after dot: '$selectorType'")
	}
}

@ExperimentalBsonPathApi
private fun parseBracketSegment(
	parent: BsonPath,
	parser: BsonPathParserUtils,
): BsonPath {
	val selectors = ArrayList<Selector>()

	do {
		parser.skipIf(',')
		parser.skipIf(' ')

		val selectorType = parser.peek()

		when (selectorType) {
			'\'' -> {
				parser.skip() // '
				parser.accumulateWhile { it != '\'' }
				parser.skip('\'')
				selectors += FieldSelector(parser.extract())
			}

			'"' -> {
				parser.skip() // "
				parser.accumulateWhile { it != '"' }
				parser.skip('"')
				selectors += FieldSelector(parser.extract())
			}

			'*' -> {
				parser.skip('*')
				parser.extract() // ignore whatever there is
				selectors += AllSelector
			}

			in Char(0x30)..Char(0x39), '-', ':' -> {
				if (parser.peek() == '-') {
					parser.accumulate()
				}

				parser.accumulateWhile { it.isDigit() }

				if (parser.peek() == ':') {
					parser.accumulate()
					parser.accumulateWhile { it.isDigit() }

					if (parser.peek() == ':') {
						parser.accumulate()
						parser.accumulateWhile { it.isDigit() || it == '-' }
					}
				}

				val content = parser.extract()
				if (':' in content) {
					val bounds = content.split(':')
					val start = bounds.getIntNotEmpty(0, default = -1)
					val end = bounds.getIntNotEmpty(1, default = -1)
					val step = bounds.getIntNotEmpty(2, default = 1)
					selectors += SliceSelector(start, end, step)
				} else {
					selectors += IndexSelector(content.toInt())
				}
			}

			else -> {
				parser.fail("Unrecognized selector type: '$selectorType'")
			}
		}
	} while (parser.peek() == ',')

	return when {
		selectors.isEmpty() -> parser.fail("At least one selector should be specified between the brackets")
		selectors.size == 1 -> SingleSelectorSegment(selectors.first(), parent)
		else -> MultiSelectorSegment(selectors, parent)
	}
}

/**
 * Helper to extract specific substrings from [text], with nice contextual error messages on failure.
 */
private class BsonPathParserUtils(
	private val text: String,
	startIndex: Int = 0,
) {
	val accumulator = StringBuilder()
	var index = startIndex

	fun fail(msg: String, cause: Throwable? = null): Nothing {
		val excerpt =
			if (index + 5 > text.length) text.substring(index, text.length)
			else text.substring(index, index + 5) + "…"

		throw IllegalArgumentException("Could not parse the BSON path expression “$text” at index $index (“${excerpt}”): $msg", cause)
	}

	fun skip() {
		index++
	}

	/**
	 * Skips the next character but only if it's [c]. Otherwise, fails parsing.
	 */
	fun skip(c: Char) {
		if (text[index] == c)
			skip()
		else
			fail("Expected '$c' but found '${text[index]}'")
	}

	/**
	 * Skips the next character but only if it's [c]. Otherwise, does nothing.
	 */
	fun skipIf(c: Char) {
		if (text[index] == c)
			skip()
	}

	fun hasNext() =
		index < text.length

	fun peek(): Char =
		text[index]

	fun peek(offset: Int): Char =
		text[index + offset]

	/**
	 * Accumulates the current character.
	 */
	fun accumulate() {
		accumulator.append(text[index])
		index++
	}

	/**
	 * Accumulates all characters until [predicate] becomes false or we reach the end of [text].
	 *
	 * The first character for which [predicate] returns `false` is not included.
	 */
	inline fun accumulateWhile(predicate: (Char) -> Boolean) {
		while (index < text.length && predicate(text[index]))
			accumulate()
	}

	/**
	 * Returns a [String] that contains all the characters previously accumulated.
	 */
	fun extract(): String {
		return accumulator.toString()
			.also { accumulator.clear() }
	}
}

private fun List<String>.getIntNotEmpty(index: Int, default: Int): Int {
	if (index !in this.indices) {
		return default
	}

	val value = this[index]

	if (value.isEmpty())
		return default

	return value.toInt()
}

private inline fun Char.isAlpha() =
	this.code in 0x41..0x51 || this.code in 0x61..0x7A

private inline fun Char.isDigit() =
	this.code in 0x30..0x39

private inline fun Char.isNameFirst() =
	isAlpha() || this == '_' || this.code in 0x80..0xD7FF || this.code in 0xE000..0x10FFFF

private inline fun Char.isNameChar() =
	isNameFirst() || isDigit()

// endregion
// region Execution/selection

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
inline fun <reified T> Bson.select(path: BsonPath): Sequence<T> {
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
 * Finds all values that match [path] in a given [BSON document][Bson].
 *
 * To learn more about the syntax, see [BsonPath].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.select<String>("$.foo.bar")
 * ```
 * will return a sequence of all values matching the path `foo.bar`.
 *
 * @see at Select a single value.
 */
@ExperimentalBsonPathApi
inline fun <reified T> Bson.select(@Language("JSONPath") path: String): Sequence<T> =
	select(BsonPath(path))

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
inline fun <reified T> Bson.selectFirst(path: BsonPath): T {
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
 * To learn more about the syntax, see [BsonPath].
 *
 * ### Example
 *
 * ```kotlin
 * val document: Bson = …
 *
 * document.selectFirst<String>("$.foo.bar")
 * ```
 * will return the value of the field `foo.bar`.
 *
 * @see BsonPath Learn more about BSON paths.
 * @see select Select multiple values with a BSON path.
 * @see at Select a single value using infix notation.
 */
@ExperimentalBsonPathApi
inline fun <reified T> Bson.selectFirst(@Language("JSONPath") path: String): T =
	selectFirst(BsonPath(path))

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

// endregion
