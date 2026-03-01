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

@file:OptIn(ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.bson.BsonPath
import opensavvy.ktmongo.bson.ExperimentalBsonPathApi
import opensavvy.ktmongo.dsl.query.update.Friend
import opensavvy.ktmongo.dsl.query.update.User
import opensavvy.ktmongo.dsl.query.update.update
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.assertions.checkThrows

val BsonPathConversions by preparedSuite {
	test("Simple path with fields") {
		val _ = update {
			val name = User::bestFriend / Friend::name

			check(name.toBsonPath() == BsonPath("$.bestFriend.name"))
		}
	}

	test("Path with an array index") {
		val _ = update {
			val name = User::friends[12] / Friend::id

			check(name.toBsonPath() == BsonPath("$.friends[12].id"))
		}
	}

	test("Path with an array wildcard") {
		val _ = update {
			val name = User::friends.all / Friend::name

			check(name.toBsonPath() == BsonPath("$.friends.*.name"))
		}
	}

	test("The positional operator is not supported") {
		val _ = update {
			val name = User::friends.selected / Friend::name

			checkThrows<IllegalArgumentException> {
				name.toBsonPath()
			}
		}
	}
}
