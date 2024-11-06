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
import opensavvy.ktmongo.bson.types.BsonType
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression

/**
 * Implementation of [PredicateOperators].
 */
@KtMongoDsl
class PredicateExpression<T>(
	context: BsonContext,
) : AbstractCompoundExpression(context), PredicateOperators<T> {

	// region Low-level operations

	@LowLevelApi
	private sealed class PredicateExpressionNode(context: BsonContext) : AbstractExpression(context)

	// endregion
	// region $eq

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun eq(value: T) {
		accept(EqualityExpressionNode(value, context))
	}

	@LowLevelApi
	private inner class EqualityExpressionNode<T>(
		val value: T,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeObjectSafe("\$eq", value, context)
		}
	}

	// endregion
	// region $ne

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun ne(value: T) {
		accept(InequalityExpressionNode(value, context))
	}

	@LowLevelApi
	private class InequalityExpressionNode<T>(
		val value: T,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeObjectSafe("\$ne", value, context)
		}
	}

	// endregion
	// region $exists

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun exists() {
		accept(ExistsPredicateExpressionNode(true, context))
	}

	@LowLevelApi
	private class ExistsPredicateExpressionNode(
		val exists: Boolean,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeBoolean("\$exists", exists)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun doesNotExist() {
		accept(ExistsPredicateExpressionNode(false, context))
	}

	// endregion
	// region $type

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun hasType(type: BsonType) {
		accept(TypePredicateExpressionNode(type, context))
	}

	@LowLevelApi
	private class TypePredicateExpressionNode(
		val type: BsonType,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		override fun write(writer: BsonFieldWriter) {
			writer.writeInt32("\$type", type.code)
		}
	}

	// endregion
	// region $not

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun not(expression: PredicateOperators<T>.() -> Unit) {
		accept(NotPredicateExpressionNode(PredicateExpression<T>(context).apply(expression), context))
	}

	@LowLevelApi
	private class NotPredicateExpressionNode<T>(
		val expression: PredicateExpression<T>,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		override fun simplify(): AbstractExpression? {
			if (expression.children.isEmpty())
				return null

			return super.simplify()
		}

		override fun write(writer: BsonFieldWriter) {
			writer.writeDocument("\$not") {
				expression.writeTo(writer)
			}
		}
	}

	// endregion
	// region $gt, $gte, $lt, $lte

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun gt(value: T) {
		accept(GtPredicateExpressionNode(value, context))
	}

	@LowLevelApi
	private class GtPredicateExpressionNode<T>(
		private val value: T,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeObjectSafe("\$gt", value, context)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun gte(value: T) {
		accept(GtePredicateExpressionNode(value, context))
	}

	@LowLevelApi
	private class GtePredicateExpressionNode<T>(
		private val value: T,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeObjectSafe("\$gte", value, context)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun lt(value: T) {
		accept(LtPredicateExpressionNode(value, context))
	}

	@LowLevelApi
	private class LtPredicateExpressionNode<T>(
		private val value: T,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeObjectSafe("\$lt", value, context)
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun lte(value: T) {
		accept(LtePredicateExpressionNode(value, context))
	}

	@LowLevelApi
	private class LtePredicateExpressionNode<T>(
		private val value: T,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeObjectSafe("\$lte", value, context)
		}
	}

	// endregion
	// region $in

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	override fun isOneOf(values: Collection<T>) {
		accept(OneOfPredicateExpressionNode(values, context))
	}

	@LowLevelApi
	private class OneOfPredicateExpressionNode<T>(
		val values: Collection<T>,
		context: BsonContext,
	) : PredicateExpressionNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) {
			writer.writeArray("\$in") {
				for (value in values)
					writeObjectSafe(value, context)
			}
		}
	}

	// endregion
}
