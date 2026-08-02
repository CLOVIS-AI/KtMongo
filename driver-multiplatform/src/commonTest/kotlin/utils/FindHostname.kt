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

package opensavvy.ktmongo.multiplatform.utils

import kotlinx.coroutines.coroutineScope
import opensavvy.ktmongo.multiplatform.MongoClient
import opensavvy.prepared.suite.cleanUp
import opensavvy.prepared.suite.foregroundScope
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.shared
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
private suspend fun tryConnect(
	hostname: String,
): Boolean {
	try {
		println("  Attempting to connect to $hostname")
		coroutineScope {
			MongoClient(hostname = hostname, coroutineContext = coroutineContext).close()
		}
		return true
	} catch (e: Throwable) {
		println("  Could not connect to $hostname: ${e.stackTraceToString()}")
		return false
	}
}

private val mongoAddress by shared {
	val attempts = listOf("localhost", "mongo")

	attempts.firstOrNull { tryConnect(it) }
		?: error("Could not find on which port MongoDB is running.")
}

@OptIn(ExperimentalAtomicApi::class)
val MongoClient by prepared {
	val address = mongoAddress()

	val client = MongoClient(
		hostname = address,
		port = 27017,
		coroutineContext = foregroundScope.coroutineContext,
	)

	cleanUp("MongoClient") {
		client.close()
	}

	client
}
