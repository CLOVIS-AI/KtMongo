# KtMongo: the next MongoDB driver for Kotlin

> _Pronounced "cat-mongo"._

In 2016, Julien Buret created [KMongo](https://litote.org/kmongo/), a Kotlin driver for MongoDB, based on the official Java driver. KMongo added a lot of syntax sugar, making complex queries much more readable and less error-prone thanks to improved type safety:
```java
// Official Java driver
Bson filter = and(eq("user.gender", "female"), gt("user.age", 29));
collection.find(filter);
```

```kotlin
class User(
	val gender: String,
	val age: Int,
)

class Document(
	val user: User,
)

// KMongo
collection.find(
	and(
		Document::user / User::gender eq "female",
		Document::user / User::age gt 29
	)
)
```

In 2023, MongoDB released an official Kotlin driver. Development of KMongo stopped, but the official driver lacked much of the syntax niceties of KMongo, as well as requiring major migration efforts. As a result, many projects decided to keep using KMongo for the foreseeable future.

We decided to take it upon ourselves to birth the future of MongoDB drivers for Kotlin. KtMongo is based on the official Kotlin driver to ensure we profit from security fixes and new features, and reimplements a DSL inspired by KMongo, taking the occasion to change the decisions we didn't like.

This project is for **everyone who works with KMongo and is worried about the future after the deprecation notice**, as well as for **everyone dissatisfied with the official Kotlin driver**.

## Why not just fork KMongo?

Since KMongo was started, MongoDB and Kotlin have changed a lot. Aggregation pipelines have become an important tool, type-safety has become more and more important. We believe KMongo cannot be brought into the next decades without some breaking changes.

We intend KtMongo to be the spiritual successor to KMongo, making changes where insight has given us new ideas. We intend to facilitate gradual migration from KMongo to KtMongo such that projects can profit from these new features and official MongoDB support at their own pace.

To achieve these objectives, KMongo and KtMongo are mutually compatible: they can both be used in a single project. However, KMongo is based on the Java driver, and KtMongo is based on the Kotlin driver, so many classes are slightly different. We recommend adding both libraries together during the migration phase, and migrating files one at a time at your own rhythm, much like when migrating from Java to Kotlin.

To learn more about the changes we have made, see [the KMongo migration guide](docs/migrate-from-kmongo).

## What's the current state of the project?

This project is currently a **prototype**. Many features are missing, and implemented features may change at any time. Use at your own risk!

## License

This project is licensed under the [Apache 2.0 license](LICENSE).
Individual files may have additional copyright holders.

The initial prototype was developed by 4SH, and is also licensed under the [Apache 2.0 license](prototype/LICENSE), though with different copyright holders.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
- To learn more about our coding conventions and workflow, see the [OpenSavvy Wiki](https://gitlab.com/opensavvy/wiki/-/blob/main/README.md#wiki).
- This project is based on the [OpenSavvy Playground](docs/playground/README.md), a collection of preconfigured project templates.
