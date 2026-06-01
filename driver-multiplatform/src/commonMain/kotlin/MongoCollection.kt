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

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.InsertOneOptions
import kotlin.reflect.KType

/**
 * A collection stores related documents together.
 *
 * Usually, all documents in a collection have the same shape (the same fields).
 * However, heterogeneous structure can be achieved by using:
 * - Kotlin collections, like [List] and [Set], the embed an arbitrary number of items.
 * - Polymorphism, for example with `sealed class`, to have different fields based on a discriminator.
 *
 * To avoid name collisions, collections are grouped into [databases][MongoDatabase].
 *
 * To obtain a collection, see [MongoDatabase.collection].
 *
 * ### Size limit
 *
 * A MongoDB document cannot exceed 16 MiB.
 *
 * You can measure the size of a document with [opensavvy.ktmongo.bson.BsonDocument.toByteArray]
 * followed by [ByteArray.size].
 */
interface MongoCollection<Document : Any> : ObjectIdGenerator {

	/**
	 * The [MongoDatabase] that contains this collection.
	 */
	val database: MongoDatabase

	/**
	 * THe name of this collection.
	 *
	 * The collection name must be unique within a single [database] (otherwise, the two instances refer to the same data).
	 */
	val name: String

	/**
	 * The concatenation of the database's [name][MongoDatabase.name] and the collection's [name].
	 */
	val fullyQualifiedName: String
		get() = "${database.name}.$name"

	override fun newId(): ObjectId =
		database.client.context.newId()

	suspend fun insertOne(
		document: Document,
		options: InsertOneOptions<Document>.() -> Unit = {},
	)

	/**
	 * The [KType] instance that corresponds to the collection's document type.
	 *
	 * This property is used by serialization libraries to know the exact type to deserialize,
	 * especially in the presence of type parameters.
	 *
	 * Everyday users should not need to interact with this property directly.
	 */
	@LowLevelApi
	val type: KType

}
