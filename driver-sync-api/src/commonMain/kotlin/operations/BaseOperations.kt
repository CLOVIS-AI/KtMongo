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

package opensavvy.ktmongo.sync.api.operations

import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.path.PropertyNameStrategy

/**
 * The common interface to all operations interfaces.
 */
interface BaseOperations {

	/**
	 * The [BsonFactory] used to serialize and deserialize values.
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
	 * The algorithm used to generate new [ObjectId] instances.
	 *
	 * For more information, see [ObjectIdGenerator].
	 */
	val objectIdGenerator: ObjectIdGenerator

	/**
	 * The full BSON configuration, used by the DSL to generate queries.
	 *
	 * For more information, see [BsonContext].
	 */
	val context: BsonContext
}
