---
template: home.html
---

# Welcome!

## Towards the future of MongoDB in Kotlin

**KtMongo is a complete rethink of [KMongo](https://litote.org/kmongo/) based on the official Kotlin MongoDB driver.** KMongo was created in 2016 and made MongoDB usage in Kotlin much better, largely participating in the popularity of MongoDB in the Kotlin ecosystem. In 2023, when the official Kotlin driver was released, KMongo was deprecated. However, the official drivers lack most the utilities that made KMongo so attractive. Our goal is to bring back the readability of KMongo, pushing type-safety even further.

How much further? Let's look at a basic example; we want to query some data:
```kotlin
class Document(
	val _id: ObjectId,
	val user: User,
)

class User(
	val gender: String,
	val age: Int,
)
```

With the Kotlin driver, we can easily write a query:
```kotlin title="With the official Kotlin driver"
collection.find(
	and(
		eq("user.gender", "female"),
		gt("user.age", 29)
	)
)
```
However, that query offers no type-safety guarantees. We could have made a typo in the name of a field (or, the name could change in the future), values aren't typed to their field, and we could accidentally use the wrong operator (for example, the aggregation `$eq` instead of the filter `$eq`).

KMongo brought a type-safe DSL that enabled the compiler to check the name, structure and types of our fields during type-checking, vastly diminishing the risk of incorrect code:
```kotlin title="With KMongo (deprecated)"
collection.find(
	and(
		Document::user / User::gender eq "female" //(1)!
		Document::user / User::age eq 29 //(2)!
	)
)
```

1. The `Document::user / User::gender` syntax is expanded to `"user.gender"`. Although this syntax is slightly more verbose, it also guarantees the path is correct, and allows "find usage" in IDE.
2. KMongo checks the type of the passed parameters. If we tried to compare the `age` field with a string, we would get a compilation error, ensuring our code stays correct.

While this example is slightly more verbose, it is also much safer, and thus more maintainable. If we want to rename a field in a document, we can use our IDE's built-in refactoring feature, and all requests are automatically kept up to date.

However, KMongo doesn't verify at compile-time the coherence of requests. For example, we could use the `$set` operator in a query, which would error out at runtime. By replacing intermediary values by DSLs, we can make the above example shorter and safer:
```kotlin title="With KtMongo"
collection.find {
	Document::user / User::gender eq "female"
	Document::user / User::age eq 29
}
```

In this new DSL, all the benefits of KMongo remain, the `$and` operator is implied by the presence of multiple filters, and operators cannot be used in incoherent ways (we cannot use `$set` in a `find()`).

Additionally, this new DSL is easier to inspect: the `this` value injected into all DSL scopes has a `toString` implementation that displays the exact JSON query that would be sent to the database.

## Going further: optional filter parameters

A pattern we very often see in the wild is the presence of optional filters. For example, users can optionally select a date range. These optional filters quickly make queries harder to read:

=== "With list builders"

	```kotlin title="With KMongo (deprecated)"
	collection.find(
		and(
			buildList {
				add(Document::user / User::name eq "Bob")
				
				if (minCreationDate != null)
					add(Document::user / User::creationDate gte minCreationDate)
				
				if (maxCreationDate != null)
					add(Document::user / User::creationDate lte maxCreationDate)
			}
		)
	)
	```

=== "With nullability"

	```kotlin title="With KMongo (deprecated)"
	collection.find(
		and(
			listOfNotNull(
				Document::user / User::name eq "Bob",
				minCreationDate?.let { Document::user / User::creationDate gte it },
				maxCreationDate?.let { Document::user / User::creationDate lte it },
			)
		)
	)
	```

Since KtMongo uses a DSL, query generation can take full advantage of the Kotlin language directly:
```kotlin title="With KtMongo"
collection.find {
	Document::user / User::name eq "Bob"
	
	if (minCreationDate != null)
		Document::user / User::creationDate gte minCreationDate
	
	if (maxCreationDate != null)
		Document::user / User::creationDate lte maxCreationDate
}
```
The same can be said of all other Kotlin language features: conditions, loops, but also creating functions to abstract away a common query that may be parameterized.

In fact, the specific case of optional query parameters is so common that we added special operators to facilitate it: the `notNull` family. Using them, the previous query can be rewritten to:
```kotlin title="With KtMongo"
collection.find {
	Document::user / User::name eq "Bob"
	Document::user / User::creationDate gteNotNull minCreationDate
	Document::user / User::creationDate lteNotNull maxCreationDate
}
```

KtMongo provides multiple features following this trend: adding operators to facilitate common usage in ways that follow the helpfulness of the Kotlin ecosystem.

## Objectives of KtMongo

Our high-level goals are as follows:

**Ease of use in new projects.** Adopting KtMongo in a new project should be as simple as possible. Ideally as simple as using the official drivers.

**Ease of use in existing KMongo projects.** If you have a large codebase using KMongo, we want to let you insert KtMongo incrementally, so you can benefit from our added features without planning a massive rewrite.

**Ease of debugging.** As much as possible, KtMongo classes have a `toString` implementation that displays the actual BSON that would be sent to the database. If you log the requests or use a debugger, you can understand them at a glance, and run the same query in MongoDB Compass or any other tool trivially.

**Documentation.** KtMongo is documented in depth: almost all functions have an example of usage, each operator has a link to the official MongoDB documentation, and DSL scopes list their operators with the MongoDB syntax so you can easily find the Kotlin function, even if it is named differently.

**Convenience for the real world.** MongoDB is used in massive codebases in the industry. We want to facilitate real-world usage patterns, taking advantage of the power of Kotlin. [`*notNull` operator variants](features/optional-filters.md) and [filtered collections](features/filtered-collections.md) are examples of such utilities.

**Keeping the door open for multiplatform.** While we are not actively developing KtMongo on other platforms than the JVM, all modules are already configured to ensure the addition of other platforms in the future is possible. In particular, we're thinking of NodeJS (for scripting) and WASM (for future backends). If you'd like to contribute in this direction, feel free to get in touch!

## Where do I start?

- [**Configuring KtMongo in a new project**](tutorials/index.md)
- **Using KtMongo alongside the official Kotlin driver**
- [**Migrating from KMongo**](tutorials/from-kmongo/index.md)
- [**Discovering the new features**](features/crud.md)
