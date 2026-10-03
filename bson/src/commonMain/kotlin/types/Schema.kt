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
	) : SimpleConstraint<Any?>("type", factory) {
		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeString(bsonType.name.lowercase())
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
		factory: BsonFactory,
	) : SimpleConstraint<Collection<*>>("uniqueItems", factory) {

		@OptIn(LowLevelApi::class)
		override fun BsonValueWriter.write() {
			writeBoolean(true)
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
