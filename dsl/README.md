# Module MongoDB query DSL

Pure Kotlin DSL targeting all MongoDB operators. 

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.ktmongo%22%20AND%20a:%22dsl%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.ktmongo/dsl.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/experimental/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.ktmongo/dsl"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

Pure Kotlin implementation of MongoDB operators, based on `dev.opensavvy.ktmongo:bson`. All platforms supported by `:bson` are also supported by this module.

It is not expected that end-users will have a direct dependency on this module. Instead, we expect end-users to have a dependency on one of the "driver" modules: `driver-sync` or `driver-coroutines`.

## Introduction

No matter whether you use the synchronous or the coroutines drivers, you must construct BSON instances to communicate with MongoDB. While the `:bson` module allows writing arbitrary BSON, it is rather low-level and requires to take care of escaping as well as remembering the syntax for each operator. This module provides a Kotlin function for each MongoDB operator:
```kotlin
collection.find {
	User::score isOneOf listOf(4, 5)
	User::age gte 18
}
```

To learn more about the `Class::field` syntax, and how to refer to fields in general, see [Field][opensavvy.ktmongo.dsl.path.Field].

## Operator DSLs

Operators are organized by the context in which they are available in. For example, the `$eq` operator cannot be used in an `update`'s modification field, but it can be used in an `update`'s filter field.

Instances of these classes are usually provided by the driver as part of its functions.

- [Filter operators][opensavvy.ktmongo.dsl.expr.FilterExpression]
- [Update operators][opensavvy.ktmongo.dsl.expr.UpdateExpression]

To create a custom operator (for example because it isn't part of the library yet), see [AbstractExpression][opensavvy.ktmongo.dsl.expr.common.AbstractExpression].

# Package opensavvy.ktmongo.dsl

Annotations and other global concepts.

# Package opensavvy.ktmongo.dsl.tree

Helpers to represent trees of data, built bottom-up.

Users of the library are not expected to interact with this package.

However, contributors to the library, and users wanting to implement custom operators, should familiarize themselves with this package.

# Package opensavvy.ktmongo.dsl.expr

Operators, classified by the context in which they are available in.

Classes of this package are not expected to be instantiated by the user. Instead, it is expected by the driver will provide an instance of these classes in its own DSL, such that the user doesn't have to think about which class they should use.

However, the user should still be aware of these classes, as they are the place where operators are documented.

# Package opensavvy.ktmongo.dsl.expr.common

The general concept of what it means to be a BSON expression or a BSON operator.

Expressions can be written into a BSON stream, use their BSON representation when `toString` is called, and are able to simplify themselves to eliminate redundant constructs or handle 0-ary operators. 

# Package opensavvy.ktmongo.dsl.path

Utilities for referencing variables and classes.

```kotlin
User::_id                          // _id
User::profile / Profile::name      // profile.name
User::friends[1] / Friend::name    // friends.$1.name
```

This package contains a [low-level type-unsafe implementation][opensavvy.ktmongo.dsl.path.Path] of arbitrary document paths, and a [high-level type-safe wrapper][opensavvy.ktmongo.dsl.path.Field] that provides the above utility functions.

Note that some functions are only provided when the [FieldDsl][opensavvy.ktmongo.dsl.path.FieldDsl] is into scope (which should be done automatically by most operators). This means that you should rarely need to import anything for these functions to be available.

### Operators

- [The `.` operator][opensavvy.ktmongo.dsl.path.Field.div]
- [The `.$x.` operator][opensavvy.ktmongo.dsl.path.get]
