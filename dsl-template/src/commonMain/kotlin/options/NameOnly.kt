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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.CollectionInfo

/**
 * Specifies that only collection names should be returned.
 *
 * See [HasNameOnly.nameOnly] and [opensavvy.ktmongo.dsl.command.ListCollections].
 */
class NameOnlyOption(
	/**
	 * `true` to return only collection names.
	 */
	val only: Boolean,
	context: BsonContext,
) : AbstractOption("nameOnly", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeBoolean(only)
	}
}

/**
 * Specifies that only collection names should be returned.
 *
 * See [HasNameOnly.nameOnly] and [opensavvy.ktmongo.dsl.command.ListCollections].
 */
interface HasNameOnly : Options {

	/**
	 * Controls the information present in [CollectionInfo].
	 *
	 * If `true`, only [CollectionInfo.name] and [CollectionInfo.type] (depending on the driver)
	 * are returned.
	 *
	 * ```kotlin
	 * client.database("my-project")
	 *     .collections {
	 *         nameOnly()
	 *     }
	 *     .forEach {
	 *         println("Collection: ${it.name}")
	 *     }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/listcollections/#syntax)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun nameOnly(only: Boolean = true) {
		accept(NameOnlyOption(only, context))
	}

}
