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

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.query.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.query.common.AbstractExpression
import opensavvy.ktmongo.dsl.query.common.Expression
import opensavvy.ktmongo.dsl.tree.acceptAll
import kotlin.reflect.KClass

/**
 * Implementation of [UpdateQuery].
 */
@KtMongoDsl
class UpdateExpression<T>(
	context: BsonContext,
) : AbstractCompoundExpression(context),
	UpsertQuery<T>,
	FieldDsl {

	// region Low-level operations

	private class OperatorCombinator<T : AbstractExpression>(
		val type: KClass<T>,
		val combinator: (List<T>, BsonContext) -> T
	) {
		@Suppress("UNCHECKED_CAST") // This is a private class, it should not be used incorrectly
		operator fun invoke(sources: List<AbstractExpression>, context: BsonContext) =
			combinator(sources as List<T>, context)
	}

	@LowLevelApi
	override fun simplify(children: List<Expression>): AbstractExpression? {
		if (children.isEmpty())
			return null

		val simplifiedChildren = combinators.fold(children) { newChildren, combinator ->
			@Suppress("UNCHECKED_CAST") // safe because of the filter
			val matching = newChildren
				.filter { it::class == combinator.type }
				as List<UpdateExpressionNode>

			if (matching.size <= 1)
			// At least two elements are required to combine them into a single one!
				return@fold newChildren

			val childrenWithoutMatching = newChildren - matching.toSet()
			childrenWithoutMatching + combinator(matching, context)
		}

		@OptIn(DangerousMongoApi::class)
		if (simplifiedChildren != children)
			return UpdateExpression<T>(context).apply {
				acceptAll(simplifiedChildren)
			}
		return this
	}

	@LowLevelApi
	private sealed class UpdateExpressionNode(context: BsonContext) : AbstractExpression(context)

	// endregion
	// region $set

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V) {
		accept(SetExpressionNode(listOf(this.path to value), context))
	}

	@LowLevelApi
	private class SetExpressionNode(
		val mappings: List<Pair<Path, *>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$set") {
				for ((field, value) in mappings) {
					writeObjectSafe(field.toString(), value)
				}
			}
		}
	}

	// endregion
	// region $setOnInsert

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setOnInsert(value: V) {
		accept(SetOnInsertExpressionNode(listOf(this.path to value), context))
	}

	@LowLevelApi
	private class SetOnInsertExpressionNode(
		val mappings: List<Pair<Path, *>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$setOnInsert") {
				for ((field, value) in mappings) {
					writeObjectSafe(field.toString(), value)
				}
			}
		}
	}

	// endregion
	// region $inc

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override infix fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.inc(amount: V) {
		accept(IncrementExpressionNode(listOf(this.path to amount), context))
	}

	@LowLevelApi
	private class IncrementExpressionNode(
		val mappings: List<Pair<Path, Number>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$inc") {
				for ((field, value) in mappings) {
					writeObjectSafe(field.toString(), value)
				}
			}
		}
	}

	// endregion
	// region $unset

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.unset() {
		accept(UnsetExpressionNode(listOf(this.path), context))
	}

	@LowLevelApi
	private class UnsetExpressionNode(
		val fields: List<Path>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { fields.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$unset") {
				for (field in fields) {
					writeBoolean(field.toString(), true)
				}
			}
		}
	}

	// endregion
	// region $rename

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.renameTo(newName: Field<T, V>) {
		accept(RenameExpressionNode(listOf(this.path to newName.path), context))
	}

	@LowLevelApi
	private class RenameExpressionNode(
		val fields: List<Pair<Path, Path>>,
		context: BsonContext,
	) : UpdateExpressionNode(context) {
		override fun simplify(): AbstractExpression? =
			this.takeUnless { fields.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$rename") {
				for ((before, after) in fields) {
					writeString(before.toString(), after.toString())
				}
			}
		}
	}

	// endregion

	companion object {
		@OptIn(LowLevelApi::class)
		private val combinators = listOf(
			OperatorCombinator(SetExpressionNode::class) { sources, context ->
				SetExpressionNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(SetOnInsertExpressionNode::class) { sources, context ->
				SetOnInsertExpressionNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(IncrementExpressionNode::class) { sources, context ->
				IncrementExpressionNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(UnsetExpressionNode::class) { sources, context ->
				UnsetExpressionNode(sources.flatMap { it.fields }, context)
			},
			OperatorCombinator(RenameExpressionNode::class) { sources, context ->
				RenameExpressionNode(sources.flatMap { it.fields }, context)
			},
		)
	}
}
