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

package opensavvy.ktmongo.api

import opensavvy.ktmongo.api.operations.*
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy
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
 *
 * The maximum nesting is 100 levels.
 * Each document or array adds a level.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/databases-and-collections/)
 * - [Size limits](https://www.mongodb.com/docs/manual/reference/limits/#bson-documents)
 */
interface MongoCollection<Document : Any> : ObjectIdGenerator,
	CollectionOperations<Document>,
	CountOperations<Document>,
	DeleteOperations<Document>,
	FindOperations<Document>,
	InsertOperations<Document>,
	UpdateOperations<Document>,
	UpdatePipelineOperations<Document> {

	/**
	 * THe name of this collection.
	 *
	 * The collection name must be unique within a single [database] (otherwise, the two instances refer to the same data).
	 *
	 * - The name should begin with a letter or an underscore (`_`).
	 * - The name cannot be empty.
	 * - The name cannot contain the null character nor the `$` character.
	 * - The name cannot being with `system.`.
	 * - The name cannot contain `.system.`.
	 * - It is recommended to avoid names longer than 171 bytes.
	 *
	 * ### External resources
	 *
	 * - [Name restrictions](https://www.mongodb.com/docs/manual/reference/limits/#mongodb-limit-Restriction-on-Collection-Names)
	 */
	val name: String

	/**
	 * The concatenation of the database's [name][MongoDatabase.name] and the collection's [name],
	 * separated by a dot (`.`).
	 */
	val fullyQualifiedName: String

	/**
	 * The [BsonFactory] used to serialize and deserialize values stored in this collection.
	 *
	 * This property stores all serialization configurations and allows creating custom BSON objects.
	 *
	 * For more information, see [BsonFactory].
	 */
	val factory: BsonFactory

	/**
	 * The strategy used to convert property names to BSON document keys.
	 *
	 * For more information, see [PropertyNameStrategy].
	 */
	val propertyNameStrategy: PropertyNameStrategy

	/**
	 * The algorithm used to generate new [ObjectId] instances for this collection.
	 *
	 * For more information, see [ObjectIdGenerator].
	 *
	 * You can also directly call [newId] on the collection itself.
	 */
	val objectIdGenerator: ObjectIdGenerator

	override fun newId(): ObjectId =
		objectIdGenerator.newId()

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
