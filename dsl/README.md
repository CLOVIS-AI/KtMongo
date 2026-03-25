# Module KtMongo: MongoDB request DSL

Pure Kotlin DSL targeting all MongoDB operators. 

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22dsl%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/dsl.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/dsl"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

Pure Kotlin implementation of MongoDB operators, based on `dev.opensavvy.ktmongo:bson`. All platforms supported by `:bson` are also supported by this module.

It is not expected that end-users will have a direct dependency on this module. Instead, we expect end-users to have a dependency on one of the "driver" modules: `driver-sync` or `driver-coroutines`.

## Introduction

No matter whether you use the synchronous or the coroutines drivers, you must construct BSON instances to communicate with MongoDB. While the `:bson` module allows writing arbitrary BSON, it is rather low-level and requires to take care of escaping as well as remembering the syntax for each operator. This module provides a Kotlin function for each MongoDB operator:
```kotlin
collection.find {
	User::score isOneOf listOf(4, 5)
	User::age gte 18
}
```

To learn more about the `Class::field` syntax, and how to refer to fields in general, see [Field][opensavvy.ktmongo.dsl.path.FieldDsl].

## Operator DSLs

Operators are organized by the context in which they are available in. For example, the `$eq` operator cannot be used in an `update`'s modification field, but it can be used in an `update`'s filter field.

Instances of these classes are usually provided by the driver as part of its functions.

- [Filter operators][opensavvy.ktmongo.dsl.query.FilterQuery]
- [Update operators][opensavvy.ktmongo.dsl.query.UpdateQuery]
- [Upsert operators][opensavvy.ktmongo.dsl.query.UpsertQuery]

To create a custom operator (for example because it isn't part of the library yet), see [AbstractExpression][opensavvy.ktmongo.dsl.tree.AbstractBsonNode].

## Aggregation DSLs

KtMongo has support for aggregation pipelines through dedicated DSLs.

- [Aggregation pipelines and stages][opensavvy.ktmongo.dsl.aggregation.Pipeline]
- [Aggregation operators][opensavvy.ktmongo.dsl.aggregation.AggregationOperators]

# Package opensavvy.ktmongo.dsl

Annotations and other global concepts.

# Package opensavvy.ktmongo.dsl.tree

Helpers to represent trees of data built bottom-up. Nodes become immutable when they become branches of another tree, and offer utilities for debugging (e.g. JSON representation in their `toString` implementation).

Users of the library are not expected to interact with this package in day-to-day operations.

However, contributors to the library, and users wanting to implement custom operators, should familiarize themselves with this package.

# Package opensavvy.ktmongo.dsl.aggregation

Aggregation pipelines are powerful ways to query and update MongoDB documents. Compared to regular queries, aggregation pipelines can perform more complex operations, can compare fields from the same document together, or combine data from other sources.

Aggregation pipelines are declared as a Sequence-like chain of **stages**. Each stage is responsible for transforming documents in a certain way. MongoDB is able to parallelize work from different stages. To see which stages are available, see [`Pipeline`][opensavvy.ktmongo.dsl.aggregation.Pipeline].

```kotlin
users.aggregate()
	.match {
		// Similar to List.filter
		User::age gt 18
	}
	.project {
		// Select which fields we are interested in.
		// All other fields are ignored.
		include(User::age)
		include(User::name)
	}
	.toList()
```

Additionally, many stages allow building complex expressions based on different fields. For example, within [`$set`][opensavvy.ktmongo.dsl.aggregation.stages.HasSet.set] and [`$project`][opensavvy.ktmongo.dsl.aggregation.stages.HasProject.project], we can use the [`$cond`][opensavvy.ktmongo.dsl.aggregation.operators.ConditionalValueOperators.cond] operator to conditionally set the value of a field based on other fields:

```kotlin
users.aggregate()
	.set {
		User::risk set cond(
			condition = of(User::isAdult),
			ifTrue = of(User::age) * of(5),
			iFalse = of(18) - of(User::age),
		)
	}
	.toList()
```
To learn more about aggregation operators and their syntax, see [`ValueDsl`][opensavvy.ktmongo.dsl.aggregation.ValueDsl].

You may also be interested in reading the [official documentation on aggregations](https://www.mongodb.com/docs/manual/aggregation/).

# Package opensavvy.ktmongo.dsl.query

Operators to query and update data.

All operators are declared as interface members. Each interface groups the operators of a given MongoDB command.

- [FilterQuery][opensavvy.ktmongo.dsl.query.FilterQuery] is used in `find()`, `count()`, `delete()` and as the filter in `updateMany()`
- [UpdateQuery][opensavvy.ktmongo.dsl.query.UpdateQuery] is used as the update in `updateMany()`
- [UpsertQuery][opensavvy.ktmongo.dsl.query.UpsertQuery] is used as the update in `upsertOne()`
- [UpdateWithPipelineQuery][opensavvy.ktmongo.dsl.query.UpdateWithPipelineQuery] is used as the update in `updateManyWithPipeline()`
- [UpsertWithPipelineQuery][opensavvy.ktmongo.dsl.query.UpsertWithPipelineQuery] is used as the update in `upsertOneWithPipeline()`

# Package opensavvy.ktmongo.dsl.path

Syntax to refer to specific fields in MongoDB documents.

When creating a query, we need to refer to specific fields in a MongoDB document, which may be in a nested document or in an array. Traditionally, this is done with the string representation of the path, but that leads to brittle code that is hard to navigate and refactor. Instead, KtMongo provides a DSL to refer to fields with type-safety:

```kotlin
User::_id                          // _id
User::profile / Profile::name      // profile.name
User::friends[1] / Friend::name    // friends.1.name
```

The operators are implemented in the interface [FieldDsl][opensavvy.ktmongo.dsl.path.FieldDsl] which is already brought into scope by all KtMongo methods, and return instances of [Field][opensavvy.ktmongo.dsl.path.Field], the type-safe representation of a MongoDB path.

### Operators

- [The `.` operator][opensavvy.ktmongo.dsl.path.FieldDsl.div]
- [The `.n.` operator][opensavvy.ktmongo.dsl.path.FieldDsl.get]
