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

package opensavvy.ktmongo.dsl.path

import opensavvy.prepared.runner.testballoon.preparedSuite
import kotlin.reflect.KProperty1

val FieldTest by preparedSuite {
	class Profile(
		val name: String,
		val age: Int,
	)

	class Friend(
		val userId: String,
		val name: String,
	)

	class User(
		val id: Int,
		val profile: Profile,
		val friends: List<Friend>,
	)

	class TestFieldDsl : FieldDsl {

		// force 'User' to ensure all functions keep the User as the root type
		infix fun Field<User, *>.shouldHavePath(path: String) =
			check(this.toString() == path)

		infix fun KProperty1<User, *>.shouldHavePath(path: String) =
			this.field shouldHavePath path

	}

	suite("Field access") {
		test("Root field") {
			with(TestFieldDsl()) {
				User::id shouldHavePath "id"
			}
		}

		test("Nested field") {
			with(TestFieldDsl()) {
				User::profile / Profile::name shouldHavePath "profile.name"
				User::profile / Profile::age shouldHavePath "profile.age"
			}
		}
	}

	suite("Indexed access") {
		test("Indexed object") {
			with(TestFieldDsl()) {
				User::friends[0] shouldHavePath "friends.0"
			}
		}

		test("Indexed nested field") {
			with(TestFieldDsl()) {
				User::friends[0] / Friend::name shouldHavePath "friends.0.name"
			}
		}
	}
}
