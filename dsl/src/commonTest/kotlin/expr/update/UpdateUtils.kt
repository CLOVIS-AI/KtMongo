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

package opensavvy.ktmongo.dsl.expr.update

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.expr.*
import opensavvy.prepared.runner.kotest.PreparedSpec

val set = "\$set"
val setOnInsert = "\$setOnInsert"
val inc = "\$inc"
val unset = "\$unset"
val rename = "\$rename"

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
)

@KtMongoDsl
fun update(block: UpdateOperators<User>.() -> Unit): String =
	UpdateExpression<User>(testContext()).apply(block).toString()

@KtMongoDsl
fun upsert(block: UpsertOperators<User>.() -> Unit): String =
	UpdateExpression<User>(testContext()).apply(block).toString()

class EmptyUpdateTest : PreparedSpec({
	test("Empty update") {
		update { } shouldBeBson "{}"
	}
})
