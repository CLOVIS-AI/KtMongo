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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.bson.buildBsonArray
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.stages.limit
import opensavvy.ktmongo.dsl.aggregation.stages.match
import opensavvy.ktmongo.dsl.aggregation.stages.sample
import opensavvy.ktmongo.dsl.aggregation.stages.skip
import opensavvy.ktmongo.dsl.expr.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.expr.common.AbstractExpression
import opensavvy.ktmongo.dsl.expr.common.CompoundExpression
import opensavvy.ktmongo.dsl.expr.common.Expression

/**
 * A multi-stage pipeline that performs complex operations on MongoDB.
 *
 * Similar to [Sequence] and [Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/),
 * but executed by MongoDB itself.
 *
 * MongoDB has different types of pipelines with different available operators.
 * Using this library, all pipeline types are modelled as instances of this class, but with a different [Type]
 * type parameter.
 *
 * Instances of this class are immutable.
 *
 * ### Stages
 *
 * A pipeline is composed of _stages_, each of which transforms the data in some way.
 * For example, some stages filter information out, some stages add more information, some stages combine documents,
 * some stages extract information from elsewhere, etc.
 *
 * Each stage is defined as an extension function on this class.
 * Note that as mentioned, not all stages are available for all pipeline types.
 * The following stages are available:
 * - [`$limit`][limit]
 * - [`$match`][match]
 * - [`$sample`][sample]
 * - [`$skip`][skip]
 *
 * If you can't find a stage you're searching for, visit the [tracking issue](https://gitlab.com/opensavvy/ktmongo/-/issues/7).
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/aggregation/)
 *
 * @param Type The type of this pipeline, which determines what stages are available. See [PipelineType] for more information.
 * @param Output The type of document that this pipeline results in. The main way to change this type is to use projection stages.
 */
class Pipeline<out Type : PipelineType, Output : Any> private constructor(

	/**
	 * The context used to generate this pipeline.
	 *
	 * Can be used when creating child expressions.
	 */
	@property:LowLevelApi val context: BsonContext,

	private val type: Type,

	private val previous: Pipeline<*, *>?,

	private val current: Expression?,
) {

	/**
	 * Constructs a new, empty pipeline, of the given [type].
	 *
	 * This method should usually be called by the driver itself and not by end-users.
	 */
	@LowLevelApi
	constructor(
		context: BsonContext,
		type: Type,
	) : this(context, type, null, null)

	/**
	 * Creates a new pipeline that expands on the current one by adding [stage].
	 *
	 * This method is analogous to [CompoundExpression.accept], with the main difference that the latter mutates the
	 * current expression, whereas this method returns a new pipeline on which the stage is applied
	 * (because pipelines are immutable).
	 *
	 * **End-users should not need to call this function.**
	 * All implemented stages provide an extension function on the [Pipeline] type.
	 * This function is provided for cases in which you need a stage that is not yet provided by the library.
	 * If that is your situation, start by reading [AbstractExpression] and [AbstractCompoundExpression].
	 * If you want to proceed and implement your own stage, consider getting in touch with the maintainers of the
	 * library so it can be shared to all users.
	 *
	 * The provided [stage] must validate the entire contract of [Expression]. Additionally, it should always emit
	 * the name of the stage first. For example, this is a valid stage:
	 * ```json
	 * "$match": {
	 *     "name": "Bob"
	 * }
	 * ```
	 * but this isn't:
	 * ```json
	 * "name": "Bob"
	 * ```
	 * because it doesn't start a stage name.
	 *
	 * Similarly, this isn't a valid stage, because it declares two different stage names:
	 * ```json
	 * "$match": {
	 *     "name": "Bob"
	 * },
	 * "$set": {
	 *     "foo": "bar"
	 * }
	 * ```
	 *
	 * @see reinterpret Change the output type of this pipeline.
	 */
	@DangerousMongoApi
	@LowLevelApi
	fun withStage(stage: Expression): Pipeline<Type, Output> {
		val simplified = stage.simplify() ?: return this

		simplified.freeze()

		return Pipeline(context, type, this, simplified)
	}

	/**
	 * Changes the type of the returned document, with no type-safety.
	 *
	 * **End-users should not need to call this function.**
	 * This function is provided to allow stages to change the return document.
	 * No type verifications are made, it is solely the responsibility of the caller to ensure that the declared return
	 * type corresponds to the reality.
	 *
	 * @see withStage Add a new stage to this pipeline.
	 */
	@Suppress("UNCHECKED_CAST")
	@DangerousMongoApi
	@LowLevelApi
	fun <New : Any> reinterpret(): Pipeline<Type, New> = this as Pipeline<Type, New>

	private fun hierarchyReversed(): Sequence<Expression> = sequence {
		var cursor: Pipeline<*, *>? = this@Pipeline

		while (cursor != null) {
			if (cursor.current != null) yield(cursor.current)

			cursor = cursor.previous
		}
	}

	/**
	 * Writes the entire pipeline into [writer].
	 *
	 * This function is similar to [Expression.writeTo], with the difference that expressions generate documents,
	 * and pipelines generate arrays.
	 *
	 * Using this method will thus write an array containing the different stages.
	 */
	@LowLevelApi
	fun writeTo(writer: BsonValueWriter) = with(writer) {
		val stages = hierarchyReversed().toList().reversed()

		for (stage in stages) {
			writeDocument {
				stage.writeTo(this)
			}
		}
	}

	/**
	 * JSON representation of this pipeline.
	 */
	@OptIn(LowLevelApi::class)
	override fun toString(): String = buildBsonArray {
		writeTo(this)
	}.toString()

}
