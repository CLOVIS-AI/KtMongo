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

@file:OptIn(ExperimentalTime::class)

package opensavvy.ktmongo.dsl.query.update

import opensavvy.ktmongo.bson.types.Timestamp
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.query.UpdateQuery
import opensavvy.ktmongo.dsl.query.UpsertQuery
import opensavvy.ktmongo.dsl.query.shouldBeBson
import opensavvy.ktmongo.dsl.query.testContext
import opensavvy.prepared.runner.testballoon.preparedSuite
import org.bson.types.ObjectId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class Friend(
	val id: String,
	val name: String,
	val money: Float,
)

class User(
	val id: ObjectId,
	val name: String,
	val age: Int?,
	val money: Double,
	val bestFriend: Friend,
	val friends: List<Friend>,
	val tokens: List<String>,
	val scores: List<Int>,
	val creationInstant: Instant,
	val modificationTimestamp: Timestamp,
)

@OptIn(LowLevelApi::class)
@KtMongoDsl
fun update(block: UpdateQuery<User>.() -> Unit): String =
	UpdateQuery<User>(testContext()).apply(block).toString()

@OptIn(LowLevelApi::class)
@KtMongoDsl
fun upsert(block: UpsertQuery<User>.() -> Unit): String =
	UpsertQuery<User>(testContext()).apply(block).toString()

val EmptyUpdateTest by preparedSuite {
	test("Empty update") {
		update { } shouldBeBson "{}"
	}
}
