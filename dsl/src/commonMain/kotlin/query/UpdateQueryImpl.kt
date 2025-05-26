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
@file:JvmName("UpdateQueryKt")

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.acceptAll
import kotlin.reflect.KClass

/**
 * Implementation of [UpdateQuery].
 */
@KtMongoDsl
private class UpdateQueryImpl<T>(
	context: BsonContext,
) : AbstractCompoundBsonNode(context),
	UpsertQuery<T>,
	FieldDsl {

	// region Low-level operations

	private class OperatorCombinator<T : AbstractBsonNode>(
		val type: KClass<T>,
		val combinator: (List<T>, BsonContext) -> T,
	) {
		@Suppress("UNCHECKED_CAST") // This is a private class, it should not be used incorrectly
		operator fun invoke(sources: List<AbstractBsonNode>, context: BsonContext) =
			combinator(sources as List<T>, context)
	}

	@LowLevelApi
	override fun simplify(children: List<BsonNode>): AbstractBsonNode? {
		if (children.isEmpty())
			return null

		val simplifiedChildren = combinators.fold(children) { newChildren, combinator ->
			@Suppress("UNCHECKED_CAST") // safe because of the filter
			val matching = newChildren
				.filter { it::class == combinator.type }
				as List<UpdateBsonNodeNode>

			if (matching.size <= 1)
			// At least two elements are required to combine them into a single one!
				return@fold newChildren

			val childrenWithoutMatching = newChildren - matching.toSet()
			childrenWithoutMatching + combinator(matching, context)
		}

		@OptIn(DangerousMongoApi::class)
		if (simplifiedChildren != children)
			return UpdateQueryImpl<T>(context).apply {
				acceptAll(simplifiedChildren)
			}
		return this
	}

	@LowLevelApi
	private sealed class UpdateBsonNodeNode(context: BsonContext) : AbstractBsonNode(context)

	// endregion
	// region $set

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V) {
		accept(SetBsonNodeNode(listOf(this.path to value), context))
	}

	@LowLevelApi
	private class SetBsonNodeNode(
		val mappings: List<Pair<Path, *>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

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
		accept(SetOnInsertBsonNodeNode(listOf(this.path to value), context))
	}

	@LowLevelApi
	private class SetOnInsertBsonNodeNode(
		val mappings: List<Pair<Path, *>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
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
		accept(IncrementBsonNodeNode(listOf(this.path to amount), context))
	}

	@LowLevelApi
	private class IncrementBsonNodeNode(
		val mappings: List<Pair<Path, Number>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
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
	// region $mul

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override infix fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.mul(amount: V) {
		accept(MultiplyBsonNodeNode(listOf(this.path to amount), context))
	}

	@LowLevelApi
	private class MultiplyBsonNodeNode(
		val mappings: List<Pair<Path, Number>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$mul") {
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
		accept(UnsetBsonNodeNode(listOf(this.path), context))
	}

	@LowLevelApi
	private class UnsetBsonNodeNode(
		val fields: List<Path>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
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
		accept(RenameBsonNodeNode(listOf(this.path to newName.path), context))
	}

	@LowLevelApi
	private class RenameBsonNodeNode(
		val fields: List<Pair<Path, Path>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
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
			OperatorCombinator(SetBsonNodeNode::class) { sources, context ->
				SetBsonNodeNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(SetOnInsertBsonNodeNode::class) { sources, context ->
				SetOnInsertBsonNodeNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(IncrementBsonNodeNode::class) { sources, context ->
				IncrementBsonNodeNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(MultiplyBsonNodeNode::class) { sources, context ->
				MultiplyBsonNodeNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(UnsetBsonNodeNode::class) { sources, context ->
				UnsetBsonNodeNode(sources.flatMap { it.fields }, context)
			},
			OperatorCombinator(RenameBsonNodeNode::class) { sources, context ->
				RenameBsonNodeNode(sources.flatMap { it.fields }, context)
			},
		)
	}
}

/**
 * Creates an empty [UpdateQuery].
 */
@LowLevelApi
fun <T> UpdateQuery(context: BsonContext): UpdateQuery<T> =
	UpdateQueryImpl(context)

/**
 * Creates an empty [UpsertQuery].
 */
@LowLevelApi
fun <T> UpsertQuery(context: BsonContext): UpsertQuery<T> =
	UpdateQueryImpl(context)
