---
template: home.html
---

# Towards the future of MongoDB in Kotlin

In 2016, Julien Buret created [KMongo](https://github.com/Litote/kmongo), a Kotlin driver for MongoDB based on the official Java driver. KMongo added a lot of syntax sugar, making complex queries much more readable and less error-prone thanks to improved type safety:

```java
// Official Java driver
Bson filter = and(eq("user.gender", "female"), gt("user.age", 29));
collection.

find(filter);
```

```kotlin
// KMongo
collection.find(
	and(
		Document::user / User::gender eq "female",
		Document::user / User::age gt 29
	)
)
```

In 2023, MongoDB released an official Kotlin driver. Development of KMongo stopped, but the official driver lacked much of the syntax niceties of KMongo, as well as requiring major migration efforts. As a result, many projects decided to keep using KMongo for the foreseeable future.

We decided to take it upon ourselves to birth the future of MongoDB drivers for Kotlin. KtMongo is based on the official Kotlin driver to ensure we profit from security fixes and new features, and reimplements a DSL inspired by KMongo.

This project is for **everyone who works with KMongo and is worried about the future after the deprecation notice**, as well as for **everyone dissatisfied with the official Kotlin driver**.

If you're starting a new project, or are using the official Java or Kotlin drivers, [discover what we can do for you](guides/overview.md).

## Why not just fork KMongo?

Since KMongo was started, MongoDB and Kotlin have changed a lot. Aggregation pipelines have become an important tool, type-safety has become more critical. We believe some breaking changes are necessary to bring KMongo into the next decades.

We intend KtMongo to be the spiritual successor to KMongo, making changes where insight has given us new ideas. We intend to facilitate gradual migration from KMongo to KtMongo such that projects can profit from these new features and official MongoDB support at their own pace.

To achieve these objectives, KMongo and KtMongo are mutually compatible: they can both be used in a single project. However, KMongo is based on the Java driver, and KtMongo is based on the Kotlin driver, so many classes are slightly different. We recommend adding both libraries together during the migration phase, and migrating files one at a time at your own rhythm, much like when migrating from Java to Kotlin.

To learn more about the changes we have made, see [the KMongo migration guide](migrate-from-kmongo/why.md).
