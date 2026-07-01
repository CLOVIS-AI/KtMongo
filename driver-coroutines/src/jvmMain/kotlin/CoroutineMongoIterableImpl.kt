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

import com.mongodb.kotlin.client.coroutine.FindFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

private class CoroutineMongoIterableImpl<Document : Any>(
	private val inner: FindFlow<Document>,
) : CoroutineMongoIterable<Document> {

	override fun asOfficial(): FindFlow<Document> =
		inner

	override suspend fun first(): Document =
		inner.first()

	override suspend fun firstOrNull(): Document? =
		inner.firstOrNull()

	override suspend fun forEach(action: suspend (Document) -> Unit): Unit =
		inner.collect(action)

	override fun asFlow(): Flow<Document> =
		inner
}

/**
 * Instantiates a KtMongo [CoroutineMongoIterable] using an existing flow from the official Kotlin driver.
 */
fun <Document : Any> FindFlow<Document>.asKtMongo(): CoroutineMongoIterable<Document> =
	CoroutineMongoIterableImpl(this)
