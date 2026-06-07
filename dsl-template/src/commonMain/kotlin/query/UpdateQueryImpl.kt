/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.UpdateOptions
import opensavvy.ktmongo.dsl.options.ArrayFiltersOption
import opensavvy.ktmongo.dsl.options.ArrayFiltersOptionDsl
import opensavvy.ktmongo.dsl.options.WithArrayFilters
import opensavvy.ktmongo.dsl.path.*
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.acceptAll
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Instant

/**
 * Implementation of [UpdateQuery].
 */
@KtMongoDsl
private class UpdateQueryImpl<T>(
	context: BsonContext,
	private val arrayFilterCreator: ArrayFilterCreator?,
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
			return UpdateQueryImpl<T>(context, arrayFilterCreator).apply {
				acceptAll(simplifiedChildren)
			}
		return this
	}

	private class Value(
		val value: Any?,
		val type: KType,
	) {
		override fun toString(): String =
			"Value($value, $type)"
	}

	@LowLevelApi
	private sealed class UpdateBsonNodeNode(context: BsonContext) : AbstractBsonNode(context)

	// endregion
	// region $set

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V, type: KType) {
		accept(SetBsonNodeNode(listOf(this.path to Value(value, type)), context))
	}

	@LowLevelApi
	private class SetBsonNodeNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$set") {
				for ((field, value) in mappings) {
					writeSafe(field.toString(), value.value, value.type)
				}
			}
		}
	}

	// endregion
	// region $setOnInsert

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.setOnInsert(value: V, type: KType) {
		accept(SetOnInsertBsonNodeNode(listOf(this.path to Value(value, type)), context))
	}

	@LowLevelApi
	private class SetOnInsertBsonNodeNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$setOnInsert") {
				for ((field, value) in mappings) {
					writeSafe(field.toString(), value.value, value.type)
				}
			}
		}
	}

	// endregion
	// region $inc

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.inc(amount: V, type: KType) {
		accept(IncrementBsonNodeNode(listOf(this.path to Value(amount, type)), context))
	}

	@LowLevelApi
	private class IncrementBsonNodeNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$inc") {
				for ((field, value) in mappings) {
					writeSafe(field.toString(), value.value, value.type)
				}
			}
		}
	}

	// endregion
	// region $mul

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V : Number> Field<T, V>.mul(amount: V, type: KType) {
		accept(MultiplyBsonNodeNode(listOf(this.path to Value(amount, type)), context))
	}

	@LowLevelApi
	private class MultiplyBsonNodeNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$mul") {
				for ((field, value) in mappings) {
					writeSafe(field.toString(), value.value, value.type)
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
	// region $min

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> Field<T, V?>.min(value: V, type: KType) {
		accept(MinBsonNodeNode(listOf(this.path to Value(value, type)), context))
	}

	@LowLevelApi
	private class MinBsonNodeNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$min") {
				for ((field, value) in mappings) {
					writeSafe(field.toString(), value.value, value.type)
				}
			}
		}
	}

	// endregion
	// region $max

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	override fun <@kotlin.internal.OnlyInputTypes V : Comparable<V>> Field<T, V?>.max(value: V, type: KType) {
		accept(MaxBsonNodeNode(listOf(this.path to Value(value, type)), context))
	}

	@LowLevelApi
	private class MaxBsonNodeNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): AbstractBsonNode? =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("\$max") {
				for ((field, value) in mappings) {
					writeSafe(field.toString(), value.value, value.type)
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
	// region $currentDate

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun Field<T, Instant?>.setToCurrentDate() {
		accept(CurrentDateBsonNode(listOf(this.path to true), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@Suppress("INAPPLICABLE_JVM_NAME")
	@JvmName("setToCurrentTimestamp")
	override fun Field<T, Timestamp?>.setToCurrentDate() {
		accept(CurrentDateBsonNode(listOf(this.path to false), context))
	}

	@LowLevelApi
	private class CurrentDateBsonNode(
		// `true` = Instant, `false` = Timestamp
		val mappings: List<Pair<Path, Boolean>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument($$"$currentDate") {
				for ((field, isInstant) in mappings) {
					write(field.toString()) {
						if (isInstant)
							writeBoolean(true)
						else
							writeDocument {
								writeString($$"$type", "timestamp")
							}
					}
				}
			}
		}
	}

	// endregion
	// region $addToSet

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun <V> Field<T, Collection<V>>.addToSet(value: V, type: KType) {
		accept(AddToSetBsonNode(listOf(this.path to Value(value, type)), context))
	}

	@LowLevelApi
	private class AddToSetBsonNode(
		val mappings: List<Pair<Path, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			val groupedMappings = mappings.groupBy(
				keySelector = { it.first },
				valueTransform = { it.second },
			)

			writeDocument($$"$addToSet") {
				for ((field, values) in groupedMappings) {
					if (values.size == 1)
						writeSafe(field.toString(), values.single().value, values.single().type)
					else
						writeDocument(field.toString()) {
							writeArray($$"$each") {
								for (value in values)
									writeSafe(value.value, value.type)
							}
						}
				}
			}
		}
	}

	// endregion
	// region $pop

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun Field<T, Collection<*>>.popLast() {
		accept(PopBsonNode(listOf(this.path to 1), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Collection<*>>.popFirst() {
		accept(PopBsonNode(listOf(this.path to -1), context))
	}

	@LowLevelApi
	private class PopBsonNode(
		val mappings: List<Pair<Path, Int>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

		override fun simplify() =
			this.takeUnless { mappings.isEmpty() }

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument($$"$pop") {
				for ((field, direction) in mappings) {
					writeInt32(field.toString(), direction)
				}
			}
		}
	}

	// endregion
	// region $push

	@LowLevelApi
	private class PushSortOptionDslBsonNode<Context : Any>(
		context: BsonContext,
	) : AbstractCompoundBsonNode(context), UpdateQuery.PushSortDsl<Context> {
		var isSimpleValue = false
		var simpleValue = 1

		@OptIn(DangerousMongoApi::class)
		override fun ascending(field: Field<Context, *>) {
			require(!isSimpleValue) { "Cannot specify both natural and field sorts (both ascending() and ascending(someField))" }
			accept(PushSortBsonNode(field.path, 1, context))
		}

		@OptIn(DangerousMongoApi::class)
		override fun descending(field: Field<Context, *>) {
			require(!isSimpleValue) { "Cannot specify both natural and field sorts (both ascending() and ascending(someField))" }
			accept(PushSortBsonNode(field.path, -1, context))
		}

		/**
		 * Sort array elements in ascending order (for simple values, not documents).
		 */
		override fun ascending() {
			require(children.isEmpty()) { "Cannot specify both natural and field sorts (both ascending() and ascending(someField))" }
			isSimpleValue = true
			simpleValue = 1
		}

		/**
		 * Sort array elements in descending order (for simple values, not documents).
		 */
		override fun descending() {
			require(children.isEmpty()) { "Cannot specify both natural and field sorts (both ascending() and ascending(someField))" }
			isSimpleValue = true
			simpleValue = -1
		}

		@LowLevelApi
		private class PushSortBsonNode(
			val path: Path,
			val value: Int,
			context: BsonContext,
		) : AbstractBsonNode(context) {

			override fun write(writer: BsonFieldWriter) = with(writer) {
				writeInt32(path.toString(), value)
			}
		}

		override fun write(writer: BsonFieldWriter, children: List<BsonNode>) {
			if (isSimpleValue) {
				writer.writeInt32($$"$sort", simpleValue)
			} else {
				writer.writeDocument($$"$sort") {
					for (child in children) {
						child.writeTo(this)
					}
				}
			}
		}
	}

	@LowLevelApi
	private class PushBuilderImpl<V>(
		context: BsonContext,
		val type: KType,
	) : AbstractBsonNode(context), UpdateQuery.PushBuilder<V> {
		var values: List<V> = emptyList()
		var sliceValue: Int? = null
		var positionValue: Int? = null
		var sortValue: PushSortOptionDslBsonNode<V & Any>? = null

		override fun each(values: Iterable<V>) {
			this.values += values
		}

		override fun slice(count: Int) {
			this.sliceValue = count
		}

		private fun sliceNotNull(count: Int?) {
			if (count != null)
				slice(count)
		}

		override fun position(index: Int) {
			this.positionValue = index
		}

		fun positionNotNull(index: Int?) {
			if (index != null)
				position(index)
		}

		override fun sort(block: UpdateQuery.PushSortDsl<V & Any>.() -> Unit) {
			val option = PushSortOptionDslBsonNode<V & Any>(context)
			option.block()
			this.sortValue = option
		}

		operator fun plus(other: PushBuilderImpl<V>): PushBuilderImpl<V> {
			val ret = PushBuilderImpl<V>(context, type)

			ret.each(this.values)
			ret.each(other.values)

			ret.sliceNotNull(other.sliceValue ?: this.sliceValue)
			ret.positionNotNull(other.positionValue ?: this.positionValue)

			ret.sortValue = this.sortValue ?: other.sortValue

			ret.freeze()

			return ret
		}

		override fun simplify(): PushBuilderImpl<V>? =
			// Ignore positionValue: if it's present but none of the others are, it still does nothing
			if (values.isNotEmpty() || sliceValue != null || sortValue != null) this
			else null

		override fun write(writer: BsonFieldWriter) {
			with(writer) {
				writeArray($$"$each") {
					for (value in values)
						writeSafe(value, type)
				}
				sliceValue?.let { sliceValue ->
					writeInt32($$"$slice", sliceValue)
				}
				positionValue.takeIf { values.isNotEmpty() }?.let { positionValue ->
					writeInt32($$"$position", positionValue)
				}
				sortValue?.writeTo(this)
			}
		}
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun <V> Field<T, Collection<V>>.push(value: V, type: KType) {
		val pushBuilder = PushBuilderImpl<V>(context, type)
		pushBuilder.each(value)
		pushBuilder.freeze()
		accept(PushBsonNode(listOf(this.path to pushBuilder), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun <V> Field<T, Collection<V>>.push(builder: UpdateQuery.PushBuilder<V>.() -> Unit, type: KType) {
		val pushBuilder = PushBuilderImpl<V>(context, type)
			.apply(builder)
		pushBuilder.freeze()
		accept(PushBsonNode(listOf(this.path to pushBuilder), context))
	}

	@LowLevelApi
	private class PushBsonNode(
		val mappings: List<Pair<Path, PushBuilderImpl<*>>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {
		override fun simplify(): PushBsonNode? {
			if (mappings.isEmpty())
				return null

			@Suppress("UNCHECKED_CAST") // For a given path, they must all have the same type (guaranteed by the DSL). We can thus treat them all as Any?.
			val newMappings = mappings
				.groupingBy { it.first }
				.fold(
					initialValueSelector = { _, (_, first) -> PushBuilderImpl<Any?>(context, first.type) },
					operation = { _, previous, (_, next) ->
						next as PushBuilderImpl<Any?>

						previous + next
					},
				)
				.toList()
				.mapNotNull { (path, builder) ->
					val simplified = builder.simplify()

					if (simplified != null)
						path to simplified
					else null
				}

			if (mappings == newMappings)
				return this

			return PushBsonNode(newMappings, context)
		}

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument($$"$push") {
				for ((field, pushBuilder) in mappings) {
					if (pushBuilder.values.size == 1 && pushBuilder.sliceValue == null) {
						writeSafe(field.toString(), pushBuilder.values.single(), pushBuilder.type)
					} else {
						writeDocument(field.toString()) {
							pushBuilder.writeTo(this)
						}
					}
				}
			}
		}
	}

	// endregion
	// region Array filters

	@OptIn(LowLevelApi::class)
	override fun <V> Field<T, Collection<V>>.filter(id: String): Field<T, V> =
		FieldImpl(this.path / PathSegment.FilteredPositional(id))

	override fun <V> Field<T, Collection<V>>.filter(
		id: String?,
		filter: ArrayFiltersOptionDsl<V>.(it: Field<ArrayFiltersOptionDsl.IteratorType<V>, V>) -> Unit,
	): Field<T, V> {
		checkNotNull(arrayFilterCreator) { "This update query has not been configured to register array filters. If you're using an UpdateQuery instance provided by the library, please report the problem. If you're create your own UpdateQuery instance with the constructor, consider passing an Options instance." }

		val id = arrayFilterCreator.declare(id, filter)

		return filter(id)
	}

	// endregion
	// region $pull

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun <V> Field<T, Collection<V>>.pull(value: V, type: KType) {
		accept(PullBsonNode(listOf(path to Value(value, type)), emptyList(), context))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun <V> Field<T, Collection<V>>.pull(predicate: FilterQuery<V>.() -> Unit, type: KType) {
		accept(PullBsonNode(emptyList(), listOf(path to FilterQuery<V>(context).apply(predicate)), context))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun <V> Field<T, Collection<V>>.pullValues(predicate: FilterQueryPredicate<V>.() -> Unit, type: KType) {
		accept(PullBsonNode(emptyList(), listOf(path to FilterQueryPredicate<V>(context, type).apply(predicate)), context))
	}

	@OptIn(LowLevelApi::class)
	private class PullBsonNode(
		val valueMappings: List<Pair<Path, Value>>,
		val predicateMappings: List<Pair<Path, BsonNode>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

		override fun simplify(): PullBsonNode? {
			// ①. If a key appears multiple times in 'valueMappings', combine it with $in and move it into 'predicateMappings'

			val duplicatedValueMappings = valueMappings
				.groupingBy { it.first }
				.fold(0 to emptyList<Value>()) { (count, mappings), newMappings ->
					(count + 1) to (mappings + newMappings.second)
				}
				.filterValues { (count, _) -> count > 1 }

			val valueMappingsWithDuplicated = valueMappings.filter { (path, _) ->
				path !in duplicatedValueMappings
			}

			val predicateMappingsWithDuplicated = predicateMappings + duplicatedValueMappings
				.map { (path, data) ->
					val type = data.second.mapTo(HashSet()) { it.type }.singleOrNull()
						?: error("Cannot call the pull operator multiple times with values of different types. Please explicitly use the 'pull { isOneOf(…) }' syntax. Values: ${data.second}")
					path to FilterQueryPredicate<Any?>(context, type).apply {
						isOneOf(data.second.map { it.value })
					}
				}

			// ②. Simplify 'predicateMappings'

			val simplifiedPredicateMappings = predicateMappingsWithDuplicated.mapNotNull { (path, value) ->
				val simplified = value.simplify() ?: return@mapNotNull null
				path to simplified
			}

			if (valueMappingsWithDuplicated.isEmpty() && predicateMappingsWithDuplicated.isEmpty())
				return null

			if (predicateMappings == simplifiedPredicateMappings)
				return this

			return PullBsonNode(valueMappingsWithDuplicated, simplifiedPredicateMappings, context)
		}

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument($$"$pull") {
				for ((field, value) in valueMappings) {
					writeSafe(field.toString(), value.value, value.type)
				}

				for ((field, predicate) in predicateMappings) {
					writeDocument(field.toString()) {
						predicate.writeTo(this)
					}
				}
			}
		}
	}

	// endregion
	// region Bitwise operators

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Int>.bitAnd(mask: Int) {
		accept(BitBsonNode(listOf(Triple(path, "and", Value(mask, typeOf<Int>()))), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Long>.bitAnd(mask: Long) {
		accept(BitBsonNode(listOf(Triple(path, "and", Value(mask, typeOf<Long>()))), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Int>.bitOr(mask: Int) {
		accept(BitBsonNode(listOf(Triple(path, "or", Value(mask, typeOf<Int>()))), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Long>.bitOr(mask: Long) {
		accept(BitBsonNode(listOf(Triple(path, "or", Value(mask, typeOf<Long>()))), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Int>.bitXor(mask: Int) {
		accept(BitBsonNode(listOf(Triple(path, "xor", Value(mask, typeOf<Int>()))), context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun Field<T, Long>.bitXor(mask: Long) {
		accept(BitBsonNode(listOf(Triple(path, "xor", Value(mask, typeOf<Long>()))), context))
	}

	@LowLevelApi
	private class BitBsonNode(
		// List<(field, operatorName, mask)>
		val mappings: List<Triple<Path, String, Value>>,
		context: BsonContext,
	) : UpdateBsonNodeNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			val mappingsByField = mappings.groupBy { it.first }

			writeDocument($$"$bit") {
				for ((field, operators) in mappingsByField) {
					writeDocument(field.toString()) {
						for ((_, operator, mask) in operators) {
							writeSafe(operator, mask.value, mask.type)
						}
					}
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
			OperatorCombinator(MinBsonNodeNode::class) { sources, context ->
				MinBsonNodeNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(MaxBsonNodeNode::class) { sources, context ->
				MaxBsonNodeNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(UnsetBsonNodeNode::class) { sources, context ->
				UnsetBsonNodeNode(sources.flatMap { it.fields }, context)
			},
			OperatorCombinator(RenameBsonNodeNode::class) { sources, context ->
				RenameBsonNodeNode(sources.flatMap { it.fields }, context)
			},
			OperatorCombinator(AddToSetBsonNode::class) { sources, context ->
				AddToSetBsonNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(PopBsonNode::class) { sources, context ->
				PopBsonNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(PullBsonNode::class) { sources, context ->
				PullBsonNode(sources.flatMap { it.valueMappings }, sources.flatMap { it.predicateMappings }, context)
			},
			OperatorCombinator(PushBsonNode::class) { sources, context ->
				PushBsonNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(CurrentDateBsonNode::class) { sources, context ->
				CurrentDateBsonNode(sources.flatMap { it.mappings }, context)
			},
			OperatorCombinator(BitBsonNode::class) { sources, context ->
				BitBsonNode(sources.flatMap { it.mappings }, context)
			},
		)
	}
}

/**
 * Creates an empty [UpdateQuery].
 */
@LowLevelApi
fun <T> UpdateQuery(
	context: BsonContext,
	options: UpdateOptions<T>? = null,
): UpdateQuery<T> =
	UpdateQueryImpl(context, options?.let(::ArrayFilterCreator))

/**
 * Creates an empty [UpsertQuery].
 */
@LowLevelApi
fun <T> UpsertQuery(
	context: BsonContext,
	options: UpdateOptions<T>? = null,
): UpsertQuery<T> =
	UpdateQueryImpl(context, options?.let(::ArrayFilterCreator))

private class ArrayFilterCreator(
	private val option: WithArrayFilters,
) {

	private var next = 0

	@OptIn(LowLevelApi::class)
	fun <V> declare(
		id: String?,
		filter: ArrayFiltersOptionDsl<V>.(it: Field<ArrayFiltersOptionDsl.IteratorType<V>, V>) -> Unit,
	): String {
		val alreadyUsed = option.allOptions
			.asSequence()
			.filterIsInstance<ArrayFiltersOption>()
			.flatMap { it.arrayFilters }
			.map { (id, _) -> id }
			.toHashSet()

		var id = id

		if (id == null) {
			do {
				id = "f${next++}"
			} while (id in alreadyUsed)
		}

		option.arrayFilter(id, filter)

		return id
	}
}
