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

package opensavvy.ktmongo.sync.kmongo

import com.mongodb.kotlin.client.MongoCollection
import opensavvy.ktmongo.sync.JvmMongoCollection
import opensavvy.ktmongo.sync.asKtMongo
import opensavvy.ktmongo.utils.kmongo.KMongoNameStrategy

/**
 * Converts a collection from the official Java MongoDB driver into a KtMongo collection.
 */
fun <T : Any> com.mongodb.client.MongoCollection<T>.asKtMongo(): JvmMongoCollection<T> =
	MongoCollection(this).asKtMongo(nameStrategy = KMongoNameStrategy())
