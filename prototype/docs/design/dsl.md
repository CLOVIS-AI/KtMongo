# Design of the Domain Specific Language (DSL)

This document describes the problems we found with KMongo's syntax, the alternatives we considered, and finally the solution we implemented.

## The problems

### KMongo allows using operators in the wrong context

KMongo operators return `Bson` instances. Other operators (e.g. `$and`) combine multiple `Bson` instances into a more complex document.

Not all MongoDB operations are created equal, and not all operators are allowed for all operations. For example, using `$eq` in an `update`'s body makes no sense, as does using `$set` in a `findOne`. KMongo cannot represent these rules.

For these simples examples, mistakes are easily caught, but this problem is exacerbated by aggregation pipelines, which accept many operators having the same name as regular queries, but with slightly different syntax. Because KMongo always returns an already-formed `Bson` instance, it cannot adapt the syntax to the current context.

### KMongo allows using operators that do not refer to the same document

KMongo also doesn't represent the document root:
```kotlin
class User(
	val id: String,
	val profile: Profile,
)

class Profile(
	val name: String,
)

val collection = database.getCollection<User>("users")
collection.updateOne(
	User::id eq "foo",
	setValue(Profile::name, "foo"),
)
```

Here, updating `Profile::name` is an error, it should be `User::profile / Profile::name`. Because the type-safety when referring to a field doesn't exist on MongoDB's side, this request executes to something like:

```js
collection.updateOne(
	filter = {
		id: { $eq: "foo" }
	},
	update = {
		name: "foo"
	}
)
```

When ran, this request creates a field `name` in `User`.

### Building complex queries is cumbersome

Let's imagine a typical search screen where we can search using multiple criteria. We must either create a list of operators in advance:
```kotlin
val bson = ArrayList<Bson>()

if (criteria.name != null)
	bson.add(User::name eq name)

if (criteria.age != null)
	bson.add(User::age eq age)

…

collection.find(and(bson))
```
This separates the criteria from the request body, which makes it harder to understand what the list is used for (especially for operators which require multiple such lists, like `updateOne` or aggregation pipelines).

Instead, we can use the standard library's `buildList` helper:
```kotlin
collection.find(
	and(
		buildList { 
			if (criteria.name != null)
				add(User::name eq name)
			
			if (criteria.age != null)
				add(User::age eq age)
		}
	)
)
```

The logic is better co-located, however this requires three boilerplate levels of indentation.

## Alternatives

The two main alternatives are:
- Combining immutable representations of the operators,
- Binding operators into a DSL.

Let's explore how these two solutions may look like.

### Combining immutable operators

We create a class that represents each operator, with an interface hierarchy that represents the context:

```kotlin
interface FilterOperator<Root>
interface UpdateOperator<Root>

class Eq<Root>(…) : FilterOperator<Root>
class Set<Root>(…) : UpdateOperator<Root>
// …and the infix functions to declare them…

// usage:
collection.findOne(
	and(
		User::name.exists(),
		User::age gt 18,
	),
	User::isAdult set true,
)
```

**Ease of migration:**
This is quite similar to KMongo's syntax, making migration slightly simpler.

**Development experience:**
When using "Code Completion: Type-Matching" in IntelliJ (CTRL SHIFT SPACE), only operators that fit the current context are suggested. However, when using "Code Completion: Basic" (CTRL SPACE), all operators are suggested.

**Debugging experience:**
Very easy to debug: each operator returns an intermediate value which can be easily evaluated.

**Using operators in the wrong context:**
Will result in a compile-time error.

**Using operators that refer to the wrong document:**
Will result in a compile-time error.

**Reusing parts of a request:**
Simply store the return value:
```kotlin
fun genericDateCriteria(start: Instant, end: Instant): FilterOperation<User> =
	and(
		User::lastLoggedInAt gt start,
		User::lastLoggedInAt lt end,
	)
``` 

**Building complex queries:**
Safer, but still quite verbose:
```kotlin
collection.find(
	and(
		buildList { 
			if (criteria.name != null)
				add(User::name eq name)
			
			if (criteria.age != null)
				add(User::age eq age)
		}
	)
)
```

### Binding operators into a DSL

We create DSLs that bind operators into themselves:

```kotlin
class FilterExpression<Root> {
	fun and(children: FilterExpression<Root>.() -> Unit) { … }
	fun or(children: FilterExpression<Root>.() -> Unit) { … }
	
	fun <T> Path<Root, T>.eq(value: T) { … }
}

class UpdateExpression {
	fun <T> Path<Root, T>.set(value: T) { … }
}

// usage:
collection.findOne {
	and {
		User::name.exists()
		User::age gt 18
	}
}
```

**Ease of migration:**
n-ary operators (`$and`…) now accept a lambda instead of accepting a vararg.
Other operators are unchanged.

**Development experience:**
IntelliJ only auto-completes the valid operators.

**Debugging experience:**
Operators do not return a value anymore, so it is harder to evaluate the request as it currently is.
Another utility to help debugging must be provided (e.g. by evaluating the DSL object itself?).

**Using operators in the wrong context:**
Will result in a compile-time error.

**Using operators that refer to the wrong document:**
Will result in a compile-time error.

**Reusing parts of a request:**
Simply done through extension functions:
```kotlin
fun FilterExpression<User>.genericDateCriteria(start: Instant, end: Instant) {
	and {
		User::lastLoggedInAt gt start
		User::lastLoggedInAt lt end
	}
}
```

**Building complex queries:**
Conditionals, loops, etc can be added directly into the DSL:

```kotlin
collection.find {
	and {
		if (criteria.name != null)
			User::name eq name
		
		if (criteria.age != null)
			User::age eq age
	}
}
```

Additionally, since operators are not selected by their return value, but by the DSL itself, we can create conditional operators which may or may not emit actual operator requests. For example, an `eqNotNull` operator could emit an `eq` operator only if the provided parameter is not `null`, massively simplifying these "optional filters" use-cases:
```kotlin
collection.find {
	and {
		User::name eqNotNull name
		User::age eqNotNull age
	}
}
```

## Chosen solution

Because of how much simpler it makes building complex requests, the DSL solution was chosen.
