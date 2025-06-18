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

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonValueWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Specifies the read concern for an operation.
 *
 * To learn more about read concerns, see [ReadConcern].
 * To apply this option, see [WithReadConcern].
 */
class ReadConcernOption(
	val concern: ReadConcern,
	context: BsonContext,
) : AbstractOption("readConcern", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeString(concern.bsonName)
	}
}

/**
 * The read concern allows configuring the level of consistency required for each operation.
 *
 * Operations which do not specify a read concern inherit the global default setting for the replica set or shard cluster.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/read-concern/)
 */
enum class ReadConcern(val bsonName: String) {

	/**
	 * The query returns data from the instance with no guarantee that the data has been written
	 * to a majority of the replica set—it may be rolled back.
	 *
	 * Default for reads against the primary and secondaries.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/read-concern-local/)
	 */
	Local("local"),

	/**
	 * The query returns data from the instance with no guarantee that the data has been written
	 * to a majority of the replica set—it may be rolled back.
	 *
	 * In a sharded cluster, this provides the lowest possible latency, but risks returning [orphaned documents](https://www.mongodb.com/docs/manual/reference/glossary/#std-term-orphaned-document).
	 * To avoid orphaned documents, use [Local] instead.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/read-concern-available/)
	 */
	Available("available"),

	/**
	 * The query returns data that has been acknowledged by a majority of the replica set members.
	 * The documents returned are durable, even in the event of failure.
	 *
	 * To fulfill this read concern, the replica set member returns data from its in-memory view of the data
	 * at the majority-commit point. As such, this read concern is comparable in performance as the others, but may return older data.
	 *
	 * In a multi-document transaction, this read concern only provides guarantees if the commits in the transaction
	 * use the write concern "majority".
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/read-concern-majority/)
	 */
	Majority("majority"),

	/**
	 * The query returns data that reflects all successful majority-acknowledged writes that completed prior
	 * to the start of the read operation. The query may wait for concurrently executing writes to propagate
	 * to a majority of replica set members before returning results.
	 *
	 * Linearizable is not compatible with the stages `$out` and `$merge`.
	 *
	 * Linearizable read concern only applies if read operations specify a query filter that uniquely
	 * identifies a single document. Additionally, if none of the following criteria are met,
	 * linearizable read concern might not read from a consistent snapshot,
	 * resulting in a document matching the filter not being returned.
	 *
	 * Always add a [maxTime][WithMaxTime.maxTime] option in case a majority
	 * cannot be elected (e.g. if two many replica set members have crashed) to avoid the read blocking forever.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/read-concern-linearizable/)
	 */
	Linearizable("linearizable"),

	/**
	 * A query returns majority-committed data as it appears across shards from a specific single point
	 * in the recent past.
	 *
	 * In a multi-document transaction, this read concern only provides guarantees if the commits in the transaction
	 * use the write concern "majority".
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/read-concern-snapshot/)
	 */
	Snapshot("snapshot"),
	;
}

/**
 * Consistency guarantees for this request.
 *
 * See [readConcern].
 */
interface WithReadConcern : Options {

	/**
	 * Specifies the [ReadConcern] for this operation.
	 *
	 * ```kotlin
	 * collections.find(
	 *     options = {
	 *         readConcern(ReadConcern.Local)
	 *     }
	 * ) {
	 *    User::name eq "David"
	 * }
	 * ```
	 */
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun readConcern(concern: ReadConcern) {
		accept(ReadConcernOption(concern, context))
	}

}
