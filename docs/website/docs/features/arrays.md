# Arrays

Documents can also be stored in arrays. In Kotlin, BSON arrays can be represented using any collection type, like `List` or `Set`.

## Using an index

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

## Based on its properties

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

## Based on the properties of a nested document

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

## Update all elements

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
