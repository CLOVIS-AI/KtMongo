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

package opensavvy.ktmongo.dsl.query.common

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.tree.CompoundNode
import opensavvy.ktmongo.dsl.utils.asImmutable

/**
 * A compound expression is an [Expression] that may have children.
 *
 * A compound expression may have `0..n` children.
 * Children are added by calling the [accept] function.
 *
 * This is also the supertype for all DSL scopes, since DSL scopes correspond to the ability to add children to
 * an expression.
 *
 * ### Implementation notes
 *
 * Prefer implementing [AbstractCompoundExpression] instead of implementing this interface directly.
 */
interface CompoundExpression : Expression, CompoundNode<Expression> {

	/**
	 * Adds a new [expression] as a child of this one.
	 *
	 * Since [Expression] subtypes may generate arbitrary BSON, it is possible
	 * to use this method to inject arbitrary BSON (escaped or not) into any KtMongo DSL.
	 * Incorrect [Expression] implementations can create memory leaks,
	 * performance issues, data corruption or data leaks.
	 *
	 * We recommend against calling this function directly.
	 * Instead, you should find other functions declared on this object (possibly as extensions)
	 * that perform the operation you want in safe manner.
	 */
	@LowLevelApi
	@DangerousMongoApi
	@KtMongoDsl
	override fun accept(expression: Expression)

	companion object
}

/**
 * Abstract utility class to help implement [CompoundExpression].
 *
 * Learn more by reading [Expression], [AbstractExpression] and [CompoundExpression].
 */
abstract class AbstractCompoundExpression(
	context: BsonContext,
) : AbstractExpression(context), CompoundExpression {

	// region Sub-expression binding

	private val _children = ArrayList<Expression>()

	@LowLevelApi
	protected val children: List<Expression>
		get() = _children.asImmutable()

	@LowLevelApi
	@DangerousMongoApi
	@KtMongoDsl
	override fun accept(expression: Expression) {
		require(!frozen) { "This expression has already been frozen, it cannot accept the child expression $expression" }
		require(expression != this) { "Trying to add an expression to itself!" }

		val simplified = expression.simplify()

		if (simplified != null) {
			require(simplified !== this) { "Trying to add an expression to itself!" }
			simplified.freeze()
			_children += simplified
		}
	}

	// endregion
	// region Simplifications

	/**
	 * Simplifies a node based on its children.
	 *
	 * @param children The children of this expression, previously added with [accept].
	 * **They have already been simplified.**
	 * @see Expression.simplify
	 */
	@LowLevelApi
	protected open fun simplify(children: List<Expression>): AbstractExpression? =
		this

	@LowLevelApi
	final override fun simplify(): AbstractExpression? =
		simplify(children)

	// endregion
	// region Writing

	/**
	 * Writes a node alongside its children.
	 *
	 * @param children The children of this expression, previously added with [accept].
	 * **They have already been simplified**.
	 * @see AbstractExpression.write
	 */
	@LowLevelApi
	protected open fun write(writer: BsonFieldWriter, children: List<Expression>) {
		for (child in children) {
			check(this !== child) { "Trying to write myself as my own child!" }
			child.writeTo(writer)
		}
	}

	@LowLevelApi
	final override fun write(writer: BsonFieldWriter) {
		write(writer, children)
	}

	// endregion

	companion object
}
