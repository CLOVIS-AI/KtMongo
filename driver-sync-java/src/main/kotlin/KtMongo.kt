/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

import com.mongodb.client.MongoCollection

/**
 * Entry-point to convert objects from the official drivers to the KtMongo library.
 *
 * To learn more about the syntax of the different operators, see the top-level functions in
 * this package.
 */
object KtMongo {

	/**
	 * Converts a Java MongoDB collection into a KtMongo collection.
	 *
	 * ### Example (Java)
	 *
	 * ```java
	 * try (var client = MongoClients.create()) {
	 *     var javaCollection = client.getDatabase("my-db")
	 *         .getCollection("my-col", MyCollection.class);
	 *
	 *     var collection = KtMongo.from(javaCollection);
	 *
	 *     System.out.println(
	 *         collection.find().toList()
	 *     );
	 * }
	 * ```
	 */
	@JvmStatic
	fun <T : Any> from(driver: MongoCollection<T>): JvmMongoCollection<T> =
		from(com.mongodb.kotlin.client.MongoCollection(driver))

	/**
	 * Converts a Kotlin MongoDB collection into a KtMongo collection.
	 *
	 * ### Example (Kotlin)
	 *
	 * ```kotlin
	 * MongoClients.create().use { client ->
	 *     val kotlinCollection = it.getDatabase("my-db")
	 *         .getCollection("my-col", MyCollection::class.java)
	 *
	 *     val collection = KtMongo.from(kotlinCollection)
	 *
	 *     println(
	 *         collection.find().toList()
	 *     )
	 * }
	 * ```
	 */
	@JvmStatic
	fun <T : Any> from(driver: com.mongodb.kotlin.client.MongoCollection<T>): JvmMongoCollection<T> =
		driver.asKtMongo()
}
