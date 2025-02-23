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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Implementation of [MongoIterable] based on [MongoDB's FindFlow][com.mongodb.kotlin.client.coroutine.FindFlow].
 *
 * To access the inner iterable, see [asKotlinMongoIterable].
 *
 * To convert an existing MongoDB iterable into an instance of this class, see [asKtMongo].
 */
class JvmMongoIterable<Document : Any> internal constructor(
	private val inner: com.mongodb.kotlin.client.coroutine.FindFlow<Document>,
	private val repr: (() -> String)? = null,
) : MongoIterable<Document> {

	/**
	 * Converts a KtMongo [MongoIterable] into a [MongoDB FindFlow][com.mongodb.kotlin.client.coroutine.FindFlow].
	 */
	@LowLevelApi
	fun asKotlinMongoIterable() = inner

	override suspend fun first(): Document =
		inner.first()

	override suspend fun firstOrNull(): Document? =
		inner.firstOrNull()

	override suspend fun forEach(action: suspend (Document) -> Unit) {
		inner.collect(action)
	}

	override suspend fun toList(): List<Document> =
		inner.toList()

	override fun asFlow(): Flow<Document> =
		inner

	override fun toString(): String =
		repr?.invoke() ?: super.toString()
}

/**
 * Converts a [MongoDB FindFlow][com.mongodb.kotlin.client.coroutine.FindFlow] into a
 * [KtMongo MongoIterable][JvmMongoIterable].
 */
fun <Document : Any> com.mongodb.kotlin.client.coroutine.FindFlow<Document>.asKtMongo(): JvmMongoIterable<Document> =
	JvmMongoIterable(this)
