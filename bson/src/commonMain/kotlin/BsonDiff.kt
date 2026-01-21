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

import opensavvy.ktmongo.dsl.LowLevelApi

@RequiresOptIn("This method is part of an experimental API to analyse the difference between BSON objects. Please share your feedback in https://gitlab.com/opensavvy/ktmongo/-/issues/98")
annotation class ExperimentalBsonDiffApi

private fun String.addIndent(size: Int) =
	this + " ".repeat(size)

private const val replacementChar = '\uFFFC'
private const val successChar = '✓'
private const val diffChar = '✗'
private const val fieldNotPresent = "(field not present)"

// returns 'true' if there is a diff
@ExperimentalBsonDiffApi
@LowLevelApi
private fun StringBuilder.diff(a: BsonDocumentReader, b: BsonDocumentReader, indent: String): Boolean {
	var hasDiff = false

	for ((name, aReader) in a.entries) {
		val isEqual: Boolean

		append(indent)
		append(replacementChar)
		append(' ')
		append(name)
		append(": ")

		val bReader = b.read(name)

		if (bReader == null) {
			appendLine(aReader)
			append(indent.addIndent(name.length + 4))
			appendLine(fieldNotPresent)
			isEqual = false
		} else {
			isEqual = !diff(aReader, bReader, indent.addIndent(name.length + 4))
		}

		if (isEqual) {
			set(lastIndexOf(replacementChar), successChar)
			appendLine(aReader)
		} else {
			set(lastIndexOf(replacementChar), diffChar)
			hasDiff = true
		}
	}

	for ((name, bReader) in b.entries) {
		if (a.read(name) == null) {
			// Found a field in 'b' that isn't in 'a'
			hasDiff = true
			append(indent)
			append(diffChar)
			append(' ')
			append(name)
			append(": ")
			appendLine(fieldNotPresent)
			append(indent.addIndent(name.length + 4))
			appendLine(bReader)
		}
	}

	return hasDiff
}

/**
 * Analyzes the difference between two BSON documents.
 *
 * This function is particularly useful in tests.
 * Since BSON documents can be large, it may be difficult to find what the difference between two documents is.
 *
 * This function generates human-readable output to find the differences.
 *
 * ### Example
 *
 * ```kotlin
 * val a = factory.buildDocument {
 *     writeString("a", "foo")
 *     writeDocument("b") {
 *         writeString("name", "Bob")
 *         writeInt32("age", 18)
 *     }
 * }
 *
 * val b = factory.buildDocument {
 *     writeString("a", "foo")
 *     writeDocument("b") {
 *         writeString("name", "Alice")
 *         writeInt32("age", 19)
 *     }
 * }
 *
 * println(a.reader() diff b.reader())
 * ```
 *
 * ```text
 * ✓ a: "foo"
 * ✗ b:
 *      ✗ name: "Bob"
 *              "Alice"
 *      ✓ age: 18
 *             19
 * ```
 *
 * @return If the two documents are equal, returns `null`.
 * Otherwise, generates a human-readable diff.
 */
@ExperimentalBsonDiffApi
@LowLevelApi
infix fun BsonDocumentReader.diff(other: BsonDocumentReader): String? {
	val sb = StringBuilder()
	val hasDiff = sb.diff(this, other, indent = "")

	return if (hasDiff) sb.toString() else null
}

// returns 'true' if there is a diff
@ExperimentalBsonDiffApi
@LowLevelApi
private fun StringBuilder.diff(a: BsonArrayReader, b: BsonArrayReader, indent: String): Boolean {
	var hasDiff = false

	for ((index, aReader) in a.elements.withIndex()) {
		val isEqual: Boolean

		append(indent)
		append(replacementChar)
		append(' ')
		append(index)
		append(": ")

		val bReader = b.read(index)

		val newIndentSize = index.toString().length + 4

		if (bReader == null) {
			appendLine(aReader)
			append(indent.addIndent(newIndentSize))
			appendLine(fieldNotPresent)
			isEqual = false
		} else {
			isEqual = !diff(aReader, bReader, indent.addIndent(newIndentSize))
		}

		if (isEqual) {
			set(lastIndexOf(replacementChar), successChar)
			appendLine(aReader)
		} else {
			set(lastIndexOf(replacementChar), diffChar)
			hasDiff = true
		}
	}

	for ((index, bReader) in b.elements.withIndex()) {
		if (a.read(index) == null) {
			// Found a field in 'b' that isn't in 'a'
			hasDiff = true
			append(indent)
			append(diffChar)
			append(' ')
			append(index)
			append(": ")
			appendLine(fieldNotPresent)
			append(indent.addIndent(index.toString().length + 4))
			appendLine(bReader)
		}
	}

	return hasDiff
}

/**
 * Analyzes the difference between two BSON arrays.
 *
 * This function is particularly useful in tests.
 * Since BSON arrays can be large, it may be difficult to find what the difference between two documents is.
 *
 * This function generates human-readable output to find the differences.
 *
 * ### Example
 *
 * ```kotlin
 * val a = factory.buildArray {
 *     writeString("foo")
 *     writeDocument {
 *         writeString("name", "Bob")
 *         writeInt32("age", 18)
 *     }
 * }
 *
 * val b = factory.buildArray {
 *     writeString("foo")
 *     writeDocument {
 *         writeString("name", "Alice")
 *         writeInt32("age", 19)
 *     }
 * }
 *
 * println(a.reader() diff b.reader())
 * ```
 *
 * ```text
 * ✓ 0: "foo"
 * ✗ 1:
 *      ✗ name: "Bob"
 *              "Alice"
 *      ✓ age: 18
 *             19
 * ```
 *
 * @return If the two arrays are equal, returns `null`.
 * Otherwise, generates a human-readable diff.
 */
@ExperimentalBsonDiffApi
@LowLevelApi
infix fun BsonArrayReader.diff(other: BsonArrayReader): String? {
	val sb = StringBuilder()
	val hasDiff = sb.diff(this, other, indent = "")

	return if (hasDiff) sb.toString() else null
}

// returns 'true' if there is a diff
@ExperimentalBsonDiffApi
@LowLevelApi
private fun StringBuilder.diff(a: BsonValueReader, b: BsonValueReader, indent: String): Boolean {
	if (a == b)
		return false

	val aType = a.type
	val bType = b.type

	if (aType != bType) {
		append('{')
		append(aType)
		append("} ")
		appendLine(a)
		append(indent)
		append('{')
		append(bType)
		append("} ")
		appendLine(b)
		return true
	} else {
		when (aType) {
			BsonType.Document -> {
				appendLine()
				return diff(a.readDocument(), b.readDocument(), indent)
			}

			BsonType.Array -> {
				appendLine()
				return diff(a.readArray(), b.readArray(), indent)
			}

			else -> {
				appendLine(a)
				append(indent)
				appendLine(b)
				return true
			}
		}
	}
}

/**
 * Analyzes the difference between two BSON values.
 *
 * This function is particularly useful in tests.
 * Since BSON documents can be large, it may be difficult to find what the difference between two documents is.
 *
 * This function generates human-readable output to find the differences.
 *
 * @return If the two documents are equal, returns `null`.
 * Otherwise, generates a human-readable diff.
 */
@ExperimentalBsonDiffApi
@LowLevelApi
infix fun BsonValueReader.diff(other: BsonValueReader): String? {
	val sb = StringBuilder()
	val hasDiff = sb.diff(this, other, indent = "")

	return if (hasDiff) sb.toString() else null
}

/**
 * Analyzes the difference between two BSON documents.
 *
 * This function is particularly useful in tests.
 * Since BSON documents can be large, it may be difficult to find what the difference between two documents is.
 *
 * This function generates human-readable output to find the differences.
 *
 * ### Example
 *
 * ```kotlin
 * val a = factory.buildDocument {
 *     writeString("a", "foo")
 *     writeDocument("b") {
 *         writeString("name", "Bob")
 *         writeInt32("age", 18)
 *     }
 * }
 *
 * val b = factory.buildDocument {
 *     writeString("a", "foo")
 *     writeDocument("b") {
 *         writeString("name", "Alice")
 *         writeInt32("age", 19)
 *     }
 * }
 *
 * println(a diff b)
 * ```
 *
 * ```text
 * ✓ a: "foo"
 * ✗ b:
 *      ✗ name: "Bob"
 *              "Alice"
 *      ✓ age: 18
 *             19
 * ```
 *
 * @return If the two documents are equal, returns `null`.
 * Otherwise, generates a human-readable diff.
 */
@ExperimentalBsonDiffApi
@OptIn(LowLevelApi::class)
infix fun Bson.diff(other: Bson): String? =
	this.reader() diff other.reader()

/**
 * Analyzes the difference between two BSON arrays.
 *
 * This function is particularly useful in tests.
 * Since BSON arrays can be large, it may be difficult to find what the difference between two documents is.
 *
 * This function generates human-readable output to find the differences.
 *
 * ### Example
 *
 * ```kotlin
 * val a = factory.buildArray {
 *     writeString("foo")
 *     writeDocument {
 *         writeString("name", "Bob")
 *         writeInt32("age", 18)
 *     }
 * }
 *
 * val b = factory.buildArray {
 *     writeString("foo")
 *     writeDocument {
 *         writeString("name", "Alice")
 *         writeInt32("age", 19)
 *     }
 * }
 *
 * println(a diff b)
 * ```
 *
 * ```text
 * ✓ 0: "foo"
 * ✗ 1:
 *      ✗ name: "Bob"
 *              "Alice"
 *      ✓ age: 18
 *             19
 * ```
 *
 * @return If the two arrays are equal, returns `null`.
 * Otherwise, generates a human-readable diff.
 */
@ExperimentalBsonDiffApi
@OptIn(LowLevelApi::class)
infix fun BsonArray.diff(other: BsonArray): String? =
	this.reader() diff other.reader()
