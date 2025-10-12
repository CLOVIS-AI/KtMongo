# The Multiplatform driver roadmap

Initially, KtMongo was built as a DSL on top of the official Kotlin drivers, which are themselves based on top of the official Java drivers.
However, KtMongo was engineered from the start to avoid strong coupling with the underlying driver.

The KtMongo DSL only requires the ability to write primitive BSON values. Once this is provided for a platform, the entire DSL is usable on that platform.

We are working on creating our own MongoDB driver, `:driver-multiplatform`, from scratch.

## Drivers overview

This new driver will be the third added to the library:

- `:driver-sync` is based on the official Kotlin driver, itself based on the official Java driver.
- `:driver-coroutines` is based on the official Kotlin Coroutines driver, itself based on the official Java Reactive driver.
- `:driver-multiplatform` will be the new pure Kotlin implementation, which will be coroutines-based on all platforms.

As a part of this rewrite, new multiplatform implementations of the BSON types will become available (e.g. `ObjectId`). Users of the official drivers can interchangeably use these new implementations or the ones provided by the official drivers; we intend them to be stored exactly in the same way in the database, so users can migrate from one to the other without any data migration.

## Why even work on this?

The official drivers are quite large and complex within. The official Kotlin coroutines driver is based on the official Java reactive driver, the serialization strategy is based on codecs and reflection (even when using KotlinX.Serialization) and the API lacks flexibility (e.g., builders everywhere, compared to KtMongo's universal `accept` method).

By creating a new implementation from scratch, we hope to make it easier to evolve, and potentially faster and simpler to maintain.

We are interested in exploring Wasm server-side deployments, which are supported by Kubernetes and other orchestrators natively. Wasm is a much better target for large-scale ephemeral deployments ("server-side functions", "serverless") and micro-services than the JVM. To our knowledge, MongoDB Inc. has no interest in these environments.

## What will happen to the modules based on the official drivers?

The `:driver-sync` and `:driver-coroutines` modules are based on the official Kotlin drivers. This will not change, as we consider this their primary feature: they are a DSL _on top_ of the official drivers, and can never be anything else.

We believe they are the primary way enterprise users should use KtMongo, as it gives much stronger guarantees over long-term support. They allow bypassing KtMongo and interacting directly with the official drivers directly, which is critical for long-term projects.

## What will the stack look like?

The multiplatform driver will be based on KotlinX.IO, KotlinX.Serialization, KotlinX.Coroutines and Ktor Sockets.

If you'd like to contribute, please get in touch.

## Roadmap

- [x] Split `:bson` and `:bson-official`: `:bson` should only contain interfaces and be multiplatform.
- [ ] Provide new pure Kotlin implementations of the specific MongoDB data types:
	- [x] All primitive types
	- [x] `ObjectId`
	- [x] `Timestamp`
	- [x] `DateTime` (using Kotlin's `Instant`)
	- [ ] `Decimal128`
	- [ ] Deprecated data types:
		- [ ] `JavaScript`
		- [ ] `JavaScriptWithScope`
		- [ ] `DBPointer`
		- [ ] `Symbol`
- [ ] Provide a way to read/write all the BSON types to/from BSON
	- [ ] Read all data types • [Tracking as #49](https://gitlab.com/opensavvy/ktmongo/-/issues/49)
	- [ ] Write all data types • [Tracking as #48](https://gitlab.com/opensavvy/ktmongo/-/issues/48)
- [x] KotlinX.Serialization support for the multiplatform implementations
- [ ] Testing the `:dsl` module both with the official and multiplatform implementations
- [ ] Start working on the TCP exchanges
- [ ] Complete the protocol
	- [ ] Compression algorithms
	- [ ] Encryption in transit (TLS)
