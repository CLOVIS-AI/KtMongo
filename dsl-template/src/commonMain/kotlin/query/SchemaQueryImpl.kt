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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonType
import opensavvy.ktmongo.bson.types.ExperimentalBsonSchemaApi
import opensavvy.ktmongo.bson.types.Schema
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@OptIn(ExperimentalBsonSchemaApi::class)
private open class SchemaQueryImpl<T>(
	context: BsonContext,
	private val title: String,
) : AbstractCompoundBsonNode(context), SchemaQuery<T> {

	// region Low-level operations

	@LowLevelApi
	private sealed class SchemaBsonNode(context: BsonContext) : AbstractBsonNode(context)

	@LowLevelApi
	override fun simplify(children: List<BsonNode>): AbstractBsonNode? =
		if (children.isEmpty()) null
		else this

	@OptIn(DangerousMongoApi::class)
	@LowLevelApi
	override fun accept(constraint: Schema.Constraint<T>) {
		accept(ConstraintBsonNode(constraint, context))
	}

	@OptIn(LowLevelApi::class)
	private class ConstraintBsonNode<T>(
		val constraint: Schema.Constraint<T>,
		context: BsonContext,
	) : SchemaBsonNode(context) {

		override fun write(writer: BsonFieldWriter) {
			constraint.writeTo(writer)
		}
	}

	@LowLevelApi
	override fun write(writer: BsonFieldWriter, children: List<BsonNode>) {
		writer.writeString("title", title)
		super.write(writer, children)
	}

	// endregion
	// region Generic

	@OptIn(LowLevelApi::class)
	override fun type(bsonType: BsonType) {
		accept(Schema.Type(bsonType, context))
	}

	@OptIn(LowLevelApi::class)
	override fun type(bsonType: SchemaQuery.Number) {
		accept(Schema.TypeNumber(context))
	}

	@OptIn(LowLevelApi::class)
	override fun enum(values: List<T>, type: KType) {
		accept(Schema.Enum(values.map { it to type }, context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun description(description: String) {
		accept(DescriptionBsonNode(description, context))
	}

	@OptIn(LowLevelApi::class)
	private class DescriptionBsonNode(
		val description: String,
		context: BsonContext,
	) : SchemaBsonNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeString("description", description)
		}
	}

	// endregion
	// region Numeric

	@OptIn(LowLevelApi::class)
	private fun multipleOf(number: Number, type: KType) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MultipleOf(number, type, context) as Schema.Constraint<T>)
	}

	override fun multipleOf(number: Double) {
		multipleOf(number, typeOf<Double>())
	}

	override fun multipleOf(number: Int) {
		multipleOf(number, typeOf<Int>())
	}

	override fun multipleOf(number: Long) {
		multipleOf(number, typeOf<Long>())
	}

	@OptIn(LowLevelApi::class)
	private fun maximum(number: Number, exclusive: Boolean, type: KType) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.Maximum(number, type, exclusive, context) as Schema.Constraint<T>)
	}

	override fun maximum(number: Double, exclusive: Boolean) {
		maximum(number, exclusive, typeOf<Double>())
	}

	override fun maximum(number: Int, exclusive: Boolean) {
		maximum(number, exclusive, typeOf<Int>())
	}

	override fun maximum(number: Long, exclusive: Boolean) {
		maximum(number, exclusive, typeOf<Long>())
	}

	@OptIn(LowLevelApi::class)
	private fun minimum(number: Number, exclusive: Boolean, type: KType) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.Minimum(number, type, exclusive, context) as Schema.Constraint<T>)
	}

	override fun minimum(number: Double, exclusive: Boolean) {
		minimum(number, exclusive, typeOf<Double>())
	}

	override fun minimum(number: Int, exclusive: Boolean) {
		minimum(number, exclusive, typeOf<Int>())
	}

	override fun minimum(number: Long, exclusive: Boolean) {
		minimum(number, exclusive, typeOf<Long>())
	}

	// endregion
	// region Strings

	@OptIn(LowLevelApi::class)
	override fun maxLength(length: Int) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MaxLength(length, context) as Schema.Constraint<T>)
	}

	@OptIn(LowLevelApi::class)
	override fun minLength(length: Int) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MinLength(length, context) as Schema.Constraint<T>)
	}

	@OptIn(LowLevelApi::class)
	override fun pattern(pattern: String) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.Pattern(pattern, context) as Schema.Constraint<T>)
	}

	// endregion
	// region Arrays

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun <I> items(title: String, schema: SchemaQuery<I>.() -> Unit) {
		accept(ItemsBsonNode(SchemaQuery<I>(title, context).apply(schema), context))
	}

	@OptIn(LowLevelApi::class)
	private class ItemsBsonNode<I>(
		val schema: SchemaQuery<I>,
		context: BsonContext,
	) : SchemaBsonNode(context) {

		@LowLevelApi
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("items") {
				schema.writeTo(this)
			}
		}
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun <I> itemsInOrder(schemas: SchemaQuery.SchemaHostArrayQuery<I>.() -> Unit) {
		accept(SchemaHostArrayQueryImpl<I>(context).apply(schemas))
	}

	@OptIn(LowLevelApi::class)
	private class SchemaHostArrayQueryImpl<T>(
		context: BsonContext,
	) : AbstractCompoundBsonNode(context), SchemaQuery.SchemaHostArrayQuery<T> {

		@OptIn(DangerousMongoApi::class)
		override fun schema(title: String, block: SchemaQuery<T>.() -> Unit) {
			accept(ItemSchemaBsonNode(SchemaQuery<T>(title, context).apply(block), context))
		}

		@OptIn(DangerousMongoApi::class)
		override fun additionalItems(title: String, schema: SchemaQuery<T>.() -> Unit) {
			accept(AdditionalItemsBsonNode(SchemaQuery<T>(title, context).apply(schema), context))
		}

		override fun simplify(children: List<BsonNode>): AbstractBsonNode {
			val items = children.filterIsInstance<ItemSchemaBsonNode<*>>()
			require(items.isNotEmpty()) { "itemsInOrder must contain at least one schema" }
			val additionalItemsCount = children.count { it is AdditionalItemsBsonNode<*> }
			check(additionalItemsCount <= 1) { "Cannot declare additionalItems more than once: $this" }
			return this
		}

		override fun write(writer: BsonFieldWriter, children: List<BsonNode>) = with(writer) {
			val items = children.filterIsInstance<ItemSchemaBsonNode<*>>()
			val additionalItems = children.filterIsInstance<AdditionalItemsBsonNode<*>>().singleOrNull()

			writeArray("items") {
				for (item in items) {
					writeDocument {
						item.writeTo(this)
					}
				}
			}

			if (additionalItems != null) {
				writeDocument("additionalItems") {
					additionalItems.writeTo(this)
				}
			} else {
				writeBoolean("additionalItems", false)
			}
		}
	}

	@LowLevelApi
	private class ItemSchemaBsonNode<T>(
		val schema: SchemaQuery<T>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			schema.writeTo(writer)
		}
	}

	@LowLevelApi
	private class AdditionalItemsBsonNode<T>(
		val schema: SchemaQuery<T>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			schema.writeTo(writer)
		}
	}

	@OptIn(LowLevelApi::class)
	override fun maxItems(size: Int) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MaxItems(size, context) as Schema.Constraint<T>)
	}

	@OptIn(LowLevelApi::class)
	override fun minItems(size: Int) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MinItems(size, context) as Schema.Constraint<T>)
	}

	@OptIn(LowLevelApi::class)
	override fun uniqueItems(unique: Boolean) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.UniqueItems(unique, context) as Schema.Constraint<T>)
	}

	// endregion
	// region Objects

	@OptIn(LowLevelApi::class)
	override fun maxProperties(count: Int) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MaxProperties(count, context) as Schema.Constraint<T>)
	}

	@OptIn(LowLevelApi::class)
	override fun minProperties(count: Int) {
		@Suppress("UNCHECKED_CAST")
		accept(Schema.MinProperties(count, context) as Schema.Constraint<T>)
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun properties(block: SchemaQuery.SchemaPropertiesQuery<T>.() -> Unit) {
		accept(SchemaPropertiesQueryImpl<T>(context).apply(block))
	}

	@OptIn(LowLevelApi::class)
	private class SchemaPropertiesQueryImpl<T>(
		context: BsonContext,
	) : AbstractCompoundBsonNode(context), SchemaQuery.SchemaPropertiesQuery<T> {

		@OptIn(DangerousMongoApi::class)
		override fun <E> property(
			name: String,
			required: Boolean,
			schema: SchemaQuery.SchemaPropertiesQuery.SchemaPropertyQuery<T, E>.() -> Unit,
		): SchemaQuery.SchemaPropertiesQuery.Property<T> {
			val propQuery = SchemaPropertyQueryImpl<T, E>(name, context).apply(schema)
			accept(PropertyDeclarationBsonNode(name, required, propQuery, context))
			return PropertyImpl(name)
		}

		@OptIn(DangerousMongoApi::class)
		override fun <E> properties(
			pattern: String,
			schema: SchemaQuery<E>.() -> Unit,
		) {
			accept(PatternPropertyDeclarationBsonNode(pattern, SchemaQuery<E>(pattern, context).apply(schema), context))
		}

		@OptIn(DangerousMongoApi::class)
		override fun additionalProperties(
			title: String,
			schema: SchemaQuery<*>.() -> Unit,
		) {
			accept(AdditionalPropertiesDeclarationBsonNode(SchemaQuery<Any?>(title, context).apply(schema), null, context))
		}

		@OptIn(DangerousMongoApi::class)
		override fun additionalProperties(
			allowed: Boolean,
		) {
			accept(AdditionalPropertiesDeclarationBsonNode(null, allowed, context))
		}

		private class PropertyImpl<R>(
			val name: String,
		) : SchemaQuery.SchemaPropertiesQuery.Property<R> {

			override fun toString(): String =
				"Property($name)"
		}

		private inner class SchemaPropertyQueryImpl<R, E>(
			val name: String,
			context: BsonContext,
		) : SchemaQueryImpl<E>(context, name), SchemaQuery.SchemaPropertiesQuery.SchemaPropertyQuery<R, E> {

			override fun dependsOn(property: SchemaQuery.SchemaPropertiesQuery.Property<R>) {
				check(property is PropertyImpl<*>) { "Unknown Property implementation: $property" }
				dependsOn(property.name)
			}

			@OptIn(DangerousMongoApi::class)
			override fun dependsOn(property: String) {
				this@SchemaPropertiesQueryImpl.accept(PropertyDependencyDeclarationBsonNode(name, property, context))
			}

			@OptIn(DangerousMongoApi::class)
			override fun dependsOn(schema: SchemaQuery<R>.() -> Unit) {
				this@SchemaPropertiesQueryImpl.accept(SchemaDependencyDeclarationBsonNode(name, SchemaQuery<R>(name, context).apply(schema), context))
			}
		}

		override fun simplify(children: List<BsonNode>): AbstractBsonNode? {
			if (children.isEmpty())
				return null

			val additionalPropertiesCount = children.count { it is AdditionalPropertiesDeclarationBsonNode }
			check(additionalPropertiesCount <= 1) { "Cannot declare additionalProperties more than once: $this" }

			val propertyNames = HashSet<String>()
			for (child in children) {
				if (child is PropertyDeclarationBsonNode) {
					check(propertyNames.add(child.name)) { "Property '${child.name}' was already declared: $children" }
				}
			}

			val patternNames = HashSet<String>()
			for (child in children) {
				if (child is PatternPropertyDeclarationBsonNode) {
					check(patternNames.add(child.pattern)) { "Pattern '${child.pattern}' was already declared: $children" }
				}
			}

			return this
		}

		@OptIn(LowLevelApi::class, DangerousMongoApi::class)
		override fun write(writer: BsonFieldWriter, children: List<BsonNode>) = with(writer) {
			val namedProperties = children.filterIsInstance<PropertyDeclarationBsonNode>()
			val patternProperties = children.filterIsInstance<PatternPropertyDeclarationBsonNode>()
			val additionalProperties = children.filterIsInstance<AdditionalPropertiesDeclarationBsonNode>().singleOrNull()
			val propertyDependencies = children.filterIsInstance<PropertyDependencyDeclarationBsonNode>()
			val schemaDependencies = children.filterIsInstance<SchemaDependencyDeclarationBsonNode>()

			val requiredProperties = namedProperties.filter { it.required }.map { it.name }
			if (requiredProperties.isNotEmpty()) {
				writeArray("required") {
					for (name in requiredProperties) {
						writeString(name)
					}
				}
			}

			if (namedProperties.isNotEmpty()) {
				writeDocument("properties") {
					for (prop in namedProperties) {
						writeDocument(prop.name) {
							prop.writeTo(this)
						}
					}
				}
			}

			if (patternProperties.isNotEmpty()) {
				writeDocument("patternProperties") {
					for (prop in patternProperties) {
						writeDocument(prop.pattern) {
							prop.writeTo(this)
						}
					}
				}
			}

			if (additionalProperties != null) {
				if (additionalProperties.schema != null) {
					writeDocument("additionalProperties") {
						additionalProperties.schema.writeTo(this)
					}
				} else {
					writeBoolean("additionalProperties", additionalProperties.allowed!!)
				}
			}

			val allDepFromKeys = (propertyDependencies.map { it.from } + schemaDependencies.map { it.from }).distinct()
			if (allDepFromKeys.isNotEmpty()) {
				writeDocument("dependencies") {
					for (from in allDepFromKeys) {
						val fromSchemaDeps = schemaDependencies.filter { it.from == from }
						val fromPropDeps = propertyDependencies.filter { it.from == from }.map { it.to }.distinct()

						if (fromSchemaDeps.isNotEmpty()) {
							val nonPropertiesChildren = ArrayList<BsonNode>()
							val propertiesChildren = ArrayList<BsonNode>()

							for (schemaDep in fromSchemaDeps) {
								val schemaImpl = schemaDep.schema as? SchemaQueryImpl<*>
								if (schemaImpl != null) {
									for (node in schemaImpl.children) {
										if (node is SchemaPropertiesQueryImpl<*>) {
											propertiesChildren.addAll(node.children)
										} else {
											nonPropertiesChildren.add(node)
										}
									}
								}
							}

							val finalPropertiesChildren = ArrayList<BsonNode>()
							for (node in propertiesChildren) {
								if (node is PropertyDeclarationBsonNode && node.name in fromPropDeps && !node.required) {
									finalPropertiesChildren.add(PropertyDeclarationBsonNode(node.name, required = true, node.schema, context))
								} else {
									finalPropertiesChildren.add(node)
								}
							}
							val existingPropertyNames = finalPropertiesChildren.filterIsInstance<PropertyDeclarationBsonNode>().map { it.name }.toSet()
							for (to in fromPropDeps) {
								if (to !in existingPropertyNames) {
									finalPropertiesChildren.add(PropertyDeclarationBsonNode(to, required = true, SchemaQuery<Any?>(to, context), context))
								}
							}

							val targetSchema = SchemaQueryImpl<Any?>(context, from)
							for (node in nonPropertiesChildren) {
								targetSchema.accept(node)
							}
							if (finalPropertiesChildren.isNotEmpty()) {
								val combinedProps = SchemaPropertiesQueryImpl<Any?>(context)
								for (node in finalPropertiesChildren) {
									combinedProps.accept(node)
								}
								targetSchema.accept(combinedProps)
							}

							writeDocument(from) {
								targetSchema.writeTo(this)
							}
						} else {
							writeArray(from) {
								for (target in fromPropDeps) {
									writeString(target)
								}
							}
						}
					}
				}
			}
		}
	}

	@LowLevelApi
	private class PropertyDeclarationBsonNode(
		val name: String,
		val required: Boolean,
		val schema: SchemaQuery<*>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			schema.writeTo(writer)
		}
	}

	@LowLevelApi
	private class PropertyDependencyDeclarationBsonNode(
		val from: String,
		val to: String,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {}
	}

	@LowLevelApi
	private class SchemaDependencyDeclarationBsonNode(
		val from: String,
		val schema: SchemaQuery<*>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			schema.writeTo(writer)
		}
	}

	@LowLevelApi
	private class PatternPropertyDeclarationBsonNode(
		val pattern: String,
		val schema: SchemaQuery<*>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			schema.writeTo(writer)
		}
	}

	@LowLevelApi
	private class AdditionalPropertiesDeclarationBsonNode(
		val schema: SchemaQuery<*>?,
		val allowed: Boolean?,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			// No-op: written by SchemaPropertiesQueryImpl.write
		}
	}

	// endregion
	// region Combinators

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun allOf(block: SchemaQuery.SchemaHostQuery<T>.() -> Unit) {
		accept(CombinatorBsonNode<T>("allOf", context).apply(block))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun anyOf(block: SchemaQuery.SchemaHostQuery<T>.() -> Unit) {
		accept(CombinatorBsonNode<T>("anyOf", context).apply(block))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun oneOf(block: SchemaQuery.SchemaHostQuery<T>.() -> Unit) {
		accept(CombinatorBsonNode<T>("oneOf", context).apply(block))
	}

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun not(title: String, block: SchemaQuery<T>.() -> Unit) {
		accept(NotBsonNode(SchemaQuery<T>(title, context).apply(block), context))
	}

	@LowLevelApi
	private class CombinatorSchemaBsonNode<T>(
		val schema: SchemaQuery<T>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) {
			schema.writeTo(writer)
		}
	}

	@OptIn(LowLevelApi::class)
	private class CombinatorBsonNode<T>(
		private val key: String,
		context: BsonContext,
	) : AbstractCompoundBsonNode(context), SchemaQuery.SchemaHostQuery<T> {

		@OptIn(DangerousMongoApi::class)
		override fun schema(title: String, block: SchemaQuery<T>.() -> Unit) {
			accept(CombinatorSchemaBsonNode(SchemaQuery<T>(title, context).apply(block), context))
		}

		override fun simplify(children: List<BsonNode>): AbstractBsonNode {
			require(children.isNotEmpty()) { "Combinator must contain at least one schema" }
			return this
		}

		override fun write(writer: BsonFieldWriter, children: List<BsonNode>) = with(writer) {
			writeArray(key) {
				for (child in children.filterIsInstance<CombinatorSchemaBsonNode<T>>()) {
					writeDocument {
						child.writeTo(this)
					}
				}
			}
		}
	}

	@LowLevelApi
	private class NotBsonNode<T>(
		val schema: SchemaQuery<T>,
		context: BsonContext,
	) : SchemaBsonNode(context) {
		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeDocument("not") {
				schema.writeTo(this)
			}
		}
	}

	// endregion

}

@LowLevelApi
fun <T> SchemaQuery(
	title: String,
	context: BsonContext,
): SchemaQuery<T> =
	SchemaQueryImpl(context = context, title = title)
