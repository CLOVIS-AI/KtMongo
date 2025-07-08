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
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.time.Duration

/**
 * Specifies the write concern for an operation.
 *
 * To learn more about read concerns, see [WriteConcern].
 * To apply this option, see [WithWriteConcern].
 */
class WriteConcernOption(
	val concern: WriteConcern,
	context: BsonContext,
) : AbstractOption("writeConcern", context) {

	@LowLevelApi
	override fun write(writer: BsonValueWriter) = with(writer) {
		writeDocument {
			if (concern.acknowledgment != null) {
				write("w") {
					when (concern.acknowledgment) {
						WriteAcknowledgment.Majority -> writeString("majority")
						is WriteAcknowledgment.Nodes -> writeInt32(concern.acknowledgment.count)
						is WriteAcknowledgment.Tagged -> writeString(concern.acknowledgment.tag)
					}
				}
			}

			if (concern.writeToJournal != null) {
				writeBoolean("j", concern.writeToJournal)
			}

			if (concern.writeTimeout != null) {
				writeInt64("wtimeout", concern.writeTimeout.inWholeMilliseconds)
			}
		}
	}
}

/**
 * The level of acknowledgment requested from a write operation.
 *
 * See [WithWriteConcern.writeConcern].
 */
class WriteConcern(

	/**
	 * Describes how many nodes must acknowledge the write operation.
	 *
	 * For the different options, see:
	 * - [WriteAcknowledgment.Majority]: majority of the nodes.
	 * - [WriteAcknowledgment.Nodes]: a given number of nodes.
	 * - [WriteAcknowledgment.Tagged]: specific nodes selected ahead of time.
	 *
	 * If this field is `null` (default), the MongoDB default acknowledgment applies, which is
	 * [WriteAcknowledgment.Majority] in most deployments.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collections.updateMany(
	 *     options = {
	 *         writeConcern(WriteConcern(WriteAcknowledgment.Nodes(2))
	 *     }
	 * ) {
	 *    User::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#w-option)
	 *
	 * @see WithWriteConcern.writeConcern Specify this option.
	 */
	val acknowledgment: WriteAcknowledgment? = null,

	/**
	 * Specifies whether the write operation must acknowledge being written to the journal.
	 *
	 * Once an operation has been written to the journal, it is much less likely that it will be rolled back,
	 * even if in case of failure.
	 * However, roll back may still happen.
	 * To learn more, see [the journal documentation](https://www.mongodb.com/docs/manual/core/journaling/#std-label-journaling-internals).
	 *
	 * If set to `true`, the nodes specified by [acknowledgment] must additionally write the operation to their journal,
	 * which forces the journal to be written to disk.
	 *
	 * If this field is `null` (default), the MongoDB default applies, which depends on the deployment configuration.
	 * For example, on some deployments, [WriteAcknowledgment.Majority] implies that [writeToJournal] is set to `true`.
	 * To avoid surprises, we recommend always specifying this option.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collections.updateMany(
	 *     options = {
	 *         writeConcern(WriteConcern(writeToJournal = true))
	 *     }
	 * ) {
	 *    User::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#j-option)
	 *
	 * @see WithWriteConcern.writeConcern Specify this option.
	 */
	val writeToJournal: Boolean? = null,

	/**
	 * Specifies a time limit for a write operation to propagate to enough members to achieve [acknowledgment] and [writeToJournal].
	 *
	 * If [acknowledgment] is set to 0 or 1 nodes, this setting does nothing.
	 *
	 * If the write operation cannot be acknowledged in less than this timeout, MongoDB returns a write concern exception,
	 * even if the operation may have later succeeded.
	 * In that case, the operations **are not undone**.
	 *
	 * You may also be interested in the [`maxTime`][WithMaxTime.maxTime] option.
	 *
	 * If this option is set to `null` and the [acknowledgment] and [writeToJournal] options are unachievable, the write operation
	 * will block indefinitely.
	 *
	 * [writeTimeout] of 0 is equivalent to `null`.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collections.updateMany(
	 *     options = {
	 *         writeConcern(WriteConcern(writeTimeout = 2.minutes))
	 *     }
	 * ) {
	 *    User::age inc 1
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#wtimeout)
	 *
	 * @see WithWriteConcern.writeConcern Specify this option.
	 */
	val writeTimeout: Duration? = null,
) {

	fun copy(
		acknowledgment: WriteAcknowledgment? = null,
		writeToJournal: Boolean? = null,
		writeTimeout: Duration? = null,
	): WriteConcern =
		WriteConcern(acknowledgment ?: this.acknowledgment, writeToJournal ?: this.writeToJournal, writeTimeout
			?: this.writeTimeout)

	companion object {

		/**
		 * Requests acknowledgement from the majority of data-bearing members, as well as requesting that the write is written to the journal.
		 *
		 * Use this write concern for requests where the result is important and shouldn't be lost.
		 * Note that some data may still be rolled back, see [writeToJournal].
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * collections.updateMany(
		 *     options = {
		 *         writeConcern(WriteConcern.Majority)
		 *     }
		 * ) {
		 *    User::age inc 1
		 * }
		 * ```
		 *
		 * @see WithWriteConcern.writeConcern Specify this option.
		 */
		val Majority = WriteConcern(WriteAcknowledgment.Majority, writeToJournal = true)

		/**
		 * Requests acknowledgement from the primary node only.
		 *
		 * This ensures that the primary node has successfully handled this write operation.
		 * If the primary changes or dies before this write has been replicated, it may be rolled back.
		 * However, in most situations, this should be enough to ensure the data is stored.
		 *
		 * Use this write concern for requests where the result should preferably not be lost, but rare rollbacks
		 * are acceptable in exchange for lower latency.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * collections.updateMany(
		 *     options = {
		 *         writeConcern(WriteConcern.Primary)
		 *     }
		 * ) {
		 *    User::age inc 1
		 * }
		 * ```
		 *
		 * @see WithWriteConcern.writeConcern Specify this option.
		 */
		val Primary = WriteConcern(WriteAcknowledgment.Nodes(1), writeToJournal = false)

		/**
		 * Requests are not acknowledged.
		 *
		 * The MongoDB server will acknowledge having received the request, but will not communicate further, no matter what happens.
		 *
		 * Use this write concern for requests where the result's loss is acceptable.
		 * For example, when storing high-volume sensor data, it may be acceptable if some measurements are lost when the cluster
		 * is overloaded.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * collections.updateMany(
		 *     options = {
		 *         writeConcern(WriteConcern.FireAndForget)
		 *     }
		 * ) {
		 *    User::age inc 1
		 * }
		 * ```
		 *
		 * @see WithWriteConcern.writeConcern Specify this option.
		 */
		val FireAndForget = WriteConcern(WriteAcknowledgment.Nodes(0), writeToJournal = false)
	}
}

/**
 * Describes how many nodes must acknowledge this write operation.
 *
 * For more information, see [WriteConcern].
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#w-option)
 */
sealed class WriteAcknowledgment {

	/**
	 * Requests acknowledgment that the [calculated majority](https://www.mongodb.com/docs/manual/reference/write-concern/#std-label-calculating-majority-count)
	 * of data-bearing voting members have durably written the change to their local [oplog](https://www.mongodb.com/docs/manual/reference/glossary/#std-term-oplog).
	 * The members then asynchronously apply changes as they read them from their local oplogs.
	 *
	 * This value is the default for most deployments.
	 *
	 * After a write operation has received a majority acknowledgment, clients can read the results of that write with a
	 * [ReadConcern.Majority] read concern.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#mongodb-writeconcern-writeconcern.-majority-)
	 *
	 * @see WithWriteConcern.writeConcern Specify this option.
	 */
	data object Majority : WriteAcknowledgment()

	/**
	 * Requests acknowledgment that the write operation has propagated to the specified number of instances.
	 *
	 * If set to `1`, the write operation must be acknowledged by the standalone mongod or by the primary in a replica set.
	 * The data may be rolled back if the primary steps down before the write has replicated to secondaries.
	 *
	 * If set to `0`, no acknowledgment about the write itself are requested.
	 * However, the client may still receive transport exceptions (e.g. networking errors).
	 * The data may be rolled back if the primary steps down before the write has replicated to secondaries.
	 *
	 * If [WriteConcern.writeToJournal] is `true`, the request will acknowledge that it has been written to the journal,
	 * meaning that at least one node will acknowledge the write, even if this option is set to `0`.
	 *
	 * If set to a number greater than `1`, the primary must acknowledge the request, as well as as many data-bearing
	 * secondaries as needed to meet the specified write concern. That is, `2` requests acknowledgment from the primary and one secondary.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#mongodb-writeconcern-writeconcern.-number-)
	 * - [More about acknowledgment](https://www.mongodb.com/docs/manual/reference/write-concern/#std-label-wc-ack-behavior)
	 *
	 * @see WithWriteConcern.writeConcern Specify this option.
	 */
	data class Nodes(
		/**
		 * See [Nodes].
		 */
		val count: Int,
	) : WriteAcknowledgment()

	/**
	 * Requests acknowledgment from members [tagged](https://www.mongodb.com/docs/manual/reference/replica-configuration/#mongodb-rsconf-rsconf.members-n-.tags) with [tag].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern/#mongodb-writeconcern-writeconcern.-custom-write-concern-name-)
	 * - [Official example](https://www.mongodb.com/docs/manual/tutorial/configure-replica-set-tag-sets/#std-label-configure-custom-write-concern)
	 *
	 * @see WithWriteConcern.writeConcern Specify this option.
	 */
	data class Tagged(
		val tag: String,
	) : WriteAcknowledgment()
}

/**
 * Consistency guarantees for this request.
 *
 * See [writeConcern].
 */
interface WithWriteConcern : Options {

	/**
	 * Specifies the [WriteConcern] for this operation.
	 *
	 * The write concern specifies which nodes must acknowledge having applied this write operation.
	 * The stronger the write concern, the less chance of data loss, but the higher the latency.
	 *
	 * To learn more about the different options, see:
	 * - [WriteConcern.acknowledgment]: how many nodes should acknowledge this request?
	 * - [WriteConcern.writeToJournal]: should the nodes also acknowledge synchronizing their journal?
	 * - [WriteConcern.writeTimeout]: after how long should we give up on the acknowledgment, if it doesn't succeed?
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collections.updateMany(
	 *     options = {
	 *         writeConcern(WriteConcern(Majority, writeTimeout = 2.seconds))
	 *     }
	 * ) {
	 *    User::age inc 1
	 * }
	 * ```
	 *
	 * ### Convenience helpers
	 *
	 * For convenience of the most common scenarii, KtMongo provides the following helpers:
	 * - [WriteConcern.Majority]: requests strong acknowledgment from the majority of nodes.
	 * - [WriteConcern.Primary]: requests weak acknowledgement from the primary node.
	 * - [WriteConcern.FireAndForget]: requests no acknowledgement at all.
	 *
	 * ### Transactions
	 *
	 * In multi-document transactions, only specify a write concern at the transaction level, and not at the level
	 * of individual operation.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/write-concern)
	 */
	@KtMongoDsl
	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	fun writeConcern(concern: WriteConcern) {
		accept(WriteConcernOption(concern, context))
	}

}
