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

package opensavvy.ktmongo.sync

import com.mongodb.kotlin.client.ListCollectionNamesIterable
import com.mongodb.kotlin.client.ListCollectionsIterable
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.dsl.command.CollectionInfo
import opensavvy.ktmongo.sync.api.MongoIterable
import org.bson.Document

private class SyncMongoListCollectionsIterableImpl(
	private val inner: ListCollectionsIterable<Document>,
	private val factory: BsonFactory,
	private val lazyStringRepresentation: (() -> String)?,
) : SyncMongoIterable<CollectionInfo> {

	private fun convert(doc: Document): CollectionInfo =
		SyncCollectionInfo(factory.readDocument(doc.toBsonDocument()))

	override fun first(): CollectionInfo =
		convert(inner.first())

	override fun firstOrNull(): CollectionInfo? =
		inner.firstOrNull()?.let(::convert)

	override fun forEach(action: (CollectionInfo) -> Unit): Unit =
		inner.forEach { action(convert(it)) }

	override fun toString(): String = lazyStringRepresentation?.invoke()
		?: super.toString()
}

internal fun ListCollectionsIterable<Document>.asKtMongo(
	factory: BsonFactory,
	lazyStringRepresentation: (() -> String)?,
): MongoIterable<CollectionInfo> =
	SyncMongoListCollectionsIterableImpl(this, factory, lazyStringRepresentation)

private class SyncMongoListCollectionsNameOnlyIterableImpl(
	private val inner: ListCollectionNamesIterable,
	private val factory: BsonFactory,
	private val lazyStringRepresentation: (() -> String)?,
) : SyncMongoIterable<CollectionInfo> {

	private fun convert(name: String): CollectionInfo =
		SyncCollectionInfoNameOnly(name, factory)

	override fun first(): CollectionInfo =
		convert(inner.first())

	override fun firstOrNull(): CollectionInfo? =
		inner.firstOrNull()?.let(::convert)

	override fun forEach(action: (CollectionInfo) -> Unit): Unit =
		inner.forEach { action(convert(it)) }

	override fun toString(): String = lazyStringRepresentation?.invoke()
		?: super.toString()
}

internal fun ListCollectionNamesIterable.asKtMongo(
	factory: BsonFactory,
	lazyStringRepresentation: (() -> String)?,
): MongoIterable<CollectionInfo> =
	SyncMongoListCollectionsNameOnlyIterableImpl(this, factory, lazyStringRepresentation)
