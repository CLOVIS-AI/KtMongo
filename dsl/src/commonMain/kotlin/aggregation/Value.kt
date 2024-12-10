/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.buildBsonArray
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.Expression
import opensavvy.ktmongo.dsl.tree.Node
import opensavvy.ktmongo.dsl.tree.NodeImpl

/**
 * An intermediary value in an aggregation expression.
 *
 * Each implementation of this interface is a logical BSON node in our own intermediate representation.
 * Each node knows how to [writeTo] itself into a BSON document.
 *
 * ### Difference with Expression
 *
 * This interface and its hierarchy mimic [Expression].
 * The main difference is the expected context: [Expression] represents an operator, which is stored as a BSON document
 * and doesn't participate in any type hierarchy.
 * Instead, [Value] is stored as a BSON value and its return type can be further embedded into more values.
 *
 * ### Security
 *
 * Implementing this interface allows injecting arbitrary BSON into a request.
 * Be very careful not to make injections possible.
 *
 * ### Implementation notes
 *
 * Prefer implementing [AbstractValue] instead of implementing this interface directly.
 *
 * ### Debugging notes
 *
 * Use [toString] to view the JSON representation of this expression.
 */
interface Value<Root : Any, Type> : Node {

	/**
	 * The context used to generate this value.
	 */
	@LowLevelApi
	val context: BsonContext

	/**
	 * Makes this value immutable.
	 *
	 * After this method has been called, the value can never be modified again.
	 * This ensures that values cannot change after they have been used within other values.
	 */
	@LowLevelApi
	override fun freeze()

	/**
	 * Returns a simplified (but equivalent) value to the current value.
	 */
	@LowLevelApi
	fun simplify(): Value<Root, Type>

	/**
	 * Writes the result of [simplifying][simplify] this value into [writer].
	 */
	@LowLevelApi
	fun writeTo(writer: BsonValueWriter)

	/**
	 * JSON representation of this expression.
	 *
	 * Note that since this class represents a BSON _value_, a BSON libraries often only support _documents_,
	 * the actual value may be surrounded by some boilerplate (like an array or a useless value).
	 */
	override fun toString(): String
}

abstract class AbstractValue<Root : Any, Type> private constructor(
	@property:LowLevelApi override val context: BsonContext,
	private val node: NodeImpl,
) : Node by node, Value<Root, Type> {

	constructor(context: BsonContext) : this(context, NodeImpl())

	/**
	 * `true` if [freeze] has been called. Can never become `false` again.
	 *
	 * If this value is `true`, this value should reject any attempt to mutate it.
	 * It is the responsibility of the implementor to satisfy this invariant.
	 */
	protected val frozen: Boolean
		get() = node.frozen

	/**
	 * Called when the value should be written to a [writer].
	 *
	 * Note that this function is only called on instances that have already passed through [simplify],
	 * so it is guaranteed that this value is fully simplified already.
	 */
	@LowLevelApi
	protected abstract fun write(writer: BsonValueWriter)

	@LowLevelApi
	override fun simplify(): AbstractValue<Root, Type> = this

	@LowLevelApi
	final override fun writeTo(writer: BsonValueWriter) {
		this.simplify().write(writer)
	}

	/**
	 * JSON representation of this expression.
	 *
	 * By default, simplifications are enabled. Set [simplified] to `false` to disable simplifications.
	 */
	@OptIn(LowLevelApi::class)
	fun toString(simplified: Boolean): String {
		val document = buildBsonArray {
			if (simplified)
				writeTo(this)
			else
				write(this)
		}

		return document.toString()
	}

	final override fun toString(): String =
		toString(simplified = true)

}
