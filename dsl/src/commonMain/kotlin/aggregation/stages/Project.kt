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

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.aggregation.Value
import opensavvy.ktmongo.dsl.aggregation.ValueDsl
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.expr.common.CompoundExpression
import opensavvy.ktmongo.dsl.expr.common.Expression
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDsl
import opensavvy.ktmongo.dsl.path.Path
import kotlin.reflect.KProperty1

/**
 * Pipeline implementing the `$project` stage.
 */
@KtMongoDsl
interface HasProject<Document : Any> : Pipeline<Document> {

	/**
	 * Specifies a list of fields which should be kept in the document.
	 *
	 * All fields (except `_id`) that are not specified in this stage are unset.
	 *
	 * `_id` is kept even if not specified. To exclude it, see [ProjectStageOperators.excludeId].
	 *
	 * ### Difference with MongoDB
	 *
	 * In BSON, the `$project` stage can be used either for declaring an allow-list (by including fields,
	 * and possibly excluding the `_id`) or a block-list (by excluding fields). MongoDB doesn't allow a single stage
	 * usage to mix both usages.
	 *
	 * Because this is confusing, KtMongo splits both of these use-cases into two different methods.
	 * The former (selecting fields we want to keep) is performed by this method.
	 * The latter (selecting fields we want to remove) is performed by the stage [`$unset`][HasUnset.unset].
	 *
	 * Note that just like in MongoDB, this stage can use all operators of the [`$set` stage][HasSet.set].
	 *
	 * ### Difference with $set
	 *
	 * This stage and the [`$set` stage][HasSet.set] are quite similar. In fact, both stages behave the same for fields
	 * that are specified.
	 *
	 * However, the stages behave differently for fields that are not specified:
	 * - `$project` removes all fields that are not explicitly specified.
	 * - `$set` does not impact fields that are not explicitly specified, they are left in the exact same state as previously.
	 *
	 * ### Performance
	 *
	 * When you use a `$project` stage it should typically be the last stage in your pipeline,
	 * used to specify which fields to return to the client.
	 *
	 * Using a `$project` stage at the beginning or middle of a pipeline to reduce the number of fields
	 * passed to subsequent pipeline stages is unlikely to improve performance,
	 * as the database performs this optimization automatically.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val year: Int?,
	 * )
	 *
	 * users.aggregate()
	 *     .project {
	 *         include(User::name)
	 *     }
	 *     .toList()
	 * ```
	 *
	 * In this example, the `year` field will not be returned by the aggregation, because it is not mentioned in the `$project` stage.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	@KtMongoDsl
	fun project(
		block: ProjectStageOperators<Document>.() -> Unit,
	): Pipeline<Document> =
		withStage(createProjectStage(context, block))

}

internal fun <Document : Any> createProjectStage(context: BsonContext, block: ProjectStageOperators<Document>.() -> Unit): Expression =
	ProjectStage(ProjectStageExpression<Document>(context).apply(block), context)

private class ProjectStage(
	val expression: ProjectStageOperators<*>,
	context: BsonContext
) : AbstractExpression(context) {

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$project") {
			expression.writeTo(this)
		}
	}
}

/**
 * The operators allowed in a [`$project` stage][HasProject.project].
 */
@KtMongoDsl
interface ProjectStageOperators<Document : Any> : CompoundExpression, ValueDsl, FieldDsl, SetStageOperators<Document> {

	/**
	 * Excludes the `_id` field.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .project {
	 *         include(User::age)
	 *         excludeId()
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/#_id-field)
	 */
	@KtMongoDsl
	fun excludeId()

	/**
	 * Explicitly includes [field].
	 *
	 * Note that fields that aren't mentioned in the `$project` stage are deleted (except the `_id` field, which must be [explicitly excluded][excludeId]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *    val name: String,
	 *    val age: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .project {
	 *         include(User::age)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/#include-fields)
	 */
	@KtMongoDsl
	fun include(field: Field<Document, *>)

	/**
	 * Explicitly includes [field].
	 *
	 * Note that fields that aren't mentioned in the `$project` stage are deleted (except the `_id` field, which must be [explicitly excluded][excludeId]).
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *    val name: String,
	 *    val age: Int,
	 * )
	 *
	 * users.aggregate()
	 *     .project {
	 *         include(User::age)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/#include-fields)
	 */
	@KtMongoDsl
	fun include(field: KProperty1<Document, *>) {
		include(field.field)
	}

}

private class ProjectStageExpression<Document : Any>(
	context: BsonContext,
) : AbstractCompoundExpression(context), ProjectStageOperators<Document> {

	// region Exclude ID

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun excludeId() {
		accept(ProjectExcludeIdExpression(context))
	}

	@LowLevelApi
	private class ProjectExcludeIdExpression(
		context: BsonContext,
	) : AbstractExpression(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeInt32("_id", 0)
		}
	}

	// endregion
	// region Include field

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun include(field: Field<Document, *>) {
		accept(ProjectIncludeExpression(field.path, context))
	}

	@LowLevelApi
	private class ProjectIncludeExpression(
		val path: Path,
		context: BsonContext,
	) : AbstractExpression(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeInt32(path.toString(), 1)
		}
	}

	// endregion
	// region Set field

	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	override fun <V> Field<Document, V>.set(value: Value<Document, V>) {
		accept(ProjectSetExpression(this.path, value, context))
	}

	@LowLevelApi
	private class ProjectSetExpression(
		val path: Path,
		val value: Value<*, *>,
		context: BsonContext,
	) : AbstractExpression(context) {

		override fun write(writer: BsonFieldWriter) =  with(writer) {
			write(path.toString()) {
				value.writeTo(this)
			}
		}
	}

	// endregion

}
