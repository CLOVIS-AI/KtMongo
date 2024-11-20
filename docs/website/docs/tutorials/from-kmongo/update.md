# Updating data

Much like [find variants](find.md), update operations use a DSL instead of passing raw BSON values.

## Updating multiple documents

Like with KMongo, the function to update multiple documents is `updateMany`.

```kotlin title="With KMongo"
collection.updateMany(
	filter = and(
		User::name.exists(),
		User::age gt 18
	),
	set(
		User::isLegal setTo true
	)
)
```

```kotlin title="With KtMongo"
collection.updateMany(
	filter = {
		User::name.exists()
		User::age gt 18
	},
	update = {
		User::isLegal set true
	}
)
```

To learn more about filtering, visit [the find documentation](find.md).

Unlike in KMongo, there is no need to combine multiple operators yourself, the library will do it for you. The order of operators is not relevant.

```kotlin title="With KMongo"
collection.updateMany(
	filter = …,
	set(
		User::name setTo "foo",
		User::isLegal setTo true,
	),
	inc(
		User::age setTo 1
	)
)
```

```kotlin title="With KtMongo"
collection.updateMany(
	filter = { … },
	update = {
		User::name set "foo"
		User::isLegal set true
		User::age inc 1
	}
)
```

## Updating a single document

Like in KMongo, KtMongo provides a dedicated function to edit a single element, `updateOne`. 

## Inserting a document if it doesn't exist

KtMongo provides an overload to perform upserts:

```kotlin title="With KMongo"
collection.updateOne(
	filter = …,
	setOnInsert(
		User::creationDate setTo Instant.now(),
	),
	UpdateOptions().upsert(true)
)
```

```kotlin title="With KtMongo"
collection.upsertOne(
	filter = { … },
	update = {
		User::creationDate setOnInsert Instant.now()
	}
)
```
