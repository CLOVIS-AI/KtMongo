# Progressive type-safety with the KtMongo driver for MongoDB

The KtMongo DSL is crafted to allow the Kotlin compiler to verify as much as possible of the validity of the queries.
Since MongoDB itself isn't typed, this trades off liberty to improve safety.

Most of the time, this is the best choice: developers can quickly write queries without having to worry about syntax details.

Still, any subsequently complex project will at some point encounter a situation that is safe in MongoDB but cannot be verified
by the Kotlin compiler. KtMongo is built to allow progressive type safety: KtMongo is type-safe by default but provides operators
that bypass some safety in some situations.

This page describes some of these operators.

## Type-safe queries

By default, KtMongo is strongly type-safe. This means that the Kotlin compiler is able to check almost the entirety of a query.

```kotlin
class User(
	val _id: ObjectId,
	val profile: Profile,
)

class Profile(
	val name: String,
	val age: Int,
)

val users = database.collection<User>("users")

users.find {
	User::profile / Profile::age gte 18
}
```

In this query, the compiler verifies that:

- The property `Profile.age` must be of type `Int`, since we're using the operator `gte` with `18`, which is an `Int`.
- The property `User.profile` must be of type `Profile`, since we're referring to its child field `Profile.age`.
- The collection must be declared of type `User`, since the query refers to its field `User.profile`.

As a comparison, the [KMongo library](../tutorials/from-kmongo/index.md), one of the main inspirations of KtMongo, is only able to verify the first two.
The Java and Kotlin official drivers make none of these verifications.

Additionally, this type-safety allows the compiler to decide on the correct operator depending on the situation.
For example, KtMongo will automatically use dot-notation or `$getField` depending on the situation, will automatically use the correct syntax of `$eq`, etc.

In a codebase, almost all queries should remain within the type-safe API, as this provides the best IDE and compiler support.

## Progressive type safety

Still, sometimes, type-safety can slow us down if we know how MongoDB will behave in these situations.

### Nullability

If a value is declared nullable, but we know `null` cannot happen in practice:

```kotlin
users.aggregate()
	.project {
		User::isAdult set (User::age.unsafeNonNull() gte 18)
	}
```

> Available in: [aggregations](../api/dsl/opensavvy.ktmongo.dsl.aggregation/-value/index.md#unsafenonnull).

### Incorrect subtype

If we use polymorphic serialization and want to access a field of one of the possible options:

```kotlin
class Invoice(
	val _id: ObjectId,
	val data: Invoice,
)

sealed interface InvoiceData

class Accepted(
	val signatureDate: Instant,
	val author: ObjectId,
) : InvoiceData

class Draft(
	val expirationDate: Instant,
) : InvoiceData

invoices.find {
	Invoice::data unsafe Accepted::signatureDate lte (clock.now() - 7.days)
}
```

Here, `Invoice::data / Accepted::signatureDate` is not allowed because `data` is not of type `Accepted`.
The `unsafe` keyword allows bypassing that type verification.

> Available in: [queries](../api/dsl/opensavvy.ktmongo.dsl.path/-field-dsl/index.md#unsafe).

### Incorrect type

Sometimes, especially when dealing with old data that may not have been migrated properly, we may have to deal with
fields that do not have the type declared in the DTO.

```kotlin
class User(
	val _id: ObjectId,
	val name: String,
	val age: Int,
)

users.filter { User::age hasType BsonType.Double }
	.updateManyWithPipeline {
		set {
			User::age set User::age.unsafeCast<Double>()
				.toInt()
		}
	}
```

> Available in: [queries](../api/dsl/opensavvy.ktmongo.dsl.path/-field/index.md#unsafecast), [aggregations](../api/dsl/opensavvy.ktmongo.dsl.aggregation.operators/-value-operators/index.md#unsafecast), on [pipelines](../api/dsl/opensavvy.ktmongo.dsl.aggregation/-aggregation-pipeline/index.md#unsafecast) and on [collections](../api/driver-api/opensavvy.ktmongo.api/-mongo-collection/index.md#unsafecast).

### Temporary field

In aggregations, it is common to create a temporary field that only exists in intermediate stages of the pipeline and is never serialized neither in the input nor the output.

```kotlin
val lookupOutput = Field.unsafe<List<Departments>>("departments")

users.aggregate()
	.lookup {
		into(lookupOutput)
		from(departments.aggregate())
		on(User::departmentId, Department::_id)
	}
	.project {
		include(User::name)
		User::department set lookupOutput[0]
	}
```

Since the field `departments` is never serialized (it is created by the `$lookup` and deleted by the `$project`), it would be inconvenient to add it to one of the DTOs.

> Available: [everywhere](../api/dsl/opensavvy.ktmongo.dsl.path/-field/-companion/index.md#unsafe).

### Arbitrary data

MongoDB documents are not typed, they can contain arbitrary data. When we want to take advantage of this power, we should use the types `BsonDocument`, `BsonArray` or `BsonValue`:

```kotlin
class User(
	val _id: ObjectId,
	val name: String,
	val externalData: BsonDocument,
)

users.find {
	User::externalData.get<Profile?>("profile") ne null
}
```

> Available in: [queries](../api/dsl/opensavvy.ktmongo.dsl.path/-field-dsl/index.md#get), [aggregations](../api/dsl/opensavvy.ktmongo.dsl.aggregation.operators/-value-operators/index.md#get).

### Arbitrary operators

If you __really__ need to go outside the features provided by KtMongo, you can implement custom operators. In this case, there are no limits at all.

To learn how to write custom operators, visit [AbstractBsonNode](../api/dsl/opensavvy.ktmongo.dsl.tree/-abstract-bson-node#index.md).
Every KtMongo DSL block provides the `accept` method which you can use to inject arbitrary BSON in any request.

If you happen to need to write custom operators, this is probably a sign that something is missing in the KtMongo library. Please create a feature request or contribute your custom operator.

> Available: [everywhere](../api/dsl/opensavvy.ktmongo.dsl.tree/-compound-bson-node/index.md#accept).
