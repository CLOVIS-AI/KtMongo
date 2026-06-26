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

package opensavvy.ktmongo.tests.api

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.tests.api.operations.*
import opensavvy.prepared.suite.*
import kotlin.coroutines.CoroutineContext

fun SuiteDsl.verifyClient(
	name: String,
	createClient: suspend (connectionString: String, coroutineContext: CoroutineContext) -> MongoClient,
) = suite(name) {

	suspend fun verifyClientConnected(client: MongoClient): Boolean = try {
		val count = client.use {
			client.database("does-not-exist")
				.collection<Unit>("does-not-exist")
				.count()
		}
		check(count == 0L) { "Client $client found values in the fake collection. Did you create it yourself?" }
		true
	} catch (_: Throwable) {
		currentCoroutineContext().ensureActive()
		false
	}

	val connectionString by shared {
		val candidates = listOf(
			"mongodb://localhost:27017",  // Dev
			"mongodb://mongo:27017",      // CI
		)

		coroutineScope {
			candidates
				.first { verifyClientConnected(createClient(it, currentCoroutineContext())) }
		}
	}

	test("Can connect to the database") {
		println("This test suite will run with connection string: ${connectionString()}")
	}

	val client by prepared {
		val client = createClient(connectionString(), backgroundScope.coroutineContext)

		cleanUp("client") {
			client.close()
		}

		client
	}

	verifyClient(client)
	verifyDatabase(client)
	verifyCollection(client)

	verifyInsertOperations(client)
	verifyCountOperations(client)
	verifyDeleteOperations(client)
	verifyFindOperations(client)
	verifyAggregationOperations(client)
	verifyUpdateOperations(client)
	verifyUpdatePipelineOperations(client)
	verifyCollectionOperations(client)
}
