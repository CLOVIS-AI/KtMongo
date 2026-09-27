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

package opensavvy.ktmongo.sync.api.blocking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.sync.api.MongoIterable as SyncMongoIterable

class BlockingMongoIterable<Document : Any>(
	private val inner: SyncMongoIterable<Document>,
) : MongoIterable<Document> {
	override suspend fun first(): Document = wrapBlocking {
		inner.first()
	}

	override suspend fun firstOrNull(): Document? = wrapBlocking {
		inner.firstOrNull()
	}

	override fun <Out : Any> map(transform: suspend (Document) -> Out): MongoIterable<Out> =
		BlockingMongoIterableMappingImpl(this, transform)

	override suspend fun forEach(action: suspend (Document) -> Unit) =
		wrapBlocking {
			asFlow().collect(action)
		}

	override fun asFlow(): Flow<Document> = flow {
		emitAll(inner.toList().asFlow())
	}.flowOn(Dispatchers.IO)

	override fun toString(): String =
		inner.toString()
}

private class BlockingMongoIterableMappingImpl<In : Any, Out : Any>(
	private val upstream: MongoIterable<In>,
	private val transform: suspend (In) -> Out,
) : MongoIterable<Out> {
	override suspend fun first(): Out =
		transform(upstream.first())

	override suspend fun firstOrNull(): Out? =
		upstream.firstOrNull()?.let { transform(it) }

	override suspend fun forEach(action: suspend (Out) -> Unit) =
		upstream.forEach { action(transform(it)) }

	override fun asFlow(): Flow<Out> =
		upstream.asFlow().map(transform)

	override fun <NewOut : Any> map(transform: suspend (Out) -> NewOut): MongoIterable<NewOut> =
		BlockingMongoIterableMappingImpl(this, transform)

	override fun toString(): String =
		upstream.toString()
}
