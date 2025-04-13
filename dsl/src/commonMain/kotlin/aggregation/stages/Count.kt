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
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.FieldDslImpl
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.path.PathSegment
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import kotlin.reflect.KProperty1

/**
 * Pipeline implementing the `$count` stage.
 */
@KtMongoDsl
interface HasCount<Document : Any> : Pipeline<Document> {

	/**
	 * Counts how many elements exist in the pipeline and outputs a single document containing a single [field] containing
	 * the count.
	 *
	 * [field] must be a simple field name. Subfields (accessed through the `/` operator) are forbidden.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Score(
	 *     val student: ObjectId,
	 *     val subject: String,
	 *     val score: Int,
	 * )
	 *
	 * class Results(
	 *     val passingScores: Int,
	 * )
	 *
	 * scores.aggregate()
	 *     .match { Score::score gt 99 }
	 *     .countTo(Results::passingScores)
	 * ```
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	@KtMongoDsl
	fun <Output : Any> countTo(field: Field<Output, Number>): Pipeline<Output> =
		withStage(CountStage( field.path, context)).reinterpret()

	/**
	 * Counts how many elements exist in the pipeline and outputs a single document containing a single [field] containing
	 * the count.
	 *
	 * [field] must be a simple field name. Subfields (accessed through the `/` operator) are forbidden.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class Score(
	 *     val student: ObjectId,
	 *     val subject: String,
	 *     val score: Int,
	 * )
	 *
	 * class Results(
	 *     val passingScores: Int,
	 * )
	 *
	 * scores.aggregate()
	 *     .match { Score::score gt 99 }
	 *     .countTo(Results::passingScores)
	 * ```
	 */
	@KtMongoDsl
	fun <Output : Any> countTo(field: KProperty1<Output, Number>): Pipeline<Output> =
		countTo(with(FieldDslImpl) { field.field })

}

@OptIn(LowLevelApi::class)
private class CountStage(
	val path: Path,
	context: BsonContext,
) : AbstractBsonNode(context) {

	init {
		require(path.parent == null) { "The \$count stage only accepts a simple field name, not nested fields. Found '$path'." }
		require(path.segment is PathSegment.Field) { "The \$count stage only accepts fields, not other types of paths. Found $path of type ${path.segment::class}" }
	}

	@LowLevelApi
	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeString("\$count", path.toString())
	}

}
