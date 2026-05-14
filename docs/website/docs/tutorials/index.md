# Choose your flavor of KtMongo

KtMongo is a DSL to interact with [MongoDB](https://www.mongodb.com/), the [most popular document database](https://survey.stackoverflow.co/2025/technology#most-popular-technologies-database-database).

MongoDB is a flexible database, allowing the Kotlin compiler to take control of type safety. **Your schema is your Kotlin code.**

KtMongo represents your schema, your data and your requests in a single unified, type-safe Kotlin DSL.

There are multiple flavors of KtMongo, each with its own advantages and use cases.

## Based on the official driver

!!! tip "Recommended for most projects"

In this flavor of KtMongo, the DSL sits on top of the [official MongoDB Kotlin driver](https://www.mongodb.com/docs/languages/kotlin/), which itself uses the MongoDB Java driver.

- **Use KtMongo at your own pace.** Use what you want from the DSL, no need to migrate your existing code.
- **Best security.** KtMongo only implements the query DSL (which is stable and rarely changes). When MongoDB Inc. publishes a security fix, you can upgrade to it immediately without waiting for KtMongo to release a new version.
- **Coroutines? Make your own decision.** KtMongo exists both in blocking or suspending variants.
- **Use your existing serialization library.** Serialization is handled by the official driver, which supports KotlinX.Serialization (recommended), Jackson, etc.

[**Get started**](official/index.md) • [**Blocking reference**](../api/driver-sync/index.md) • [**Coroutines reference**](../api/driver-coroutines/index.md)

## Migrating from KMongo

The [KMongo library](https://litote.org/kmongo/), unmaintained since 2023, is the primary inspiration for the KtMongo DSL.
While the KtMongo DSL is different in important ways, it is also similar in usage and is easy to migrate to.

- **Use KtMongo at your own pace.** Use what you want from the DSL, for example our improved support for aggregations or optional filters. No need to migrate your existing code.

[**Learn more**](from-kmongo/index.md)

## Going multiplatform

!!! warning "Experimental"
    The documentation may mention features that are not yet implemented, or not yet available in released versions.

The Multiplatform driver is a complete reimplementation of a MongoDB driver, using no code from MongoDB Inc.
It uses KotlinX.Serialization, KotlinX.IO and Ktor Sockets.

- **Streamlined for Kotlin.** Direct streaming from the query DSL into the socket.
- **Built on top of coroutines.** Coroutines from your code to the socket, no intermediate threadpool.
- **Wasm support.** [WebAssembly](https://webassembly.org/) is the future of microservices and serverless deployments. The JVM is optimized for large, long-running servers; Wasm is optimized for small, isolated modules which don't need a container runtime.
- **Opinionated serialization.** Tight integration with KotlinX.Serialization for even better performance.

[**Learn more**](multiplatform/index.md) • [**Roadmap**](multiplatform/index.md#roadmap)

## For Java developers

!!! warning "Prototypal"
    The Java driver is a proof-of-concept that isn't currently our focus.

KtMongo's secret sauce relies on features of the Kotlin language that Java doesn't have. And yet, even without them, KtMongo is _still_ more ergonomic than the simple Java-like builders.

- **Based on the official Java driver.** The official Kotlin driver uses the official Java driver under the hood.
- **Better type-safety.** The official driver provides no type-safety in the query builders. KtMongo takes advantage of Java's records to provide similar type-safety to Kotlin's property syntax.

[**Learn more**](../api/driver-sync-java/index.md)
