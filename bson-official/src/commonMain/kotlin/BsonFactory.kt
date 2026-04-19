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

package opensavvy.ktmongo.bson.official

import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType

/**
 * Creates KtMongo [BsonDocument] and [BsonArray] instances by wrapping the equivalents from the official drivers.
 */
expect class BsonFactory : BsonFactory {

	@LowLevelApi
	override fun buildDocument(block: BsonFieldWriter.() -> Unit): BsonDocument

	@LowLevelApi
	override fun buildDocument(instance: BsonFieldWriteable): BsonDocument

	@LowLevelApi
	override fun <T : Any> encode(obj: T, type: KType): BsonDocument

	@LowLevelApi
	override fun readDocument(bytes: ByteArray): BsonDocument

	@LowLevelApi
	override fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray

	@LowLevelApi
	override fun buildArray(instance: BsonValueWriteable): BsonArray

	@LowLevelApi
	override fun readArray(bytes: ByteArray): BsonArray
}
