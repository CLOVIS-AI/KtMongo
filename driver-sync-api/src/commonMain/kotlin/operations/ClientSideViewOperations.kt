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

package opensavvy.ktmongo.sync.api.operations

import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.sync.api.MongoCollection

/**
 * The different MongoDB operations related to client-side views.
 */
interface ClientSideViewOperations<Document : Any> : BaseOperations {

	/**
	 * Creates a client-side view containing all the documents that match [filter].
	 *
	 * ### Client-side views
	 *
	 * MongoDB has a concept of [views](https://www.mongodb.com/docs/manual/core/views/): read-only results of aggregation
	 * pipelines useful to avoid repeating the same queries in multiple places.
	 *
	 * This function **does not create a MongoDB view**.
	 * Instead, it creates a logical view, which is purely syntax sugar in the KtMongo library and doesn't exist
	 * in MongoDB itself.
	 * The database is never aware of client-side views.
	 *
	 * Client-side views do not have the limitations of real MongoDB views: they can be mutable and support all operators.
	 *
	 * Essentially, this method returns a [MongoCollection] implementation that combines the [filter] with every filter
	 * provided by any other operation, using a [`$and`][FilterQuery.and].
	 *
	 * ### Example
	 *
	 * Let's imagine you want to implement logical deletion of items:
	 * ```kotlin
	 * class Parcel(
	 *     val _id: ObjectId,
	 *     val owner: ObjectId,
	 *     val isActive: Boolean = true,
	 * )
	 * ```
	 * In that situation, you will need to remember to apply a filter in almost all methods you implement:
	 * ```kotlin
	 * // Find the user's active parcels
	 * parcels.find({ sort { descending(Parcel::_id) } }) {
	 *     Parcel::owner eq currentUserId()
	 *     Parcel::isActive ne false  // ⚠ Don't forget!
	 * }
	 *
	 * // An owner transfers all active parcels to another one
	 * parcels.updateMany(
	 *     filter = {
	 *         Parcel::owner eq currentUserId()
	 *         Parcel::isActive ne false  // ⚠ Don't forget!
	 *     },
	 *     update = {
	 *         Parcel::owner set transferDestinationUserId
	 *     }
	 * )
	 * ```
	 *
	 * To avoid worrying about specifying the same filter each time, you can use client-side logical views to
	 * factor it out into a subset collection:
	 * ```kotlin
	 * val activeParcels = parcels.filter { Parcel::isActive ne false }
	 *
	 * // Find the user's active parcels
	 * activeParcels.find({ sort { descending(Parcel::_id) } }) {
	 *     Parcel::owner eq currentUserId()
	 * }
	 *
	 * // An owner transfers all active parcels to another one
	 * activeParcels.updateMany(
	 *     filter = { Parcel::owner eq currentUserId() },
	 *     update = { Parcel::owner set transferDestinationUserId }
	 * )
	 * ```
	 * This example is strictly identical to the previous one: the driver combines the client-side view's
	 * and the operation's filters.
	 *
	 * A client-side view can be created from another one, which allows to further shorten the update:
	 * ```kotlin
	 * // An owner transfers all active parcels to another one
	 * activeParcels.filter { Parcel::owner eq currentUserId() }
	 *     .updateMany { Parcel::owner set transferDestinationUserId }
	 * ```
	 * This style, using an explicit `filter` function instead of using the operation's own filter, allows using
	 * Kotlin's trailing syntax. We encourage its usage, there is no performance impact.
	 *
	 * Learn more in the [KtMongo feature page](https://ktmongo.opensavvy.dev/features/filtered-collections.html).
	 */
	fun filter(
		filter: FilterQuery<Document>.() -> Unit,
	): ClientSideViewOperations<Document>
	// ↑ Each implementation should override with a more specific type
	//   This is an emulated self-type

}
