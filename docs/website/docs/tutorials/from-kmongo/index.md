# Why migrate from KMongo?

If you've heard of this project, and are currently using [KMongo](https://litote.org/kmongo/), but aren't quite sure why you should migrate to KtMongo, this page is made for you.

If you're not using KMongo, but you are using one of the official MongoDB drivers, you may prefer reading [the features overview](../../index.md) instead.

## KMongo is deprecated

The first reason, and the instigator for the creation of KtMongo in the first place, is that [KMongo is deprecated](https://litote.org/kmongo/). After the release of the official Kotlin driver in 2023, development of the KMongo project stopped. However, the Kotlin driver doesn't have the type-safe DSL we have grown accustomed to. Because of this, many projects cannot migrate to the official driver: it would require rewriting all queries, for an end result that is less safe and harder to read.

KtMongo aims to help migrate to the official Kotlin driver: because it reimplements a DSL inspired by KMongo, it is the best of both worlds—your project can continue using a familiar DSL, while internally using the official Kotlin driver.

## Migration is easy

KtMongo's DSL does have a few breaking changes as compared to KMongo's, so migration isn't just changing the imports. We make these breaking changes when we believe they improve the safety, performance or readability of the queries.

The most visible change is the move from `vararg`-based operators to a lambda-based DSL:

```kotlin title="Using KMongo"
songs.find(
	and(
		Song::artist / Artist::name eq "Zutomayo",
		Song::title eq "Truth in lies",
	)
)
```

```kotlin title="Using KtMongo"
songs.find {
	and {
		Song::artist / Artist::name eq "Zutomayo"
		Song::title eq "Truth in lies"
	}
}
```

As you can see, the main difference is the replacement of parentheses by braces, and the disappearance of the comma at line endings. Other than that, most operators are unchanged, so you'll feel right at home.

Although you could keep the request as-is, KtMongo actually allows us to simplify this example further, which we'll see [later on this article](#query-dsl-and-optional-filters).

!!! tip "This migration could be automatic!"
    The growth in popularity of [OpenRewrite](https://docs.openrewrite.org/) has made it a possible solution to automate these small refactors. We're searching for someone to help us set this up—if you'd like to help, [please contact us](https://gitlab.com/opensavvy/ktmongo/-/issues/25).

## Migrate at your own pace

KtMongo and KMongo are compatible, meaning that both can be used in the same project.

Most projects are structured with one repository class per collection. We recommend migrating one such repository to KtMongo at a time, little by little over the course of multiple releases. This ensures the migration can be done at your own pace, without slowing down the rest of the development. **There is no need to feature-freeze during the migration!**

## Query DSL and optional filters

As we have seen above, the main difference KtMongo makes is to replace `vararg`-based functions by DSLs. This approach brings a few benefits.

Let's write a KMongo query, directly translating it to KtMongo:

```kotlin
songs.find {
	and {
		Song::artist / Artist::name eq "Zutomayo"
		Song::title eq "Truth in lies"
	}
}
```

We can simplify this query by removing the `$and` operator: it is implied when a `find` contains multiple filters.

```kotlin
songs.find {
	Song::artist / Artist::name eq "Zutomayo"
	Song::title eq "Truth in lies"
}
```

Another improvement is the introduction of operators to handle optional filter criteria. For example, with KMongo, if we wanted to make a request and optionally filter by a date, we could write:

```kotlin title="Using KMongo"
songs.find(
	and(
		buildList {
			add(Song::artist / Artist::name eq artistName)

			if (minDate != null)
				add(Song::releaseDate gte minDate)

			if (maxDate != null)
				add(Song::releaseDate lte maxDate)
		}
	)
)
```

The DSL approach by itself eliminates most of the boilerplate of this request, because it allows us to use conditionals directly in the request body:

```kotlin title="Using KtMongo"
songs.find {
	Song::artist / Artist::name eq artistName

	if (minDate != null)
		Song::releaseDate gte minDate

	if (maxDate != null)
		Song::releaseDate lte maxDate
}
```

This is great for building complex queries that have a different structure each time they are called. Loops, conditions, and any other Kotlin language features are available directly in the DSL.

The specific use-case of optional filters is quite common, so KtMongo provides a purpose-built operator variant: the `notNull` operators apply to the request only if their argument is non-`null`. Using them, we can simplify the request further:

```kotlin title="Using KtMongo"
songs.find {
	Song::artist / Artist::name eq artistName
	Song::releaseDate gteNotNull minDate
	Song::releaseDate lteNotNull maxDate
}
```

## Avoid using an operator in the wrong context

KMongo operators are instantiated by calling top-level functions like `eq()`, `set()` and `gt()`, that return `Bson` instances. However, not all operators are available for all contexts. For example, using `set()` instead of a `find()` is always an error. Although this particular example will be discovered quickly, that is not always the case; for example what about the two syntaxes of the `$eq` operator? The `$eq` operator in regular updates and in aggregation updates is different. KMongo doesn't protect against this type of mistakes.

Because KMongo cannot make such difference, most aggregation operators are not implemented at all. Many projects in the real world reimplement aggregation operators with varying level of quality, mainly due to this limitation.

Thanks to the DSL, KtMongo can check these constraints. Using a `set` operator where it is not allowed will lead to a compilation error. Therefore, KtMongo can offer both an update `$eq` and an aggregation `$eq`, with the same API but different implementations, as the correct one will be selected by the compiler. Additionally, this makes finding operators easier: each operation injects a receiver into the DSL block, which contains documentation on the different available operators.

## Aggregation support

Because KtMongo understands in which context operators are available, KtMongo can support aggregation operators transparently, with a similar syntax to query operators.
Additionally, aggregation pipelines are declared with a fluent API, like streams, sequences and flows:

```kotlin
users.aggregate()
	.match {
		User::age gte 18
	}
	.limit(10)
	.sort {
		ascending(User::name)
	}
	.project {
		excludeId()
		include(User::name)
		include(User::age)
	}
	.toList()
```

## Avoid using expressions that do not match the current collection

KMongo is typesafe in some ways, but isn't in some others. For example, KMongo correctly doesn't allow this code:

```kotlin
users.find(
	User::pet / User::name
)
```

This code (correctly) doesn't compile, because `User::name` isn't a child of the `User::pet` property. Instead, the user should write:

```kotlin
users.find(
	User::pet / Pet::name
)
```

However, KMongo doesn't protect from this similar error:

```kotlin
users.find(
	Pet::name eq "Foo"
)
```

Here, we are using a filter based on `Pet::name` in a collection of type `User`. If `User` happens to have a field `name`, this request will be valid from MongoDB's point of view (as the type is not written in BSON). Such bug could exist for a long time before it is discovered, and may cause some data to be lost. For example, because some requests are based on a domain class, but some others are based on its DTO representation.

KtMongo protects against both of these errors at compile-time.

## Improved documentation

We strive for making KtMongo's documentation as comprehensive as possible. In particular:

- The documentation is split between the website (this page) and the reference.
- The website provides guides to discover a concept, then a feature page describing the concepts further, and finally the reference provides an exhaustive description of all methods.
- Most methods in the reference have at least one example.
- Most methods in the reference have a link to the official MongoDB documentation.
- Operation and operator containers (e.g. operator interfaces) list their operators using the MongoDB syntax (e.g. `$in`) so you can easily find the Kotlin function even if it is named differently.

## Ease of debugging

We strive for breaking the barrier between the Kotlin representation and the BSON examples in the official documentation. One way we do this is by having meaningful `toString` implementations on most objects.

For example, the `toString` representation of a collection displays its name. The `toString` representation of a filtered collection (client-side view) displays the name of the underlying collection as well as the filter, in BSON syntax.

All DSLs have a `toString` implementation that displays the exact BSON that would be sent to the database, as of that moment in time.
For example:

```kotlin
users.find {
	println("Before: $this")

	User::name eq "Bob"

	println("After:  $this")
}
```

```text
Before: {}
After:  {"name": {"$eq": "Bob"}}
```

This transparency makes debugging complex requests much easier.
