# Nested documents

As we have seen in the [CRUD operations](crud.md) article, KtMongo operators apply to specific fields in a document. This article describes the different ways in which we can refer to these fields.

Note that field access is type-safe: accessing to a field in an invalid way, or accessing to a field in a context in which it cannot be accessed, will lead to compilation errors.

## In the root document

Each collection is declared on a specific document class. That document class is called the root document.

When we want to refer to fields from the root document, we simply type the root document's class name, followed by `::`, followed by the field's name:

```kotlin hl_lines="7"
class User(
	val name: String,
	val age: Int?
)

users.find {
	User::name eq "John"
}
```

Note that referring to fields from the wrong class will not compile.

## In nested documents

If we have nested documents, we can refer to any field using the `/` operator (similar to file paths):
```kotlin hl_lines="13"
class User(
	val _id: ObjectId,
	val profile: Profile,
	val hashedPassword: String,
)

class Profile(
	val name: String,
	val age: Int?
)

users.find {
	User::profile / Profile::name eq "John"
}
```

Again, this syntax is fully typed.
