# Module Kotlin BSON • Multiplatform abstraction for different BSON implementations

Primitives for BSON types.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22bson%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/bson.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/bson"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

This module contains abstractions and utilities for implementing the BSON specification.

BSON objects are obtained from the [`BsonFactory`][opensavvy.ktmongo.bson.BsonFactory] interface. Each BSON implementation should provide its own implementation.

The KtMongo project provides two different implementations: `:bson-official` uses the official BSON Java driver, and `:bson-multiplatform` is a brand new implementation purely written in Kotlin Multiplatform. End-users are expected to depend on one of these two modules, instead of depending directly on `:bson`.

Depending on this module provides an easy way to declare utilities that work across BSON implementations. For example, this module contains a [`BsonPath`][opensavvy.ktmongo.bson.BsonPath] class that works for any BSON implementation.

The module `:bson-tests` provides unit tests to verify any new BSON implementation.

# Package opensavvy.ktmongo.bson

Utilities and primitives to read and write BSON documents.

[`Bson`][opensavvy.ktmongo.bson.Bson] and [`BsonArray`][opensavvy.ktmongo.bson.BsonArray] respectively represent BSON documents and arrays.

[`BsonFactory`][opensavvy.ktmongo.bson.BsonFactory] is the entry point to create new BSON documents.

[`BsonPath`][opensavvy.ktmongo.bson.BsonPath] provides a convenient way to search for data in a complex BSON document.

# Package opensavvy.ktmongo.bson.types

Data types specified in the BSON specification or in the MongoDB specification.
