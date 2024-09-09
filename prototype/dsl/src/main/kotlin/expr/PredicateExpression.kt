package fr.qsh.ktmongo.dsl.expr

import fr.qsh.ktmongo.dsl.*
import fr.qsh.ktmongo.dsl.expr.common.AbstractCompoundExpression
import fr.qsh.ktmongo.dsl.expr.common.AbstractExpression
import fr.qsh.ktmongo.dsl.path.PropertySyntaxScope
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.configuration.CodecRegistry

/**
 * DSL for MongoDB operators that are used as predicates in conditions in a context where the targeted field is already
 * specified.
 */
@KtMongoDsl
class PredicateExpression<T>(
	codec: CodecRegistry,
) : AbstractCompoundExpression(codec), PropertySyntaxScope {

	// region Low-level operations

	@LowLevelApi
	private sealed class PredicateExpressionNode(codec: CodecRegistry) : AbstractExpression(codec)

	// endregion
	// region $eq

	/**
	 * Matches documents where the value of a field equals the [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         eq("foo")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 *
	 * @see FilterExpression.eq Shorthand.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun eq(value: T) {
		accept(EqualityExpressionNode(value, codec))
	}

	@LowLevelApi
	private class EqualityExpressionNode<T>(
		val value: T,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		override fun write(writer: BsonWriter) {
			if (value == null)
				writer.writeNull("\$eq")
			else {
				writer.writeName("\$eq")
				writer.writeObject(value, codec)
			}
		}
	}

	/**
	 * Matches documents where the value of a field equals [value].
	 *
	 * If [value] is `null`, the operator is not added (all documents are matched).
	 *
	 * ### Example
	 *
	 * This operator is useful to simplify searches when the criteria is optional.
	 * For example, instead of writing:
	 * ```kotlin
	 * collection.find {
	 *     User::name {
	 *         if (criteria.name != null)
	 *             eq(criteria.name)
	 *     }
	 * }
	 * ```
	 * this operator can be used instead:
	 * ```kotlin
	 * collection.find {
	 *     User::name {
	 *         eqNotNull(criteria.name)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/eq/)
	 *
	 * @see FilterExpression.eqNotNull Shorthand.
	 * @see eq Equality filter.
	 */
	@KtMongoDsl
	fun eqNotNull(value: T?) {
		if (value != null) eq(value)
	}

	// endregion
	// region $ne

	/**
	 * Matches documents where the value of a field does not equal the [value].
	 *
	 * The result includes documents which do not contain the specified field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         ne("foo")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/ne/)
	 *
	 * @see FilterExpression.ne Shorthand.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun ne(value: T) {
		accept(InequalityExpressionNode(value, codec))
	}

	@LowLevelApi
	private class InequalityExpressionNode<T>(
		val value: T,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		override fun write(writer: BsonWriter) {
			if (value == null)
				writer.writeNull("\$ne")
			else {
				writer.writeName("\$ne")
				writer.writeObject(value, codec)
			}
		}
	}

	// endregion
	// region $exists

	/**
	 * Matches documents that contain the specified field, including
	 * values where the field value is `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         exists()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see FilterExpression.exists Shorthand.
	 * @see doesNotExist Opposite.
	 * @see isNotNull Identical, but does not match elements where the field is `null`.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun exists() {
		accept(ExistsPredicateExpressionNode(true, codec))
	}

	@LowLevelApi
	private class ExistsPredicateExpressionNode(
		val exists: Boolean,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		override fun write(writer: BsonWriter) {
			writer.writeBoolean("\$exists", exists)
		}
	}

	/**
	 * Matches documents that do not contain the specified field.
	 * Documents where the field if `null` are counted as existing.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String?,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         doesNotExist()
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/exists/)
	 *
	 * @see FilterExpression.doesNotExist Shorthand.
	 * @see exists Opposite.
	 * @see isNull Only matches elements that are specifically `null`.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun doesNotExist() {
		accept(ExistsPredicateExpressionNode(false, codec))
	}

	// endregion
	// region $type

	/**
	 * Selects documents where the value of the field is an instance of the specified BSON [type].
	 *
	 * Querying by data type is useful when dealing with highly unstructured data where data types
	 * are not predictable.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Any,
	 * )
	 *
	 * collection.find {
	 *     User::age {
	 *         type(BsonType.STRING)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/type/)
	 *
	 * @see FilterExpression.hasType Shorthand.
	 * @see isNull Checks if a value has the type [BsonType.NULL].
	 * @see isUndefined Checks if a value has the type [BsonType.UNDEFINED].
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun hasType(type: BsonType) {
		accept(TypePredicateExpressionNode(type, codec))
	}

	@LowLevelApi
	private class TypePredicateExpressionNode(
		val type: BsonType,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		override fun write(writer: BsonWriter) {
			writer.writeInt32("\$type", type.value)
		}
	}

	// endregion
	// region $not

	/**
	 * Performs a logical `NOT` operation on the specified [expression] and selects the
	 * documents that *do not* match the expression. This includes the elements
	 * that do not contain the field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *     User::age {
	 *         not {
	 *             hasType(BsonType.STRING)
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/not/)
	 *
	 * @see FilterExpression.not Shorthand.
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun not(expression: PredicateExpression<T>.() -> Unit) {
		accept(NotPredicateExpressionNode(PredicateExpression<T>(codec).apply(expression), codec))
	}

	@LowLevelApi
	private class NotPredicateExpressionNode<T>(
		val expression: PredicateExpression<T>,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		override fun simplify(): AbstractExpression? {
			if (expression.children.isEmpty())
				return null

			return super.simplify()
		}

		override fun write(writer: BsonWriter) {
			writer.writeDocument("\$not") {
				expression.writeTo(writer)
			}
		}
	}

	// endregion
	// region Nullability

	/**
	 * Selects documents for which the field is `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isNull() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterExpression.isNull Shorthand.
	 * @see doesNotExist Checks if the value is not set.
	 * @see isNotNull Opposite.
	 */
	@KtMongoDsl
	fun isNull() =
		hasType(BsonType.NULL)

	/**
	 * Selects documents for which the field is not `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isNotNull() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterExpression.isNotNull Shorthand.
	 * @see isNull Opposite.
	 */
	@KtMongoDsl
	fun isNotNull() =
		not { isNull() }

	/**
	 * Selects documents for which the field is `undefined`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isUndefined() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterExpression.isUndefined Shorthand.
	 * @see isNotUndefined Opposite.
	 */
	@KtMongoDsl
	fun isUndefined() =
		hasType(BsonType.UNDEFINED)

	/**
	 * Selects documents for which the field is not `undefined`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { isNotUndefined() }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-for-null-fields/#type-check)
	 *
	 * @see FilterExpression.isNotUndefined Shorthand.
	 * @see isUndefined Opposite.
	 */
	@KtMongoDsl
	fun isNotUndefined() =
		not { isUndefined() }

	// endregion
	// region $gt, $gte, $lt, $lte

	/**
	 * Selects documents for which this field has a value strictly greater than [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { gt(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see FilterExpression.gt
	 * @see gtNotNull
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun gt(value: T) {
		accept(GtPredicateExpressionNode(value, codec))
	}

	@LowLevelApi
	private class GtPredicateExpressionNode<T>(
		private val value: T,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		@LowLevelApi
		override fun write(writer: BsonWriter) {
			writer.writeName("\$gt")
			writer.writeObject(value, codec)
		}
	}

	/**
	 * Selects documents for which this field has a value strictly greater than [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { gtNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gt/)
	 *
	 * @see FilterExpression.gtNotNull
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun gtNotNull(value: T?) {
		if (value != null)
			gt(value)
	}

	/**
	 * Selects documents for which this field has a value greater or equal to [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { gte(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see FilterExpression.gte
	 * @see gteNotNull
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun gte(value: T) {
		accept(GtePredicateExpressionNode(value, codec))
	}

	@LowLevelApi
	private class GtePredicateExpressionNode<T>(
		private val value: T,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		@LowLevelApi
		override fun write(writer: BsonWriter) {
			writer.writeName("\$gte")
			writer.writeObject(value, codec)
		}
	}

	/**
	 * Selects documents for which this field has a value greater or equal to [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { gteNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/gte/)
	 *
	 * @see FilterExpression.gteNotNull
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun gteNotNull(value: T?) {
		if (value != null)
			gte(value)
	}

	/**
	 * Selects documents for which this field has a value strictly lesser than [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { lt(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see FilterExpression.lt
	 * @see ltNotNull
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun lt(value: T) {
		accept(LtPredicateExpressionNode(value, codec))
	}

	@LowLevelApi
	private class LtPredicateExpressionNode<T>(
		private val value: T,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		@LowLevelApi
		override fun write(writer: BsonWriter) {
			writer.writeName("\$lt")
			writer.writeObject(value, codec)
		}
	}

	/**
	 * Selects documents for which this field has a value strictly lesser than [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { ltNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lt/)
	 *
	 * @see FilterExpression.ltNotNull
	 * @see lqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun ltNotNull(value: T?) {
		if (value != null)
			lt(value)
	}

	/**
	 * Selects documents for which this field has a value lesser or equal to [value].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::age { lte(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see FilterExpression.lte
	 * @see lteNotNull
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun lte(value: T) {
		accept(LtePredicateExpressionNode(value, codec))
	}

	@LowLevelApi
	private class LtePredicateExpressionNode<T>(
		private val value: T,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		@LowLevelApi
		override fun write(writer: BsonWriter) {
			writer.writeName("\$lte")
			writer.writeObject(value, codec)
		}
	}

	/**
	 * Selects documents for which this field has a value lesser or equal to [value].
	 *
	 * If [value] is `null`, the operator is not added (all elements are matched).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?
	 * )
	 *
	 * collection.find {
	 *     User::age { lteNotNull(18) }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/lte/)
	 *
	 * @see FilterExpression.lteNotNull
	 * @see eqNotNull Learn more about the 'notNull' variants
	 */
	@KtMongoDsl
	fun lteNotNull(value: T?) {
		if (value != null)
			lte(value)
	}

	// endregion
	// region $in

	/**
	 * Selects documents for which this field is equal to one of the given [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         isOneOf(listOf("Alfred", "Arthur"))
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see FilterExpression.isOneOf
	 */
	@OptIn(LowLevelApi::class)
	@KtMongoDsl
	fun isOneOf(values: Collection<T>) {
		accept(OneOfPredicateExpressionNode(values, codec))
	}

	@LowLevelApi
	private class OneOfPredicateExpressionNode<T>(
		val values: Collection<T>,
		codec: CodecRegistry,
	) : PredicateExpressionNode(codec) {

		@LowLevelApi
		override fun write(writer: BsonWriter) {
			writer.writeArray("\$in") {
				for (value in values)
					writer.writeObject(value, codec)
			}
		}
	}

	/**
	 * Selects documents for which this field is equal to one of the given [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,
	 * )
	 *
	 * collection.find {
	 *     User::name {
	 *         isOneOf("Alfred", "Arthur")
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/in/)
	 *
	 * @see FilterExpression.isOneOf
	 */
	@KtMongoDsl
	fun isOneOf(vararg values: T) {
		isOneOf(values.asList())
	}

	// endregion
}
