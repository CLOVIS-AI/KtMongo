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

package opensavvy.ktmongo.multiplatform

import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.CollectionInfo
import opensavvy.ktmongo.dsl.command.ListCollections
import opensavvy.ktmongo.dsl.command.ListCollectionsOptions
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.dsl.query.FilterQuery
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KType

internal class MultiplatformMongoDatabaseImpl(
	override val client: MultiplatformMongoClient,
	override val factory: BsonFactory,
	override val objectIdGenerator: ObjectIdGenerator,
	override val propertyNameStrategy: PropertyNameStrategy,
	override val name: String,
) : MultiplatformMongoDatabase, MultiplatformNamespace {

	override val namespace: String
		get() = name

	@OptIn(ExperimentalAtomicApi::class)
	override val context: BsonContext by lazy {
		BsonContext(factory, objectIdGenerator, propertyNameStrategy)
	}

	@LowLevelApi
	override fun <Document : Any> collection(name: String, type: KType): MultiplatformMongoCollection<Document> =
		MultiplatformMongoCollectionImpl(this, name, type, factory, propertyNameStrategy, objectIdGenerator)

	override fun toString(): String =
		"MultiplatformMongoDatabase($name)"

	// region List collections

	@OptIn(LowLevelApi::class)
	override fun collections(
		options: ListCollectionsOptions.() -> Unit,
		filter: FilterQuery<CollectionInfo>.() -> Unit,
	): MongoIterable<CollectionInfo> {
		val command = ListCollections(context).apply {
			this.options.options()
			this.filter.filter()
		}

		return MultiplatformMongoIterableListCollectionsImpl(
			database = this,
			command = command,
		).map { MultiplatformCollectionInfo(it) }
	}

	// endregion
}
