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

package opensavvy.ktmongo.dsl.query.filter

import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.FilterQuery
import opensavvy.ktmongo.dsl.query.testContext
import org.bson.types.ObjectId

class Pet(
	val name: String,
	val age: Int,
)

class User(
	val id: ObjectId,
	val name: String,
	val age: Int?,
	val grades: List<Int>,
	val pets: List<Pet>,
	val isAlive: Boolean = true,
)

@OptIn(LowLevelApi::class)
@KtMongoDsl
fun filter(block: FilterQuery<User>.() -> Unit): String =
	FilterQuery<User>(testContext()).apply(block).toString()
