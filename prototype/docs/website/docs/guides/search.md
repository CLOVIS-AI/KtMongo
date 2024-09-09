# Find data

!!! note ""
    Before retrieving data, you must [connect to the database and obtain a collection](setup.md).

Let's assume we have the following class:

```kotlin
class User(
	val name: String,
	val age: Int,
)
```

To return all documents in a collection, we can use `find`:

```kotlin
collection.find()
	.toList()
	.forEach { println(" - $it") }
```

To only return specific documents, we can add a filter expression:

```kotlin
// Find all users who have a name and who are older than 18
collection.find {
	User::name.isNotNull()
	User::age gt 18
}.toList()
	.forEach { println(" - $it") }
```

If we know that only one user may exist, we can use `findOne` instead:

```kotlin
collection.findOne { User::name eq "Sylvain De La Fontaine" }
```
