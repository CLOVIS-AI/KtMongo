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
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * BSON implementation based on the official MongoDB drivers.
 */
interface BsonContext : BsonContext {

	@LowLevelApi
	override fun buildDocument(block: BsonFieldWriter.() -> Unit): Bson

	@LowLevelApi
	override fun buildDocument(instance: BsonFieldWriteable): Bson =
		buildDocument { instance.writeTo(this) }

	@LowLevelApi
	override fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray

	@LowLevelApi
	override fun buildArray(instance: BsonValueWriteable): BsonArray =
		buildArray { instance.writeTo(this) }


	@LowLevelApi
	@DangerousMongoApi
	override fun openDocument(): CompletableBsonFieldWriter<Bson>

	@LowLevelApi
	@DangerousMongoApi
	override fun openArray(): CompletableBsonValueWriter<BsonArray>
}
