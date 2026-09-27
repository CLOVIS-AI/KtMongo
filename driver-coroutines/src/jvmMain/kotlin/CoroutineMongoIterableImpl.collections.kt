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

package opensavvy.ktmongo.coroutines

import com.mongodb.kotlin.client.coroutine.ListCollectionNamesFlow
import com.mongodb.kotlin.client.coroutine.ListCollectionsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import opensavvy.ktmongo.api.MongoIterable
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.dsl.command.CollectionInfo
import org.bson.Document

private class CoroutineMongoListCollectionsIterableImpl(
	private val inner: ListCollectionsFlow<Document>,
	private val factory: BsonFactory,
	private val lazyStringRepresentation: (() -> String)?,
) : CoroutineMongoIterable<CollectionInfo> {

	private fun convert(doc: Document): CollectionInfo =
		CoroutineCollectionInfo(factory.readDocument(doc.toBsonDocument()))

	override suspend fun first(): CollectionInfo =
		convert(inner.first())

	override suspend fun firstOrNull(): CollectionInfo? =
		inner.firstOrNull()?.let(::convert)

	override suspend fun forEach(action: suspend (CollectionInfo) -> Unit): Unit =
		inner.map(::convert).collect(action)

	override fun asFlow(): Flow<CollectionInfo> =
		inner.map(::convert)

	override fun toString(): String = lazyStringRepresentation?.invoke()
		?: super.toString()
}

internal fun ListCollectionsFlow<Document>.asKtMongo(
	factory: BsonFactory,
	lazyStringRepresentation: (() -> String)?,
): MongoIterable<CollectionInfo> =
	CoroutineMongoListCollectionsIterableImpl(this, factory, lazyStringRepresentation)

private class CoroutineMongoListCollectionsNameOnlyIterableImpl(
	private val inner: ListCollectionNamesFlow,
	private val factory: BsonFactory,
	private val lazyStringRepresentation: (() -> String)?,
) : CoroutineMongoIterable<CollectionInfo> {

	private fun convert(name: String): CollectionInfo =
		CoroutineCollectionInfoNameOnly(name, factory)

	override suspend fun first(): CollectionInfo =
		convert(inner.first())

	override suspend fun firstOrNull(): CollectionInfo? =
		inner.firstOrNull()?.let(::convert)

	override suspend fun forEach(action: suspend (CollectionInfo) -> Unit): Unit =
		inner.map(::convert).collect(action)

	override fun asFlow(): Flow<CollectionInfo> =
		inner.map(::convert)

	override fun toString(): String = lazyStringRepresentation?.invoke()
		?: super.toString()
}

internal fun ListCollectionNamesFlow.asKtMongo(
	factory: BsonFactory,
	lazyStringRepresentation: (() -> String)?,
): MongoIterable<CollectionInfo> =
	CoroutineMongoListCollectionsNameOnlyIterableImpl(this, factory, lazyStringRepresentation)
