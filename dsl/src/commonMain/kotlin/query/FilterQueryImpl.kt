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

@file:JvmMultifileClass
@file:JvmName("FilterQueryKt")

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.ValueDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.query.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode

@KtMongoDsl
private class FilterQueryImpl<T>(
	context: BsonContext,
) : AbstractCompoundExpression(context),
	FilterQuery<T>,
	FieldDsl {

	// region Low-level operations

	@OptIn(DangerousMongoApi::class)
	@LowLevelApi
	override fun simplify(children: List<BsonNode>): AbstractBsonNode? =
		when (children.size) {
			0 -> null
			1 -> this
			else -> AndFilterBsonNodeNode<T>(children, context)
		}

	@LowLevelApi
	private sealed class FilterBsonNodeNode(context: BsonContext) : AbstractBsonNode(context)

	// endregion
	// region $and, $or

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun and(block: FilterQuery<T>.() -> Unit) {
		accept(AndFilterBsonNodeNode<T>(FilterQueryImpl<T>(context).apply(block).children, context))
	}

	@DangerousMongoApi
	@LowLevelApi
	private class AndFilterBsonNodeNode<T>(
		val declaredChildren: List<BsonNode>,
		context: BsonContext,
	) : FilterBsonNodeNode(context) {

		override fun simplify(): AbstractBsonNode? {
			if (declaredChildren.isEmpty())
				return null

			if (declaredChildren.size == 1)
				return FilterQueryImpl<T>(context).apply { accept(declaredChildren.single()) }

			// If there are nested $and operators, we combine them into the current one
			val nestedChildren = ArrayList<BsonNode>()

			for (child in declaredChildren) {
				if (child is AndFilterBsonNodeNode<*>) {
					for (nestedChild in child.declaredChildren) {
						nestedChildren += nestedChild
					}
				} else {
					nestedChildren += child
				}
			}

			return AndFilterBsonNodeNode<T>(nestedChildren, context)
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
	override fun or(block: FilterQuery<T>.() -> Unit) {
		accept(OrFilterBsonNodeNode<T>(FilterQueryImpl<T>(context).apply(block).children, context))
	}

	@DangerousMongoApi
	@LowLevelApi
	private class OrFilterBsonNodeNode<T>(
		val declaredChildren: List<BsonNode>,
		context: BsonContext,
	) : FilterBsonNodeNode(context) {

		override fun simplify(): AbstractBsonNode? {
			if (declaredChildren.isEmpty())
				return null

			if (declaredChildren.size == 1)
				return FilterQueryImpl<T>(context).apply { accept(declaredChildren.single()) }

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
	override operator fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.invoke(block: FilterQueryPredicate<V>.() -> Unit) {
		accept(PredicateInFilterBsonNode(path, FilterQueryPredicate<V>(context).apply(block), context))
	}

	@LowLevelApi
	private class PredicateInFilterBsonNode(
		val target: Path,
		val expression: BsonNode,
		context: BsonContext,
	) : FilterBsonNodeNode(context) {

		override fun simplify(): AbstractBsonNode? =
			expression.simplify()
				?.let { PredicateInFilterBsonNode(target, it, context) }

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
	override fun <V> Field<T, Collection<V>>.anyValue(block: FilterQueryPredicate<V>.() -> Unit) {
		accept(ElementMatchBsonNodeNode<V>(this.path, FilterQueryPredicate<V>(context).apply(block), context))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun <V> Field<T, Collection<V>>.any(block: FilterQuery<V>.() -> Unit) {
		accept(ElementMatchBsonNodeNode<V>(path, FilterQueryImpl<V>(context).apply(block), context))
	}

	@DangerousMongoApi
	@LowLevelApi
	private class ElementMatchBsonNodeNode<T>(
		val target: Path,
		val expression: BsonNode,
		context: BsonContext,
	) : FilterBsonNodeNode(context) {

		override fun simplify(): AbstractBsonNode =
			ElementMatchBsonNodeNode<T>(target, expression.simplify()
				?: OrFilterBsonNodeNode<T>(emptyList(), context), context)

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
		accept(ArrayAllBsonNodeNode(path, values, context))
	}

	@LowLevelApi
	private class ArrayAllBsonNodeNode<T>(
		val path: Path,
		val values: Collection<T>,
		context: BsonContext,
	) : FilterBsonNodeNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument(path.toString()) {
				writeArray("\$all") {
					for (value in values) {
						writeObjectSafe(value)
					}
				}
			}
		}
	}

	// endregion
	// region $expr

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun expr(block: ValueDsl.() -> Value<T & Any, Boolean>) {
		val value = ExprEvaluator(context).block()
		accept(ExprBsonNodeNode(value, context))
	}

	@LowLevelApi
	private class ExprEvaluator(override val context: BsonContext) : ValueDsl

	@OptIn(LowLevelApi::class)
	private class ExprBsonNodeNode<T>(
		val value: Value<*, T>,
		context: BsonContext,
	) : FilterBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			write("\$expr") {
				value.writeTo(this)
			}
		}
	}

	// endregion

}

/**
 * Creates an empty [FilterQuery].
 */
@LowLevelApi
fun <T> FilterQuery(context: BsonContext): FilterQuery<T> =
	FilterQueryImpl(context)
