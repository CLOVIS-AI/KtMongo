# Fields and nested documents

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

## In arrays

Documents can also be stored in arrays. In Kotlin, BSON arrays can be represented using any collection type, like `List` or `Set`.

### Using an index

The easiest way to access an array item is using its index. For example, if we know that we want to update Bob's second-best friend, we can use:
```kotlin hl_lines="14"
class User(
	val name: String,
	val friends: List<Friend>,
)

class Friend(
	val name: String,
	val preferredFood: String,
)

users.updateOne(
	filter = { User::name eq "Bob" }
) {
	User::friends[1] / Friend::preferredFood set "Lasagna"
}
```

### Based on its properties

We may be interested in searching for a document based on the properties of an item in an array. For example, if we want to find all students who have had a perfect grade, we can use the `any` operator:
```kotlin hl_lines="7"
class Student(
	val name: String,
	val grades: List<Int>,
)

students.find {
	Student::grades.any eq 20
}
```
This reads as: "find all students from which any grade is 20". If we want to find all students that also have the worst possible grade, we can write:
```kotlin
students.find {
	Student::grades.any eq 20
	Student::grades.any eq 0
}
```
This finds all students that have a grade of 20, and another grade of 0.

If, instead, we want to provide multiple filters on _the same grade_, we can use the `anyValue` operator:
```kotlin
students.find {
	Student::grades.anyValue {
		gt(18)
		lte(19)
	}
}
```

Now that we are able to select a specific grade, we can update it using the `selected` operator. For example, if we wanted to increase that grade:
```kotlin
students.updateOne(
	filter = {
		Student::grades.anyValue {
			gt(18)
			lte(19)
		}
	}
) {
	Student::grades.selected inc 1
}
```

### Based on the properties of a nested document

This section is the same as the previous one, but instead of filtering on an item itself, we filter based on the field of the item.

If we want to find all users who have a pet named Lucy:
```kotlin
class User(
	val name: String,
	val pets: List<Pet>,
)

class Pet(
	val name: String,
	val age: Int,
)

users.find {
	User::pets.any / Pet::name eq "Lucy"
}
```

If we want to find all users who have a pet named Lucy, and also a pet that is 4 years old, but these could be two different pets:
```kotlin
users.find {
	User::pets.any / Pet::name eq "Lucy"
	User::pets.any / Pet::age eq 4
}
```

If we want to find all users who have a pet named Lucy that is 4 years old (must be the same pet):
```kotlin
users.find {
	User::pets.any {
		Pet::name eq "Lucy"
		Pet::age eq 4
	}
}
```

Now that we are able to select a specific pet, we can use the `selected` operator to update it:
```kotlin
users.updateOne(
	filter = {
		User::pets.any {
			Pet::name eq "Lucy"
			Pet::age eq 4
		}
	}
) {
	User::pets.selected / Pet::age inc 1
}
```
Only the pet we referred to will be updated.

### Update all elements

If we want to update all elements in an array, we can use the `all` operator:
```kotlin
class User(
	val name: String,
	val pets: List<Pet>,
)

class Pet(
	val name: String,
	val age: Int,
)

users.updateMany {
	User::pets.all / Pet::age set 1
}
```

## In maps

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
