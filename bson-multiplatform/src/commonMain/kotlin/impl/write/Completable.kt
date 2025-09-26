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

package opensavvy.ktmongo.bson.multiplatform.impl.write

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

@LowLevelApi
internal interface CompletableBsonFieldWriter : BsonFieldWriter {
	@LowLevelApi
	fun complete()

	@DangerousMongoApi
	fun open(name: String): CompletableBsonValueWriter

	@DangerousMongoApi
	fun openDocument(name: String): CompletableBsonFieldWriter

	@DangerousMongoApi
	fun openArray(name: String): CompletableBsonValueWriter
}

@LowLevelApi
internal interface CompletableBsonValueWriter : BsonValueWriter {
	@LowLevelApi
	fun complete()

	@DangerousMongoApi
	fun openDocument(): CompletableBsonFieldWriter

	@DangerousMongoApi
	fun openArray(): CompletableBsonValueWriter
}
