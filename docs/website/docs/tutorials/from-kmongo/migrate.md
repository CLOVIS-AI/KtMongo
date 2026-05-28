# Convert your existing KMongo queries to KtMongo's DSL

!!! info ""
    **This page is about converting queries written with the deprecated library [KMongo](https://litote.org/kmongo/) to use KtMongo's DSL instead.**

    - To learn why you should migrate, [read the dedicated article](index.md).
    - To configure KtMongo in your existing KMongo project, [see our dedicated guide](setup.md).
    - If you are using the official MongoDB driver for Java or for Kotlin, [see our dedicated guide](../official/index.md).
    - If you're unsure in which situation you are, [see our dedicated guide](../index.md).

## Access the KtMongo DSL

The KMongo library offers extension functions directly on top of the official Java driver. This leads to overload pollution: you may confuse methods from the official Java driver with those from KMongo, and vice versa.

Instead, KtMongo has its own collection type. KtMongo methods are only available on that type. Therefore, it's never ambiguous which library you're trying to call.

To convert from a KMongo collection to a KtMongo collection, you can use the `MongoCollection.asKtMongo()` extension function.
To learn more, [see the dedicated guide](setup.md).

## Main differences

### Vararg vs DSL

The main difference between KMongo operators and the KtMongo DSL is that KMongo accepts parameters as a `vararg`, whereas KtMongo uses a DSL: 

```kotlin title="Using KMongo"
collection.findOne(
	and(
		User::name.exists(),
		User::age gt 18,
	)
)
```

```kotlin title="Using KtMongo"
collection.findOne {
	and {
		User::name.exists()
		User::age gt 18
	}
}
```

Notice how:

- the parentheses become braces,
- the trailing commas are gone.

The KMongo operators are top-level functions that return an opaque type-unsafe `Bson` type, whereas KtMongo operators directly attach themselves into the current operation and have no return value.

As a consequence, KtMongo is more type-safe:

- Operators will not compile when called in the wrong context (e.g. `$eq` in an update).
- Operators will correctly disambiguate between different contexts, even if they have the exact same Kotlin syntax (e.g. `$eq` in find and `$eq` in an aggregation).

### Dynamic queries

This allows us to more easily create complex queries. Compare the following KMongo query:
```kotlin title="Using KMongo"
collection.findOne(
	and(
		listOfNotNull(
			(User::name eq criteria.name).takeUnless { criteria.name == null },
			(User::age eq criteria.age).takeUnless { criteria.age == null },
		)
	)
)
```

When these kinds of requests grow, they become harder to understand and easier to get wrong. Also, with these way of writing them, the criteria is instantiated even if it isn't used later.

With KtMongo, everything is co-located and the intermediate list is eliminated:

```kotlin title="Using KtMongo"
collection.findOne {
	if (criteria.name != null)
		User::name eq criteria.name

	if (criteria.age != null)
		User::age eq criteria.age
}
```

Note that you also don't need the root `and()` operator with KtMongo, because KtMongo automatically detects that a `findOne` with multiple criteria must be using an `$and` operator.

For the specific case of optional criteria, KtMongo provides [a dedicated syntax that is even more concise](../../features/optional-filters.md).

### Extracting into functions

To make parts of complex queries reusable, it is common to extract them as independent functions.
In KMongo, this is done by creating a function that returns a `Bson` instance:
```kotlin title="Using KMongo"
fun filter(criteria: UserFilterCriteria): Bson = and(
	User::name eq criteria.name,
	User::age gte criteria.minAge,
)
```

KtMongo needs to know the type of the document for type-safety reasons, and operators automatically bind themselves to the current DSL. Each DSL provides its own scope:
```kotlin title="Using KtMongo"
fun FilterQuery<User>.filter(criteria: UserFilterCriteria) = and {
	User::name eq criteria.name
	User::age gte criteria.minAge
}
```

### Options

Options are passed as their own DSL and are usually written first. For example, the following query:
```kotlin title="Using KMongo"
users.countDocuments(
	filter = User::name eq "Patrick",
	options = CountOptions().limit(10)
)
```
is written:
```kotlin title="Using KtMongo"
users.count(
	options = { limit(10) },
	filter = {
		User::name eq "Patrick"
	}
)
```

## Nested fields

### Nested documents

Both KMongo and KtMongo use the syntax `User::profile / Profile::name`.

!!! danger
    Although both libraries use the same syntax, they are implemented differently and do not recognize each other. Do not use the KMongo `/` operator in a KtMongo query, as it will result in an incorrect query. Using the KtMongo operator in a KMongo query will not compile.

    The KMongo `/` operator needs to be explicitly imported. The KtMongo `/` operator is only available within a KtMongo command's DSL and doesn't need an import.

    You can follow the progress of lifting this restriction [here](https://gitlab.com/opensavvy/ktmongo/-/work_items/96).

### Unsafe nested documents

To unsafely access a nested field (without type-safety), KMongo adds the `%` operator: `User::profile % Car::name`. 

Instead, KtMongo provides the [`unsafe` extension function](../../api/dsl/opensavvy.ktmongo.dsl.path/-field-dsl/index.md#unsafe): `User::profile unsafe Car::name`. 

You can also [unsafely cast](../../api/dsl/opensavvy.ktmongo.dsl.path/-field/index.md#unsafecast) a field to another type and then use any type-safe operator: `User::profile.unsafeCast<Car>() / Car::name`.

### Nested arrays

| MongoDB syntax      | KMongo                                     | KtMongo                                                                                                                              | Meaning                                               |
|---------------------|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| `"friends.2"`       | `User::friends.pos(2)`                     | [`User::friends[2]`](../../api/dsl/opensavvy.ktmongo.dsl.path/-field-dsl/index.md#get)                                               | The friend at index 2.                                |
| `"friends.$"`       | `User::friends.posOp`                      | [`User::friends.selected`](../../api/dsl/opensavvy.ktmongo.dsl.query/-upsert-query/index.md#selected)<br/>Available only in updates. | The first friend selected<br/>by the update's filter. |
| `"friends.$[]"`     | `User::friends.allPosOp`                   | [`User::friends.all`](../../api/dsl/opensavvy.ktmongo.dsl.query/-update-query/index.md#all)<br/>Available only in updates.           | All friends.                                          |
| `"friends.$[<id>]"` | `User::friends`<br/>`.filteredPosOp("id")` | [`User::friends.filter {}`](../../api/dsl/opensavvy.ktmongo.dsl.query/-update-query/index.md#filter)<br/>Available only in updates.  | Creates an array filter.                              |

## Find

Use `find()` (returns a cursor) and `findOne()` (returns a single element).

As explained above, the `vararg` becomes a DSL.
```kotlin title="Using KMongo"
collection.findOne(
	and(
		User::name.exists(),
		User::age gt 18,
	)
)
```

Filter operations with multiple criteria imply a root `$and`:
```kotlin title="Using KtMongo"
collection.findOne {
	User::name.exists()
	User::age gt 18
}
```

Complex query building can be replaced by simple `if` and `for` directly within the DSL.

## Update (simple)

KtMongo provides:

- `updateMany`: updates all documents that match a filter.
- `updateOne`: updates a single document that matches a filter.
- `upsertOne`: upserts a single document that matches a filter.

They each follow the same pattern as the `find()` methods.

```kotlin title="With KMongo"
collection.updateMany(
	filter = and(
		User::name.exists(),
		User::age gt 18,
	),
	set(
		User::isLegal setTo true,
	)
)
```

```kotlin title="With KtMongo"
collection.updateMany(
	filter = {
		User::name.exists()
		User::age gt 18
	},
	update = {
		User::isLegal set true
	}
)
```

Unlike in KMongo, there is no need to combine multiple operators yourself.
```kotlin title="With KMongo"
collection.updateMany(
	filter = …,
	set(
		User::name setTo "foo",
		User::isLegal setTo true,
	),
	inc(
		User::age setTo 1
	)
)
```

```kotlin title="With KtMongo"
collection.updateMany(
	filter = { … },
	update = {
		User::name set "foo"
		User::isLegal set true
		User::age inc 1
	}
)
```

## Update (aggregation pipeline)

MongoDB provides two syntaxes for updates: one with regular query operators, and one with aggregation operators.

In KMongo, they are differentiated by the overload of `updateOne` (or similar): the overload that takes a `vararg Bson` uses the query syntax, but the overload that takes a `List<Bson>` uses the aggregation syntax. This is confusing, especially because KMongo doesn't provide the aggregation operators out of the box.

KtMongo uses a suffix to use the aggregation pipeline syntax:

- `updateManyWithPipeline`: updates all documents that match a filter.
- `updateOneWithPipeline`: updates a single document that matches a filter.
- `upsertOneWithPipeline`: upserts a single document that matches a filter.
