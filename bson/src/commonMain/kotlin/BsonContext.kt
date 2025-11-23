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

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.bson.types.ObjectIdGenerator

/**
 * Configuration for the BSON serialization.
 *
 * Instances of this class are platform-specific and are used to create BSON documents.
 * Platforms can thus parameterize the behavior of writers and readers.
 *
 * For example, a platform may store its serialization configuration in this class.
 */
interface BsonContext : ObjectIdGenerator, BsonFactory {

	/**
	 * The naming strategy used to generate paths.
	 */
	val nameStrategy: PropertyNameStrategy
}
