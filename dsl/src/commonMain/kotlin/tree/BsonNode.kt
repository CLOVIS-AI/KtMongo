/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.tree

import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriteable
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.FilterQueryPredicate

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
 * Prefer implementing [AbstractBsonNode] instead of implementing this interface directly.
 *
 * ### Debugging notes
 *
 * Use [toString][Any.toString] to view the JSON representation of this expression.
 */
interface BsonNode : Node, BsonFieldWriteable {

	/**
	 * The context used to generate this expression.
	 */
	@LowLevelApi
	val context: BsonContext

	/**
	 * Makes this expression immutable.
	 *
	 * After this method has been called, the expression can never be modified again.
	 * This ensures that expressions cannot change after they have been used within other expressions.
	 */
	@LowLevelApi
	override fun freeze()

	/**
	 * Returns a simplified (but equivalent) expression to the current expression.
	 *
	 * Returns `null` when the current expression was simplified into a no-op (= it does nothing).
	 */
	@LowLevelApi
	fun simplify(): BsonNode?

	/**
	 * Writes the result of [simplifying][simplify] this expression into [writer].
	 */
	@LowLevelApi
	override fun writeTo(writer: BsonFieldWriter)

	/**
	 * Writes the result of [simplifying][simplify] to a new [BSON document][Bson].
	 */
	@LowLevelApi
	fun toBson(): Bson =
		context.buildDocument { writeTo(this) }

	/**
	 * JSON representation of this expression.
	 */
	override fun toString(): String

	companion object
}

/**
 * Utility implementation for [BsonNode], which handles the [context], [toString] representation and [freezing][freeze].
 *
 * ### Implementing a new operator
 *
 * Instances of this class are BSON operators, like `$eq`, `$xor`, `$setOnInsert` and `$lookup`.
 *
 * **Custom operators bypass the entirety of the safety features provided by this library.
 * Because they are able to write arbitrary BSON, no checks whatsoever are possible.
 * If you are not careful, this may make injection attacks or data leaking possible.**
 *
 * Before writing your own operator, familiarize yourself with the documentation of [BsonNode], [AbstractBsonNode],
 * [CompoundBsonNode] and [AbstractCompoundBsonNode], as well as [BsonFieldWriter].
 *
 * Fundamentally, an operator is anything that is able to [write] itself into a BSON document.
 * Operators should not be mutable, except through their [accept][CompoundBsonNode.accept] method (if they have one).
 *
 * An operator generally looks like the following:
 * ```kotlin
 * @LowLevelApi
 * private class TypePredicateExpressionNode(
 *     val type: BsonType,
 *     context: BsonContext,
 * ) : AbstractBsonNode(context) {
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
 * Once you have created your operator, use the [accept][CompoundBsonNode.accept] method to register it into a DSL:
 * ```kotlin
 * collection.find {
 *     User::name {
 *         accept(TypePredicateExpressionNode(BsonType.Undefined))
 *     }
 * }
 * ```
 *
 * Of course, the operator described above is already made available: [FilterQueryPredicate.hasType].
 *
 * **Note that if your operator accepts a variable number of sub-expressions (e.g. `$and`), you must ensure that it works for any
 * number of expressions, including 1 and 0.** See [simplify].
 *
 * To create an operator that can accept multiple children operators (for example `$and`), implement [AbstractCompoundBsonNode].
 *
 * Since operators are complex to write, risky to get wrong, and hard to test, we highly recommend to upstream any
 * operator you create so they can benefit from future fixes. Again, **an improperly-written operator may allow data
 * corruption or leaking**.
 *
 * Operators should preferably be immutable. To create mutable operators, prefer using [AbstractCompoundBsonNode].
 * Note that once [frozen] is `true`, operators **must be immutable forever**, or other features of this library will break.
 */
abstract class AbstractBsonNode private constructor(
	@property:LowLevelApi override val context: BsonContext,
	private val node: NodeImpl,
) : Node by node, BsonNode {

	constructor(context: BsonContext) : this(context, NodeImpl())

	/**
	 * `true` if [freeze] has been called. Can never become `false` again.
	 *
	 * If this value is `true`, this node should reject any attempt to mutate it.
	 * It is the responsibility of the implementor to satisfy this invariant.
	 */
	protected val frozen: Boolean
		get() = node.frozen

	/**
	 * Called when this operator should be written to a [writer].
	 *
	 * Note that this function is only called on instances that have passed through [simplify],
	 * so it is guaranteed that this expression is fully simplified already.
	 */
	@LowLevelApi
	protected abstract fun write(writer: BsonFieldWriter)

	@LowLevelApi
	override fun simplify(): AbstractBsonNode? = this

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
		val document = context.buildDocument {
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
