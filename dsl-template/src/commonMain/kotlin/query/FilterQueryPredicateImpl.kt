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
@file:JvmName("FilterQueryPredicateKt")

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Implementation of [FilterQueryPredicate].
 */
@KtMongoDsl
private class FilterQueryPredicateImpl<T>(
	context: BsonContext,
	val type: KType,
) : AbstractCompoundBsonNode(context), FilterQueryPredicate<T> {

	// region Low-level operations

	@LowLevelApi
	private sealed class PredicateBsonNodeNode(context: BsonContext) : AbstractBsonNode(context)

	@LowLevelApi
	override fun simplify(children: List<BsonNode>): AbstractBsonNode? =
		if (children.isEmpty()) null
		else this

	// endregion
	// region $eq

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun eq(value: T) {
		accept(EqualityBsonNodeNode(value, context, type))
	}

	@LowLevelApi
	private class EqualityBsonNodeNode<T>(
		val value: T,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeSafe("\$eq", value, type)
		}
	}

	// endregion
	// region $ne

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun ne(value: T) {
		accept(InequalityBsonNodeNode(value, context, type))
	}

	@LowLevelApi
	private class InequalityBsonNodeNode<T>(
		val value: T,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeSafe("\$ne", value, type)
		}
	}

	// endregion
	// region $exists

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun exists() {
		accept(ExistsPredicateBsonNodeNode(true, context))
	}

	@LowLevelApi
	private class ExistsPredicateBsonNodeNode(
		val exists: Boolean,
		context: BsonContext,
	) : PredicateBsonNodeNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeBoolean("\$exists", exists)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun doesNotExist() {
		accept(ExistsPredicateBsonNodeNode(false, context))
	}

	// endregion
	// region $type

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun hasType(type: BsonType) {
		accept(TypePredicateBsonNodeNode(type, context))
	}

	@LowLevelApi
	private class TypePredicateBsonNodeNode(
		val type: BsonType,
		context: BsonContext,
	) : PredicateBsonNodeNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeInt32("\$type", type.code)
		}
	}

	// endregion
	// region $not

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun not(expression: FilterQueryPredicate<T>.() -> Unit) {
		accept(NotPredicateBsonNodeNode(FilterQueryPredicateImpl<T>(context, type).apply(expression), context))
	}

	@LowLevelApi
	private class NotPredicateBsonNodeNode<T>(
		val expression: FilterQueryPredicateImpl<T>,
		context: BsonContext,
	) : PredicateBsonNodeNode(context) {

		override fun simplify(): AbstractBsonNode? {
			if (expression.children.isEmpty())
				return null

			return super.simplify()
		}

		override fun write(writer: BsonFieldWriter) {
			writer.writeDocument("\$not") {
				expression.writeTo(this)
			}
		}
	}

	// endregion
	// region $gt, $gte, $lt, $lte

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun gt(value: T) {
		accept(GtPredicateBsonNodeNode(value, context, type))
	}

	@LowLevelApi
	private class GtPredicateBsonNodeNode<T>(
		private val value: T,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeSafe("\$gt", value, type)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun gte(value: T) {
		accept(GtePredicateBsonNodeNode(value, context, type))
	}

	@LowLevelApi
	private class GtePredicateBsonNodeNode<T>(
		private val value: T,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeSafe("\$gte", value, type)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun lt(value: T) {
		accept(LtPredicateBsonNodeNode(value, context, type))
	}

	@LowLevelApi
	private class LtPredicateBsonNodeNode<T>(
		private val value: T,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeSafe("\$lt", value, type)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun lte(value: T) {
		accept(LtePredicateBsonNodeNode(value, context, type))
	}

	@LowLevelApi
	private class LtePredicateBsonNodeNode<T>(
		private val value: T,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeSafe("\$lte", value, type)
		}
	}

	// endregion
	// region $in

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun isOneOf(values: Collection<T>) {
		accept(OneOfPredicateBsonNodeNode(values, context, type))
	}

	@LowLevelApi
	private class OneOfPredicateBsonNodeNode<T>(
		val values: Collection<T>,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeArray("\$in") {
				for (value in values)
					writeSafe(value, type)
			}
		}
	}

	// endregion
	// region $nin

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun isNotOneOf(values: Collection<T>) {
		accept(NotOneOfPredicateExpressionNode(values, context, type))
	}

	@LowLevelApi
	private class NotOneOfPredicateExpressionNode<T>(
		val values: Collection<T>,
		context: BsonContext,
		val type: KType,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeArray("\$nin") {
				for (value in values)
					writeSafe(value, type)
			}
		}
	}

	// endregion
	// region $regex

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@KtMongoDsl
	override fun regex(
		pattern: String,
		caseInsensitive: Boolean,
		dotAll: Boolean,
		extended: Boolean,
		matchEachLine: Boolean,
	) {
		accept(RegexBsonNode(pattern, caseInsensitive, dotAll, extended, matchEachLine, context))
	}

	@LowLevelApi
	private class RegexBsonNode(
		val pattern: String,
		val caseInsensitive: Boolean,
		val dotAll: Boolean,
		val extended: Boolean,
		val matchEachLine: Boolean,
		context: BsonContext,
	) : PredicateBsonNodeNode(context) {
		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeRegularExpression(
				"\$regex",
				pattern,
				buildString {
					// ⚠ Must be in alphabetical order

					if (caseInsensitive)
						append('i')

					if (matchEachLine)
						append('m')

					if (dotAll)
						append('s')

					if (extended)
						append('x')
				}
			)
		}
	}

	// endregion
	// region Bitwise operators

	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAllClear(mask: UInt) {
		accept(BitwiseIntNode(context, mask, "bitsAllClear"))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAllClear(mask: ByteArray) {
		accept(BitwiseByteArrayNode(context, mask, "bitsAllClear"))
	}

	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAllSet(mask: UInt) {
		accept(BitwiseIntNode(context, mask, "bitsAllSet"))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAllSet(mask: ByteArray) {
		accept(BitwiseByteArrayNode(context, mask, "bitsAllSet"))
	}

	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAnyClear(mask: UInt) {
		accept(BitwiseIntNode(context, mask, "bitsAnyClear"))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAnyClear(mask: ByteArray) {
		accept(BitwiseByteArrayNode(context, mask, "bitsAnyClear"))
	}

	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAnySet(mask: UInt) {
		accept(BitwiseIntNode(context, mask, "bitsAnySet"))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun bitsAnySet(mask: ByteArray) {
		accept(BitwiseByteArrayNode(context, mask, "bitsAnySet"))
	}

	@LowLevelApi
	private class BitwiseIntNode(
		context: BsonContext,
		private val value: UInt,
		private val operatorName: String,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeInt64("$$operatorName", value.toULong().toLong())
		}
	}

	@LowLevelApi
	private class BitwiseByteArrayNode(
		context: BsonContext,
		private val value: ByteArray,
		private val operatorName: String,
	) : PredicateBsonNodeNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeBinaryData("$$operatorName", 0u.toUByte(), value)
		}
	}

	// endregion
}

/**
 * Creates an empty [FilterQueryPredicate].
 *
 * Users should generally not need to interact with this function, as KtMongo already provides an instance of
 * [FilterQueryPredicate] in all contexts where one is useful.
 *
 * If calling this method, prefer the inline overload.
 *
 * @param type The [KType] instance that corresponds to type [T].
 * If [KType] doesn't correspond to [T], the behavior is unspecified.
 */
@LowLevelApi
fun <T> FilterQueryPredicate(context: BsonContext, type: KType): FilterQueryPredicate<T> =
	FilterQueryPredicateImpl(context, type)

/**
 * Creates an empty [FilterQueryPredicate].
 *
 * Users should generally not need to interact with this function, as KtMongo already provides an instance of
 * [FilterQueryPredicate] in all contexts where one is useful.
 */
@LowLevelApi
inline fun <reified T> FilterQueryPredicate(context: BsonContext): FilterQueryPredicate<T> =
	FilterQueryPredicate(context, typeOf<T>())
