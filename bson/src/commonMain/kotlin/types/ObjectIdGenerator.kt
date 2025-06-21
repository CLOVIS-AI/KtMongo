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

import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random
import kotlin.time.Clock
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
	 * The default [ObjectIdGenerator], implementing the MongoDB ObjectId generation algorithm.
	 *
	 * Note that it isn't guaranteed that IDs are generated monotonically: it is possible that an ID is lesser
	 * (according to [ObjectId.compareTo]) than one generated previously, but only if both were generated
	 * during the same second.
	 *
	 * However, it is guaranteed until [the year 2016][ObjectId.timestamp] that an ID
	 * is always strictly greater than one generated in a previous second.
	 */
	@ExperimentalAtomicApi
	@ExperimentalTime
	class Default(
		private val clock: Clock = Clock.System,
		private val random: Random = Random,
		private var processId: Long = random.nextLong(0, ObjectId.PROCESS_ID_BOUND),
	) : ObjectIdGenerator {

		private var counter = 0
		private var counterOffset = random.nextInt(0, ObjectId.COUNTER_BOUND)

		override fun newId(): ObjectId {
			// TODO in #71: Make this algorithm thread-safe

			val now = clock.now()

			val myCounter = counter++
			if (counter >= ObjectId.COUNTER_BOUND) {
				counter = 0
				processId++
				processId %= ObjectId.PROCESS_ID_BOUND
			}

			return ObjectId(
				timestamp = now,
				processId = processId,
				counter = (myCounter + counterOffset) % ObjectId.COUNTER_BOUND,
			)
		}
	}

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

	companion object
}
