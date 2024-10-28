# Module MongoDB driver for Kotlin (synchronous)

Blocking/synchronous driver for MongoDB, using a rich Kotlin DSL.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22driver-sync%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/driver-sync.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/driver-sync"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

## Configuration

Add a dependency on `dev.opensavvy.ktmongo:driver-sync`.

For example, in a Kotlin Multiplatform project:
```kotlin
plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	// …
	
	sourceSets.commonMain.dependencies {
		implementation("dev.opensavvy.ktmongo:driver-sync:VERSION-HERE")
	}
}
```

In a Kotlin JVM project:
```kotlin
plugins {
	kotlin("jvm")
}

dependencies {
	implementation("dev.opensavvy.ktmongo:driver-sync:VERSION-HERE")
}
```

## Basic usage

Once you have obtained an instance of [MongoCollection][opensavvy.ktmongo.sync.MongoCollection] (see platform-specific instructions on how to do this), you can use it to access the database:

```kotlin
class User(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

collection.findOne {
	User::age gte 18
}

collection.update(
	filter = {
		User::_id eq ObjectId("507f1f77bcf86cd799439011")
	},
	update = {
		User::name set "Bob"
	}
)
```

[Learn more about the available operations][opensavvy.ktmongo.sync.MongoCollection].

# Package opensavvy.ktmongo.sync

Declaration and implementation of collections and iterables.

# Package opensavvy.ktmongo.sync.operations

Declaration of the different types of operations.
