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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.tests.api

import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.prepared
import kotlin.reflect.typeOf

fun SuiteDsl.verifyCollection(
	client: Prepared<MongoClient>,
) = suite("Collection") {

	val coll by prepared {
		client()
			.database("test")
			.collection<Unit>("does-not-exist")
	}

	test("Create") {
		check(coll().name == "does-not-exist")
		check(coll().fullyQualifiedName == "test.does-not-exist")

		check(coll().type == typeOf<Unit>())

		check(coll().toString().endsWith("MongoCollection(test.does-not-exist)"))
	}

	test("The factory is functional") {
		check(coll().factory.buildDocument { writeString("a", "b") }["a"]?.decodeString() == "b")
	}

	test("The name strategy is functional") {
		check(coll().propertyNameStrategy.pathOf(String::length) == Path("length"))
	}

	test("The ObjectId generator is functional") {
		// We want to check that it doesn't throw
		check(coll().newId() != ObjectId.MIN)
	}

	test("The BsonContext is functional") {
		// We want to check that it doesn't throw
		val _ = coll().context
	}

}
