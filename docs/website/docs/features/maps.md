# Maps

Kotlin has the `Map` type, which doesn't exist in BSON. Most serialization libraries treat `Map<String, V>` specially, and serialize it as an object (the keys become fields and the value their value).

KtMongo assumes your serialization library works like this and provides a few helpers. If these types are serialized differently, these operators may not work.

### Using an index

The easiest way to access a map element is using its index. For example, if we know that we want to update Bob's physics score, we can use:
```kotlin hl_lines="14"
class User(
	val name: String,
	val scores: Map<String, Int>,
)

users.updateOne(
	filter = { User::name eq "Bob" }
) {
	User::scores["physics"] set 20
}
```
