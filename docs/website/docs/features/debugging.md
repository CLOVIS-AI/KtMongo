# Debugging a MongoDB application written with KtMongo

KtMongo offers a powerful DSL to write MongoDB queries with Kotlin code. However, when reading code, it can help to be able to translate it back in MongoDB queries to edit and run it in a dedicated application, like [MongoDB Compass](https://www.mongodb.com/products/tools/compass).

!!! tip "Need help writing code?"
    If you're in the opposite situation, and know what MongoDB query you want to write but not how to express it using KtMongo's operators, visit [the quick navigation](index.md#quick-navigation).



## String representation

Almost all the KtMongo types have a `toString()` implementation which returns the JSON representation of the object. To see it, either print the object or evaluate it in a debugger.

Currently, KtMongo uses the [MongoDB JSON representation](https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/), which differs from the `mongosh` command's syntax for some data types. The following table contains the main differences:

| Type                                                                           | MongoDB JSON                                                           | `mongosh`                                   |
|--------------------------------------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------|
| [`ObjectId`](../api/bson/opensavvy.ktmongo.bson.types/-object-id/index.md)     | `#!json {"$oid": "6aa2fbf70fb35578e08e17d6"}`                          | `#!js ObjectId("6aa2fbf70fb35578e08e17d6")` |
| [`DateTime`](../api/bson/opensavvy.ktmongo.bson/-bson-type/-datetime/index.md) | `#!json {"$date": "1970-01-01T00:00:00Z"}`                             | `#!js ISODate("1970-01-01T00:00:00Z")`      |
| [`RegEx`](../api/bson/opensavvy.ktmongo.bson/-bson-type/-reg-exp/index.md)     | `#!json {"$regularExpression": {"pattern": "^foo.*", "options": "i"}}` | `#!js /^foo.*/i`                            |

If you're interested in KtMongo generating the `mongosh` representation directly, please vote on [#115](https://gitlab.com/opensavvy/ktmongo/-/work_items/115).

### Queries

In queries, the DSL block contains the string representation:

```kotlin
users.find {
	println("Before:  $this")
	User::age gte 18

	println("Between: $this")
	User::name eq "Bob"

	println("After:   $this")
}
```

```json
Before:  {}
Between: {"age": {"$gte": 18}}
After:   {"$and": {"age": {"$gte": 18}, "name": {"$eq": 18}}}
```

KtMongo added the `$and` operator automatically once there were multiple criteria (you can add it explicitly with [`and`](../api/dsl/opensavvy.ktmongo.dsl.query/-filter-query/index.md#and)).

### Aggregations

In aggregations, the operators contain their own string representation, and the DSL block contains the stage's representation:

```kotlin
users.aggregate()
	.project {
		User::isAdult set (User::age gte 18)
			.also { println("Operator: $it") }
		println("Stage: $this")
	}
```

```json
Operator: {"$gte": ["$age", {"$literal": 18}}
Stage:    {"isAdult": {"$gte": ["$age",{"$literal": 18}]}}
```

Although our Kotlin expression `User::age gte 18` is identical in queries and aggregations, KtMongo automatically knows which syntax to use based on the current method. This is a big advantage of KtMongo's type-safety, which helps developers easily write code that can be used in many contexts even when MongoDB has special syntax differences.

KtMongo also inserts `$literal` statements around every serialized value to block possible injection attacks and avoid ambiguities in some commands.

### Commands

Lazy commands, like `find()` and `aggregate()`, return a data structure that holds the entire command's representation:

```kotlin
users.find(
	options = {
		limit(5)
		maxTime(5.seconds)
	}
) {
	User::isAlive eq true
}
	.also(::println)
	.toList()
```

```terminaloutput
CoroutineMongoCollection(my-app.users).find({"filter": {"isAlive": {"$eq": true}}, "limit": 5, "maxTimeMS": 5000})
```

The string representation of a database and a collection include their namespace, and it is also included in commands' representation.

If using [filtered collections](filtered-collections.md), the string representation includes the declared filter, so you can see the resulting filter after the combination.

### Options

Just like queries, the string representation of options is the DSL block itself:

```kotlin
users.find(
	options = {
		limit(5)
		maxTime(5.seconds)
		println(this)
	}
) {
	User::isAlive eq true
}
```

```json
{
	"limit": 5,
	"maxTimeMS": 5000
}
```

!!! warning "The official driver"
    If you use KtMongo alongside the official driver (`driver-coroutines` or `driver-sync`) **the options representation may be different from the real request sent to the database**. Unlike every other data type, the official driver doesn't accept arbitrary BSON documents for options. KtMongo must map the options for every command, so there is a low risk that some mapping is missing. If you find any such case, [please report it to us](https://gitlab.com/opensavvy/ktmongo/-/work_items/new)!



## Aggregation debugging

A complex aggregation pipeline broken in multiple subfunctions can be tough to understand, especially when it returns the wrong results or no results at all. To facilitate understanding complex pipelines, KtMongo provides a [`debug`](../api/driver-api/opensavvy.ktmongo.api/-mongo-aggregation-pipeline/index.html#debug) method which returns intermediate results:

```kotlin
users.aggregate()
	.match { User::age gte 18 }
	.sort { ascending(User::age) }
	.limit(1)
	.also { println(it.debug()) } // Temporarily add when debugging, or call using the debugger
	.toList()
```

```terminaloutput
Pipeline debug (first 10 resulting documents after each stage):
  Stage: {}
    {"_id": {"$oid": "6aa304b526a3e786897e397a"}, "name": "Alice", "age": 30}
    {"_id": {"$oid": "6aa304b526a3e786897e397b"}, "name": "Bob", "age": 17}
    {"_id": {"$oid": "6aa304b526a3e786897e397c"}, "name": "Charlie", "age": 22}
  
  Stage: {"$match": {"age": {"$gte": 18}}}
    {"_id": {"$oid": "6aa304b526a3e786897e397a"}, "name": "Alice", "age": 30}
    {"_id": {"$oid": "6aa304b526a3e786897e397c"}, "name": "Charlie", "age": 22}
  
  Stage: {"$sort": {"age": 1}}
    {"_id": {"$oid": "6aa304b526a3e786897e397c"}, "name": "Charlie", "age": 22}
    {"_id": {"$oid": "6aa304b526a3e786897e397a"}, "name": "Alice", "age": 30}
  
  Stage: {"$limit": 1}
    {"_id": {"$oid": "6aa304b526a3e786897e397c"}, "name": "Charlie", "age": 22}
```

If a stage throws an exception, the debug method will say which one explicitly.
