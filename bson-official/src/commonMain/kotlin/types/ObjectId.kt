/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.bson.official.types

/**
 * MongoDB native identifier.
 *
 * ObjectIds are 12 bytes and can be generated safely in a distributed manner with very low probability of collision.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/bson-types/#std-label-objectid)
 */
expect class ObjectId : Comparable<ObjectId> {

	constructor(bytes: ByteArray)

	constructor(hexString: String)

	/**
	 * Generates a new random ObjectId.
	 */
	constructor()

	fun toHexString(): String

	fun toByteArray(): ByteArray

	override fun compareTo(other: ObjectId): Int
}
