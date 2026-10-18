/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.types.ExperimentalBsonSchemaApi
import opensavvy.ktmongo.bson.types.Schema
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.PathSegment
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import org.intellij.lang.annotations.Language
import kotlin.reflect.KProperty1

/**
 * DSL for the [`$jsonSchema` operator][FilterQuery.schema].
 *
 * ### Example
 *
 * Find all documents that can be successfully deserialized:
 *
 * ```kotlin
 * class City(
 *     val _id: ObjectId,
 *     val name: String,
 *     val countryCode: String,
 *     val popularity: Int? = null,
 * )
 *
 * collection.find {
 *     schema("City") {
 *         type(BsonType.Document)
 *         properties {
 *             property(City::_id) {
 *                 type(BsonType.ObjectId)
 *             }
 *
 *             property(City::name) {
 *                 type(BsonType.String)
 *             }
 *
 *             property(City::countryCode) {
 *                 type(BsonType.String)
 *                 length(2)
 *             }
 *
 *             property(City::popularity, required = false) {
 *                 type(BsonType.Int32)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ### Operators
 *
 * Documentation:
 * - [`title`][FilterQuery.schema]
 * - [`description`][description]
 *
 * General operators:
 * - [`bsonType`][type]
 * - [`enum`][enum]
 *
 * Numeric operators:
 * - [`multipleOf`][multipleOf]
 * - [`maximum`][maximum]
 * - [`exclusiveMaximum`][maximum]
 * - [`minimum`][minimum]
 * - [`exclusiveMinimum`][minimum]
 *
 * String operators:
 * - [`maxLength`][maxLength]
 * - [`minLength`][minLength]
 * - [`pattern`][pattern]
 *
 * Array operators:
 * - [`items`][items]
 * - [`additionalItems`][SchemaHostArrayQuery.additionalItems]
 * - [`maxItems`][maxItems]
 * - [`minItems`][minItems]
 * - [`uniqueItems`][uniqueItems]
 *
 * Document operators:
 * - [`maxProperties`][maxProperties]
 * - [`minProperties`][minProperties]
 * - [`required`][SchemaPropertiesQuery.property]
 * - [`additionalProperties`][SchemaPropertiesQuery.additionalProperties]
 * - [`properties`][SchemaPropertiesQuery.property]
 * - [`patternProperties`][SchemaPropertiesQuery.properties]
 * - [`dependencies`][SchemaPropertiesQuery.Property.dependsOn]
 *
 * Combination operators:
 * - [`allOf`][allOf]
 * - [`anyOf`][anyOf]
 * - [`oneOf`][oneOf]
 * - [`not`][not]
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/)
 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00)
 */
@KtMongoDsl
interface SchemaQuery<T> : CompoundBsonNode, FieldDsl {

	/**
	 * Adds a new [constraint] to this schema.
	 *
	 * We recommend against calling this function directly.
	 * Instead, you should find other functions declared on this object (possibly as extensions) that perform the
	 * operation you want safely.
	 */
	@LowLevelApi
	@ExperimentalBsonSchemaApi
	fun accept(constraint: Schema.Constraint<T>)

	@KtMongoDsl
	interface SchemaHostQuery<T> : CompoundBsonNode {

		fun schema(title: String, block: SchemaQuery<T>.() -> Unit)

	}

	// region Generic

	/**
	 * A value of one of the numeric types: [BsonType.Int32], [BsonType.Int64], [BsonType.Double] or [BsonType.Decimal128].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Number)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * @see type Specify a constraint on a value's type.
	 */
	object Number

	/**
	 * A value of one of the numeric types: [BsonType.Int32], [BsonType.Int64], [BsonType.Double] or [BsonType.Decimal128].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Number)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * @see type Specify a constraint on a value's type.
	 */
	@Suppress("PropertyName")
	val BsonType.Companion.Number: Number
		get() = SchemaQuery.Number

	/**
	 * Verifies that the value has type [bsonType].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.5.2)
	 */
	fun type(bsonType: BsonType)

	/**
	 * Verifies that the value has one of the types [BsonType.Int32], [BsonType.Int64], [BsonType.Double] or [BsonType.Decimal128].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Number)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.5.2)
	 */
	fun type(bsonType: Number)

	fun enum(values: List<T>)

	/**
	 * Provides a human-readable description of this schema.
	 *
	 * This operator is purely documentation, it does not impact the query.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         description("Information about users")
	 *
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 description("The age of the user")
	 *                 type(BsonType.Number)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-6.1)
	 */
	fun description(description: String)

	// endregion
	// region Numeric

	/**
	 * Restricts this property's value to multiples of [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * [number] must be strictly positive.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val _id: ObjectId,
	 *     val quantityInPack: Int,
	 * )
	 *
	 * products.find {
	 *     schema("Product") {
	 *         properties {
	 *             property(Product::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(Product::quantityInPack) {
	 *                 type(BsonType.Int32)
	 *                 multipleOf(6)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.1)
	 */
	fun multipleOf(number: Int)

	/**
	 * Restricts this property's value to multiples of [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * [number] must be strictly positive.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val _id: ObjectId,
	 *     val quantityInPack: Int,
	 * )
	 *
	 * products.find {
	 *     schema("Product") {
	 *         properties {
	 *             property(Product::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(Product::quantityInPack) {
	 *                 type(BsonType.Int32)
	 *                 multipleOf(6)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.1)
	 */
	fun multipleOf(number: Long)

	/**
	 * Restricts this property's value to multiples of [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * [number] must be strictly positive.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Product(
	 *     val _id: ObjectId,
	 *     val quantityInPack: Int,
	 * )
	 *
	 * products.find {
	 *     schema("Product") {
	 *         properties {
	 *             property(Product::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(Product::quantityInPack) {
	 *                 type(BsonType.Int32)
	 *                 multipleOf(6)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.1)
	 */
	fun multipleOf(number: Double)

	/**
	 * Restricts this property's value to be less than [number].
	 *
	 * - If [exclusive] is `true`, the value must be strictly less than [number].
	 * - If [exclusive] is `false`, the value must be less than or equal to [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *                 maximum(120)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.2)
	 */
	fun maximum(number: Int, exclusive: Boolean = false)

	/**
	 * Restricts this property's value to be less than [number].
	 *
	 * - If [exclusive] is `true`, the value must be strictly less than [number].
	 * - If [exclusive] is `false`, the value must be less than or equal to [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *                 maximum(120)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.2)
	 */
	fun maximum(number: Long, exclusive: Boolean = false)

	/**
	 * Restricts this property's value to be less than [number].
	 *
	 * - If [exclusive] is `true`, the value must be strictly less than [number].
	 * - If [exclusive] is `false`, the value must be less than or equal to [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *                 maximum(120)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.2)
	 */
	fun maximum(number: Double, exclusive: Boolean = false)

	/**
	 * Restricts this property's value to be greater than [number].
	 *
	 * - If [exclusive] is `true`, the value must be strictly greater than [number].
	 * - If [exclusive] is `false`, the value must be greater than or equal to [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *                 minimum(0)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.3)
	 */
	fun minimum(number: Int, exclusive: Boolean = false)

	/**
	 * Restricts this property's value to be greater than [number].
	 *
	 * - If [exclusive] is `true`, the value must be strictly greater than [number].
	 * - If [exclusive] is `false`, the value must be greater than or equal to [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *                 minimum(0)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.3)
	 */
	fun minimum(number: Long, exclusive: Boolean = false)

	/**
	 * Restricts this property's value to be greater than [number].
	 *
	 * - If [exclusive] is `true`, the value must be strictly greater than [number].
	 * - If [exclusive] is `false`, the value must be greater than or equal to [number].
	 *
	 * If the property does not contain a [Number], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val age: Int,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::age) {
	 *                 type(BsonType.Int32)
	 *                 minimum(0)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.1.3)
	 */
	fun minimum(number: Double, exclusive: Boolean = false)

	// endregion
	// region Strings

	fun maxLength(length: Int)

	fun minLength(length: Int)

	fun length(length: Int) {
		minLength(length)
		maxLength(length)
	}

	fun length(length: IntRange) {
		minLength(length.first)
		maxLength(length.last)
	}

	fun length(length: UIntRange) {
		minLength(length.first.toInt())
		maxLength(length.last.toInt())
	}

	fun pattern(@Language("JSRegexp") pattern: String)

	// endregion
	// region Arrays

	fun <I> items(title: String, schema: SchemaQuery<I>.() -> Unit)

	@KtMongoDsl
	interface SchemaHostArrayQuery<T> : SchemaHostQuery<T> {

		fun additionalItems(title: String, schema: SchemaQuery<T>.() -> Unit)
	}

	fun <I> itemsInOrder(schemas: SchemaHostArrayQuery<I>.() -> Unit)

	fun maxItems(size: Int)

	fun minItems(size: Int)

	fun uniqueItems(boolean: Boolean)

	// endregion
	// region Objects

	fun maxProperties(count: Int)

	fun minProperties(count: Int)

	@KtMongoDsl
	interface SchemaPropertiesQuery<T> : CompoundBsonNode, FieldDsl {

		@IgnorableReturnValue
		fun <E> property(
			name: String,
			required: Boolean = true,
			schema: SchemaQuery<E>.() -> Unit,
		): Property<T>

		@IgnorableReturnValue
		@OptIn(LowLevelApi::class)
		fun <E> property(
			field: Field<T, E>,
			required: Boolean = true,
			schema: SchemaQuery<E>.() -> Unit,
		): Property<T> {
			val name = (field.path.segment as? PathSegment.Field)?.name
			requireNotNull(name) { $$"$jsonSchema only supports textual fields, found: $$field" }

			return property(name, required, schema)
		}

		@IgnorableReturnValue
		fun <E> property(
			field: KProperty1<T, E>,
			required: Boolean = true,
			schema: SchemaQuery<E>.() -> Unit,
		): Property<T> {
			return property(field.field, required, schema)
		}

		fun <E> properties(
			@Language("JSRegexp") pattern: String,
			schema: SchemaQuery<E>.() -> Unit,
		)

		fun additionalProperties(
			schema: SchemaQuery<*>.() -> Unit,
		)

		@KtMongoDsl
		interface Property<R> : CompoundBsonNode, FieldDsl {

			fun dependsOn(property: Property<R>)

			fun dependsOn(property: String)

			fun dependsOn(schema: SchemaQuery<R>.() -> Unit)

		}
	}

	fun properties(block: SchemaPropertiesQuery<T>.() -> Unit)

	// endregion
	// region Combinators

	fun allOf(
		block: SchemaHostQuery<T>.() -> Unit,
	)

	fun anyOf(
		block: SchemaHostQuery<T>.() -> Unit,
	)

	fun oneOf(
		block: SchemaHostQuery<T>.() -> Unit,
	)

	fun not(
		title: String,
		block: SchemaQuery<T>.() -> Unit,
	)

	// endregion
}
