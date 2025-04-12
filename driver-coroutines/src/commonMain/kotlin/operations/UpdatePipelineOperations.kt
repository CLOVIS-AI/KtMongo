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

package opensavvy.ktmongo.coroutines.operations

import opensavvy.ktmongo.dsl.options.UpdateOptions
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.UpdatePipelineOperators

/**
 * Interface grouping MongoDB operations allowing to update existing information using aggregation pipelines.
 */
interface UpdatePipelineOperations<Document : Any> : BaseOperations {

	/**
	 * Updates all documents that match [filter] according to the [update] pipeline.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.updateManyWithPipeline(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     }
	 * ) {
	 *     set {
	 *         User::age set 15
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/#update-with-an-aggregation-pipeline)
	 *
	 * @param filter Optional filter to select which documents are updated.
	 * If no filter is specified, all documents are updated.
	 * @see updateOneWithPipeline Update a single document.
	 */
	suspend fun updateManyWithPipeline(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpdatePipelineOperators<Document>.() -> Unit,
	)

	/**
	 * Updates a single document that matches [filter] according to the [update] pipeline.
	 *
	 * If multiple documents match [filter], only the first one found is updated.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.updateOneWithPipeline(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     }
	 * ) {
	 *     set {
	 *         User::age set 15
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/#update-with-an-aggregation-pipeline)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateManyWithPipeline Update multiple documents.
	 * @see upsertOneWithPipeline Update a document, creating it if it doesn't exist.
	 */
	suspend fun updateOneWithPipeline(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpdatePipelineOperators<Document>.() -> Unit,
	)

	/**
	 * Updates a single document that matches [filter] according to the [update] pipeline.
	 *
	 * If multiple documents match [filter], only the first one is updated.
	 *
	 * If no documents match [filter], a new one is created.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.upsertOneWithPipeline(
	 *     filter = {
	 *          User::name eq "Patrick"
	 *     }
	 * ) {
	 *     set {
	 *         User::age set 15
	 *     }
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/#update-with-an-aggregation-pipeline)
	 *
	 * @see updateOneWithPipeline Do nothing if the document doesn't already exist.
	 */
	suspend fun upsertOneWithPipeline(
		options: UpdateOptions<Document>.() -> Unit = {},
		filter: FilterQuery<Document>.() -> Unit = {},
		update: UpdatePipelineOperators<Document>.() -> Unit,
	)

}
