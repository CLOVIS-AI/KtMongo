/*
 * Copyright (c) 2024, OpenSavvy, 4SH and contributors.
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

package opensavvy.ktmongo.dsl.expr.common

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.buildBsonDocument
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.PredicateExpression

/**
 * A node in the BSON AST.
 *
 * Each implementation of this interface is a logical BSON node in our own intermediary representation.
 * Each node knows how to [writeTo] itself into a BSON document.
 *
 * ### Security
 *
 * Implementing this interface allows injecting arbitrary BSON into a request. Be very careful not to make injections possible.
 *
 * ### Implementation notes
 *
 * Prefer implementing [AbstractExpression] instead of implementing this interface directly.
 *
 * ### Debugging notes
 *
 * Use [toString][Any.toString] to view the JSON representation of this expression.
 */
interface Expression {

	/**
	 * Makes this expression immutable.
	 *
	 * After this method has been called, the expression can never be modified again.
	 * This ensures that expressions cannot change after they have been used within other expressions.
	 */
	@LowLevelApi
	fun freeze()

	/**
	 * Returns a simplified (but equivalent) expression to the current expression.
	 *
	 * Returns `null` when the current expression was simplified into a no-op (= it does nothing).
	 */
	@LowLevelApi
	fun simplify(): Expression?

	/**
	 * Writes the result of [simplifying][simplify] this expression into [writer].
	 */
	@LowLevelApi
	fun writeTo(writer: BsonFieldWriter)

	/**
	 * JSON representation of this expression.
	 */
	override fun toString(): String

	companion object
}

/**
 * Utility implementation for [Expression], which handles the [context], [toString] representation and [freezing][freeze].
 *
 * ### Implementing a new operator
 *
 * Instances of this class are BSON operators, like `$eq`, `$xor`, `$setOnInsert` and `$lookup`.
 *
 * **Custom operators bypass the entirety of the safety features provided by this library.
 * Because they are able to write arbitrary BSON, no checks whatsoever are possible.
 * If you are not careful, this may make injection attacks or data leaking possible.**
 *
 * Before writing your own operator, familiarize yourself with the documentation of [Expression], [AbstractExpression],
 * [CompoundExpression] and [AbstractCompoundExpression], as well as [BsonFieldWriter].
 *
 * Fundamentally, an operator is anything that is able to [write] itself into a BSON document.
 * Operators should not be mutable, except through their [accept][CompoundExpression.accept] method (if they have one).
 *
 * An operator generally looks like the following:
 * ```kotlin
 * @LowLevelApi
 * private class TypePredicateExpressionNode(
 *     val type: BsonType,
 *     context: BsonContext,
 * ) : AbstractExpression(context) {
 *
 *     override fun write(writer: BsonFieldWriter) {
 *         writer.writeInt32("\$type", type.code)
 *     }
 * }
 * ```
 * The [BsonContext] is required at construction because it is needed to implement [toString], which the user could call at any time,
 * including while the operator is being constructed (e.g. when using a debugger). It is extremely important that the
 * `toString` representation they see is consistent with the final BSON sent over the wire.
 *
 * Once you have created your operator, use the [accept][CompoundExpression.accept] method to register it into a DSL:
 * ```kotlin
 * collection.find {
 *     User::name {
 *         accept(TypePredicateExpressionNode(BsonType.Undefined))
 *     }
 * }
 * ```
 *
 * Of course, the operator described above is already made available: [PredicateExpression.hasType].
 *
 * **Note that if your operator accepts a variable number of sub-expressions (e.g. `$and`), you must ensure that it works for any
 * number of expressions, including 1 and 0.** See [simplify].
 *
 * To create an operator that can accept multiple children operators (for example `$and`), implement [AbstractCompoundExpression].
 *
 * Since operators are complex to write, risky to get wrong, and hard to test, we highly recommend to upstream any
 * operator you create so they can benefit from future fixes. Again, **an improperly-written operator may allow data
 * corruption or leaking**.
 */
abstract class AbstractExpression(
	protected val context: BsonContext,
) : Expression {

	/**
	 * `true` if this expression is immutable.
	 */
	protected var frozen: Boolean = false
		private set

	@LowLevelApi
	final override fun freeze() {
		frozen = true
	}

	/**
	 * Called when this operator should be written to a [writer].
	 *
	 * Note that this function is only called on instances that have passed through [simplify],
	 * so it is guaranteed that this expression is fully simplified already.
	 */
	@LowLevelApi
	protected abstract fun write(writer: BsonFieldWriter)

	@LowLevelApi
	override fun simplify(): AbstractExpression? = this

	@LowLevelApi
	final override fun writeTo(writer: BsonFieldWriter) {
		this.simplify()?.write(writer)
	}

	/**
	 * JSON representation of this expression.
	 *
	 * By default, simplifications are enabled.
	 * Set [simplified] to `false` to disable simplifications.
	 */
	@OptIn(LowLevelApi::class)
	fun toString(simplified: Boolean): String {
		val document = buildBsonDocument {
			if (simplified)
				writeTo(this)
			else
				write(this)
		}

		return document.toString()
	}

	final override fun toString(): String =
		toString(simplified = true)

	companion object
}
