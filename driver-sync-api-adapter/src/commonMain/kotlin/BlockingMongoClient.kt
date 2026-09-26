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

import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import opensavvy.ktmongo.sync.api.MongoClient as SyncMongoClient

class BlockingMongoClient(
	private val inner: SyncMongoClient,
) : MongoClient {

	override val factory: BsonFactory
		get() = inner.factory

	override val objectIdGenerator: ObjectIdGenerator
		get() = inner.objectIdGenerator

	override val propertyNameStrategy: PropertyNameStrategy
		get() = inner.propertyNameStrategy

	override val context: BsonContext
		get() = inner.context

	override fun database(
		name: String,
		factory: BsonFactory,
		objectIdGenerator: ObjectIdGenerator,
		propertyNameStrategy: PropertyNameStrategy,
	): BlockingMongoDatabase =
		BlockingMongoDatabase(inner.database(name, factory, objectIdGenerator, propertyNameStrategy))

	override suspend fun close() = wrapBlocking {
		inner.close()
	}

	override fun toString(): String =
		inner.toString()
}
