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

import opensavvy.ktmongo.bson.BsonValueWriteable
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * User-defined message associated with an operation, which is repeated in the logs.
 *
 * See [WithComment.comment].
 */
class CommentOption(
	val comment: BsonValueWriteable,
	context: BsonContext,
) : AbstractOption("comment", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		comment.writeTo(this)
	}
}

/**
 * User-defined message associated with an operation, which is repeated in the logs.
 *
 * See [WithComment.comment].
 */
interface WithComment : Options {

	/**
	 * Specifies a user-defined message which will be associated with this information in the logs, etc.
	 *
	 * This option exists to easily find the query in the database logs. It does not impact the behavior of the request.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * users.insertOne(
	 *     User("Bob", 25),
	 *     options = { comment("register user") },
	 * )
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun comment(comment: String) {
		comment { writeString(comment) }
	}

	/**
	 * Specifies a user-defined message which will be associated with this information in the logs, etc.
	 *
	 * This option exists to easily find the query in the database logs. It does not impact the behavior of the request.
	 */
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun comment(comment: BsonValueWriteable) {
		accept(CommentOption(comment, context))
	}

	/**
	 * Specifies a user-defined message which will be associated with this information in the logs, etc.
	 *
	 * This option exists to easily find the query in the database logs. It does not impact the behavior of the request.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * users.insertOne(
	 *     User("Bob", 25),
	 *     options = {
	 *         comment {
	 *             writeDocument {
	 *                 writeString("request", "Register user")
	 *                 writeString("name", "Bob")
	 *             }
	 *         }
	 *     },
	 * )
	 * ```
	 */
	@OptIn(LowLevelApi::class) // The comment has no impact on the query, it's safe to use
	fun comment(comment: BsonValueWriter.() -> Unit) {
		comment(ArbitraryComment(comment, context))
	}

	/**
	 * Specifies a user-defined message which will be associated with this information in the logs, etc.
	 *
	 * This option exists to easily find the query in the database logs. It does not impact the behavior of the request.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * users.insertOne(
	 *     User("Bob", 25),
	 *     options = { comment(TelemetryInformation.current) },
	 * )
	 * ```
	 */
	@OptIn(LowLevelApi::class)
	fun <T> comment(comment: T, type: KType) {
		comment {
			writeSafe(comment, type)
		}
	}

	/**
	 * Specifies a user-defined message which will be associated with this information in the logs, etc.
	 *
	 * This option exists to easily find the query in the database logs. It does not impact the behavior of the request.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * users.insertOne(
	 *     User("Bob", 25),
	 *     options = { comment(TelemetryInformation.current) },
	 * )
	 * ```
	 */
	@Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
	final inline fun <reified T> comment(comment: T) {
		comment(comment, type = typeOf<T>())
	}

	@LowLevelApi
	private class ArbitraryComment(
		private val comment: BsonValueWriter.() -> Unit,
		private val context: BsonContext,
	) : BsonValueWriteable {

		@LowLevelApi
		override fun writeTo(writer: BsonValueWriter) {
			comment(writer)
		}

		override fun toString(): String =
			context.buildDocument {
				write("comment") {
					writeTo(this)
				}
			}.toString()
	}
}
