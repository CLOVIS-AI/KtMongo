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

package opensavvy.ktmongo.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CoroutineMongoIterableMappingImpl<In : Any, Out : Any>(
	private val upstream: CoroutineMongoIterable<In>,
	private val transform: suspend (In) -> Out,
) : CoroutineMongoIterable<Out> {
	override suspend fun first(): Out =
		transform(upstream.first())

	override suspend fun firstOrNull(): Out? =
		upstream.firstOrNull()?.let { transform(it) }

	override suspend fun forEach(action: suspend (Out) -> Unit) =
		upstream.forEach { action(transform(it)) }

	override fun asFlow(): Flow<Out> =
		upstream.asFlow().map(transform)

	override fun toString(): String =
		upstream.toString()
}
