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

package opensavvy.ktmongo.sync

import com.mongodb.kotlin.client.AggregateIterable

private class SyncMongoAggregateIterableImpl<Document : Any>(
	private val inner: AggregateIterable<Document>,
) : SyncMongoAggregateIterable<Document> {

	override fun asOfficial(): AggregateIterable<Document> =
		inner

	override fun first(): Document =
		inner.first()

	override fun firstOrNull(): Document? =
		inner.firstOrNull()

	override fun forEach(action: (Document) -> Unit): Unit =
		inner.forEach(action)
}

/**
 * Instantiates a KtMongo [SyncMongoAggregateIterable] using an existing flow from the official Kotlin driver.
 */
fun <Document : Any> AggregateIterable<Document>.asKtMongo(): SyncMongoAggregateIterable<Document> =
	SyncMongoAggregateIterableImpl(this)
