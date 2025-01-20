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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.*
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.expr.common.CompoundExpression
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import kotlin.reflect.KProperty1

/**
 * Marks that a pipeline is able to use [set].
 */
@OptIn(DangerousMongoApi::class)
interface HasSet : PipelineFeature

/**
 * Adds new fields to documents, or overwrites existing fields.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/set/)
 */
@OptIn(LowLevelApi::class, DangerousMongoApi::class)
fun <Type, Document : Any> Pipeline<Type, Document>.set(
	block: SetOperators<Document>.() -> Unit,
): Pipeline<Type, Document> where Type : PipelineType, Type : HasSet =
	withStage(SetStage(SetExpression<Document>(context).apply(block), context))

@OptIn(LowLevelApi::class)
private class SetStage(
	val expression: SetOperators<*>,
	context: BsonContext,
) : AbstractExpression(context) {
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$set") {
			expression.writeTo(this)
		}
	}
}

/**
 * The operators allowed in a [set] stage.
 */
@KtMongoDsl
interface SetOperators<T : Any> : CompoundExpression, ValueDsl, FieldDsl {

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: Value<T, V>)

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: Value<T, V>) {
		this.field.set(value)
	}

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> Field<T, V>.set(value: V) {
		this.set(of(value))
	}

	/**
	 * Replaces the value of a field with the specified [value].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/update/set/)
	 */
	@Suppress("INVISIBLE_REFERENCE")
	@KtMongoDsl
	infix fun <@kotlin.internal.OnlyInputTypes V> KProperty1<T, V>.set(value: V) {
		this.field.set(value)
	}

}

private class SetExpression<T : Any>(
	context: BsonContext,
) : AbstractCompoundExpression(context), SetOperators<T> {

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun <V> Field<T, V>.set(value: Value<T, V>) {
		accept(SetExpression(this.path, value, context))
	}

	@LowLevelApi
	private class SetExpression(
		val path: Path,
		val value: Value<*, *>,
		context: BsonContext,
	) : AbstractExpression(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			write(path.toString()) {
				value.writeTo(this)
			}
		}
	}
}
