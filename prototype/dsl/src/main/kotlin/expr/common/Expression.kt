package fr.qsh.ktmongo.dsl.expr.common

import fr.qsh.ktmongo.dsl.LowLevelApi
import fr.qsh.ktmongo.dsl.writeDocument
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.BsonInvalidOperationException
import org.bson.BsonWriter
import org.bson.codecs.configuration.CodecRegistry

/**
 * A node in the BSON AST.
 *
 * Each node knows how to [writeTo] itself into the expression.
 *
 * ### Security
 *
 * Implementing this interface allows injecting arbitrary BSON into a request.
 * Be very careful not to allow injections.
 *
 * ### Implementation notes
 *
 * Prefer implementing [AbstractExpression] than implementing this interface directly.
 *
 * ### Debugging notes
 *
 * Use [toString] to generate the JSON of this expression.
 */
interface Expression {

	/**
	 * Makes this expression immutable.
	 *
	 * After this method has been called, the expression can never be modified again.
	 * This ensures that requests cannot change after they have been used by other requests.
	 */
	@LowLevelApi
	fun freeze()

	/**
	 * Returns a simplified (but equivalent) expression to the current expression.
	 *
	 * If `null` is returned, it means the current expression was simplified into a no-op
	 * (i.e. it does nothing).
	 */
	@LowLevelApi
	fun simplify(): Expression?

	/**
	 * Writes this expression into a [writer].
	 *
	 * Depending on the type of expression, the expected current context may be different.
	 */
	@LowLevelApi
	fun writeTo(writer: BsonWriter)

	companion object
}

/**
 * Utility implementation for [Expression], which handles the [codec], [toString] representation and [freezing][freeze].
 */
abstract class AbstractExpression(
	protected val codec: CodecRegistry,
) : Expression {

	/**
	 * See [freeze].
	 */
	protected var frozen: Boolean = false
		private set

	/**
	 * Forbid further mutations to this expression.
	 */
	@LowLevelApi
	final override fun freeze() {
		frozen = true
	}

	/**
	 * Writes this expression into [writer] **exactly as it is described**.
	 *
	 * This function is not allowed to edit the expression in any way,
	 * in particular, simplifying it is not allowed.
	 * To modify the expression before writing it, implement [simplify].
	 *
	 * **Implementations must be pure.**
	 */
	@LowLevelApi
	protected abstract fun write(writer: BsonWriter)

	/**
	 * Allows the implementation to replace itself by another more appropriate representation.
	 *
	 * For example, if the current node is an `$and` operator with a single child,
	 * it may use this function to replace itself by that child.
	 *
	 * **Implementations must be pure.**
	 *
	 * @return The simplified expression.
	 * Returning `null` means that the entire expression has been simplified to a no-op, and can be removed.
	 */
	@LowLevelApi
	override fun simplify(): AbstractExpression? = this

	/**
	 * Writes this expression into a [writer].
	 *
	 * This function is guaranteed to be pure.
	 */
	@LowLevelApi
	final override fun writeTo(writer: BsonWriter) {
		this.simplify()?.write(writer)
	}

	private fun writeWithSimplifications(writer: BsonWriter, simplified: Boolean) {
		@OptIn(LowLevelApi::class)
		if (simplified)
			writeTo(writer)
		else
			write(writer)
	}

	/**
	 * Returns a JSON representation of this node.
	 *
	 * If [simplified] is `true`, [simplifications][simplify] are executed before printing.
	 */
	fun toString(simplified: Boolean): String {
		val document = BsonDocument()

		val writer = BsonDocumentWriter(document)
			.withLoggedContext()

		@OptIn(LowLevelApi::class)
		try {
			writeWithSimplifications(writer, simplified)
		} catch (e: BsonInvalidOperationException) {
			// Some operators cannot be written to the root document,
			// and require a surrounding document.
			// This isn't a problem in production code, because the DSLs are type-safe,
			// and cannot be called in the wrong context.
			// However, it is a problem for toString, which can be called in any context
			// to help debug. If writing this fake document fails too, we give up.
			writer.writeDocument {
				writeWithSimplifications(writer, simplified)
			}
		}

		return document.toString()
	}

	/**
	 * Returns a JSON representation of this node, generated using [writeTo].
	 */
	final override fun toString(): String =
		toString(simplified = true)

	companion object
}
