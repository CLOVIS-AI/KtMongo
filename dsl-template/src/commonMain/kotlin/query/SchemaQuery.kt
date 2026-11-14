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
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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

	/**
	 * DSL to allow declaring new schema. For more information, see [SchemaQuery].
	 */
	@KtMongoDsl
	interface SchemaHostQuery<T> : CompoundBsonNode {

		/**
		 * Matches documents that satisfy the specified schema.
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
		 * To see the list of operators, read [SchemaQuery].
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema)
		 */
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

	/**
	 * Verifies that the value is one of the specified [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val type: String,
	 * )
	 *
	 * enum class UserType {
	 *     Admin,
	 *     Member,
	 *     Guest,
	 * }
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::type) {
	 *                 enum(Admin, Member, Guest)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.5.1)
	 */
	fun enum(values: List<T>, type: KType)

	/**
	 * Verifies that the value is one of the specified [values].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val type: String,
	 * )
	 *
	 * enum class UserType {
	 *     Admin,
	 *     Member,
	 *     Guest,
	 * }
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::type) {
	 *                 enum(Admin, Member, Guest)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.5.1)
	 */
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	final inline fun <reified E : T> enum(
		vararg values: E,
	) {
		enum(values.asList(), typeOf<E>())
	}

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

	/**
	 * Restricts this property's value to have at most [length] characters (inclusive).
	 *
	 * If the property does not contain a [BsonType.String], this constraint is ignored.
	 *
	 * [length] must be non-negative.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::name) {
	 *                 type(BsonType.String)
	 *                 maxLength(50)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.2.1)
	 */
	fun maxLength(length: Int)

	/**
	 * Restricts this property's value to have at least [length] characters (inclusive).
	 *
	 * If the property does not contain a [BsonType.String], this constraint is ignored.
	 *
	 * [length] must be non-negative.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::name) {
	 *                 type(BsonType.String)
	 *                 minLength(2)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.2.2)
	 */
	fun minLength(length: Int)

	/**
	 * Restricts this property's value to have exactly [length] characters.
	 *
	 * If the property does not contain a [BsonType.String], this constraint is ignored.
	 *
	 * [length] must be non-negative.
	 *
	 * This function is syntax sugar to set both [minLength] and [maxLength].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val postalCode: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::postalCode) {
	 *                 type(BsonType.String)
	 *                 length(5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 */
	fun length(length: Int) {
		minLength(length)
		maxLength(length)
	}

	/**
	 * Restricts this property's value to have a length compatible with [length].
	 *
	 * If the property does not contain a [BsonType.String], this constraint is ignored.
	 *
	 * This function is syntax sugar to set both [minLength] and [maxLength].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::name) {
	 *                 type(BsonType.String)
	 *                 length(2..50)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 */
	fun length(length: IntRange) {
		minLength(length.first)
		maxLength(length.last)
	}

	/**
	 * Restricts this property's value to have a length compatible with [length].
	 *
	 * If the property does not contain a [BsonType.String], this constraint is ignored.
	 *
	 * This function is syntax sugar to set both [minLength] and [maxLength].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::name) {
	 *                 type(BsonType.String)
	 *                 length(2..50)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 */
	fun length(length: UIntRange) {
		minLength(length.first.toInt())
		maxLength(length.last.toInt())
	}

	/**
	 * Restricts this property's value to match the regular expression [pattern], in the `ECMA 262` dialect.
	 *
	 * If the property does not contain a [String], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val email: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::email) {
	 *                 type(BsonType.String)
	 *                 pattern("^.+@.+\\..+$")
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.2.3)
	 */
	fun pattern(@Language("JSRegexp") pattern: String)

	// endregion
	// region Arrays

	/**
	 * Restricts this property's array items to conform to [schema].
	 *
	 * Using this method, all items of the array must confirm to the specified schema.
	 * To specify different schema for the different items, see [itemsInOrder].
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 items<String>("Tag") {
	 *                     type(BsonType.String)
	 *                     maxLength(20)
	 *                 }
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.1)
	 */
	fun <I> items(title: String, schema: SchemaQuery<I>.() -> Unit)

	/**
	 * Operators available in [itemsInOrder].
	 */
	@KtMongoDsl
	interface SchemaHostArrayQuery<T> : SchemaHostQuery<T> {

		/**
		 * Specifies the behavior of [itemsInOrder] when more items are found than declared in the schema.
		 *
		 * If this function is not called, additional items beyond the declared schemas are forbidden.
		 *
		 * If this function is called, additional items beyond the declared schemas are allowed if they confirm to the [schema].
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class Coordinates(
		 *     val _id: ObjectId,
		 *     val values: List<Double>,
		 * )
		 *
		 * coordinates.find {
		 *     schema("Coordinates") {
		 *         properties {
		 *             property(Coordinates::_id) {
		 *                 type(BsonType.ObjectId)
		 *             }
		 *
		 *             property(Coordinates::values) {
		 *                 type(BsonType.Array)
		 *                 itemsInOrder<Double> {
		 *                     // The first item must be a Double
		 *                     schema("Latitude") {
		 *                         type(BsonType.Double)
		 *                     }
		 *
		 *                     // The second item must be a Double
		 *                     schema("Longitude") {
		 *                         type(BsonType.Double)
		 *                     }
		 *
		 *                     // If there are other items, they must also be a Double
		 *                     additionalItems("Altitude") {
		 *                         type(BsonType.Double)
		 *                     }
		 *                 }
		 *             }
		 *         }
		 *     }
		 * }
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
		 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.1)
		 */
		fun additionalItems(title: String, schema: SchemaQuery<T>.() -> Unit)
	}

	/**
	 * Restricts this property's array items to conform to the given sequence of [schemas].
	 *
	 * The first item in the array must match the first declared schema, the second item must match the second declared schema, etc.
	 *
	 * By default, if the array contains more items than declared schemas, validation fails.
	 * To allow additional items, declare an [additionalItems][SchemaHostArrayQuery.additionalItems] schema inside [schemas].
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Coordinates(
	 *     val _id: ObjectId,
	 *     val values: List<Double>,
	 * )
	 *
	 * coordinates.find {
	 *     schema("Coordinates") {
	 *         properties {
	 *             property(Coordinates::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(Coordinates::values) {
	 *                 type(BsonType.Array)
	 *                 itemsInOrder<Double> {
	 *                     schema("Latitude") {
	 *                         type(BsonType.Double)
	 *                     }
	 *                     schema("Longitude") {
	 *                         type(BsonType.Double)
	 *                     }
	 *                 }
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.1)
	 */
	fun <I> itemsInOrder(schemas: SchemaHostArrayQuery<I>.() -> Unit)

	/**
	 * Restricts this property's array value to have at most [size] items (inclusive).
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * [size] must be non-negative.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 maxItems(5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.2)
	 */
	fun maxItems(size: Int)

	/**
	 * Restricts this property's array value to have at least [size] items (inclusive).
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * [size] must be non-negative.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 minItems(1)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.3)
	 */
	fun minItems(size: Int)

	/**
	 * Restricts this property's array value to have exactly [size] items.
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * [size] must be non-negative.
	 *
	 * This function is syntax sugar to set both [minItems] and [maxItems].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 size(5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.2)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.3)
	 */
	fun size(size: Int) {
		minItems(size)
		maxItems(size)
	}

	/**
	 * Restricts this property's array value to have between `size.first` and `size.last` items (inclusive).
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * The range boundaries must be non-negative.
	 *
	 * This function is syntax sugar to set both [minItems] and [maxItems].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 size(1..5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.2)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.3)
	 */
	fun size(size: IntRange) {
		minItems(size.first)
		maxItems(size.last)
	}

	/**
	 * Restricts this property's array value to have between `size.first` and `size.last` items (inclusive).
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * This function is syntax sugar to set both [minItems] and [maxItems].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 size(1..5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.2)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.3)
	 */
	fun size(size: UIntRange) {
		minItems(size.first.toInt())
		maxItems(size.last.toInt())
	}

	/**
	 * Restricts this property's array value to only contain unique items.
	 *
	 * If the property does not contain a [BsonType.Array], this constraint is ignored.
	 *
	 * If [unique] is `false`, duplicate items are allowed.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val tags: List<String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::tags) {
	 *                 type(BsonType.Array)
	 *                 uniqueItems()
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.3.4)
	 */
	fun uniqueItems(unique: Boolean = true)

	// endregion
	// region Objects

	/**
	 * Restricts this property's object value to have at most [count] properties (inclusive).
	 *
	 * If the property does not contain a [BsonType.Document], this constraint is ignored.
	 *
	 * [count] must be non-negative.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val extraInfo: Map<String, String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::extraInfo) {
	 *                 type(BsonType.Document)
	 *                 maxProperties(5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.1)
	 */
	fun maxProperties(count: Int)

	/**
	 * Restricts this property's object value to have at least [count] properties (inclusive).
	 *
	 * If the property does not contain a [BsonType.Document], this constraint is ignored.
	 *
	 * [count] must be non-negative.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val extraInfo: Map<String, String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::extraInfo) {
	 *                 type(BsonType.Document)
	 *                 minProperties(1)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.2)
	 */
	fun minProperties(count: Int)

	/**
	 * Restricts this property's object value to have exactly [count] properties.
	 *
	 * If the property does not contain a [BsonType.Document], this constraint is ignored.
	 *
	 * [count] must be non-negative.
	 *
	 * This function is syntax sugar to set both [minProperties] and [maxProperties].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val extraInfo: Map<String, String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::extraInfo) {
	 *                 type(BsonType.Document)
	 *                 properties(5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.1)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.2)
	 */
	fun properties(count: Int) {
		minProperties(count)
		maxProperties(count)
	}

	/**
	 * Restricts this property's object value to have between `count.first` and `count.last` properties (inclusive).
	 *
	 * If the property does not contain a [BsonType.Document], this constraint is ignored.
	 *
	 * [count] must be non-negative.
	 *
	 * This function is syntax sugar to set both [minProperties] and [maxProperties].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val extraInfo: Map<String, String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::extraInfo) {
	 *                 type(BsonType.Document)
	 *                 properties(1..5)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.1)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.2)
	 */
	fun properties(count: IntRange) {
		minProperties(count.first)
		maxProperties(count.last)
	}

	/**
	 * Restricts this property's object value to have between `count.first` and `count.last` properties (inclusive).
	 *
	 * If the property does not contain a [BsonType.Document], this constraint is ignored.
	 *
	 * This function is syntax sugar to set both [minProperties] and [maxProperties].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val extraInfo: Map<String, String>,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *
	 *             property(User::extraInfo) {
	 *                 type(BsonType.Document)
	 *                 properties(1u..5u)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.1)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.2)
	 */
	fun properties(count: UIntRange) {
		minProperties(count.first.toInt())
		maxProperties(count.last.toInt())
	}

	@KtMongoDsl
	interface SchemaPropertiesQuery<T> : CompoundBsonNode, FieldDsl {

		/**
		 * Declares constraints on a property named [name].
		 *
		 * If [required] is `true`, the property must be present in the document.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val _id: ObjectId,
		 *     val name: String,
		 * )
		 *
		 * users.find {
		 *     schema("User") {
		 *         properties {
		 *             property("name") {
		 *                 type(BsonType.String)
		 *             }
		 *         }
		 *     }
		 * }
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
		 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.4)
		 */
		@IgnorableReturnValue
		fun <E> property(
			name: String,
			required: Boolean = true,
			schema: SchemaPropertyQuery<T, E>.() -> Unit,
		): Property<T>

		/**
		 * Declares constraints on a property represented by [field].
		 *
		 * If [required] is `true`, the property must be present in the document.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val _id: ObjectId,
		 *     val name: String,
		 * )
		 *
		 * users.find {
		 *     schema("User") {
		 *         properties {
		 *             property(User::name) {
		 *                 type(BsonType.String)
		 *             }
		 *         }
		 *     }
		 * }
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
		 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.4)
		 */
		@IgnorableReturnValue
		@OptIn(LowLevelApi::class)
		fun <E> property(
			field: Field<T, E>,
			required: Boolean = true,
			schema: SchemaPropertyQuery<T, E>.() -> Unit,
		): Property<T> {
			val name = (field.path.segment as? PathSegment.Field)?.name
			requireNotNull(name) { "\$jsonSchema only supports textual fields, found: $field" }

			return property(name, required, schema)
		}

		/**
		 * Declares constraints on a property represented by [field].
		 *
		 * If [required] is `true`, the property must be present in the document.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * class User(
		 *     val _id: ObjectId,
		 *     val name: String,
		 * )
		 *
		 * users.find {
		 *     schema("User") {
		 *         properties {
		 *             property(User::name) {
		 *                 type(BsonType.String)
		 *             }
		 *         }
		 *     }
		 * }
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
		 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.4)
		 */
		@IgnorableReturnValue
		fun <E> property(
			field: KProperty1<T, E>,
			required: Boolean = true,
			schema: SchemaPropertyQuery<T, E>.() -> Unit,
		): Property<T> {
			return property(field.field, required, schema)
		}

		/**
		 * Declares constraints on any property whose name matches the regular expression [pattern].
		 *
		 * [pattern] must follow the `ECMA 262` regular expression dialect.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * users.find {
		 *     schema("User") {
		 *         properties {
		 *             properties("^str_.*$") {
		 *                 type(BsonType.String)
		 *             }
		 *         }
		 *     }
		 * }
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
		 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.4)
		 */
		fun <E> properties(
			@Language("JSRegexp") pattern: String,
			schema: SchemaQuery<E>.() -> Unit,
		)

		/**
		 * Declares constraints on any property not explicitly declared with [property] or [properties].
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * users.find {
		 *     schema("User") {
		 *         properties {
		 *             property("name") {
		 *                 type(BsonType.String)
		 *             }
		 *             additionalProperties("Extra") {
		 *                 type(BsonType.String)
		 *             }
		 *         }
		 *     }
		 * }
		 * ```
		 *
		 * ### External resources
		 *
		 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
		 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.4)
		 */
		fun additionalProperties(
			title: String,
			schema: SchemaQuery<*>.() -> Unit,
		)

		/**
		 * Operators available within [SchemaQuery.SchemaPropertiesQuery.property].
		 */
		@KtMongoDsl
		interface SchemaPropertyQuery<R, T> : SchemaQuery<T> {

			/**
			 * Declares that if this property is present in the document, then the given [property] must also be present.
			 *
			 * ### Example
			 *
			 * ```kotlin
			 * class User(
			 *     val _id: ObjectId,
			 *     val shippingAddress: Address?,
			 *     val billingAddress: Address?,
			 * )
			 *
			 * users.find {
			 *     schema("User") {
			 *         properties {
			 *             val billing = property(User::billingAddress, required = false) {
			 *                 type(BsonType.Document)
			 *             }
			 *             property(User::shippingAddress, required = false) {
			 *                 type(BsonType.Document)
			 *                 dependsOn(billing)
			 *             }
			 *         }
			 *     }
			 * }
			 * ```
			 *
			 * ### External resources
			 *
			 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
			 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.5)
			 */
			fun dependsOn(property: Property<R>)

			/**
			 * Declares that if this property is present in the document, then the property named [property] must also be present.
			 *
			 * ### Example
			 *
			 * ```kotlin
			 * class User(
			 *     val _id: ObjectId,
			 *     val shippingAddress: Address?,
			 *     val billingAddress: Address?,
			 * )
			 *
			 * users.find {
			 *     schema("User") {
			 *         properties {
			 *             property(User::billingAddress, required = false) {
			 *                 type(BsonType.Document)
			 *             }
			 *             property(User::shippingAddress, required = false) {
			 *                 type(BsonType.Document)
			 *                 dependsOn("billingAddress")
			 *             }
			 *         }
			 *     }
			 * }
			 * ```
			 *
			 * ### External resources
			 *
			 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
			 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.5)
			 */
			fun dependsOn(property: String)

			/**
			 * Declares that if this property is present in the document, then the document must also satisfy the schema given by [schema].
			 *
			 * ### Example
			 *
			 * ```kotlin
			 * class User(
			 *     val _id: ObjectId,
			 *     val creditCard: String?,
			 *     val billingAddress: Address?,
			 * )
			 *
			 * users.find {
			 *     schema("User") {
			 *         properties {
			 *             property(User::creditCard, required = false) {
			 *                 type(BsonType.String)
			 *                 dependsOn {
			 *                     properties {
			 *                         property(User::billingAddress) {
			 *                             type(BsonType.Document)
			 *                         }
			 *                     }
			 *                 }
			 *             }
			 *         }
			 *     }
			 * }
			 * ```
			 *
			 * ### External resources
			 *
			 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
			 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.5)
			 */
			fun dependsOn(schema: SchemaQuery<R>.() -> Unit)

		}

		/**
		 * Allows specifying additional configuration on this property.
		 *
		 * See [SchemaPropertyQuery.dependsOn].
		 */
		@KtMongoDsl
		sealed interface Property<R>
	}

	/**
	 * Declares constraints on the properties of this object.
	 *
	 * If the property does not contain a [BsonType.Document], this constraint is ignored.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * users.find {
	 *     schema("User") {
	 *         properties {
	 *             property(User::_id) {
	 *                 type(BsonType.ObjectId)
	 *             }
	 *             property(User::name) {
	 *                 type(BsonType.String)
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.4.4)
	 */
	fun properties(block: SchemaPropertiesQuery<T>.() -> Unit)

	// endregion
	// region Combinators

	/**
	 * Matches documents that do not satisfy the specified schema.
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
	 *         not("NotYoung") {
	 *             properties {
	 *                 property(User::age) {
	 *                     maximum(18)
	 *                 }
	 *             }
	 *         }
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#json-schema)
	 * - [RFC](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00#section-5.5.6)
	 */
	fun not(
		title: String,
		block: SchemaQuery<T>.() -> Unit,
	)

	// endregion
}
