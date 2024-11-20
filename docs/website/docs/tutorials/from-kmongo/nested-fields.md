# Referring to nested documents

For the rest of this article, let's take the following example, in which the collection we're interested in is `User`:

```kotlin
class User(
	val name: String,
	val country: Country,
	val pets: List<Pet>,
)

class Country(
	val id: String,
	val code: String,
)

class Pet(
	val id: String,
	val name: String,
)
```

!!! tip
    Note that the KtMongo syntax is only available within the various DSL methods offered by the library.

## Top-level field

Referring to a non-nested field is identical with KMongo and KtMongo:

```kotlin
User::name eq "foo"
```

## Nested field

Referring to nested documents is identical with both libraries:

```kotlin
User::country / Country::code eq "FR"
```

## Array items

Referring to an array item by index uses the `get` operator:

```kotlin title="Using KMongo"
User::pets.pos(4) / Pet::name eq "Chocolat"
```

```kotlin title="Using KtMongo"
User::pets[4] / Pet::name eq "Chocolat"
```
