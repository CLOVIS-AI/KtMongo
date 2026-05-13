# Using KotlinX.Serialization with MongoDB and the official Kotlin driver

[KotlinX.Serialization](https://github.com/kotlin/kotlinx.serialization) is the recommended serialization library by the official Kotlin driver for MongoDB.

KotlinX.Serialization uses a compiler plugin to generate serializers at compile-time.

- **Faster.** During execution, no data analysis takes place. The data is written or read directly.
- **Safer.** Because deserialization is hard-coded in the binary, it is harder for attackers to deserialize unexpected data.
- **More type-safe.** Don't accidentally use your domain objects in your DTOs. If a type is not marked as serializable, it can't be serialized.

## Configuration

Because KotlinX.Serialization uses a compiler plugin, it requires both a Gradle plugin and a runtime library.

```kotlin
plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.serialization") version "2.3.21" //(1)!
}

dependencies {
	implementation("org.mongodb:bson-kotlinx:VERSION") //(2)!
	
	// Choose one of:
	implementation("dev.opensavvy.ktmongo:driver-coroutines:VERSION") //(3)!
	implementation("dev.opensavvy.ktmongo:driver-sync:VERSION") //(4)!
	implementation("dev.opensavvy.ktmongo:bson-official:VERSION") //(5)!
}
```

1. The KotlinX.Serialization plugin should have the exact same version as the Kotlin plugin.
2. The official KotlinX.Serialization support module. [View versions](https://central.sonatype.com/artifact/org.mongodb/bson-kotlinx/versions).
3. KtMongo's coroutine-aware MongoDB driver. Recommended for new projects. [View versions](https://central.sonatype.com/artifact/dev.opensavvy.ktmongo/driver-coroutines/versions)
4. KtMongo's blocking MongoDB driver. Recommended if you have an existing blocking codebase. [View versions](https://central.sonatype.com/artifact/dev.opensavvy.ktmongo/driver-sync/versions)
5. KtMongo's BSON types, without MongoDB DSL. Useful for creating libraries or reusable utilities. [View versions](https://central.sonatype.com/artifact/dev.opensavvy.ktmongo/bson-official/versions)

## Writing serializable classes

To be serializable, classes must be annotated with [`@Serializable`](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-core/kotlinx.serialization/-serializable/) annotation:
```kotlin
@Serializable
class MyDocument(
	val a: String,
	val b: Int,
)
```

Regular classes, sealed classes, sealed interfaces, objects, and other Kotlin types can be marked `@Serializable`. Serialization is not restricted to `data class`.

!!! info
    The KtMongo documentation often doesn't have the `@Serializable` annotation in code examples. You will need to add it to your code.

To learn more about serializable types, inheritance and custom serializers, see [the KotlinX.Serialization documentation](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md).

All documents stored in MongoDB have a field `_id`. You do not need to declare it, MongoDB will generate one if absent when inserting. To declare it, explicitly name the field `_id` in your Kotlin code:
```kotlin
@Serializable
data class User(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)
```
The field `_id` is usually of the type [`ObjectId`](../../api/bson/opensavvy.ktmongo.bson.types/-object-id/index.md), as it is MongoDB's optimized identifier. However, you can use any type, including `Uuid` and complex documents, as long as each inserted document has a unique value.

!!! warning
    Currently, KtMongo does not support annotations such as `@SerialName` to rename a field out of the box. However, you can configure KtMongo to recognize them, see [`PropertyNameStrategy`](../../api/dsl/opensavvy.ktmongo.dsl.path/-property-name-strategy/index.md).

## Schema evolution

Use default values when adding new fields, to ensure you can deserialize older documents that do not have the new field:
```kotlin
@Serializable
data class User(
	val _id: ObjectId,
	val name: String,
	val age: Int? = null, // If absent, 'null'
	val isAdult: Boolean = false, // If absent, 'false'
)
```

## Particular types

Some types require special handling.

### Instant

The type `kotlin.time.Instant` is serialized by default as a `String`, not as BSON's native `DateTime` type. To serialize it as a `DateTime`, we provide a custom serializer [`InstantAsBsonDatetimeSerializer`](../../api/bson/opensavvy.ktmongo.bson.types/-instant-as-bson-datetime-serializer/index.md):
```kotlin
@Serializable
data class MyDocument(
	val _id: ObjectId,
	val editionDate: @Serializable(with = InstantAsBsonDatetimeSerializer::class) Instant,
)
```

### Uuid

The type `kotlin.uuid.Uuid` is serialized by default as a `String`, not as BSON's native `BinaryData` type with subtype 4. To serialize it as a `BinaryData`, we provide a custom serializer [`UuidAsBsonBinarySerializer`](../../api/bson/opensavvy.ktmongo.bson.types/-uuid-as-bson-binary-serializer/index.md):
```kotlin
@Serializable
data class MyDocument(
	val _id: @Serializable(with = UuidAsBsonBinarySerializer::class) Uuid,
)
```
