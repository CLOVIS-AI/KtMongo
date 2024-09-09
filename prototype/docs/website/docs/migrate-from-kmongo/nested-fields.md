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

Referring to a non-nested field is identical with KMongo and KtMongo:

```kotlin
User::name eq "foo"
```

Referring to nested documents is identical with both libraries:

```kotlin
User::country / Country::code eq "FR"
```

Referring to a list item by index uses the `get` operator:

```kotlin title="Using KMongo"
User::pets.pos(4) / Pet::name eq "Chocolat"
```

```kotlin title="Using KtMongo"
User::pets[4] / Pet::name eq "Chocolat"
```
