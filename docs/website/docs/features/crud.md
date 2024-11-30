# CRUD operations

CRUD operations (Create, Read, Update and Delete) are the most basic operations a database supports.

!!! note ""
    Before using any of the code in this page, you will need to connect to a database. To learn how to do so, visit the [Getting Started tutorial](../tutorials/index.md).

MongoDB separates its data into:

- **Databases** are similar to packages in programming languages. Each database is a namespace providing isolation, so you can deploy multiple projects onto a single MongoDB installation.
- **Collections** are the equivalent of tables in SQL. Each collection can contain an unlimited amount of data. There are various types of collections for our various needs.
- **Documents** are BSON (binary-JSON) objects. MongoDB doesn't verify a schema by default, so they could be completely homogeneous. Since we aim to use Kotlin as a source of truth, however, in practice our Kotlin code serves as the schema. Since documents are JSON, they can contain nested documents and nested arrays, but they cannot exceed 16MB each.

In MongoDB, CRUD operations target a single collection.
All writes are atomic on the level of a single document.

## Basic modeling

With KtMongo, we represent the schema of a collection by declaring a Kotlin class. Depending on the serialization library you are using, this may be slightly different (for example, if you're using KotlinX.Serialization, you'll need to add an `@Serializable` annotation to the class).

```kotlin
class User(
	val name: String,
	val age: Int = 0
)
```

MongoDB requires the presence of a unique field named `_id`. If we don't declare it, MongoDB will automatically create it. We can declare the ID to be of any type (including nested documents), but MongoDB is optimized for the special type `ObjectId`:

```kotlin
class User(
	val _id: ObjectId,
	val name: String,
	val age: Int = 0
)
```

Since we do not use a schema, our Kotlin class represents the source of truth for what the collection can hold. Some serialization libraries (including KotlinX.Serialization) allow using default values in case the field doesn't exist, which allows us to create new fields without breaking the existing data. Similarly, if we remove a field, the existing data is simply ignored. Together, this means migration scripts are rarely needed with MongoDB, unlike with SQL.

Many people declare these classes as `data class`, the advantage being an improved `toString` representation, but it isn't mandatory.

In the rest of this article, we assume you have [obtained a collection](../tutorials/index.md) and named it `users`.

## Create

Creating a new document is done directly with an instance of the class and the method `insertOne`:

```kotlin
users.insertOne(User(ObjectId(), "Bob"))
```

If the collection didn't yet exist, any write operation creates it.

If we want to insert multiple documents at the same time, we can use `insertMany`:

```kotlin
users.insertMany(
	User(ObjectId(), "Bob"),
	User(ObjectId(), "Marcel"),
	User(ObjectId(), "Jeanne")
)
```

## Read

Read operations retrieve documents from a collection.
For example, we can `count` how many documents exist in a collection:

```kotlin
users.count()
```

Or, we can get all the documents using the `find` method:

```kotlin
users.find().toList()
```

However, lists are in-memory data structures, and it may not be appropriate to query an entire collection into memory. Instead, we can stream the results using `forEach`:

```kotlin
users.find().forEach { println("Found a document: $it") }
```

Or, if we want to further process the documents, we can use asynchronous steaming functionalities:

=== "Coroutines driver"

    ```kotlin
    users.find().asFlow()
    ```

=== "Synchronous driver"

    ```kotlin
    users.find().asStream()
    ```

Of course, we usually want to let the database perform filters, as it benefits from indexes. Filters are declared in a trailing lambda, usually as infix functions. Filters apply to a specific field, which is referred to using the name of the class, followed by `::`, followed by the name of the field:

```kotlin
users.find {
	User::name gte "C"
	User::name lt "G"
}.toList()
```

This query will return all users with a name that is alphabetically between "C" and "G".
This syntax is typesafe: invalid requests (for example comparing against another type) will not compile.

[//]: # (TODO: add a link to the 'collation' option, whenever it is implemented)

If you are only interested in a single document, use `findOne`, which returns a nullable value instead of a list:

```kotlin
users.findOne {
	User::name eq "Bob"
}
```

Learn more:

- [Referring to fields](fields.md)

## Update

Update operations modify existing documents in a collection.

Similarly to search criteria, we can use infix operators to update some fields. To update all documents, use `updateMany`:

```kotlin
users.updateMany {
	User::age inc 1
}
```

If you want to only edit some documents (not the entire collection), use the optional `filter` parameter, which accepts the same syntax as `find()` and `findOne()`:

```kotlin
users.updateMany(
	filter = {
		User::name eq "Bob"
	}
) {
	User::age inc 1
}
```

If you only want to update a single document, use `updateOne` instead, which has the same syntax.

Finally, if you want to ensure that a specific document exists, and want to create it if it doesn't, use `upsertOne`.

## Delete

Delete operations remove documents from a collection. Delete operations accept a filter, just like `findOne` and `findMany`.

To delete one document, use `deleteOne`:

```kotlin
users.deleteOne {
	User::name eq "Bob"
}
```

To delete multiple documents, use `deleteMany`:

```kotlin
users.deleteMany {
	User::age lt 18
}
```

Additionally, to delete the entire collection, use `drop`:

```kotlin
users.drop()
```
