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

package opensavvy.ktmongo.tests.api.operations

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.api.MongoClient
import opensavvy.ktmongo.api.first
import opensavvy.ktmongo.api.toList
import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.tests.api.collection
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@Serializable
data class AggregationOperationsUser(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

@Serializable
data class AggregationCountResult(val count: Int)

@Serializable
data class AggregationGroupResult(
	val _id: ObjectId? = null,
	val total: Int = 0,
)

fun SuiteDsl.verifyAggregationOperations(
	client: Prepared<MongoClient>,
) = suite("Aggregation operations") {
	val collection by client.collection<AggregationOperationsUser>("operation-aggregation-users")

	test("Aggregate all") {
		collection().insertMany(
			AggregationOperationsUser(
				_id = collection().newId(),
				name = "Alice",
				age = 30,
			),
			AggregationOperationsUser(
				_id = collection().newId(),
				name = "Bob",
				age = 25,
			),
		)

		val results = collection().aggregate().toList()
		check(results.size == 2)
	}

	test("Aggregate with match") {
		collection().insertMany(
			AggregationOperationsUser(
				_id = collection().newId(),
				name = "Alice",
				age = 30,
			),
			AggregationOperationsUser(
				_id = collection().newId(),
				name = "Bob",
				age = 17,
			),
			AggregationOperationsUser(
				_id = collection().newId(),
				name = "Charlie",
				age = 22,
			),
		)

		val results = collection().aggregate()
			.match { AggregationOperationsUser::age gte 18 }
			.toList()

		check(results.all { it.age >= 18 })
		check(results.none { it.name == "Bob" })
	}

	test("Aggregate with limit") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 30),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 22),
			AggregationOperationsUser(_id = collection().newId(), name = "Diana", age = 28),
		)

		val results = collection()
			.aggregate()
			.limit(2)
			.toList()
		check(results.size == 2)
	}

	test("Aggregate with skip") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 30),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 22),
		)

		val results = collection()
			.aggregate()
			.skip(1)
			.toList()
		check(results.size == 2)
	}

	test("Aggregate with sort ascending") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 22),
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 30),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
		)

		val results = collection().aggregate()
			.sort { ascending(AggregationOperationsUser::age) }
			.toList()
		check(results.map { it.age } == listOf(22, 25, 30))
	}

	test("Aggregate with sort descending") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 22),
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 30),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
		)

		val results = collection().aggregate()
			.sort { descending(AggregationOperationsUser::age) }
			.toList()
		check(results.map { it.age } == listOf(30, 25, 22))
	}

	test("Aggregate with set") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 20),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
		)

		val results = collection().aggregate()
			.set { AggregationOperationsUser::age set 99 }
			.toList()
		check(results.all { it.age == 99 })
	}

	test("Aggregate with sample") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 30),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 22),
		)

		val results = collection()
			.aggregate()
			.sample(1)
			.toList()
		check(results.size == 1)
	}

	test("Aggregate with countTo") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 30),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 25),
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 22),
		)

		val result = collection().aggregate()
			.countTo(AggregationCountResult::count)
			.first()
		check(result.count == 3)
	}

	test("Aggregate with group sum") {
		collection().insertMany(
			AggregationOperationsUser(_id = collection().newId(), name = "Alice", age = 10),
			AggregationOperationsUser(_id = collection().newId(), name = "Bob", age = 20),
			AggregationOperationsUser(_id = collection().newId(), name = "Charlie", age = 30),
		)

		val result = collection().aggregate()
			.group {
				AggregationGroupResult::total sum AggregationOperationsUser::age
			}
			.first()
		check(result.total == 60)
	}
}
