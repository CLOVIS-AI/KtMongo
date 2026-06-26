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

package opensavvy.ktmongo.api.operations

import opensavvy.ktmongo.api.MongoAggregationPipeline

/**
 * The different MongoDB operations related to aggregation pipelines.
 */
interface AggregationOperations<Document : Any> : BaseOperations {

	/**
	 * Starts an aggregation pipeline on this collection.
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
	 * - [`mongosh` documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.aggregate/)
	 */
	fun aggregate(): MongoAggregationPipeline<Document>

}
