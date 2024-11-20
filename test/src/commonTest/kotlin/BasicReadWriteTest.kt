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

package opensavvy.ktmongo.sync

import opensavvy.ktmongo.test.testCollection
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.SuiteDsl

fun SuiteDsl.basicReadWriteTest() = suite("Basic read/write test") {
	class User(
		val name: String,
		val age: Int,
	)

	val users by testCollection<User>("basic-users")

	test("Foo") {
		users().upsertOne(
			filter = {
				User::name eq "Foo"
			},
			update = {
				User::name set "Bad"
				User::age setOnInsert 0
			}
		)
	}
}

class BasicReadWriteTest : PreparedSpec({
	basicReadWriteTest()
})