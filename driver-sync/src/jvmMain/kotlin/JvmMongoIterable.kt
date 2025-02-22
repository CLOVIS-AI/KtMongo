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

package opensavvy.ktmongo.sync

import com.mongodb.kotlin.client.MongoCursor
import opensavvy.ktmongo.dsl.LowLevelApi
import java.util.*
import java.util.Spliterator.*
import java.util.function.Consumer
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Implementation of [MongoIterable] based on [MongoDB's MongoIterable][com.mongodb.kotlin.client.MongoIterable].
 *
 * To access the inner iterable, see [asKotlinMongoIterable].
 *
 * To convert an existing MongoDB iterable into an instance of this class, see [asKtMongo].
 */
class JvmMongoIterable<Document : Any> internal constructor(
	private val inner: com.mongodb.kotlin.client.MongoIterable<Document>,
	private val repr: (() -> String)? = null,
) : MongoIterable<Document> {

	/**
	 * Converts a KtMongo [MongoIterable] into a [MongoDB MongoIterable][com.mongodb.kotlin.client.MongoIterable].
	 */
	@LowLevelApi
	fun asKotlinMongoIterable() = inner

	override fun first(): Document =
		inner.first()

	override fun firstOrNull(): Document? =
		inner.firstOrNull()

	override fun forEach(action: (Document) -> Unit) {
		inner.forEach(action)
	}

	override fun toList(): List<Document> =
		inner.toList()

	/**
	 * Streams the results of this query into a Java [Stream].
	 */
	fun asStream(): Stream<Document> {
		val cursor = inner.cursor()

		return StreamSupport.stream<Document>(
			/* spliterator = */ MongoSpliterator<Document>(cursor),
			/* parallel = */ false,
		).onClose { cursor.close() }
	}

	private class MongoSpliterator<Document : Any>(
		private val cursor: MongoCursor<Document>,
	) : Spliterator<Document> {
		override fun tryAdvance(action: Consumer<in Document>): Boolean {
			if (cursor.hasNext()) {
				action.accept(cursor.next())
				return true
			} else {
				return false
			}
		}

		override fun trySplit(): Spliterator<Document?>? {
			return null
		}

		override fun estimateSize(): Long {
			return Long.MAX_VALUE
		}

		override fun characteristics(): Int =
			ORDERED + NONNULL + IMMUTABLE
	}

	override fun toString(): String =
		repr?.invoke() ?: super.toString()
}

/**
 * Converts a [MongoDB MongoIterable][com.mongodb.kotlin.client.MongoIterable] into a
 * [KtMongo MongoIterable][JvmMongoIterable].
 */
fun <Document : Any> com.mongodb.kotlin.client.MongoIterable<Document>.asKtMongo(): JvmMongoIterable<Document> =
	JvmMongoIterable(this)
