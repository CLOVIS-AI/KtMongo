# Module Kotlin BSON • Compatibility with the official MongoDB implementation

Kotlin-first BSON library, based on the official MongoDB implementations.

<a href="https://search.maven.org/search?q=dev.opensavvy.ktmongo.bson-official"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/bson-tests.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/bson-official"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

BSON API for Kotlin, implemented on top of the [official `org.mongodb:bson` library](https://javadoc.io/doc/org.mongodb/bson/latest/index.html).

The class [`BsonFactory`][opensavvy.ktmongo.bson.official.BsonFactory] is the entry point to all types in the module. It allows creating a new BSON factory using the same configuration as the official library (via the `CodecRegistry`).

# Package opensavvy.ktmongo.bson.official

The BSON factory, documents and arrays.

# Package opensavvy.ktmongo.bson.official.types

Utilities for working with KtMongo's BSON types and utilities to convert them to the official implementations.
