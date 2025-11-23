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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.ReadPreference.Primary

/**
 * Specifies the read preference for an operation.
 *
 * To learn more about read preferences, see [ReadPreference].
 * To apply this option, see [WithReadPreference].
 */
class ReadPreferenceOption(
	val concern: ReadPreference,
	context: BsonContext,
) : AbstractOption("readPreference", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeString(concern.bsonName)
	}
}

/**
 * The read preference allows configuring which instance the request will be sent to.
 *
 * Operations which do not specify a read preference are sent to the [primary node][Primary].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/)
 */
enum class ReadPreference(val bsonName: String) {

	/**
	 * The query is sent to the primary node in the replica set.
	 *
	 * If the primary node is not available, the query throws an exception.
	 *
	 * This is the default option.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/#mongodb-readmode-primary)
	 */
	Primary("primary"),

	/**
	 * The query is sent to the primary node in the replica set.
	 * If the primary node is not available (e.g. during a failover), the operation reads from secondary nodes.
	 *
	 * Read operations using this preference may return stale data.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/#mongodb-readmode-primary)
	 */
	PrimaryPreferred("primaryPreferred"),

	/**
	 * The query is sent only to secondary members of the replica set.
	 *
	 * If no secondaries are available, the query throws an exception.
	 *
	 * Read operations using this preference may return stale data.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/#mongodb-readmode-secondary)
	 */
	Secondary("secondary"),

	/**
	 * The query is sent to the secondary members of the replica set.
	 *
	 * If the replica set doesn't have secondaries (e.g. they all crashed), then the query is sent to the primary.
	 *
	 * Read operations using this preference may return stale data.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/#mongodb-readmode-secondaryPreferred)
	 */
	SecondaryPreferred("secondaryPreferred"),

	/**
	 * The driver reads from a member whose network latency falls within the acceptable latency window.
	 * The driver does not take into account whether that member is a primary or secondary.
	 *
	 * Set this mode to minimize the effect of network latency on read operations without preference for
	 * current or stale data.
	 *
	 * Read operations using this preference may return stale data.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/#mongodb-readmode-nearest)
	 */
	Nearest("nearest"),
	;
}

/**
 * Declares which nodes should receive the request.
 *
 * See [readPreference].
 */
interface WithReadPreference : Options {

	/**
	 * Specifies the [ReadPreference] for this operation.
	 *
	 * ```kotlin
	 * collections.find(
	 *     options = {
	 *         readPreference(ReadPreference.SecondaryPreferred)
	 *     }
	 * ) {
	 *    User::name eq "David"
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/core/read-preference/)
	 * - [Use cases](https://www.mongodb.com/docs/manual/core/read-preference-use-cases/#std-label-read-preference-use-cases)
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun readPreference(concern: ReadPreference) {
		accept(ReadPreferenceOption(concern, context))
	}

}
