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

package opensavvy.ktmongo.bson.types

import kotlin.time.ExperimentalTime

/**
 * An object responsible for generating new [ObjectId] instances.
 *
 * This object is similar to [kotlin.time.Clock]: a `Clock` is a way to generate `Instant` instances, and services
 * that need to generate instants should receive a clock via dependency injection.
 * Similarly, services that generate IDs should get a generator via dependency injection.
 *
 * To get the real generator instance, see the database's [opensavvy.ktmongo.bson.BsonContext].
 */
interface ObjectIdGenerator {

	/**
	 * Creates a new instance of an [ObjectId].
	 *
	 * @throws NoSuchElementException If the generator is not able to generate more IDs.
	 */
	@ExperimentalTime
	fun newId(): ObjectId

	/**
	 * Hardcoded [ObjectId] generator with a deterministic input sequence.
	 *
	 * Mostly useful as a fake when testing algorithms that must generate IDs.
	 */
	@ExperimentalTime
	class Hardcoded(
		private val ids: Iterator<ObjectId>,
	) : ObjectIdGenerator {

		constructor(ids: Iterable<ObjectId>) : this(ids.iterator())

		constructor(vararg ids: ObjectId) : this(ids.asIterable())

		override fun newId(): ObjectId {
			if (!ids.hasNext())
				throw NoSuchElementException("This ObjectIdGenerator has finished generating all of its instances, but `newId()` was called once more.")

			return ids.next()
		}
	}
}
