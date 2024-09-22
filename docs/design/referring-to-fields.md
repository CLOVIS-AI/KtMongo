# Referring to fields

|               |                                                                                                                                                                                |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Status        | [![Status: active](https://badgen.net/static/Status/active/purple)](https://gitlab.com/opensavvy/playgrounds/baseline/-/blob/main/docs/design/README.md#creating-a-new-record) |
| Discussion    |                                                                                                                                                                                |
| Superseded by |                                                                                                                                                                                |
| Relates to    |                                                                                                                                                                                |

Implementation of the Fields DSL: a concise way to refer to fields in nested documents.

```kotlin
class User(
	val name: String,
	val age: Int,
	val friends: List<Friend>,
)

class Friend(
	val name: String,
	val age: Int,
)

User::name                       // 'name'
User::age                        // 'age'
User::friends.any / Friend::name // 'friends.name'
User::friends[0] / Friend::age   // 'friends.$0.age'
```

[TOC]

## The problem

The `/` syntax is the most visible feature of KMongo. It is critical that code can be migrated easily from KMongo, and thus we must closely mimic KMongo's syntax.

The main difficulty is to allow both of these syntaxes to work:
```kotlin
User::name eq "foo"
User::profile / Profile::name eq "foo"
```

In the first line, `User::name` necessarily has the type `KProperty1` (it's the Kotlin language specification). The question is what type should `User::profile / Profile::name` have:
- If it's `KProperty1`, then we must implement a custom `KProperty1` instance that can remember previous path instructions whilst remaining type-safe.
- If it's something different, then all operators need to be duplicated, because top-level properties will always be `KProperty1`.

## Constraints

Migration from KMongo to KtMongo should be easy. If possible, the syntax should be identical.

## Considered solutions

### A. Custom KProperty1 impostor

In KMongo, this is implemented with a custom `/` operator on `kotlin.reflect.KProperty1`, which is the type of property references.
This operator returns a custom implementation of `KProperty1` that masquerades as being a real value of `kotlin-reflect` without implementing anything, instead storing information about the `/` calls so it knows the entire path.

KMongo's `KProperty1` impostor source code is [available here](https://github.com/Litote/kmongo/blob/0aecb9dd13faf629993818b2d7e68d52ebdc09a6/kmongo-property/src/main/kotlin/org/litote/kmongo/property/KPropertyPath.kt#L32), the custom `/` operator is [available here](https://github.com/Litote/kmongo/blob/0aecb9dd13faf629993818b2d7e68d52ebdc09a6/kmongo-property/src/main/kotlin/org/litote/kmongo/Properties.kt#L42).

This approach has multiple drawbacks:

- The approach exploits that the two type parameters of `KProperty1` are not checked for meaning, and overrides the meaning of the first type parameter from "the class in which this property is declared" to "the class in which this field chain starts". This interacts poorly with other libraries that expect `KProperty1`'s documentation to be respected.
- Almost all functions of the masquerade `KProperty1` throw `NotImplementedError`, since it is not possible to implement it as the type invariants are not respected.
- Constructing arbitrary properties is complex, because we must instantiate `KProperty1` instances to be able to combine them.
- The type `KProperty1` cannot be subclassed on Kotlin/JS.

It also has a multiple advantages:

- The rest of the codebase can act transparently in regard to whether a field is top-level or not: all fields are subtypes of `KProperty1`. This reduces the overall amount of code.
- Since everything is auto-magical, the user doesn't have to know about how it works.

### B. Hybrid: custom KProperty1 impostor and Path type

This solution creates a low-level `Path` type that represents the actual path syntax from MongoDB without type-safety, but also uses a `KProperty1` impostor to implement the high-level accessors used in day-to-day life.

This increases complexity because all operators must be implemented twice: once for `Path`, once for `KProperty1`. In practice, the `KProperty1` variant is a thin wrapper for calling the `Path` variant.

Advantages:

- Increased flexibility when constructing complex paths with `Path` directly.

Downsides:

- All the drawbacks of the solution A., except the lack of flexibility.

This solution was the one followed by the 4SH prototype of KtMongo.

### C. Duplicate all operators

With this solution, we accept that top-level fields and nested fields have different types. We must therefore duplicate all operators.

Since solution B. already requires duplicating all operators, this solution obtains the same amount of code without requiring a `KProperty1` impostor.

In practice, the top-level overload always consists of converting `KProperty1` into a field type and calling the field overload, so the cost of creating operators is low.

### D. Explicitly convert KProperty1

With this solution, we have a custom field type that is used by operators. Top-level properties cannot be used as-is, and must be explicitly converted to the field type.

Multiple syntaxes may be considered for the conversion. For example, with a custom conversion attribute:
```kotlin
collection.find {
    User::friends.field / Friends::pets / Pets::name eq "foo"
}                 ^^^^^
```

Or, by accessing a child of an imaginary 'root' field:
```kotlin
collection.find {
    root / User::friends / Friends::pets / Pets::name eq "foo"
}   ^^^^
```

Or, using a custom operator:
```kotlin
collection.find {
    +User::friends / Friends::pets / Pets::name eq "foo"
}   ^
```

This solution is conceptually much simpler than all others, because it doesn't require complex conversion functions. However, it requires a slight modification to the way requests are declared. 

## Selected solution

The selected solution for KtMongo is C.

The `Path` type will represent the low-level MongoDB path. The `Field` type will be an interface that can be converted to a `Path` (similar to `Iterable`/`Iterator`). `Field` instances will be type-safe.

`Field` instances will be generated either from a root `KProperty1` or by nesting other `Field` instances.

The DSL will be split in two:

- Functions working with `Field` instances will be declared directly on the `Field` interface.
- Functions working with `KProperty1` instances will be declared in a `FieldDsl` interface that will be brought into scope by all other KtMongo DSLs.

This will decrease the risk of import conflicts if KtMongo is used together with KMongo, but it will not eliminate it. In particular, using a KMongo-built `KProperty1` instance in a KtMongo query will _always_ be a bug.
