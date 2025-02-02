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

package opensavvy.ktmongo.sync.operations

import opensavvy.ktmongo.sync.MongoAggregationPipeline

/**
 * Interface grouping MongoDB operations relating to aggregation pipelines
 */
interface AggregationOperations<Document : Any> : BaseOperations {

	/**
	 * Start an aggregation pipeline.
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
	 *     .match { User::age gt 18 }
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/aggregation-pipeline/)
	 */
	fun aggregate(): MongoAggregationPipeline<Document>

}
