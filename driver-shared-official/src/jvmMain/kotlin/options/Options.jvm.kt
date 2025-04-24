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

package opensavvy.ktmongo.official.options

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.CountOptions
import opensavvy.ktmongo.dsl.options.*
import opensavvy.ktmongo.official.toJava

@LowLevelApi
fun CountOptions<*>.toJava(): com.mongodb.client.model.CountOptions = com.mongodb.client.model.CountOptions()
	.limit(readLimit())
	.skip(readSkip())

@LowLevelApi
fun WithLimit.readLimit() =
	option<LimitOption>()?.limit?.toInt() ?: 0

@LowLevelApi
fun WithSkip.readSkip() =
	option<SkipOption>()?.skip?.toInt() ?: 0

@LowLevelApi
fun WithSort<*>.readSortDocument() =
	option<SortOption<*>>()?.block?.toJava()
