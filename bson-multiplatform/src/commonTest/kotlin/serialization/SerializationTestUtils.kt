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

@file:OptIn(ExperimentalSerializationApi::class, LowLevelApi::class)

package opensavvy.ktmongo.bson.multiplatform.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import opensavvy.ktmongo.bson.multiplatform.context
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.TestDsl

suspend inline fun <reified T : Any> TestDsl.serializeRoundTrip(value: T) {
	val ctx = context()

	check(decodeFromBson<T>(ctx, encodeToBson<T>(ctx, value).toByteArray()) == value)
}
