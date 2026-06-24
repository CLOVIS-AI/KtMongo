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

import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.tests.api.operations.verifyCountOperations
import opensavvy.prepared.suite.*
import kotlin.coroutines.CoroutineContext

fun SuiteDsl.verifyClient(
	name: String,
	createClient: suspend (connectionString: String, coroutineContext: CoroutineContext) -> MongoClient,
) = suite(name) {

	val connectionString by shared {
		"mongodb://localhost:27017"
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

	verifyCountOperations(client)
}
