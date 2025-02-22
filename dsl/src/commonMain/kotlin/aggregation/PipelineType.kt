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

import opensavvy.ktmongo.dsl.aggregation.stages.*

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
interface AggregationPipeline<Document : Any> : Pipeline<Document>,
	HasLimit<Document>,
	HasMatch<Document>,
	HasProject<Document>,
	HasSample<Document>,
	HasSet<Document>,
	HasSkip<Document>,
	HasSort<Document>,
	HasUnionWith<Document>,
	HasUnionWithCompatibility<Document>,
	HasUnset<Document>

/**
 * An update pipeline.
 *
 * Update pipelines allow more complex updates without resorting to an entire [AggregationPipeline].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/#update-with-aggregation-pipeline)
 */
interface UpdatePipeline<Document : Any> : Pipeline<Document>,
	HasProject<Document>,
	HasSet<Document>,
	HasUnset<Document>
