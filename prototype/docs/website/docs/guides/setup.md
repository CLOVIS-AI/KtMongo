# Quick start

## Choose between suspension and blocking

## Add the dependency

[//]: # (TODO: publish to central)

KtMongo is currently not published to MavenCentral. To import it into your projects, use a [Gradle composite build](https://docs.gradle.org/current/userguide/composite_builds.html).

## Connect to a database

First, instantiate a client and request a specific database:

```kotlin
val database = MongoClient.create("mongodb://localhost:PORT-NUMBER")
	.getDatabase("foo")
```

To configure more options, see [the official documentation](https://www.mongodb.com/docs/drivers/kotlin-sync/).

## Access a collection

First, create a class that represents the documents stored in the collection:

```kotlin
class User(
	val name: String,
	val age: Int,
)
```

Now, instantiate the collection:

```kotlin
val collection = database.getCollection<User>("users")
	.asKtMongo()
```

## Perform a simple operation

Now that we have access to the collection, we can perform operations on it:

```kotlin
val count = collection.countDocumentsEstimated()
```
