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

package opensavvy.ktmongo.coroutines

import kotlinx.coroutines.flow.Flow

/**
 * Streaming-ready iterable client to read data from the database.
 */
interface MongoIterable<Document : Any> {

	// region Extract data

	/**
	 * Returns the first document found by this query, or throws an exception.
	 *
	 * @throws NoSuchElementException If this query returned no results.
	 * @see firstOrNull
	 */
	suspend fun first(): Document =
		firstOrNull() ?: throw NoSuchElementException("No element was returned by this query")

	/**
	 * Returns the first document found by this query, or `null` if none were found.
	 *
	 * @see first
	 */
	suspend fun firstOrNull(): Document?

	/**
	 * Executes [action] for each document returned by this query.
	 *
	 * This method streams all returned elements into the [action] function.
	 * The entire response is not loaded at once into memory.
	 */
	suspend fun forEach(action: suspend (Document) -> Unit)

	// endregion
	// region To another data structure

	/**
	 * Reads the entirety of this response into a [List].
	 *
	 * Since lists are in-memory, this will load the entirety of the results of this query into memory.
	 *
	 * @see toSet
	 */
	suspend fun toList(): List<Document> {
		val list = ArrayList<Document>()
		forEach { list.add(it) }
		return list
	}

	/**
	 * Reads the entirety of this response into a [Set].
	 *
	 * Since sets are in-memory, this will load the entirety of the results of this query into memory.
	 *
	 * @see toList
	 */
	suspend fun toSet(): Set<Document> {
		val set = LinkedHashSet<Document>()
		forEach { set.add(it) }
		return set
	}

	/**
	 * Streams the results of this response into a [Flow].
	 */
	fun asFlow(): Flow<Document>

	@Deprecated("Kotlin Sequences are not capable of closing a resource after they are done. Using sequences with a MongoIterable will create memory leaks. Instead, use toList, forEach, asStream (Java only) or the coroutines driver's asFlow", ReplaceWith("this.toList().asSequence()"), level = DeprecationLevel.ERROR)
	fun asSequence(): Sequence<Document> = throw UnsupportedOperationException("Sequences are not supported because they create memory lists. Use lists, streams, flows, or simply forEach instead.")

	@Deprecated("Kotlin Sequences are not capable of closing a resource after they are done. Using sequences with a MongoIterable will create memory leaks. Instead, use toList, forEach, asStream (Java only) or the coroutines driver's asFlow", ReplaceWith("this.toList().asSequence()"), level = DeprecationLevel.ERROR)
	fun toSequence(): Sequence<Document> = throw UnsupportedOperationException("Sequences are not supported because they create memory lists. Use lists, streams, flows, or simply forEach instead.")

	// endregion
}

internal interface LazyMongoIterable<Document : Any> : MongoIterable<Document> {
	fun asIterable(): MongoIterable<Document>

	override suspend fun firstOrNull(): Document? =
		asIterable().firstOrNull()

	override suspend fun forEach(action: suspend (Document) -> Unit) =
		asIterable().forEach(action)

	override fun asFlow(): Flow<Document> =
		asIterable().asFlow()
}
