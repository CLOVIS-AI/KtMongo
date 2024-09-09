# Why migrate from KMongo?

If you've heard of this project, and are currently using [KMongo](https://litote.org/kmongo/), but aren't quite sure why you should migrate to KtMongo, this page is made for you.

If you're not using KMongo, but you are using one of the official MongoDB drivers, you may prefer reading [the features overview](../guides/overview.md) instead.

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
The growth in popularity of OpenRewrite has made it a possible solution to automate these small refactors. We're searching for someone to help us set this up—if you'd like to help, [please contact us](https://github.com/4sh/ktmongo/discussions/21).

## Migrate at your own pace

KtMongo and KMongo are compatible, meaning that both can be used in the same project.

Most projects are structured with one repository class per collection. We recommend migrating one such repository to KtMongo at a time, little by little over the course of multiple releases. This ensures the migration can be done at your own pace, without slowing down the rest of the development. **There is no need to feature-freeze during the migration!**

[//]: # (TODO: show how to convert a KMongo collection to a KtMongo collection)

## Query DSL and optional filters

As we have seen above, the main difference KtMongo makes is to replace `vararg`-based functions by DSLs. This approach brings a few benefits.

Let's write a query like we would have with KMongo:

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
	Song::releaseDate gte minDate
	Song::releaseDate lte maxDate
}
```

## Avoid using an operator in the wrong context

## Avoid using expressions that do not match the current collection
