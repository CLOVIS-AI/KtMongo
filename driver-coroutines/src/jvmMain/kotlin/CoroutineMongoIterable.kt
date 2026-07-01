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

@file:JvmMultifileClass
@file:JvmName("KtMongo")

package opensavvy.ktmongo.coroutines

import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.FindFlow
import opensavvy.ktmongo.api.MongoIterable

/**
 * Streaming-capable iterable cursor to read data from the database.
 *
 * The Coroutine client provides a coroutine-aware API which internally uses the
 * [official Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
 *
 * This type wraps a [FindFlow] from the official driver.
 * See also [CoroutineMongoAggregateIterable].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/cursors/)
 */
interface CoroutineMongoFindIterable<Document : Any> : MongoIterable<Document> {

	/**
	 * Obtains the underlying MongoDB flow from the official Kotlin driver.
	 */
	fun asOfficial(): FindFlow<Document>

}

/**
 * Streaming-capable iterable cursor to read data from the database.
 *
 * The Coroutine client provides a coroutine-aware API which internally uses the
 * [official Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
 *
 * This type wraps a [AggregateFlow] from the official driver.
 * See also [CoroutineMongoFindIterable].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/cursors/)
 */
interface CoroutineMongoAggregateIterable<Document : Any> : MongoIterable<Document> {

	/**
	 * Obtains the underlying MongoDB flow from the official Kotlin driver.
	 */
	fun asOfficial(): AggregateFlow<Document>

}
