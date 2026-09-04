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

import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KType

internal class MultiplatformMongoDatabaseImpl(
	override val client: MultiplatformMongoClient,
	override val name: String,
) : MultiplatformMongoDatabase {

	@OptIn(ExperimentalAtomicApi::class)
	@LowLevelApi
	override fun <Document : Any> collection(name: String, type: KType): MultiplatformMongoCollection<Document> =
		MultiplatformMongoCollectionImpl(this, name, type, client.factory, PropertyNameStrategy.Default, ObjectIdGenerator.Default())

	override fun toString(): String =
		"MongoDatabase($name)"
}
