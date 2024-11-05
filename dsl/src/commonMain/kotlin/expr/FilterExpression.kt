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

package opensavvy.ktmongo.dsl.expr

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.expr.common.Expression
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path

/**
 * Implementation of the [FilterOperators] interface.
 */
@KtMongoDsl
class FilterExpression<T>(
	context: BsonContext,
) : AbstractCompoundExpression(context),
	FilterOperators<T>,
	FieldDsl {

	// region Low-level operations

	@OptIn(DangerousMongoApi::class)
	@LowLevelApi
	override fun simplify(children: List<Expression>): AbstractExpression? =
		when (children.size) {
			0 -> null
			1 -> this
			else -> AndFilterExpressionNode<T>(children, context)
		}

	@LowLevelApi
	private sealed class FilterExpressionNode(context: BsonContext) : AbstractExpression(context)

	// endregion
	// region $and, $or

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun and(block: FilterOperators<T>.() -> Unit) {
		accept(AndFilterExpressionNode<T>(FilterExpression<T>(context).apply(block).children, context))
	}

	@DangerousMongoApi
	@LowLevelApi
	private class AndFilterExpressionNode<T>(
		val declaredChildren: List<Expression>,
		context: BsonContext,
	) : FilterExpressionNode(context) {

		override fun simplify(): AbstractExpression? {
			if (declaredChildren.isEmpty())
				return null

			if (declaredChildren.size == 1)
				return FilterExpression<T>(context).apply { accept(declaredChildren.single()) }

			// If there are nested $and operators, we combine them into the current one
			val nestedChildren = ArrayList<Expression>()

			for (child in declaredChildren) {
				if (child is AndFilterExpressionNode<*>) {
					for (nestedChild in child.declaredChildren) {
						nestedChildren += nestedChild
					}
				} else {
					nestedChildren += child
				}
			}

			return AndFilterExpressionNode<T>(nestedChildren, context)
		}

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeArray("\$and") {
				for (child in declaredChildren) {
					writeDocument {
						child.writeTo(this)
					}
				}
			}
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun or(block: FilterOperators<T>.() -> Unit) {
		accept(OrFilterExpressionNode<T>(FilterExpression<T>(context).apply(block).children, context))
	}

	@DangerousMongoApi
	@LowLevelApi
	private class OrFilterExpressionNode<T>(
		val declaredChildren: List<Expression>,
		context: BsonContext,
	) : FilterExpressionNode(context) {

		override fun simplify(): AbstractExpression? {
			if (declaredChildren.isEmpty())
				return null

			if (declaredChildren.size == 1)
				return FilterExpression<T>(context).apply { accept(declaredChildren.single()) }

			return super.simplify()
		}

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeArray("\$or") {
				for (child in declaredChildren) {
					writeDocument {
						child.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region Predicate access

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override operator fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.invoke(block: PredicateExpression<V>.() -> Unit) {
		accept(PredicateInFilterExpression(path, PredicateExpression<V>(context).apply(block), context))
	}

	@LowLevelApi
	private class PredicateInFilterExpression(
		val target: Path,
		val expression: Expression,
		context: BsonContext,
	) : FilterExpressionNode(context) {

		override fun simplify(): AbstractExpression? =
			expression.simplify()
				?.let { PredicateInFilterExpression(target, it, context) }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(target.toString()) {
				expression.writeTo(this)
			}
		}
	}

	// endregion
	// region $elemMatch

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun <V> Field<T, Collection<V>>.any(block: PredicateExpression<V>.() -> Unit) {
		accept(ElementMatchExpressionNode<V>(this.path, PredicateExpression<V>(context).apply(block), context))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun <V> Field<T, Collection<V>>.anyObject(block: FilterOperators<V>.() -> Unit) {
		accept(ElementMatchExpressionNode<V>(path, FilterExpression<V>(context).apply(block), context))
	}

	@DangerousMongoApi
	@LowLevelApi
	private class ElementMatchExpressionNode<T>(
		val target: Path,
		val expression: Expression,
		context: BsonContext,
	) : FilterExpressionNode(context) {

		override fun simplify(): AbstractExpression =
			ElementMatchExpressionNode<T>(target, expression.simplify()
				?: OrFilterExpressionNode<T>(emptyList(), context), context)

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(target.toString()) {
				writeDocument("\$elemMatch") {
					expression.writeTo(this)
				}
			}
		}
	}

	// endregion
	// region $all

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override infix fun <V> Field<T, Collection<V>>.containsAll(values: Collection<V>) {
		accept(ArrayAllExpressionNode(path, values, context))
	}

	@LowLevelApi
	private class ArrayAllExpressionNode<T>(
		val path: Path,
		val values: Collection<T>,
		context: BsonContext,
	) : FilterExpressionNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(path.toString()) {
				writeArray("\$all") {
					for (value in values) {
						writeObjectSafe(value, context)
					}
				}
			}
		}
	}

	// endregion

}
