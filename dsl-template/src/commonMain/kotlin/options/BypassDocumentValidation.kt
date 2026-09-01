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

/**
 * Whether this operation is allowed to bypass document validation.
 *
 * See [HasBypassDocumentValidation.bypassDocumentValidation].
 */
class BypassDocumentValidationOption(
	val bypassDocumentValidation: Boolean,
	context: BsonContext,
) : AbstractOption("bypassDocumentValidation", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeBoolean(bypassDocumentValidation)
	}
}

/**
 * Whether this operation is allowed to bypass document validation.
 *
 * See [HasBypassDocumentValidation.bypassDocumentValidation].
 */
interface HasBypassDocumentValidation : Options {

	/**
	 * Whether this operation is allowed to bypass document validation.
	 *
	 * By default, or when called with `false`, this operation will need to respect the configured documentation
	 * validation for the current collection.
	 *
	 * If called with `true` or no parameters, this operation will succeed even if documentation validation should've
	 * blocked it.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * users.insertOne(
	 *     User("John Doe", 42),
	 *     options = { bypassDocumentValidation() }
	 * )
	 * ```
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun bypassDocumentValidation(bypass: Boolean = true) {
		accept(BypassDocumentValidationOption(bypass, context))
	}
}
