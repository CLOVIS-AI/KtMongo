/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.test

import kotlinx.coroutines.ensureActive
import opensavvy.ktmongo.coroutines.MongoCollection
import opensavvy.prepared.suite.PreparedProvider
import opensavvy.prepared.suite.cleanUp
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.randomInt
import kotlin.coroutines.coroutineContext

expect inline fun <reified Document : Any> testCollectionExact(name: String): PreparedProvider<MongoCollection<Document>>

val collectionPostfix by randomInt(0, Int.MAX_VALUE)
inline fun <reified Document : Any> testCollection(name: String): PreparedProvider<MongoCollection<Document>> = prepared {
	val name = "$name-${collectionPostfix()}"

	val realCollection by testCollectionExact<Document>(name)

	cleanUp("Log the collection after failed test", onSuccess = false) {
		println("Collection $name with ${realCollection().count()} documents:")

		try {
			realCollection().find().forEach { document ->
				println(" • $document")
			}
		} catch (e: Exception) {
			coroutineContext.ensureActive()
			println("Could not print collection • ${e.stackTraceToString()}")
		}
	}

	cleanUp("Drop the collection", onFailure = false) {
		println("Dropping the collection is not implemented yet")
	}

	realCollection()
}
