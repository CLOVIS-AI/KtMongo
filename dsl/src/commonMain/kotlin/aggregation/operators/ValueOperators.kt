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

package opensavvy.ktmongo.dsl.aggregation.operators

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AbstractValue
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import kotlin.reflect.KProperty1

/**
 * Supertype for all interface operators describing operators on aggregation values.
 *
 * Most of the time, end-users will be using the subtype [AggregationOperators] instead of this interface.
 */
interface ValueOperators : FieldDsl {

	@LowLevelApi
	val context: BsonContext

	/**
	 * Refers to a [field] within an [aggregation value][AggregationOperators].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val acceptanceDate: Instant,
	 *     val publishingDate: Instant,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::publishingDate) lt of(Product::acceptanceDate)
	 *     }
	 * }
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun <Context : Any, Result> of(field: Field<Context, Result>): Value<Context, Result> =
		FieldValue(field, context)

	/**
	 * Refers to a [field] within an [aggregation value][AggregationOperators].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val acceptanceDate: Instant,
	 *     val publishingDate: Instant,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::publishingDate) lt of(Product::acceptanceDate)
	 *     }
	 * }
	 * ```
	 */
	fun <Context : Any, Result> of(field: KProperty1<Context, Result>): Value<Context, Result> =
		of(field.field)

	/**
	 * Refers to a Kotlin [value] within an [aggregation value][AggregationOperators].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val age: Int,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::age) lt of(15)
	 *     }
	 * }
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun <Result> of(value: Result): Value<Any, Result> =
		LiteralValue(value, context)

	/**
	 * Refers to a [BsonType] within an [aggregation value][AggregationOperators].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val age: Int,
	 * )
	 *
	 * val publishedBeforeAcceptance = products.find {
	 *     expr {
	 *         of(Product::age).type eq of(BsonType.Int32)
	 *     }
	 * }
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun of(value: BsonType): Value<Any, BsonType> =
		BsonTypeValue(value, context)

	/**
	 * Refers to [field] as a nested field of the current value.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 * )
	 *
	 * class Data(
	 *     val users: List<User>,
	 *     val userNames: List<Int>,
	 * )
	 *
	 * data.aggregate()
	 *     .set {
	 *         Data::userNames set Data::users.map { it / User::name }
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/getField/)
	 */
	@OptIn(LowLevelApi::class)
	operator fun <Context: Any, Root, Child> Value<Context, Root>.div(field: Field<Root, Child>): Value<Context, Child> =
		GetFieldValue(this, field.path, context)

	/**
	 * Refers to [field] as a nested field of the current value.
	 *
	 * ### Examples
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 * )
	 *
	 * class Data(
	 *     val users: List<User>,
	 *     val userNames: List<Int>,
	 * )
	 *
	 * data.aggregate()
	 *     .set {
	 *         Data::userNames set Data::users.map { it / User::name }
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/getField/)
	 */
	operator fun <Context: Any, Root, Child> Value<Context, Root>.div(field: KProperty1<Root, Child>): Value<Context, Child> =
		this / field.field

}

@OptIn(LowLevelApi::class)
private class FieldValue<Context : Any, Result>(
	val field: Field<Context, Result>,
	context: BsonContext,
) : AbstractValue<Context, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeString("$$field")
	}
}

@OptIn(LowLevelApi::class)
private class LiteralValue<Result>(
	val value: Any?,
	context: BsonContext,
) : AbstractValue<Any, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeDocument {
			writeObjectSafe("\$literal", value)
		}
	}
}

@OptIn(LowLevelApi::class)
private class BsonTypeValue(
	val type: BsonType,
	context: BsonContext,
) : AbstractValue<Any, BsonType>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) {
		writer.writeDocument {
			writeInt32("\$literal", type.code)
		}
	}
}

@OptIn(LowLevelApi::class)
private class GetFieldValue<Context : Any, Result>(
	private val root: Value<Context, *>,
	private val child: Path,
	context: BsonContext,
) : AbstractValue<Context, Result>(context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeDocument {
			writeDocument("\$getField") {
				write("input") {
					root.writeTo(this)
				}

				writeString("field", child.toString())
			}
		}
	}

}
