/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.aggregation.stages.HasMatch
import opensavvy.ktmongo.dsl.aggregation.stages.HasSample

/**
 * The super-type for all [pipeline][Pipeline] features.
 *
 * A pipeline feature is a marker interface placed on [pipeline types][PipelineType] to declare the availability
 * of one or more stages for that type.
 *
 * Therefore, all inheritors of this interface are the various kinds of stages.
 * Each of their inheritors is a type of pipeline in which that feature can be used.
 */
@DangerousMongoApi
interface PipelineFeature

/**
 * The different [pipeline][Pipeline] types.
 *
 * MongoDB pipelines are represented by the [Pipeline] class.
 * However, not all stages are available in all pipelines.
 *
 * Instances of this interface describe a pipeline type, meaning a given usage of MongoDB pipelines.
 * Instances describe which [features][PipelineFeature] are available in their context, by inheriting from them.
 */
// Not sealed, because we want to allow users to create their own pipeline types
// However, users cannot edit existing pipeline types, to avoid compatibility issues
interface PipelineType {

	/**
	 * Marker type for pipeline features that are available in aggregation pipelines.
	 */
	object Aggregate : PipelineType,
		HasMatch,
		HasSample

	/**
	 * Marker type for pipeline features that are available in update operations using a pipeline.
	 */
	object Update : PipelineType
}

/**
 * An aggregation pipeline.
 *
 * Aggregation pipelines read data from one or more collections and transform it in a manner of ways.
 * Finally, the data can be sent to the server, or written to another collection.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/aggregation-pipeline/)
 */
typealias AggregationPipeline<Document> = Pipeline<PipelineType.Aggregate, Document>

/**
 * An update pipeline.
 *
 * Update pipelines allow more complex updates without resorting to an entire [AggregationPipeline].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/#update-with-aggregation-pipeline)
 */
typealias UpdatePipeline<Document> = Pipeline<PipelineType.Update, Document>
