/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.AggregationOperators
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import kotlin.reflect.KProperty1

/**
 * Pipeline implementing the `$unset` stage.
 */
interface HasUnset<Document : Any> : Pipeline<Document> {

	/**
	 * Removes fields from documents.
	 *
	 * ### Example
	 *
	 * We can use this stage to migrate from a schema to a newer schema.
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int?,       // deprecated
	 *     val birthYear: Int?, // new field
	 * )
	 *
	 * val currentYear = 2025
	 * users.updateManyWithPipeline {
	 *     set {
	 *         User::birthYear set (of(currentYear) - of(age))
	 *     }
	 *     unset {
	 *         exclude(User::age)
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/unset)
	 */
	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun unset(
		block: UnsetStageOperators<Document>.() -> Unit,
	): Pipeline<Document> =
		withStage(createUnsetStage(context, block))

}

internal fun <Document : Any> createUnsetStage(context: BsonContext, block: UnsetStageOperators<Document>.() -> Unit): BsonNode =
	UnsetStage<Document>(context).apply { block() }

/**
 * The operators allowed in an [`$unset`][HasUnset.unset] stage.
 */
@KtMongoDsl
interface UnsetStageOperators<Document : Any> : BsonNode, AggregationOperators, FieldDsl {

	/**
	 * Excludes a field from the current document.
	 *
	 * ### Example
	 *
	 * See [HasUnset.unset].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/unset)
	 */
	fun exclude(field: Field<Document, *>)

	/**
	 * Excludes a field from the current document.
	 *
	 * ### Example
	 *
	 * See [HasUnset.unset].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/unset)
	 */
	fun exclude(field: KProperty1<Document, *>) {
		exclude(field.field)
	}

}

private class UnsetStage<Document : Any>(
	context: BsonContext,
) : AbstractBsonNode(context), UnsetStageOperators<Document> {

	private val fields = HashSet<Field<*, *>>()

	override fun exclude(field: Field<Document, *>) {
		require(!frozen) { "This \$unset stage has already been frozen, it is too late to exclude the field $field" }
		fields += field
	}

	@LowLevelApi
	override fun simplify(): AbstractBsonNode? =
		if (fields.isEmpty()) null
		else super.simplify()

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeArray("\$unset") {
			for (field in fields) {
				writeString(field.toString())
			}
		}
	}
}
