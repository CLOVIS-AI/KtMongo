# Features overview

This page describes the improvements brought by KtMongo over the official drivers.
If you're interested in a comparison with KMongo, see [the dedicated page](../migrate-from-kmongo/why.md).

## Query DSL and optional filters

A typical scenario is to filter data based on criteria passed by the user. In this example, we'll be searching for users of a given last name, with an optional minimum age filter.

First, let's declare our classes to represent the data we're working with. The drivers will automatically deserialize documents to these classes.

```kotlin
class User(
	val _id: String,
	val name: UserName,
	val age: Int,
)

class UserName(
	val firstName: String,
	val lastName: String,
)
```

Let's implement this request with the official drivers.

```java title="With the official Java driver"
Bson filter = eq("name.lastName", lastName);

if(minAge !=null)
filter =

and(filter, gt("age", minAge));

	users.

find(filter);
```

The official drivers use the builder pattern. In this example, reference mutability is used to incrementally create the filter.

This example is hard to maintain:

- Field names, and their hierarchy, are passed as strings: when refactoring code, requests risk being outdated.
- Filters are of the type `Bson`: the type doesn't help us verify that the request actually applies to the contents of the collection. If we accidentally use a function made for another collection, its filters may apply in different ways or not apply at all if the field names are different.
- There are no type checks: we could try to compare the `name` field with a `String`, which would give no results since it is a child document.
- The structure of the request is not immediately visible at a glance, due to being spread over the condition.

Now, let's rewrite this example with KtMongo:

```kotlin title="With KtMongo"
users.find {
	User::name / UserName::lastName eq lastName
	User::age gtNotNull minAge
}
```

A few improvements have been made to simplify the request:

- Referring to fields is done by writing a reference to the actual fields, meaning we can find all requests using a field by clicking on it in our IDE.
- Refactoring fields will correctly update all requests using them.
- Operator arguments are type-checked, so we cannot compare a field with an incompatible type.
- The structure of the request is immediately visible, and the `$and` operator is implied by the presence of multiple arguments.
- The `gtNotNull` operator handles the optional filter for us.
- Only filters that apply to the collection being searched in can be specified in the `find` block, we cannot accidentally use filters meant for another collection.

These various improvements make the final request much easier to read and understand at a glance.
