# Configure your existing KMongo project to use the new KtMongo DSL

!!! info ""
    **This page is about configuring KtMongo in an existing project using the deprecated library [KMongo](https://litote.org/kmongo/).**

    - To learn why you should migrate, [read the dedicated article](index.md).
    - If you are using the official MongoDB driver for Java or for Kotlin, [see our dedicated guide](../official/index.md).
    - If you're unsure in which situation you are, [see our dedicated guide](../index.md).

## Dependencies

KtMongo and KMongo are compatible, meaning that both can be used in the same project. Additionally, we provide dedicated modules with simple conversion functions.

=== "Without coroutines"

	Add the dependency ([list of versions](../../news)):
	```kotlin
	implementation("dev.opensavvy.ktmongo:driver-sync-kmongo:VERSION")
	```

    This will add the [`MongoCollection.asKtMongo()`](../../api/driver-sync-kmongo/opensavvy.ktmongo.sync.kmongo/as-kt-mongo.md) extension function which converts from a KMongo `MongoCollection` to a KtMongo [`JvmMongoCollection`](../../api/driver-sync/opensavvy.ktmongo.sync/-jvm-mongo-collection/index.md).

=== "With coroutines"

	Add the dependency ([list of versions](../../news)):
	```kotlin
	implementation("dev.opensavvy.ktmongo:driver-coroutines-kmongo:VERSION")
	```

    This will add the [`MongoCollection.asKtMongo()`](../../api/driver-coroutines-kmongo/opensavvy.ktmongo.coroutines.kmongo/as-kt-mongo.md) extension function which converts from a KMongo `MongoCollection` to a KtMongo [`JvmMongoCollection`](../../api/driver-coroutines/opensavvy.ktmongo.coroutines/-jvm-mongo-collection/index.md).

## Serialization

Under the hood, both KMongo and KtMongo rely on the official driver's `Codec` system. This means you can use KtMongo and your objects will be serialized exactly in the same way.

However, the `Codec` system doesn't provide a way for a library to observe how the data is structured. This means that special annotations from serialization libraries that change the structure of the objects require explicit configuration. By default, the KtMongo compatibility module for KMongo adds support for the annotations:

- `@kotlinx.serialization.SerialName` from KotlinX.Serialization.
- `@org.bson.codecs.pojo.annotations.BsonId` from the official MongoDB Java driver.

If you use other annotations (for example with Jackson), you will need to customize KtMongo to be aware of them. To learn more, see [`PropertyNameStrategy`](../../api/dsl/opensavvy.ktmongo.dsl.path/-property-name-strategy/index.md) which is an optional argument to `MongoCollection.asKtMongo()`.

The annotations listed above are only supported by the KMongo compatibility module.
You can learn more about other serialization aspects in our guides for the official drivers, since they share the same system:

- [Using KotlinX.Serialization](../official/serialization-kotlinx.md)
- [Using reflection](../official/serialization-reflection.md)
- [Using any other library](../official/serialization-custom.md)

## Mixed usage

The KtMongo DSL is only available on KtMongo types. For example, if you have a KMongo collection, you should use the `asKtMongo()` function to obtain the KtMongo equivalent.

```kotlin
// Before
users.find(
	User::name eq "Bob"
)

// After
users.asKtMongo().find {
	User::name eq "Bob"
}
```

!!! warning "Avoid mixing both libraries in a single request"
    KtMongo and KMongo can be used together in the same project, but not in the same request. Although both libraries use the syntax `User::profile / Profile::name` and have many similar operators, they are distinct implementations that do not understand each other.

	KtMongo operators are always used within a DSL block, in which they have resolution priority.

If you want to use KtMongo only in place where its DSL is more advanced than KMongo's, you can call `.asKtMongo()` on each operation where you want to switch library.

If you want to migrate, you can call `.asKtMongo()` early in your program (when instantiating collections) and inject KtMongo's types everywhere. Note that there is no reverse operation (get a KMongo type from a KtMongo type) due to a limitation of the official Kotlin driver.

We recommend migrating your repositories one by one over multiple versions. This ensures that you can catch behavior differences early in case there are any and avoids the pressure of delivering a large refactor.
