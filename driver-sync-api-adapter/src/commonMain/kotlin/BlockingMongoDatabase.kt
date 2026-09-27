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

package opensavvy.ktmongo.sync.api.blocking

import opensavvy.ktmongo.api.MongoDatabase
import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.CollectionInfo
import opensavvy.ktmongo.dsl.command.ListCollectionsOptions
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import kotlin.reflect.KType
import opensavvy.ktmongo.sync.api.MongoDatabase as SyncMongoDatabase

class BlockingMongoDatabase(
	private val inner: SyncMongoDatabase,
) : MongoDatabase {
	override val name: String
		get() = inner.name

	override val factory: BsonFactory
		get() = inner.factory

	override val propertyNameStrategy: PropertyNameStrategy
		get() = inner.propertyNameStrategy

	override val objectIdGenerator: ObjectIdGenerator
		get() = inner.objectIdGenerator

	@LowLevelApi
	override val context: BsonContext
		get() = inner.context

	@LowLevelApi
	override fun <Document : Any> collection(name: String, type: KType): BlockingMongoCollection<Document> =
		BlockingMongoCollection(inner.collection(name, type))

	override fun collections(options: ListCollectionsOptions.() -> Unit, filter: FilterQuery<CollectionInfo>.() -> Unit): MongoIterable<CollectionInfo> =
		BlockingMongoIterable(inner.collections(options, filter))

	override fun toString(): String =
		inner.toString()
}
