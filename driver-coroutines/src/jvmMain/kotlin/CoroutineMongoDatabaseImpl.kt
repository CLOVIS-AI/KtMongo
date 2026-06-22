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

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType

private class CoroutineMongoDatabaseImpl(
	private val inner: MongoDatabase,
) : CoroutineMongoDatabase {

	override fun asOfficial(): MongoDatabase =
		inner

	@LowLevelApi
	override fun <Document : Any> collection(name: String, type: KType): CoroutineMongoCollection<Document> {
		TODO("Not yet implemented")
	}

	override val name: String
		get() = inner.name

	override fun toString(): String =
		"CoroutineMongoDatabase($name)"
}

/**
 * Instantiates a KtMongo [CoroutineMongoDatabase] using an existing client from the official Kotlin driver.
 *
 * ### Example
 *
 * ```kotlin
 * import com.mongodb.kotlin.client.coroutine.MongoClient
 *
 * fun main() = runBlocking {
 *     val client = MongoClient.create(/* … */)
 *     val database = client.database("my-app")
 *         .asKtMongo()
 *
 *     val users = database.collection<UserDto>("users")
 *
 *     println("Users: ${users.count()}")
 * }
 * ```
 */
fun MongoDatabase.asKtMongo(): CoroutineMongoDatabase =
	CoroutineMongoDatabaseImpl(
		inner = this,
	)
