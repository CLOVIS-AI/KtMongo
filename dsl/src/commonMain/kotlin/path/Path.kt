/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Single segment in a [Path].
 *
 * Each subclass represents a different type of segment that can appear in a path, and links to the high-level
 * factory to obtain an instance of this path.
 *
 * The high-level operators are only available in correct contexts to disambiguate multiple usages of the same
 * operator. Subclasses of this type do not protect against these usages.
 */
@LowLevelApi
sealed class PathSegment {

	/**
	 * Returns the string representation of this [PathSegment]. To obtain the representation of an entire [Path], see [Path.toString].
	 */
	abstract override fun toString(): String

	/**
	 * Path segment representing the name of a field.
	 *
	 * This class isn't type-safe. Instead, see [FieldDsl.div].
	 */
	@LowLevelApi
	data class Field(val name: String) : PathSegment() {
		override fun toString() = name
	}

	/**
	 * Path segment representing an indexed element in an array.
	 *
	 * This class isn't type-safe. Instead, see [FieldDsl.get].
	 */
	@LowLevelApi
	data class Indexed(val index: Int) : PathSegment() {
		override fun toString() = "$index"
	}

	/**
	 * Path segment for the "positional" operator (`.$.`).
	 *
	 * This class isn't type-safe. Instead, see [opensavvy.ktmongo.dsl.query.UpdateQuery.selected].
	 *
	 * Official documentation:
	 * - [In aggregations](https://www.mongodb.com/docs/manual/reference/operator/projection/positional/#mongodb-projection-proj.-)
	 * - [In updates](https://www.mongodb.com/docs/manual/reference/operator/update/positional/)
	 */
	@LowLevelApi
	data object Positional : PathSegment() {
		override fun toString() = "\$"
	}

	/**
	 * Path segment for the "all positional" operator (`.$[].`).
	 *
	 * This class isn't type-safe. Instead, see [opensavvy.ktmongo.dsl.query.UpdateQuery.all].
	 *
	 * Official documentation:
	 * - [In updates](https://www.mongodb.com/docs/manual/reference/operator/update/positional-all/)
	 */
	@LowLevelApi
	data object AllPositional : PathSegment() {
		override fun toString() = "\$[]"
	}

}

/**
 * Low-level, type-unsafe pointer to a specific field in a document.
 *
 * A path is a string pointer that identifies which field(s) are impacted by an operator.
 *
 * For example, the following are valid paths:
 * - `"foo"`: targets the field "foo",
 * - `"foo.bar"`: targets the field "bar" which is part of the object "foo",
 * - `"arr.5.bar"`: targets the field "bar" which is part of the item with index 5 in the array "arr".
 *
 * This structure is a singly-linked list representing the entire path.
 * Each segment is represented by [PathSegment].
 *
 * The high-level type-safe equivalent of this type is [Field].
 */
@LowLevelApi
data class Path(
	val segment: PathSegment,

	/**
	 * The previous link in this [Path].
	 *
	 * For example, the path `Path(PathSegment.Indexed(5), Path(PathSegment.Field("foo"), null))` represents the path `"foo.5"`.
	 */
	val parent: Path?,
) {

	@LowLevelApi
	private suspend fun SequenceScope<PathSegment>.buildSequence(current: Path) {
		current.parent?.let { buildSequence(it) }
		yield(current.segment)
	}

	/**
	 * Returns a [Sequence] of the different [PathSegment] instances that form this [Path], in hierarchical order.
	 *
	 * For example, the path `Path(PathSegment.Indexed(5), Path(PathSegment.Field("foo"), null))` represents the path `"foo.5"` and
	 * would return the sequence `[PathSegment.Field("foo"), PathSegment.Indexed(5)]`.
	 */
	@LowLevelApi
	fun asSequence(): Sequence<PathSegment> =
		sequence { buildSequence(this@Path) }

	/**
	 * Returns the string representation of this [Path]. This is the representation that is sent to MongoDB to refer to a [Field].
	 *
	 * For example, the path `Path(PathSegment.Indexed(5), Path(PathSegment.Field("foo"), null))` represents the path `"foo.5"`.
	 */
	@OptIn(LowLevelApi::class)
	override fun toString() =
		asSequence().joinToString(separator = ".")

	companion object
}

/**
 * Creates a root path from the provided [root] field name.
 *
 * To obtain children instances, use the [div] operator.
 */
@LowLevelApi
fun Path(root: String) = Path(PathSegment.Field(root), parent = null)

/**
 * Returns a new [Path] instance that is the concatenation of the current path and a [segment].
 */
@LowLevelApi
operator fun Path.div(segment: PathSegment): Path =
	Path(segment, parent = this)

/**
 * Returns a new [Path] instance that is the concatenation of the current path and a child [path].
 *
 * **Danger.** This API does not check that [path] makes sense as a child of the current path!
 */
@LowLevelApi
@DangerousMongoApi
operator fun Path.div(path: Path): Path =
	path.asSequence().fold(this) { parent, segment -> Path(segment, parent = parent) }
