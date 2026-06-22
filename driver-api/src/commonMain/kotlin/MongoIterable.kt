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

import kotlinx.coroutines.flow.Flow

/**
 * Streaming-capable iterable cursor to read data from the database.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/cursors/)
 */
interface MongoIterable<Document : Any> {

	/**
	 * Returns the first document found by this query, or throws an exception.
	 *
	 * @throws NoSuchElementException If this query returned no results.
	 * @see firstOrNull Return `null` instead of throwing an exception.
	 */
	suspend fun first(): Document

	/**
	 * Returns the first document found by this query, or returns `null`.
	 *
	 * @see first Throw an exception instead of returning `null`.
	 */
	suspend fun firstOrNull(): Document?

	/**
	 * Executes [action] for each document returned by this query.
	 *
	 * This method streams all returned documents into the [action] function.
	 * The entire response set is not loaded at once into memory.
	 *
	 * MongoDB cursors are batched: a batch is queried, processed, then another batch is requested, etc.
	 * The batch size can be configured in the operation creating this iterable.
	 *
	 * If the operation contains a sort without an index, MongoDB will load all results
	 * into memory. The driver will still stream the results.
	 *
	 * @see toList Store all results in a [List].
	 * @see toSet Store all results in a [Set].
	 * @see asFlow Stream all results in a [Flow].
	 */
	suspend fun forEach(action: suspend (Document) -> Unit)

	/**
	 * Reads the entirety of this iterable into a [List].
	 *
	 * Since lists are in-memory, this method loads the entirety of the results into memory.
	 *
	 * @see forEach Execute an action for each result.
	 * @see toSet Store all results in a [Set].
	 * @see asFlow Stream all results in a [Flow].
	 */
	suspend fun toList(): List<Document> {
		val list = ArrayList<Document>()
		forEach { list.add(it) }
		return list
	}

	/**
	 * Reads the entirety of this iterable into a [Set].
	 *
	 * Since sets are in-memory, this method loads the entirety of the results into memory.
	 *
	 * @see forEach Execute an action for each result.
	 * @see toList Store all results in a [List].
	 * @see asFlow Stream all results in a [Flow].
	 */
	suspend fun toSet(): Set<Document> {
		val set = HashSet<Document>()
		forEach { set.add(it) }
		return set
	}

	/**
	 * Streams the results into a [Flow].
	 *
	 * The flow is lazy: new elements are streamed in when the consumer requests them.
	 *
	 * MongoDB cursors are batched: a batch is queried, processed, then another batch is requested, etc.
	 * The batch size can be configured in the operation creating this iterable.
	 *
	 * If you intend to query a large number of batches and
	 * perform complex operations on them, we recommend using
	 * [buffer][kotlinx.coroutines.flow.buffer] with a low capacity,
	 * to reduce latency between two batches.
	 *
	 * @see forEach Execute an action for each result.
	 * @see toList Store all results in a [List].
	 * @see toSet Store all results in a [Set].
	 */
	fun asFlow(): Flow<Document>

	/**
	 * This method always throws an exception.
	 *
	 * Streaming a [MongoIterable] into a [Sequence] is not supported, because iterables
	 * must be closed, and sequences cannot detect when iteration finishes.
	 *
	 * If you want to use a [Sequence] data structure for convenience, and the volume of data
	 * is low, use [toList] followed by [asSequence][List.asSequence].
	 * All data will be loaded in memory at once.
	 *
	 * If streaming is important, either use [forEach], [asFlow] or `stream` (Java-only).
	 *
	 * @see forEach Execute an action for each result.
	 * @see toList Store all results in a [List].
	 * @see toSet Store all results in a [Set].
	 * @see asFlow Stream all results in a [Flow].
	 */
	@Deprecated("Kotlin Sequences are not capable of closing a resource after they are done. Using sequences with a MongoIterable will create memory leaks. Instead, use toList, forEach, stream (Java only) or the coroutines driver's asFlow", ReplaceWith("this.toList().asSequence()"), level = DeprecationLevel.ERROR)
	fun asSequence(): Sequence<Document> = throw UnsupportedOperationException("Sequences are not supported because they create memory lists. Use lists, streams, flows, or simply forEach instead.")

	/**
	 * This method always throws an exception.
	 *
	 * Streaming a [MongoIterable] into a [Sequence] is not supported, because iterables
	 * must be closed, and sequences cannot detect when iteration finishes.
	 *
	 * If you want to use a [Sequence] data structure for convenience, and the volume of data
	 * is low, use [toList] followed by [asSequence][List.asSequence].
	 * All data will be loaded in memory at once.
	 *
	 * If streaming is important, either use [forEach], [asFlow] or `stream` (Java-only).
	 *
	 * @see forEach Execute an action for each result.
	 * @see toList Store all results in a [List].
	 * @see toSet Store all results in a [Set].
	 * @see asFlow Stream all results in a [Flow].
	 */
	@Deprecated("Kotlin Sequences are not capable of closing a resource after they are done. Using sequences with a MongoIterable will create memory leaks. Instead, use toList, forEach, stream (Java only) or the coroutines driver's asFlow", ReplaceWith("this.toList().asSequence()"), level = DeprecationLevel.ERROR)
	fun toSequence(): Sequence<Document> = throw UnsupportedOperationException("Sequences are not supported because they create memory lists. Use lists, streams, flows, or simply forEach instead.")

}
