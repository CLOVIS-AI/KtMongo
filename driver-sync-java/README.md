# Module MongoDB driver for Kotlin (synchronous, with Java helpers)

Blocking/synchronous driver for MongoDB Java users.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22driver-sync-java%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/driver-sync-java.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/driver-sync-java"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

While KtMongo is a Kotlin-first library, we are prototyping its usage from Java. The KtMongo builder-based DSL can still be more convenient to use in Java than the official driver, because it deals with conditional requests more elegantly.

Note that this is a prototype. It is not complete and is not our priority for now. However, we welcome contributions.

## Configuration

Add a dependency on `dev.opensavvy.ktmongo:driver-sync-java`.

In a Java project:

```kotlin
plugins {
	java
}

dependencies {
	implementation("dev.opensavvy.ktmongo:driver-sync:VERSION-HERE")
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

Start by creating a `com.mongodb.client.MongoCollection` instance by following the instructions from the official Java driver.

Use the method [`KtMongo.from`][opensavvy.ktmongo.sync.KtMongo.from] to convert it into a KtMongo collection.

Because Java doesn't provide optional parameters, and because Java and Kotlin lambdas are slightly different, we offer convenience methods.

```java
collection.find(options(),filter(filter ->{
	filter.

eq(JavaField.of(Profile::name), "Fred");
	}));
```

You can find more complex examples in the [test directory](https://gitlab.com/opensavvy/ktmongo/-/tree/main/driver-sync-java/src/test/java/opensavvy/ktmongo/sync?ref_type=heads).

Java doesn't support operator overloading, so the `User::profile / Profile::name` syntax isn't possible. It is replaced by the [`JavaField`][opensavvy.ktmongo.sync.JavaField] class, which provides similar functionality.
