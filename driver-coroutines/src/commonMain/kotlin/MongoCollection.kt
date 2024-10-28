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

package opensavvy.ktmongo.coroutines

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.coroutines.operations.FindOperations
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Methods to interact with a MongoDB collection.
 *
 * ### Operations
 *
 * - [find][FindOperations.find]
 * - [findOne][FindOperations.findOne]
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-documents)
 */
interface MongoCollection<Document : Any> : FindOperations<Document> {

	@LowLevelApi
	val context: BsonContext
}
