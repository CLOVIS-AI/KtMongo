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
import opensavvy.ktmongo.bson.BsonPath.Selector
import opensavvy.ktmongo.dsl.LowLevelApi
import org.intellij.lang.annotations.Language
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FUNCTION)
@RequiresOptIn("This symbol is part of the experimental BsonPath API. It may change or be removed without warnings. Please provide feedback in https://gitlab.com/opensavvy/ktmongo/-/issues/93.")
annotation class ExperimentalBsonPathApi

/**
 * Access specific fields in arbitrary BSON documents using a [JSONPath](https://www.rfc-editor.org/rfc/rfc9535.html)-like API.
 *
 * To access fields of a [BSON document][BsonDocument], use [select] or [at].
 *
 * ### Why BSON paths?
 *
 * Most of the time, users want to deserialize documents, which they can do with [read].
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
	 * Points to a [field] in a [BsonDocument] document.
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
	 * - The children of a [BsonDocument] document are the values of its fields.
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

	/**
	 * The current node, noted `@`.
	 *
	 * This object is used in filter expressions to refer to the node currently being evaluated.
	 */
	data object Current : BsonPath {
		@LowLevelApi
		override fun findIn(reader: BsonValueReader): Sequence<BsonValueReader> =
			sequenceOf(reader)

		@ExperimentalBsonPathApi
		override val parent: Nothing?
			get() = null

		override fun toString() = "@"
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

@ExperimentalBsonPathApi
private data class FilterSelector(
	val filter: LogicalExpression,
) : Selector {
	@LowLevelApi
	override fun findInParent(reader: BsonValueReader): Sequence<BsonValueReader> =
		if (filter.test(reader)) sequenceOf(reader)
		else emptySequence()

	override fun toString(): String =
		"?$filter"
}

// endregion
// region Filters

private sealed class FilterValue {

	abstract val type: Type<FilterValue>

	sealed interface Type<out V : FilterValue>

	data object Nothing : FilterValue(), Type<Nothing> {
		override val type get() = this
	}

	@OptIn(LowLevelApi::class)
	sealed class Value : FilterValue() {
		override val type get() = Value

		data class Reader(val reader: BsonValueReader) : Value() {
			override fun toString() = "Value($reader)"
		}

		data class Text(val text: String) : Value() {
			override fun toString() = "Value(\"$text\")"
		}

		data class Integer(val number: Long) : Value() {
			override fun toString() = "Value($number)"
		}

		companion object : Type<Value> {
			override fun toString() = "Value"
		}
	}

	sealed class Logical : FilterValue() {
		data object True : Logical() {
			override fun toString() = "LogicalTrue"
			override val type get() = Logical
		}

		data object False : Logical() {
			override fun toString() = "LogicalFalse"
			override val type get() = Logical
		}

		companion object : Type<Logical> {
			// fake constructor
			operator fun invoke(bool: Boolean): Logical =
				if (bool) True
				else False

			override fun toString() = "Logical"
		}
	}

	@OptIn(LowLevelApi::class)
	data class Nodes(val nodes: Sequence<BsonValueReader>) : FilterValue() {
		override fun toString() = "Nodes(${nodes.toList().joinToString(", ")})"
		override val type get() = Companion

		companion object : Type<Nodes> {
			override fun toString() = "Nodes"
		}
	}
}

@ExperimentalBsonPathApi
private sealed interface FilterExpression<out Type : FilterValue> {

	val type: FilterValue.Type<Type>

	@LowLevelApi
	fun eval(reader: BsonValueReader): Type

	data class LogicalOr(
		val left: LogicalExpression,
		val right: LogicalExpression,
	) : LogicalExpression {
		override val type get() = FilterValue.Logical

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			FilterValue.Logical(left.test(reader) || right.test(reader))

		override fun toString(): String =
			"($left || $right)"
	}

	data class LogicalAnd(
		val left: LogicalExpression,
		val right: LogicalExpression,
	) : LogicalExpression {
		override val type get() = FilterValue.Logical

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			FilterValue.Logical(left.test(reader) && right.test(reader))

		override fun toString(): String =
			"($left && $right)"
	}

	data class LogicalNot(
		val operand: LogicalExpression,
	) : LogicalExpression {
		override val type get() = FilterValue.Logical

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			FilterValue.Logical(!operand.test(reader))

		override fun toString(): String =
			"!$operand"
	}

	data class Exists(
		val field: FilterExpression<FilterValue.Nodes>,
	) : LogicalExpression {
		override val type get() = FilterValue.Logical

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			FilterValue.Logical(field.eval(reader).nodes.any())

		override fun toString(): String =
			field.toString()
	}

	data class Literal<V : FilterValue>(
		val value: V,
	) : FilterExpression<V> {
		@Suppress("UNCHECKED_CAST") // Unchecked because we don't have self types, but it's safe
		override val type: FilterValue.Type<V> get() = value.type as FilterValue.Type<V>

		@LowLevelApi
		override fun eval(reader: BsonValueReader): V = value

		override fun toString(): String =
			value.toString()
	}

	data class Path(
		val path: BsonPath,
	) : FilterExpression<FilterValue.Nodes> {
		override val type get() = FilterValue.Nodes

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Nodes =
			FilterValue.Nodes(path.findIn(reader))

		override fun toString(): String =
			path.toString()
	}

	data class Equals(
		val left: FilterExpression<*>,
		val right: FilterExpression<*>,
	) : LogicalExpression {
		override val type get() = FilterValue.Logical

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical {
			val leftEval = left.eval(reader)
			val rightEval = right.eval(reader)

			return when {
				leftEval is FilterValue.Logical && rightEval is FilterValue.Logical ->
					FilterValue.Logical(leftEval == rightEval)

				leftEval is FilterValue.Nothing && rightEval is FilterValue.Nothing ->
					FilterValue.Logical.True

				leftEval is FilterValue.Value.Integer && rightEval is FilterValue.Value.Integer ->
					FilterValue.Logical(leftEval.number == rightEval.number)

				leftEval is FilterValue.Value.Text && rightEval is FilterValue.Value.Text ->
					FilterValue.Logical(leftEval.text == rightEval.text)

				leftEval is FilterValue.Nodes && !leftEval.nodes.any() -> {
					when (rightEval) {
						// There are no results on the left, and the right is Nothing → equal
						FilterValue.Nothing -> FilterValue.Logical.True

						// There are no results on the left nor on the right → equal
						is FilterValue.Nodes if !rightEval.nodes.any() -> FilterValue.Logical.True

						// There are no results on the left but there is something on the right → !equal
						else -> FilterValue.Logical.False
					}
				}

				rightEval is FilterValue.Nodes && !rightEval.nodes.any() -> {
					when (leftEval) {
						// There are no results on the right, and the left is Nothing → equal
						FilterValue.Nothing -> FilterValue.Logical.True

						// There are no results on the right nor on the left → equal
						is FilterValue.Nodes if !rightEval.nodes.any() -> FilterValue.Logical.True

						// There are no results on the right but there is something on the left → !equal
						else -> FilterValue.Logical.False
					}
				}

				else -> {
					// We found at least one result
					val leftExtracted = leftEval.extractValue()
					val rightExtracted = rightEval.extractValue()

					FilterValue.Logical(leftExtracted == rightExtracted)
				}
			}
		}

		@OptIn(LowLevelApi::class)
		private fun FilterValue.extractValue(): ExtractedValue = when (this) {
			is FilterValue.Logical -> ExtractedValue(BsonType.Boolean, this, this === FilterValue.Logical.True)
			is FilterValue.Nodes -> {
				val nodes = nodes.toList()
				when {
					nodes.isEmpty() -> ExtractedValue(BsonType.Null, this, null)
					nodes.size == 1 -> FilterValue.Value.Reader(nodes.first()).extractValue()
					else -> TODO("Unknown type of nodelist $this")
				}
			}

			FilterValue.Nothing -> ExtractedValue(BsonType.Null, this, null)
			is FilterValue.Value.Integer -> ExtractedValue(BsonType.Double, this, this.number.toDouble())
			is FilterValue.Value.Reader -> when (this.reader.type) {
				BsonType.Int32 -> ExtractedValue(BsonType.Double, this, this.reader.readInt32().toDouble())
				BsonType.Int64 -> ExtractedValue(BsonType.Double, this, this.reader.readInt64().toDouble())
				BsonType.Double -> ExtractedValue(BsonType.Double, this, this.reader.readDouble())
				BsonType.Decimal128 -> TODO("Decimal128 support is not fully implemented. See https://gitlab.com/opensavvy/ktmongo/-/merge_requests/150")
				else -> ExtractedValue(this.reader.type, this, this.reader)
			}

			is FilterValue.Value.Text -> ExtractedValue(BsonType.String, this, this.text)
		}

		private class ExtractedValue(
			val type: BsonType,
			val value: FilterValue,
			/**
			 * - If the value is a number, contains a [Double].
			 * - If the value is `null` or empty, contains `null`.
			 * - If the value is a string, contains a [String].
			 * - Otherwise, contains a [BsonValueReader].
			 */
			val raw: Any?,
		) {

			override fun equals(other: Any?): Boolean {
				if (other == null) return false
				if (other !is ExtractedValue) return false
				if (type != other.type) return false

				return this.raw == other.raw
			}

			override fun hashCode(): Int {
				throw UnsupportedOperationException("ExtractedValue.hashCode() is not supported")
			}

			override fun toString(): String =
				"${this.value} → ${this.raw} ($type)"
		}

		override fun toString(): String =
			"($left == $right)"
	}

	data class NotEquals(
		val left: FilterExpression<*>,
		val right: FilterExpression<*>,
	) : LogicalExpression by LogicalNot(Equals(left, right)) {

		override fun toString(): String =
			"($left != $right)"
	}

	data class FewerStrict(
		val left: FilterExpression<*>,
		val right: FilterExpression<*>,
	) : LogicalExpression {
		override val type get() = FilterValue.Logical

		@OptIn(LowLevelApi::class)
		private fun FilterValue.kind(): Int = when (this) {
			is FilterValue.Value.Integer -> IS_NUMBER

			is FilterValue.Value.Text -> IS_STRING

			is FilterValue.Value.Reader -> {
				when (this.reader.type) {
					BsonType.Int32, BsonType.Int64,
					BsonType.Double, BsonType.Decimal128,
						-> IS_NUMBER

					BsonType.String -> IS_STRING

					else -> IS_OTHER
				}
			}

			else -> IS_OTHER
		}

		@OptIn(LowLevelApi::class)
		private fun FilterValue.numericValue(): Double = when (this) {
			is FilterValue.Value.Integer -> this.number.toDouble()
			is FilterValue.Value.Reader -> {
				when (val type = this.reader.type) {
					BsonType.Int32 -> this.reader.readInt32().toDouble()
					BsonType.Int64 -> this.reader.readInt64().toDouble()
					BsonType.Double -> this.reader.readDouble()
					BsonType.Decimal128 -> TODO("Decimal128 support is not fully implemented. See https://gitlab.com/opensavvy/ktmongo/-/merge_requests/150")
					else -> error("Unsupported type in numeric value: $type, $this")
				}
			}

			else -> error("Unsupported type in numeric value: $this")
		}

		@OptIn(LowLevelApi::class)
		private fun FilterValue.stringValue(): String = when (this) {
			is FilterValue.Value.Text -> this.text
			is FilterValue.Value.Reader -> {
				when (val type = this.reader.type) {
					BsonType.String -> this.reader.readString()
					else -> error("Unsupported type in text value: $type, $this")
				}
			}

			else -> error("Unsupported type in text value: $this")
		}

		/**
		 * Same as [eval], but returns `null` if the comparison is not valid.
		 */
		@LowLevelApi
		fun evalIfValid(reader: BsonValueReader): FilterValue.Logical? {
			val leftEval = left.eval(reader)
			val rightEval = right.eval(reader)

			if (leftEval is FilterValue.Nothing ||
				rightEval is FilterValue.Nothing ||
				(leftEval is FilterValue.Nodes && !leftEval.nodes.any()) ||
				(rightEval is FilterValue.Nodes && !rightEval.nodes.any())) {
				return FilterValue.Logical.False
			}

			if (leftEval is FilterValue.Value && rightEval is FilterValue.Value) {
				// Both are already computed values
				// The only comparable types are: number, or string

				val leftType = leftEval.kind()
				val rightType = rightEval.kind()

				if (leftType == IS_NUMBER && rightType == IS_NUMBER) {
					val leftNumeric = leftEval.numericValue()
					val rightNumeric = rightEval.numericValue()

					return FilterValue.Logical(leftNumeric < rightNumeric)
				} else if (leftType == IS_STRING && rightType == IS_STRING) {
					val leftString = leftEval.stringValue()
					val rightString = rightEval.stringValue()

					return FilterValue.Logical(leftString < rightString)
				}
			}

			// Could not compare the two values
			return null
		}

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			evalIfValid(reader) ?: FilterValue.Logical.False

		override fun toString(): String =
			"($left < $right)"

		companion object {
			private const val IS_OTHER = 0
			private const val IS_NUMBER = 1
			private const val IS_STRING = 2
		}
	}

	data class GreaterStrict(
		val left: FilterExpression<*>,
		val right: FilterExpression<*>,
	) : LogicalExpression {
		override val type: FilterValue.Type<FilterValue.Logical>
			get() = FilterValue.Logical

		@LowLevelApi
		fun evalIfValid(reader: BsonValueReader): FilterValue.Logical? {
			val isFewerStrict = FewerStrict(left, right)
				.evalIfValid(reader)
				?: return null

			val isNotEqual = NotEquals(left, right)
				.eval(reader)

			return FilterValue.Logical(
				isFewerStrict == FilterValue.Logical.False &&
					isNotEqual == FilterValue.Logical.True
			)
		}

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			evalIfValid(reader) ?: FilterValue.Logical.False

		override fun toString(): String =
			"($left > $right)"
	}

	data class FewerOrEqual(
		val left: FilterExpression<*>,
		val right: FilterExpression<*>,
	) : LogicalExpression {
		override val type: FilterValue.Type<FilterValue.Logical>
			get() = FilterValue.Logical

		@LowLevelApi
		fun evalIfValid(reader: BsonValueReader): FilterValue.Logical? {
			val isEqual = Equals(left, right)
				.eval(reader)

			// If they are equal, it doesn't matter whether they are comparable
			if (isEqual == FilterValue.Logical.True)
				return FilterValue.Logical.True

			val isFewerStrict = FewerStrict(left, right)
				.evalIfValid(reader)
				?: return null

			return isFewerStrict
		}

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			evalIfValid(reader) ?: FilterValue.Logical.False

		override fun toString(): String =
			"($left <= $right)"
	}

	data class GreaterOrEqual(
		val left: FilterExpression<*>,
		val right: FilterExpression<*>,
	) : LogicalExpression {
		override val type: FilterValue.Type<FilterValue.Logical>
			get() = FilterValue.Logical

		@LowLevelApi
		fun evalIfValid(reader: BsonValueReader): FilterValue.Logical? {
			val isEqual = Equals(left, right)
				.eval(reader)

			// If they are equal, it doesn't matter whether they are comparable
			if (isEqual == FilterValue.Logical.True)
				return FilterValue.Logical.True

			val isFewerStrict = FewerStrict(left, right)
				.evalIfValid(reader)
				?: return null

			return isFewerStrict
		}

		@LowLevelApi
		override fun eval(reader: BsonValueReader): FilterValue.Logical =
			evalIfValid(reader) ?: FilterValue.Logical.False

		override fun toString(): String =
			"($left >= $right)"
	}
}

@ExperimentalBsonPathApi
private typealias LogicalExpression = FilterExpression<FilterValue.Logical>

@ExperimentalBsonPathApi
@LowLevelApi
private fun LogicalExpression.test(reader: BsonValueReader): Boolean =
	eval(reader) == FilterValue.Logical.True

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
 * | `[?@.a > @.b]`        | Accessor for elements that satisfy the given condition.      |
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

	val parser = BsonPathParserUtils(text, startIndex = 1) // skip the '$'

	return parseBsonPath(parser, BsonPath.Root, isTopLevel = true)
}

@ExperimentalBsonPathApi
private fun parseBsonPath(
	parser: BsonPathParserUtils,
	root: BsonPath,
	isTopLevel: Boolean,
): BsonPath {
	var expr = root
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

			else if !isTopLevel -> {
				// If we're not in the top-level expression, and we find anything that doesn't
				// look like a BsonPath expression, just give up and let the caller handle it.
				// For example, it could be a ']' or a ' == …'.
				return expr
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
				val _ = parser.extract() // ignore whatever there is
				selectors += AllSelector
			}

			in Char(0x30)..Char(0x39), '-', ':' -> {
				selectors += parseIndexOrSlice(parser)
			}

			'?' -> {
				parser.skip('?')
				selectors += FilterSelector(parseLogicalExpression(parser))
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

@OptIn(ExperimentalContracts::class)
@ExperimentalBsonPathApi
private inline fun <T> parseParenthesizedExpression(
	parser: BsonPathParserUtils,
	inner: () -> T,
): T {
	contract {
		callsInPlace(inner, InvocationKind.EXACTLY_ONCE)
	}

	var parensCounter = 0
	parser.skipWhitespace()
	while (parser.peek() == '(') {
		parser.skip()
		parensCounter++
		parser.skipWhitespace()
	}

	val result = inner()

	repeat(parensCounter) {
		parser.skipWhitespace()
		parser.skip(')')
	}

	return result
}

@OptIn(ExperimentalContracts::class)
@ExperimentalBsonPathApi
private inline fun parseNegationableExpression(
	parser: BsonPathParserUtils,
	inner: () -> LogicalExpression,
): LogicalExpression {
	contract {
		callsInPlace(inner, InvocationKind.EXACTLY_ONCE)
	}

	var negationCounter = 0
	parser.skipWhitespace()
	while (parser.peek() == '!') {
		parser.skip()
		negationCounter++
		parser.skipWhitespace()
	}

	val result = inner()

	return if (negationCounter % 2 == 1)
		FilterExpression.LogicalNot(result)
	else result
}

@OptIn(ExperimentalContracts::class)
@ExperimentalBsonPathApi
private inline fun parseParenthesizedLogicalExpression(
	parser: BsonPathParserUtils,
	inner: () -> LogicalExpression,
): LogicalExpression {
	contract {
		callsInPlace(inner, InvocationKind.EXACTLY_ONCE)
	}

	return parseNegationableExpression(parser) {
		parseParenthesizedExpression(parser, inner)
	}
}

@ExperimentalBsonPathApi
private fun parseLogicalExpression(
	parser: BsonPathParserUtils,
): LogicalExpression = parseParenthesizedLogicalExpression(parser) {
	var expression = parseLogicalAndExpression(parser)

	parser.skipWhitespace()
	while (parser.peek() == '|' && parser.peek(1) == '|') {
		parser.skip()
		parser.skip()
		parser.skipWhitespace()

		val other = parseLogicalAndExpression(parser)
		expression = FilterExpression.LogicalOr(expression, other)
	}

	expression
}

@ExperimentalBsonPathApi
private fun parseLogicalAndExpression(
	parser: BsonPathParserUtils,
): LogicalExpression = parseParenthesizedLogicalExpression(parser) {
	var expression = parseLogicalBasicExpression(parser)

	parser.skipWhitespace()
	while (parser.peek() == '|' && parser.peek(1) == '|') {
		parser.skip()
		parser.skip()
		parser.skipWhitespace()

		val other = parseLogicalBasicExpression(parser)
		expression = FilterExpression.LogicalAnd(expression, other)
	}

	expression
}

@ExperimentalBsonPathApi
private fun parseLogicalBasicExpression(
	parser: BsonPathParserUtils,
): LogicalExpression = parseParenthesizedLogicalExpression(parser) {
	val expression = parseFilterExpression(parser)

	when (expression.type) {
		FilterValue.Logical ->
			@Suppress("UNCHECKED_CAST") // Unchecked but safe because we just verified the type
			expression as FilterExpression<FilterValue.Logical>

		FilterValue.Nodes ->
			@Suppress("UNCHECKED_CAST") // Unchecked but safe because we just verified the type
			FilterExpression.Exists(expression as FilterExpression<FilterValue.Nodes>)

		else -> parser.fail("Type mismatch: '$expression' of type ${expression.type} is not a valid filter. Only ${FilterValue.Logical} and ${FilterValue.Nodes} are supported.")
	}
}

@ExperimentalBsonPathApi
private fun parseFilterExpression(
	parser: BsonPathParserUtils,
): FilterExpression<*> = parseParenthesizedExpression(parser) {
	var expression = parseFilterExpressionSingle(parser)

	// Handle infix operators

	parser.skipWhitespace()
	when (parser.peek()) {
		'=' if parser.peek(1) == '=' -> {
			parser.skip() // =
			parser.skip() // =
			val other = parseFilterExpression(parser)
			expression = FilterExpression.Equals(expression, other)
		}

		'!' if parser.peek(1) == '=' -> {
			parser.skip() // !
			parser.skip() // =
			val other = parseFilterExpression(parser)
			expression = FilterExpression.NotEquals(expression, other)
		}

		'<' if parser.peek(1) == '=' -> {
			parser.skip() // <
			parser.skip() // =
			val other = parseFilterExpression(parser)
			expression = FilterExpression.FewerOrEqual(expression, other)
		}

		'<' -> {
			parser.skip() // <
			val other = parseFilterExpression(parser)
			expression = FilterExpression.FewerStrict(expression, other)
		}

		'>' if parser.peek(1) == '=' -> {
			parser.skip() // >
			parser.skip() // =
			val other = parseFilterExpression(parser)
			expression = FilterExpression.GreaterOrEqual(expression, other)
		}

		'>' -> {
			parser.skip() // >
			val other = parseFilterExpression(parser)
			expression = FilterExpression.GreaterStrict(expression, other)
		}
	}

	expression
}

@ExperimentalBsonPathApi
private fun parseFilterExpressionSingle(
	parser: BsonPathParserUtils,
): FilterExpression<*> = parseParenthesizedExpression(parser) {
	when (val current = parser.peek()) {
		't' if parser.peek(1) == 'r' && parser.peek(2) == 'u' && parser.peek(3) == 'e' -> {
			parser.skip() // t
			parser.skip() // r
			parser.skip() // u
			parser.skip() // e
			FilterExpression.Literal(FilterValue.Logical.True)
		}

		'f' if parser.peek(1) == 'a' && parser.peek(2) == 'l' && parser.peek(3) == 's' && parser.peek(4) == 'e' -> {
			parser.skip() // f
			parser.skip() // a
			parser.skip() // l
			parser.skip() // s
			parser.skip() // e
			FilterExpression.Literal(FilterValue.Logical.False)
		}

		'$' -> {
			parser.skip()
			val path = parseBsonPath(parser, BsonPath.Root, isTopLevel = false)
			FilterExpression.Path(path)
		}

		'@' -> {
			parser.skip()
			val path = parseBsonPath(parser, BsonPath.Current, isTopLevel = false)
			FilterExpression.Path(path)
		}

		'\'' -> {
			parser.skip() // '
			parser.accumulateWhile { it != '\'' }
			parser.skip('\'')
			val text = parser.extract()
			FilterExpression.Literal(
				FilterValue.Value.Text(text),
			)
		}

		'"' -> {
			parser.skip() // "
			parser.accumulateWhile { it != '"' }
			parser.skip('"')
			val text = parser.extract()
			FilterExpression.Literal(
				FilterValue.Value.Text(text),
			)
		}

		in Char(0x30)..Char(0x39), '-' -> {
			if (current == '-') {
				parser.accumulate()
			}

			parser.accumulateWhile { it in Char(0x30)..Char(0x39) }
			val text = parser.extract()
			val value = text.toLongOrNull()
				?: parser.fail("Invalid integer: '$text'")
			FilterExpression.Literal(
				FilterValue.Value.Integer(value)
			)
		}

		else -> parser.fail("Unrecognized filter expression")
	}
}

@ExperimentalBsonPathApi
private fun parseIndexOrSlice(
	parser: BsonPathParserUtils,
): Selector {
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
		return SliceSelector(start, end, step)
	} else {
		return IndexSelector(content.toInt())
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

	/**
	 * Skips all characters until [predicate] becomes `false` or we reach the end of [text].
	 *
	 * The first character for which [predicate] returns `false` is not included.
	 */
	inline fun skipWhile(predicate: (Char) -> Boolean) {
		while (index < text.length && predicate(text[index]))
			index++
	}

	fun skipWhitespace() {
		skipWhile { it.isWhitespace() }
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
 * Finds all values that match [path] in a given [BSON document][BsonDocument].
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
inline fun <reified T> BsonDocument.select(path: BsonPath): Sequence<T> {
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
 * Finds all values that match [path] in a given [BSON document][BsonDocument].
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
inline fun <reified T> BsonDocument.select(@Language("JSONPath") path: String): Sequence<T> =
	select(BsonPath(path))

/**
 * Finds the first value that matches [path] in a given [BSON document][BsonDocument].
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
inline fun <reified T> BsonDocument.selectFirst(path: BsonPath): T {
	val iter = select<T>(path).iterator()

	if (iter.hasNext()) {
		return iter.next()
	} else {
		throw NoSuchElementException("Could not find any value at path $path for document $this")
	}
}

/**
 * Finds the first value that matches [path] in a given [BSON document][BsonDocument].
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
inline fun <reified T> BsonDocument.selectFirst(@Language("JSONPath") path: String): T =
	selectFirst(BsonPath(path))

/**
 * Finds the first value that matches [path] in a given [BSON document][BsonDocument].
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
inline infix fun <reified T : Any?> BsonDocument.at(path: BsonPath): T =
	selectFirst(path)

// endregion
