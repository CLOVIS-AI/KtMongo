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
 */
abstract class AbstractExpression(
	protected val context: BsonContext,
) : Expression {

	protected var frozen: Boolean = false
		private set

	@LowLevelApi
	final override fun freeze() {
		frozen = true
	}

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
