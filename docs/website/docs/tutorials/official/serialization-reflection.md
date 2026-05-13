# Using reflection-based serialization for Kotlin data classes in MongoDB and the official Kotlin driver

The official MongoDB Kotlin driver includes a reflection-based serializer for Kotlin data classes.

- **Simpler for basic usage.** No configuration, no annotations, no compiler plugin.
- **Slower.** Reflection can be more than 10× slower than [KotlinX.Serialization](serialization-kotlinx.md).
- **Rigid.** Inheritance and polymorphism use the Java driver's codec system.

## Configuration

```kotlin
plugins {
	kotlin("jvm") version "2.3.21"
}

dependencies {
	implementation("org.mongodb:bson-kotlin:VERSION") //(1)!
	
	// Choose one of:
	implementation("dev.opensavvy.ktmongo:driver-coroutines:VERSION") //(2)!
	implementation("dev.opensavvy.ktmongo:driver-sync:VERSION") //(3)!
	implementation("dev.opensavvy.ktmongo:bson-official:VERSION") //(4)!
}
```

1. The official reflection support module. [View versions](https://central.sonatype.com/artifact/org.mongodb/bson-kotlin/versions).
2. KtMongo's coroutine-aware MongoDB driver. Recommended for new projects. [View versions](https://central.sonatype.com/artifact/dev.opensavvy.ktmongo/driver-coroutines/versions)
3. KtMongo's blocking MongoDB driver. Recommended if you have an existing blocking codebase. [View versions](https://central.sonatype.com/artifact/dev.opensavvy.ktmongo/driver-sync/versions)
4. KtMongo's BSON types, without MongoDB DSL. Useful for creating libraries or reusable utilities. [View versions](https://central.sonatype.com/artifact/dev.opensavvy.ktmongo/bson-official/versions)

## Writing serializable classes

Kotlin data classes are supported.

```kotlin
data class User(
	val _id: ObjectId,
	val name: String,
	val age: Int? = null,
)
```

Here is a non-exhaustive list of unsupported situations:

- `private` classes.
- Local classes.
- Non-`data` classes.
