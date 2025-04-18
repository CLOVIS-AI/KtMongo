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

import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode

/**
 * A multi-stage pipeline that performs complex operations on MongoDB.
 *
 * Similar to [Sequence] and [Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/),
 * but executed by MongoDB itself.
 *
 * MongoDB has different types of pipelines with different available operators, which are represented by the different
 * implementations of this interface.
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
 * - [`$limit`][opensavvy.ktmongo.dsl.aggregation.stages.HasLimit.limit]
 * - [`$match`][opensavvy.ktmongo.dsl.aggregation.stages.HasMatch.match]
 * - [`$project`][opensavvy.ktmongo.dsl.aggregation.stages.HasProject.project]
 * - [`$sample`][opensavvy.ktmongo.dsl.aggregation.stages.HasSample.sample]
 * - [`$set`][opensavvy.ktmongo.dsl.aggregation.stages.HasSet.set]
 * - [`$skip`][opensavvy.ktmongo.dsl.aggregation.stages.HasSkip.skip]
 * - [`$sort`][opensavvy.ktmongo.dsl.aggregation.stages.HasSort.sort]
 * - [`$unionWith`][opensavvy.ktmongo.dsl.aggregation.stages.HasUnionWith.unionWith]
 * - [`$unset`][opensavvy.ktmongo.dsl.aggregation.stages.HasUnset.unset]
 *
 * If you can't find a stage you're searching for, visit the [tracking issue](https://gitlab.com/opensavvy/ktmongo/-/issues/7).
 *
 * ### Implementing a new stage
 *
 * Just like operators, stages can be added as extension methods on this type or any of its subtypes.
 * To register the stage, call [withStage], optionally followed by [reinterpret], and return the resulting pipeline.
 *
 * Stages should return [Pipeline] instances **that were generated by the [withStage] or [reinterpret] methods**.
 * [Pipeline] implementations are allowed to assume all stages they will be provided were generated by their own
 * implementation of these methods, and thus may downcast the resulting pipeline to another type safely.
 * Returning any other [Pipeline] instance has unspecified behavior.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/aggregation/)
 *
 * @param Output The type of document that this pipeline results in. Changing this type is possible by calling [reinterpret].
 */
@KtMongoDsl
interface Pipeline<Output : Any> {

	/**
	 * The context used to generate this pipeline.
	 *
	 * Can be accessed within children expressions.
	 */
	@LowLevelApi
	val context: BsonContext

	/**
	 * Creates a new pipeline that expands on the current one by adding [stage].
	 *
	 * This method is analogous to [CompoundBsonNode.accept], with the main difference that the latter mutates the
	 * current expression, whereas this method returns a new pipeline on which the stage is applied
	 * (because pipelines are immutable).
	 *
	 * **End-users should not need to call this function.**
	 * All implemented stages provide an extension function on the [Pipeline] type.
	 * This function is provided for cases in which you need a stage that is not yet provided by the library.
	 * If that is your situation, start by reading [AbstractBsonNode] and [AbstractCompoundBsonNode].
	 * If you want to proceed and implement your own stage, consider getting in touch with the maintainers of the
	 * library so it can be shared to all users.
	 *
	 * The provided [stage] must validate the entire contract of [BsonNode]. Additionally, it should always emit
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
	 * because it doesn't start with a stage name.
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
	fun withStage(stage: BsonNode): Pipeline<Output>

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
	fun <New : Any> reinterpret(): Pipeline<New>

	/**
	 * Writes the entire pipeline into [writer].
	 *
	 * This function is similar to [BsonNode.writeTo], with the difference that expressions generate documents,
	 * and pipelines generate arrays.
	 *
	 * Using this method will thus write an array containing the different stages.
	 */
	@LowLevelApi
	fun writeTo(writer: BsonValueWriter)

	/**
	 * JSON representation of this pipeline.
	 */
	override fun toString(): String

}

/**
 * A single link in the [Pipeline] chain.
 *
 * **End-users should not interact with this class.**
 * This class is provided as an implementation detail of [AbstractPipeline].
 * If you are not implementing your own pipeline type, you do not need to interact with this class at all.
 */
@LowLevelApi
class PipelineChainLink internal constructor(
	private val context: BsonContext,
	private val previous: PipelineChainLink?,
	private val current: BsonNode?,
) {

	/**
	 * Creates an empty [PipelineChainLink], corresponding to an empty aggregation pipeline.
	 */
	constructor(
		context: BsonContext,
	) : this(context, null, null)

	/**
	 * Equivalent to [Pipeline.withStage], but generating a chain link instead.
	 */
	fun withStage(stage: BsonNode): PipelineChainLink {
		val simplified = stage.simplify() ?: return this
		simplified.freeze()
		return PipelineChainLink(context, this, simplified)
	}

	/**
	 * Iterates through this chain.
	 *
	 * The first returned element is the current one, the second returned element is the previous one,
	 * the third element is the previous one, etc.
	 */
	private fun hierarchyReversed(): Sequence<BsonNode> = sequence {
		var cursor: PipelineChainLink? = this@PipelineChainLink

		while (cursor != null) {
			if (cursor.current != null)
				yield(cursor.current)

			cursor = cursor.previous
		}
	}

	/**
	 * Converts this chain to a list of expressions.
	 *
	 * The first element of the returned list is the root of the chain, followed by the second element of the chain, etc.
	 */
	@LowLevelApi
	fun toList(): List<BsonNode> =
		hierarchyReversed().toList().reversed()

	/**
	 * Converts this chain in a list of BSON documents, each representing a stage.
	 *
	 * The first element of the returned list is the root of the chain, followed by the second element of the chain, etc.
	 */
	@LowLevelApi
	fun toBsonList(): List<Bson> =
		hierarchyReversed()
			.map { context.buildDocument { it.writeTo(this) } }
			.toList().reversed()

	/**
	 * Equivalent to [Pipeline.writeTo].
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
	override fun toString(): String = context.buildArray {
		writeTo(this)
	}.toString()

}

/**
 * Helper class to implement [Pipeline].
 *
 * ### Notes for implementors
 *
 * When implementing a new type of pipeline, the main requirement is to override the return tpe of all existing stage
 * methods to return the same type as the current instance. This sadly has to be done manually because Kotlin doesn't
 * have self-types.
 *
 * When overriding the stage methods, avoid doing anything other than down-casting the resulting pipeline.
 *
 * You will also need to implement [withStage].
 * Note how creating an instance of [AbstractPipeline] requires passing a [PipelineChainLink].
 * [PipelineChainLink] implements all complex methods from [Pipeline] for you.
 */
abstract class AbstractPipeline<Output : Any> @OptIn(LowLevelApi::class) constructor(

	@property:LowLevelApi
	override val context: BsonContext,

	/**
	 * Internal representation of the pipeline state.
	 */
	@property:LowLevelApi
	val chain: PipelineChainLink,
) : Pipeline<Output> {

	/**
	 * Creates a new pipeline that expands on the current one by adding [stage].
	 *
	 * For usage documentation, see [Pipeline.withStage].
	 *
	 * ### Notes for implementors
	 *
	 * A typical pipeline implementation will look like:
	 * ```kotlin
	 * class YourPipelineType<Output : Any>(
	 *     context: BsonContext,
	 *     chain: PipelineChainLink,
	 * ): AbstractPipeline<Output>(context, chain) {
	 *     // …
	 *
	 *     override fun withStage(stage: Expression): YourPipelineType<Output> =
	 *         YourPipelineType(context, chain.withStage(expression))
	 *
	 *     // …
	 * }
	 * ```
	 */
	@DangerousMongoApi
	@LowLevelApi
	abstract override fun withStage(stage: BsonNode): Pipeline<Output>

	@LowLevelApi
	final override fun writeTo(writer: BsonValueWriter) {
		chain.writeTo(writer)
	}

	/**
	 * JSON representation of this pipeline.
	 */
	@OptIn(LowLevelApi::class)
	final override fun toString(): String =
		chain.toString()

}
