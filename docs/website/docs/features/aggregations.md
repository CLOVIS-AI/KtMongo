# Introduction to aggregations

MongoDB aggregations are a way to declare a pipeline of operations that MongoDB will execute on your data. Unlike in regular queries, aggregation allow complex operators to better control your data. For example, you can compare two fields of the same document, sort based on a computed value, or combine multiple documents.

The aggregation framework is extensive, so not everything is currently supported by KtMongo. You can follow the progress in [#7](https://gitlab.com/opensavvy/ktmongo/-/issues/7). Don't hesitate to comment if you would want to prioritize a specific operator. We are also interested in real-world examples of your existing MongoDB aggregations.

In KtMongo, aggregations are written with a fluent builder, like `Iterable`, `Sequence`, `Flow` and `Stream`:

```kotlin
users.aggregate()
	.match { … }
	.sort { … }
	.limit(200)
	.toList()
```

Within each stage, you may combine values together.

!!! note ""
    Unlike other stages, `match` uses regular find syntax and not aggregation operators. Use the [`expr` operator](../api/-mongo-d-b%20request%20-d-s-l/opensavvy.ktmongo.dsl.query/-filter-query/expr.md) within `match` to access aggregation operators.

## Aggregation operators

Overall, most regular operators can also be used in aggregations, but with a very different syntax. KtMongo limits most of these syntactic differences, so your Kotlin code is almost the same for both. There are, however, two main differences between regular queries and aggregations, that you need to be careful about.

**Aggregation operators operate like Kotlin expressions**

In a regular query written with KtMongo, all operators called are automatically bound into the request:

```kotlin
users.find {
	User::name eq "Bob"
	User::age gte 18
}
```

In this example, both operators are applied to the request.

However, aggregation operators behave like regular Kotlin code: only the last value in the lambda is actually taken into account.

```kotlin
users.find {
	expr {
		of(User::name) eq of("Bob")  // ⚠ Unused ⚠
		of(User::age) gte of(18)
	}
}
```

In this example, the `$eq` isn't generated, due to the way aggregation operators work.

The Kotlin team is adding new inspections to the compiler that will be able to warn you in such situations, but they are not yet available.

In the meantime, as with any KtMongo object, you can add a `println(this)` just before the closing bracket of `find {}` to know exactly what request will be generated.

**Use regular values requires `of`**

As you can see in the example above, we sometimes need to use the conversion function `of` to pass arguments to aggregation operators. This is sadly not something we can do anything about. As a rule of thumb, any argument of an operator that doesn't implement the KtMongo `Value` interface requires an `of` conversion.

While there are multiple ways the Kotlin language could be modified to remove this requirement, we believe the most promising is [KT-68318](https://youtrack.jetbrains.com/issue/KT-68318/Declaration-site-defined-conversions). Please vote and give your opinion.

<p>

You can find the list of implemented aggregation operators [here](../api/-mongo-d-b%20request%20-d-s-l/opensavvy.ktmongo.dsl.aggregation/-aggregation-operators/index.html).

## Aggregation stages

You can find the list of implemented aggregation stages [here](../api/-mongo-d-b%20request%20-d-s-l/opensavvy.ktmongo.dsl.aggregation/-aggregation-pipeline/index.html).
