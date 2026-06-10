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

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import opensavvy.ktmongo.multiplatform.MongoClient
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.shared
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
private suspend fun tryConnect(
	hostname: String,
): Boolean {
	try {
		println("KtMongo • Attempting to connect to $hostname")
		val _ = coroutineScope {
			val job = Job()
			this.coroutineContext.job.invokeOnCompletion { e -> job.cancel("Finished searching for the address. (ended with: $e)") }
			MongoClient(hostname = hostname, coroutineContext = coroutineContext + job)
		}
		return true
	} catch (e: Throwable) {
		println("KtMongo • Could not connect to $hostname: $e")
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

	MongoClient(
		hostname = address,
		port = 27017,
		coroutineContext = backgroundScope.coroutineContext,
	)
}
