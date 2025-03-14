# Module Kotlin BSON • Multiplatform abstraction for different BSON implementations

Kotlin Multiplatform-ready implementation of BSON.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22bson%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/bson.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/bson"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

This module is centered around the [`BsonValueWriter`][opensavvy.ktmongo.bson.BsonValueWriter] and [`BsonFieldWriter`][opensavvy.ktmongo.bson.BsonFieldWriter] classes. Together, they provide a low-level API to write arbitrary BSON.

This module is the basis for the rest of the driver: if this module can be implemented on a platform, the rest of the driver mostly likely can as well.

# Package opensavvy.ktmongo.bson

Utilities to read and write BSON.

# Package opensavvy.ktmongo.bson.types

Data types contained in the BSON specification.
