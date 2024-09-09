# Find data

KMongo operators primarily work by accepting varargs which are combined into `Bson` documents.

KtMongo operators primarily work by exposing a DSL in which operators can be bound.

For example,

```kotlin title="Using KMongo"
collection.findOne(
	and(
		User::name.exists(),
		User::age gt 18,
	)
)
```

becomes:

```kotlin title="Using KtMongo"
collection.findOne {
	and {
		User::name.exists()
		User::age gt 18
	}
}
```

Note how:

- the parentheses become braces,
- the trailing commas are gone.

## Default composition operators

Each operation has a default operator when multiple values are passed. For example, `findOne` has a default of `$and`, meaning that these two snippets are identical:

```kotlin
collection.findOne {
	and {
		User::name.exists()
		User::age gt 18
	}
}
```

```kotlin
collection.findOne {
	User::name.exists()
	User::age gt 18
}
```

## Complex requests

One advantage of the DSL syntax is using conditionals and loops directly into the request itself, making complex requests much easier to write.

Here's an example of a complex request with KMongo:

```kotlin title="Using KMongo"
val bson = ArrayList<Bson>()

if (criteria.name != null)
	bson.add(User::name eq criteria.name)

if (criteria.age != null)
	bson.add(User::age eq criteria.age)

collection.findOne(and(bson))
```

When these kinds of requests grow, they become harder to understand because the criteria are defined further from the operation call.

With KtMongo, everything is co-located and the intermediate list is eliminated:

```kotlin title="Using KtMongo"
collection.findOne {
	if (criteria.name != null)
		User::name eq criteria.name

	if (criteria.age != null)
		User::age eq criteria.age
}
```

Since the specific use-case of optional filter criteria is so common, KtMongo offers specific operators that only apply to the request when their argument is non-`null`:

```kotlin title="Using KtMongo"
collection.findOne {
	User::name eqNotNull criteria.name
	User::age eqNotNull criteria.age
}
```
