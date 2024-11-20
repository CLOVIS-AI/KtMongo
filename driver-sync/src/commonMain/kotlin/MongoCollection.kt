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

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.sync.operations.*

/**
 * Methods to interact with a MongoDB collection.
 *
 * ### Operations
 *
 * - [bulkWrite][UpdateOperations.bulkWrite]
 * - [count][CountOperations.count]
 * - [countEstimated][CountOperations.countEstimated]
 * - [deleteOne][DeleteOperations.deleteOne]
 * - [deleteMany][DeleteOperations.deleteMany]
 * - [drop][CollectionOperations.drop]
 * - [find][FindOperations.find]
 * - [findOne][FindOperations.findOne]
 * - [findOneAndUpdate][UpdateOperations.findOneAndUpdate]
 * - [insertOne][InsertOperations.insertOne]
 * - [insertMany][InsertOperations.insertMany]
 * - [updateOne][UpdateOperations.updateOne]
 * - [updateMany][UpdateOperations.updateMany]
 * - [upsertOne][UpdateOperations.upsertOne]
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/tutorial/query-documents)
 */
interface MongoCollection<Document : Any> :
	FindOperations<Document>,
	CountOperations<Document>,
	UpdateOperations<Document>,
	DeleteOperations<Document>,
	CollectionOperations<Document>,
	InsertOperations<Document>
