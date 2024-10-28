# Module MongoDB driver for Kotlin (synchronous)

Blocking/synchronous driver for MongoDB, using a rich Kotlin DSL.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22driver-sync%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/driver-sync.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/driver-sync"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

## Configuration

Start by declaring a dependency on this module (see the common page for help).

This driver is built on the top of the [official Kotlin driver](https://www.mongodb.com/docs/languages/kotlin/kotlin-sync-driver/current/). You can use the official tutorials to get started and learn how to connect to the database.

The most basic option is to connect to your local MongoDB instance running on `localhost:27017`, use:
```kotlin
class User(
	val _id: ObjectId,
	val name: String,
)

val client = MongoClient.create()
val database = client.getDatabase("my_project")
val collection = database.getCollection<User>("users").asKtMongo()
```

Note the call to [`asKtMongo()`][opensavvy.ktmongo.sync.asKtMongo] which is the only difference from the official usage.
From then on, all methods from this driver are available on the `collection` variable: see [MongoCollection][opensavvy.ktmongo.sync.MongoCollection].

This means you are able to use KtMongo DSLs within your existing repositories: simply convert into a KtMongo equivalent where you need KtMongo functionality.
