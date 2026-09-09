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

package opensavvy.ktmongo.dsl.command.errors

import opensavvy.ktmongo.dsl.command.Command

/**
 * Something went wrong while exchanging information with the database.
 */
sealed class MongoException(
	message: String,
	cause: Throwable? = null,
) : RuntimeException(message, cause) {

	/**
	 * The address of any given server.
	 *
	 * This is mostly useful when a command fails to identify which member of a replica set the command was sent to.
	 */
	interface ServerAddress {
		val host: String
		val port: Int
	}
}

/**
 * A write operation failed.
 */
class MongoWriteException(
	message: String = "Write failed",
	val server: ServerAddress,
	val command: Command,
	val errors: List<WriteErrorData>,
	val namespace: String,
	cause: Throwable? = null,
) : MongoException(
	message = buildString {
		appendLine(message)

		for (error in errors) {
			appendLine("\tfailed with ${error.message}")
		}

		appendLine("\tat ${command::class.simpleName} $command")
		appendLine("\tat $server $namespace")
	},
	cause = cause,
) {

	/**
	 * Information about a failed write.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/insert/#mongodb-data-insert.writeErrors)
	 */
	interface WriteErrorData {
		val code: Int
		val message: String
	}
}
