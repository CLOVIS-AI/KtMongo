package fr.qsh.ktmongo.dsl.path

import fr.qsh.ktmongo.dsl.LowLevelApi

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
	 * Path segment representing the name of a field.
	 *
	 * This class isn't meant to be used directly by end users. Instead, see [div].
	 */
	@LowLevelApi
	data class Field(val name: String) : PathSegment() {
		override fun toString() = name
	}

	/**
	 * Path segment representing an indexed element in an array.
	 *
	 * This class isn't meant to be used directly by end users. Instead, see [get].
	 */
	@LowLevelApi
	data class Indexed(val index: Int) : PathSegment() {
		override fun toString() = "\$$index"
	}

	/**
	 * Path segment for the "positional" operator (`.$.`).
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
	 * Official documentation:
	 * - [In updates](https://www.mongodb.com/docs/manual/reference/operator/update/positional-all/)
	 */
	@LowLevelApi
	data object AllPositional : PathSegment() {
		override fun toString() = "\$[]"
	}

}

/**
 * A path is a string pointer that identifies which field(s) are impacted by an operator.
 *
 * For example, the following are valid paths:
 * - `"foo"`: targets the field "foo",
 * - `"foo.bar"`: targets the field "bar" which is part of the object "foo",
 * - `"arr.$5.bar"`: targets the field "bar" which is part of the item with index 5 in the array "arr".
 *
 * This structure is a singly-linked list representing the entire path.
 * Each segment is represented by [PathSegment].
 */
@LowLevelApi
data class Path(
	val segment: PathSegment,
	val parent: Path?,
) {

	private suspend fun SequenceScope<PathSegment>.buildSequence(current: Path) {
		current.parent?.let { buildSequence(it) }
		yield(current.segment)
	}

	fun asSequence(): Sequence<PathSegment> = sequence { buildSequence(this@Path) }

	override fun toString() = asSequence().joinToString(separator = ".")

	companion object {
		@LowLevelApi
		fun root(segment: PathSegment) = Path(segment, parent = null)
	}
}

@LowLevelApi
operator fun Path.plus(segment: PathSegment): Path =
	Path(segment, parent = this)
