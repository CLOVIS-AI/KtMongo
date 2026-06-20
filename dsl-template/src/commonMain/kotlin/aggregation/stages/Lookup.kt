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
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode
import kotlin.reflect.KProperty1

/**
 * Pipeline implementing the `$lookup` stage.
 */
@KtMongoDsl
interface HasLookup<Document : Any> : Pipeline<Document> {

	/**
	 * Performs an equality match join between this collection and another collection.
	 *
	 * For each document in this pipeline, matching documents from the foreign collection are appended
	 * into the new array field [into].
	 * If [into] already has a value, it is overwritten.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * If you want to store the results in a temporary field (for example, for further processing in a subsequent stage),
	 * you can use [Field.unsafe] to avoid adding the field to the DTO:
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val departmentId: ObjectId,
	 *     val department: Department? = null,
	 * )
	 *
	 * val temporaryField = Field.unsafe<List<Department>>("departments")
	 *
	 * users.aggregate()
	 *     .lookup(temporaryField) {
	 *         from(departments.aggregate())
	 *         on(User::departmentId, Department::_id)
	 *     }
	 *     .project {
	 *         // Write '.department' from the first value returned by the lookup.
	 *         // Because we matched on an _id: ObjectId, we know there can never be multiple results.
	 *         User::department set temporaryField[0]
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/)
	 *
	 * @param into The field into which the result of the lookup will be written.
	 * @param block The operators declaring which lookup to perform.
	 * - [LookupStageOperators.from]: Specifies the foreign collection.
	 * - [LookupStageOperators.on]: Specifies an equality criteria between a local and a foreign field.
	 * Documents are only returned if the value of the two fields is strictly equal.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun <ForeignDocument : Any> lookup(
		into: Field<Document, List<ForeignDocument>>,
		block: LookupStageOperators<Document, ForeignDocument>.() -> Unit,
	): Pipeline<Document> =
		withStage(LookupStageBsonNode<Document, ForeignDocument>(into.path, context).apply(block).apply { freeze() })

	/**
	 * Performs an equality match join between this collection and another collection.
	 *
	 * For each document in this pipeline, matching documents from the foreign collection are appended
	 * into the new array field [into].
	 * If [into] already has a value, it is overwritten.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * If you want to store the results in a temporary field (for example, for further processing in a subsequent stage),
	 * you can use [Field.unsafe] to avoid adding the field to the DTO:
	 * ```kotlin
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val departmentId: ObjectId,
	 *     val department: Department? = null,
	 * )
	 *
	 * val temporaryField = Field.unsafe<List<Department>>("departments")
	 *
	 * users.aggregate()
	 *     .lookup(temporaryField) {
	 *         from(departments.aggregate())
	 *         on(User::departmentId, Department::_id)
	 *     }
	 *     .project {
	 *         // Write '.department' from the first value returned by the lookup.
	 *         // Because we matched on an _id: ObjectId, we know there can never be multiple results.
	 *         User::department set temporaryField[0]
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/)
	 *
	 * @param into The field into which the result of the lookup will be written.
	 * @param block The operators declaring which lookup to perform.
	 * - [LookupStageOperators.from]: Specifies the foreign collection.
	 * - [LookupStageOperators.on]: Specifies an equality criteria between a local and a foreign field.
	 * Documents are only returned if the value of the two fields is strictly equal.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun <ForeignDocument : Any> lookup(
		into: KProperty1<Document, List<ForeignDocument>>,
		block: LookupStageOperators<Document, ForeignDocument>.() -> Unit,
	): Pipeline<Document> =
		lookup(
			into = with(FieldDsl(context)) { into.field },
			block = block,
		)
}

/**
 * Pipeline that can be used as the foreign collection in a [`$lookup` stage][HasLookup.lookup].
 *
 * Instances of this interface should be immutable.
 */
@KtMongoDsl
interface HasLookupPipelineCompatibility<Document : Any> : Pipeline<Document> {

	/**
	 * Writes this pipeline's contribution into a `$lookup` stage.
	 *
	 * This method is a low-level API for building custom `$lookup` stages.
	 * Regular users should use [HasLookup.lookup] instead.
	 *
	 * ### Implementation contract
	 *
	 * When a `$lookup` stage embeds this pipeline as its foreign collection, it calls this method
	 * from within the `$lookup` body. This method should emit at minimum the `from` field:
	 *
	 * ```json
	 * {
	 *     "from": "<collection>"
	 * }
	 * ```
	 *
	 * When sub-pipeline support is added, the `pipeline` array will also be written here.
	 *
	 * @see HasLookup.lookup The `$lookup` stage.
	 */
	@LowLevelApi
	fun embedInLookup(writer: BsonFieldWriter)

}

/**
 * The operators allowed in a [lookup][HasLookup.lookup] stage.
 */
@KtMongoDsl
interface LookupStageOperators<LocalDocument : Any, ForeignDocument : Any> : CompoundBsonNode, AggregationOperators, FieldDsl {

	/**
	 * Specifies the foreign collection to join with.
	 *
	 * To learn more about the `$lookup` stage, see [HasLookup.lookup].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#syntax)
	 */
	fun from(foreignPipeline: HasLookupPipelineCompatibility<ForeignDocument>)

	/**
	 * Specifies that the field [foreignField] in the [foreign collection][from]
	 * must have a value that is strictly equal to the value of the local field [localField].
	 *
	 * To learn more about the `$lookup` stage, see [HasLookup.lookup].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#equality-match-with-a-single-join-condition)
	 */
	fun <Key> on(
		localField: Field<LocalDocument, Key>,
		foreignField: Field<ForeignDocument, Key>,
	)

	/**
	 * Specifies that the field [foreignField] in the [foreign collection][from]
	 * must have a value that is strictly equal to the value of the local field [localField].
	 *
	 * To learn more about the `$lookup` stage, see [HasLookup.lookup].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#equality-match-with-a-single-join-condition)
	 */
	@OptIn(LowLevelApi::class)
	fun <Key> on(
		localField: Field<LocalDocument, Key>,
		foreignField: KProperty1<ForeignDocument, Key>,
	) = on(
		localField = localField,
		foreignField = with(FieldDsl(context)) { foreignField.field },
	)

	/**
	 * Specifies that the field [foreignField] in the [foreign collection][from]
	 * must have a value that is strictly equal to the value of the local field [localField].
	 *
	 * To learn more about the `$lookup` stage, see [HasLookup.lookup].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#equality-match-with-a-single-join-condition)
	 */
	@OptIn(LowLevelApi::class)
	fun <Key> on(
		localField: KProperty1<LocalDocument, Key>,
		foreignField: Field<ForeignDocument, Key>,
	) = on(
		localField = with(FieldDsl(context)) { localField.field },
		foreignField = foreignField,
	)

	/**
	 * Specifies that the field [foreignField] in the [foreign collection][from]
	 * must have a value that is strictly equal to the value of the local field [localField].
	 *
	 * To learn more about the `$lookup` stage, see [HasLookup.lookup].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Department(
	 *     val _id: ObjectId,
	 *     val name: String,
	 * )
	 *
	 * class User(
	 *     val _id: ObjectId,
	 *     val name: String,
	 *     val department: ObjectId,
	 *     val departments: List<Department>,
	 * )
	 *
	 * users.aggregate()
	 *     .lookup(User::departments) {
	 *         from(departments.aggregate())
	 *         on(User::department, Department::_id)
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#equality-match-with-a-single-join-condition)
	 */
	@OptIn(LowLevelApi::class)
	fun <Key> on(
		localField: KProperty1<LocalDocument, Key>,
		foreignField: KProperty1<ForeignDocument, Key>,
	) = on(
		localField = with(FieldDsl(context)) { localField.field },
		foreignField = with(FieldDsl(context)) { foreignField.field },
	)

}

@OptIn(LowLevelApi::class)
private class LookupStageBsonNode<LocalDocument : Any, ForeignDocument : Any>(
	val outputPath: Path,
	context: BsonContext,
) : AbstractCompoundBsonNode(context), LookupStageOperators<LocalDocument, ForeignDocument> {

	@OptIn(DangerousMongoApi::class)
	override fun from(foreignPipeline: HasLookupPipelineCompatibility<ForeignDocument>) {
		accept(ForeignSourceBsonNode(foreignPipeline, context))
	}

	@OptIn(DangerousMongoApi::class)
	override fun <Key> on(localField: Field<LocalDocument, Key>, foreignField: Field<ForeignDocument, Key>) {
		accept(EqualityJoinCriteriaBsonNode(localField.path, foreignField.path, context))
	}

	override fun write(writer: BsonFieldWriter, children: List<BsonNode>) = with(writer) {
		writeDocument($$"$lookup") {
			for (child in children) {
				child.writeTo(this)
			}

			writeString("as", outputPath.toString())
		}
	}
}

private class ForeignSourceBsonNode(
	val pipeline: HasLookupPipelineCompatibility<*>,
	context: BsonContext,
) : AbstractBsonNode(context) {
	@LowLevelApi
	override fun write(writer: BsonFieldWriter) {
		pipeline.embedInLookup(writer)
	}
}

@OptIn(LowLevelApi::class)
private class EqualityJoinCriteriaBsonNode(
	val localField: Path,
	val foreignField: Path,
	context: BsonContext,
) : AbstractBsonNode(context) {
	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeString("localField", localField.toString())
		writeString("foreignField", foreignField.toString())
	}
}
