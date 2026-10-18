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

package opensavvy.ktmongo.bson.types

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.dsl.LowLevelApi
import org.intellij.lang.annotations.Language
import kotlin.reflect.KType

@RequiresOptIn("This API is part of the experimental JsonSchema implementation. Please provide feedback in https://gitlab.com/opensavvy/ktmongo/-/work_items/333")
annotation class ExperimentalBsonSchemaApi

/**
 * Marker of the builder DSL for [Schema].
 */
@DslMarker
annotation class SchemaDsl

/**
 * A schema for a BSON document.
 *
 * A schema specifies which fields are allowed, their type, and constraints on their values.
 *
 * Creating an instance of a schema is much more convenient using the facilities provided in the `:dsl` module.
 *
 * To create a schema that accepts any value, use [Schema.acceptAll].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/query/jsonschema/#std-label-jsonSchema-keywords)
 * - [Keywords](https://datatracker.ietf.org/doc/html/draft-fge-json-schema-validation-00)
 */
@ExperimentalBsonSchemaApi
class Schema<T> internal constructor(
	/**
	 * The title of the schema, used to differentiate it from other schemas.
	 */
	val title: String,

	/**
	 * The different properties this document should have.
	 */
	val constraints: List<Constraint<*>>,
) : BsonFieldWriteable {

	@LowLevelApi
	override fun writeTo(writer: BsonFieldWriter) = with(writer) {
		writeString("title", title)

		for (constraint in constraints) {
			constraint.writeTo(this)
		}
	}

	override fun toString(): String {
		return super.toString() // TODO
	}

	/**
	 * A constraint on a [BsonValue].
	 *
	 * @param T The type of the value this constraint applies to.
	 */
	sealed interface Constraint<in T> : BsonFieldWriteable {

		/**
		 * JSON representation of this constraint object.
		 */
		override fun toString(): String
	}

	/**
	 * Helper to create instances of [Constraint] that handle multiple constraints.
	 */
	abstract class ComplexConstraint<in T>(
		private val factory: BsonFactory,
	) : Constraint<T> {

		@OptIn(LowLevelApi::class)
		final override fun toString(): String =
			factory.buildDocument { writeTo(this) }.toString()
	}

	/**
	 * Helper to create instances of [Constraint] that handle a single constraint named [name].
	 */
	abstract class SimpleConstraint<in T>(
		/**
		 * The name of this constraint.
		 */
		val name: String,
		factory: BsonFactory,
	) : ComplexConstraint<T>(factory) {

		@OptIn(LowLevelApi::class)
		protected abstract fun BsonValueWriter.write()

		@OptIn(LowLevelApi::class)
		final override fun writeTo(writer: BsonFieldWriter) = with(writer) {
			write(name) {
				write()
			}
		}
	}

	// region Generic

	/**
	 * This property must have a value of the given [bsonType].
	 */
	class Type(
		val bsonType: BsonType,
		factory: BsonFactory,
	) : SimpleConstraint<Any?>("bsonType", factory) {
		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeString(bsonType.alias)
		}
	}

	/**
	 * This property must have a numeric value ([BsonType.Double], [BsonType.Int32], [BsonType.Int64] or [BsonType.Decimal128]).
	 */
	class TypeNumber(
		factory: BsonFactory,
	) : SimpleConstraint<Any?>("bsonType", factory) {
		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeString("number")
		}
	}

	/**
	 * This property must have a value that is equal to one of the given [values].
	 */
	class Enum<T>(
		val values: List<Pair<T, KType>>,
		factory: BsonFactory,
	) : SimpleConstraint<T>("enum", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeArray {
				for ((value, type) in values) {
					writeSafe(value, type)
				}
			}
		}
	}

	// endregion
	// region Numeric

	/**
	 * This property must have a value that, when divided by [number], is an integer.
	 *
	 * If the property does not contain a number, this constraint is ignored.
	 */
	class MultipleOf(
		val number: Number,
		val numberType: KType,
		factory: BsonFactory,
	) : SimpleConstraint<Number>("multipleOf", factory) {
		init {
			require(number.toDouble() > 0.0) { "$name requires a non-zero positive number, found: $number" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeSafe(number, numberType)
		}
	}

	/**
	 * This property must have a value less than or equal to [number].
	 *
	 * If the property does not contain a number, this constraint is ignored.
	 */
	class Maximum(
		val number: Number,
		val numberType: KType,
		val exclusive: Boolean,
		factory: BsonFactory,
	) : ComplexConstraint<Number>(factory) {

		@OptIn(LowLevelApi::class)
		override fun writeTo(writer: BsonFieldWriter) = with(writer) {
			writeSafe("maximum", number, numberType)
			writeBoolean("exclusiveMaximum", exclusive)
		}
	}

	/**
	 * This property must have a value greater than or equal to [number].
	 *
	 * If the property does not contain a number, this constraint is ignored.
	 */
	class Minimum(
		val number: Number,
		val numberType: KType,
		val exclusive: Boolean,
		factory: BsonFactory,
	) : ComplexConstraint<Number>(factory) {

		@OptIn(LowLevelApi::class)
		override fun writeTo(writer: BsonFieldWriter) = with(writer) {
			writeSafe("minimum", number, numberType)
			writeBoolean("exclusiveMinimum", exclusive)
		}
	}

	// endregion
	// region Strings

	/**
	 * This property must have a value with a length less than or equal to [length].
	 *
	 * If the property does not contain a string, this constraint is ignored.
	 */
	class MaxLength(
		val length: Int, // Int because 2 147 000 000 is larger than 16 MiB so longer strings can never appear
		factory: BsonFactory,
	) : SimpleConstraint<String>("maxLength", factory) {
		init {
			require(length >= 0) { "$name must be non-negative, found: $length" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeInt32(length)
		}
	}

	/**
	 * This property must have a value with a length greater than or equal to [length].
	 *
	 * If the property does not contain a string, this constraint is ignored.
	 */
	class MinLength(
		val length: Int,
		factory: BsonFactory,
	) : SimpleConstraint<String>("minLength", factory) {
		init {
			require(length >= 0) { "$name must be non-negative, found: $length" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeInt32(length)
		}
	}

	/**
	 * This property must have a value that matches the regular expression [pattern], in the `ECMA 262` dialect.
	 *
	 * If the property does not contain a string, this constraint is ignored.
	 */
	class Pattern(
		@param:Language("JSRegexp")
		val pattern: String,
		factory: BsonFactory,
	) : SimpleConstraint<String>("pattern", factory) {
		init {
			require(pattern.isNotEmpty()) { "$name must not be empty, found: $pattern" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeString(pattern)
		}
	}

	// endregion
	// region Arrays

	/**
	 * This property must have items following the specified configuration.
	 *
	 * If the property does not contain an array, this constraint is ignored.
	 */
	class ArrayItems<in T>(
		/**
		 * Specifies constraints on the items of the array.
		 *
		 * - [Homogenous]: all items must have the same shape.
		 * - [Heterogeneous]: items can have different shapes.
		 */
		val items: ArrayItemsConfiguration<T>,
		factory: BsonFactory,
	) : ComplexConstraint<Collection<T>>(factory) {

		@LowLevelApi
		override fun writeTo(writer: BsonFieldWriter) =
			items.writeTo(writer)

		sealed class ArrayItemsConfiguration<in T>(
			private val factory: BsonFactory,
		) : BsonFieldWriteable {

			@OptIn(LowLevelApi::class)
			override fun toString(): String =
				factory.buildDocument { writeTo(this) }.toString()
		}

		/**
		 * Pass this value to [ArrayItems] to ensure that all items in an array conform to [schema].
		 */
		class Homogenous<T>(
			val schema: Schema<T>,
			factory: BsonFactory,
		) : ArrayItemsConfiguration<T>(factory) {

			@LowLevelApi
			override fun writeTo(writer: BsonFieldWriter) = with(writer) {
				writeDocument("items") {
					schema.writeTo(this)
				}
			}
		}

		/**
		 * Pass this value to [ArrayItems] to specify that each item has its own schema.
		 */
		class Heterogeneous<T>(
			/**
			 * Defines the schema of the different items in this array.
			 *
			 * - The first item in the array must match the schema defined at index 0.
			 * - The second item in the array must match the schema defined at index 1.
			 * - The third item in the array must match the schema defined at index 2.
			 * - Etc.
			 *
			 * If there are more items in the array than there are schemas, the validation is controlled by the [additionalItems] field.
			 *
			 * If there are fewer items in the array than there are schemas, the validation succeeds.
			 *
			 * Must not be empty.
			 */
			val items: List<Schema<T>>,

			/**
			 * If there are more items in the array than in [items], this property controls whether validation succeeds.
			 *
			 * If [additionalItems] is `null`, validation fails: the array must contain the exact same number of items as [items].
			 *
			 * If [additionalItems] is not `null`, all additional items are validated against this schema.
			 */
			val additionalItems: Schema<T>?,
			factory: BsonFactory,
		) : ArrayItemsConfiguration<T>(factory) {
			init {
				require(items.isNotEmpty()) { "items must not be empty: $items" }
			}

			@LowLevelApi
			override fun writeTo(writer: BsonFieldWriter) = with(writer) {
				writeArray("items") {
					for (schema in items) {
						writeDocument {
							schema.writeTo(this)
						}
					}
				}

				if (additionalItems != null) {
					writeDocument("additionalItems") {
						additionalItems.writeTo(this)
					}
				} else {
					writeBoolean("additionalItems", false)
				} // no need for 'writeBoolean("additionalItems", true)' because it's the same behavior as the non-null branch with an empty schema
			}
		}
	}

	/**
	 * The maximum number of items in an array (inclusive).
	 *
	 * If the property does not contain an array, this constraint is ignored.
	 */
	class MaxItems(
		val size: Int,
		factory: BsonFactory,
	) : SimpleConstraint<Collection<*>>("maxItems", factory) {
		init {
			require(size >= 0) { "$name must be non-negative, found: $size" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeInt32(size)
		}
	}

	/**
	 * The minimum number of items in an array (inclusive).
	 *
	 * If the property does not contain an array, this constraint is ignored.
	 */
	class MinItems(
		val size: Int,
		factory: BsonFactory,
	) : SimpleConstraint<Collection<*>>("minItems", factory) {
		init {
			require(size >= 0) { "$name must be non-negative, found: $size" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeInt32(size)
		}
	}

	/**
	 * Whether the array must contain unique items.
	 *
	 * If the property does not contain an array, this constraint is ignored.
	 */
	class UniqueItems(
		val unique: Boolean,
		factory: BsonFactory,
	) : SimpleConstraint<Collection<*>>("uniqueItems", factory) {

		constructor(factory: BsonFactory) : this(true, factory)

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeBoolean(unique)
		}
	}

	// endregion
	// region Objects

	/**
	 * The maximum number of properties in an object (inclusive).
	 *
	 * If the property does not contain an object, this constraint is ignored.
	 */
	class MaxProperties(
		val count: Int,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("maxProperties", factory) {
		init {
			require(count >= 0) { "$name must be non-negative, found: $count" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeInt32(count)
		}
	}

	/**
	 * The minimum number of properties in an object (inclusive).
	 *
	 * If the property does not contain an object, this constraint is ignored.
	 */
	class MinProperties(
		val count: Int,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("minProperties", factory) {
		init {
			require(count >= 0) { "$name must be non-negative, found: $count" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeInt32(count)
		}
	}

	/**
	 * The properties that must be present in an object.
	 *
	 * If the property does not contain an object, this constraint is ignored.
	 */
	class Required(
		val properties: Set<String>,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("required", factory) {
		init {
			require(properties.isNotEmpty()) { "$name must not be empty, found: $properties" }
		}

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeArray {
				for (property in properties) {
					writeString(property)
				}
			}
		}
	}

	/**
	 * The properties that must be present in an object.
	 *
	 * Each key of [properties] is the name of a property of the object; its value must follow the associated schema.
	 *
	 * If this property does not contain an object, this constraint is ignored.
	 *
	 * If the property [AdditionalProperties] is not present, this constraint is ignored.
	 */
	class Properties(
		val properties: Map<String, Schema<*>>,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("properties", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeDocument {
				for ((property, schema) in properties) {
					writeDocument(property) {
						schema.writeTo(this)
					}
				}
			}
		}
	}

	/**
	 * The properties that must be present in an object, declared by pattern.
	 *
	 * Each key of [properties] is a regular expression of the `ECMA 262` dialect.
	 * Any field in the object that matches the pattern must have a value that follows the associated schema.
	 *
	 * If this property does not contain an object, this constraint is ignored.
	 *
	 * If the property [AdditionalProperties] is not present, this constraint is ignored.
	 */
	class PatternProperties(
		val properties: Map<String, Schema<*>>,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("patternProperties", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeDocument {
				for ((property, schema) in properties) {
					writeDocument(property) {
						schema.writeTo(this)
					}
				}
			}
		}
	}

	/**
	 * Specifies the behavior of [Properties] and [PatternProperties].
	 *
	 * If this constraint is not present, any properties are allowed.
	 *
	 * If this constraint is present and set to `null`, all properties must either:
	 * - Have a name present in [Properties], and follow the associated schema.
	 * - Match a pattern present in [PatternProperties], and follow the associated schema.
	 *
	 * If this constraint is present and set to a schema, all properties must either:
	 * - Be valid according to rules of the `null` case.
	 * - Match the [schema].
	 */
	class AdditionalProperties(
		val schema: Schema<*>?,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("additionalProperties", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			if (schema != null) {
				writeDocument {
					schema.writeTo(this)
				}
			} else {
				writeBoolean(false)
			}
		}
	}

	/**
	 * Properties that trigger additional constraints when present.
	 *
	 * Each key of [dependencies] is the name of a property.
	 * If the property is present, then the object must confirm to the associated dependency.
	 */
	class Dependencies(
		/**
		 * Property dependencies declared in this object.
		 *
		 * The keys are property names.
		 * If such a property is present, then the object must respect the additional dependency constraints.
		 *
		 * @see DependOnProperties: other properties become [Required].
		 * @see DependOnSchema: additional constraints are added to the schema.
		 */
		val dependencies: Map<String, DependencyConstraints>,
		factory: BsonFactory,
	) : SimpleConstraint<Any>("dependencies", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeDocument {
				for ((property, dependency) in dependencies) {
					write(property, dependency)
				}
			}
		}

		sealed class DependencyConstraints(
			private val factory: BsonFactory,
		) : BsonValueWriteable {

			@OptIn(LowLevelApi::class)
			override fun toString(): String =
				factory.buildArray { writeTo(this) }[0].toString()
		}

		/**
		 * To be valid, a property requires, when it is present, that other properties are present as well.
		 *
		 * For example, if a property `"send_email_notifications"` depends on the property `"email"`,
		 * validation fails if the `"send_email_notifications"` field is present but `"email"` is not.
		 *
		 * This relationship is monodirectional: if `"send_email_notifications"` is not present, then `"email"`
		 * is allowed to be absent.
		 *
		 * This dependency can be seen as a conditional [Required] constraint.
		 */
		class DependOnProperties(
			val properties: List<String>,
			factory: BsonFactory,
		) : DependencyConstraints(factory) {

			init {
				require(properties.isNotEmpty()) { "Cannot declare a dependency on no properties; found: $properties" }
			}

			@LowLevelApi
			override fun writeTo(writer: BsonValueWriter) = with(writer) {
				writeArray {
					for (property in properties) {
						writeString(property)
					}
				}
			}
		}

		/**
		 * To be valid, a property requires, when it is present, that the object confirms to additional constraints.
		 *
		 * For example, if a property `"send_email_notifications"` depends on a [schema] that says a field `"email"`
		 * must be a string, validation fails if the `"send_email_notifications"` is present but `"email"` is not a string.
		 *
		 * This relationship is monodirectional: if `"send_email_notifications"` is not present, then `"email"`
		 * is allowed to be absent or any other kind of value.
		 */
		class DependOnSchema(
			val schema: Schema<*>,
			factory: BsonFactory,
		) : DependencyConstraints(factory) {

			@LowLevelApi
			override fun writeTo(writer: BsonValueWriter) = with(writer) {
				writeDocument {
					schema.writeTo(this)
				}
			}
		}
	}

	// endregion
	// region Combinators

	/**
	 * The value must confirm to all of the given [schemas].
	 */
	class AllOf<T>(
		val schemas: List<Schema<T>>,
		factory: BsonFactory,
	) : SimpleConstraint<T>("allOf", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeArray {
				for (schema in schemas) {
					writeDocument {
						schema.writeTo(this)
					}
				}
			}
		}
	}

	/**
	 * The value must confirm to at least one of the given [schemas].
	 */
	class AnyOf<T>(
		val schemas: List<Schema<T>>,
		factory: BsonFactory,
	) : SimpleConstraint<T>("anyOf", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeArray {
				for (schema in schemas) {
					writeDocument {
						schema.writeTo(this)
					}
				}
			}
		}
	}

	/**
	 * The value must confirm to exactly one of the given [schemas].
	 */
	class OneOf<T>(
		val schemas: List<Schema<T>>,
		factory: BsonFactory,
	) : SimpleConstraint<T>("oneOf", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeArray {
				for (schema in schemas) {
					writeDocument {
						schema.writeTo(this)
					}
				}
			}
		}
	}

	class Not<T>(
		val schema: Schema<T>,
		factory: BsonFactory,
	) : SimpleConstraint<T>("not", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeDocument {
				schema.writeTo(this)
			}
		}
	}

	// endregion

	companion object {

		/**
		 * Creates a [Schema] that accepts all values.
		 */
		fun acceptAll(): Schema<Any?> =
			Schema("kotlin.Any?", emptyList())
	}
}
