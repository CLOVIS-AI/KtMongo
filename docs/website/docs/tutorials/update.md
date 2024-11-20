# Updating data

!!! note ""
    Before retrieving data, you must [connect to the database and obtain a collection](index.md).

Let's assume we have the following class:

```kotlin
class User(
	val name: String,
	val age: Int,
)
```

## Updating multiple documents

To update all elements in a collection, use `updateMany`:

```kotlin
collection.updateMany {
	User::age set 18
}
```

To update only specific documents, add a filter:

```kotlin
collection.updateMany(
	filter = {
		User::name eq "Bob"
	},
	update = {
		User::age set 18
	}
)
```

Alternatively, create a filtered collection to avoid the two-lambda syntax:

```kotlin
collection
	.filter { User::name eq "Bob" }
	.updateMany { User::age set 18 }
```

## Updating a single document

To update a single document, use `updateOne` instead. The syntax is identical.

If you'd like to atomically update and retrieve a document, use `findOneByUpdate`. See its option to decide between retrieving the document before or after the update.

## Inserting a document if it doesn't exist

When using `updateOne` with a filter, the operation does nothing if the filter doesn't exist. If, instead, you want to create the document if it doesn't exist, use `upsertOne`:
```kotlin
collection.upsertOne(
	filter = {
		User::name eq "Bob"
	},
	update = {
		User::age set 18
	}
)
```

If there exists a document with a `name` of "Bob", it is updated. If none exist, the following document is created:
```json
{
    "name": "Bob",
    "age": 18
}
```

Not all filter expressions are added to the created object. To learn more, visit `upsertOne`'s documentation.

`upsertOne` is also compatible with filtered collections.
