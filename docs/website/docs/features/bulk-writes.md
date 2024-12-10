# Bulk writes

Changing one document at a time can be quite expensive because of the high network activity and high latencies. Instead, when we know we want to edit multiple documents, we prefer to do so in a single request.

For example, this is **bad code** that should be avoided:

```kotlin title="Bad example!"
val usersToCreate = listOf(
	User("Bob"),
	User("Marcel"),
	/* … */
)

for (user in usersToCreate) {
	users.insertOne(user)
}
```

This code is bad because each insert will send data to the database and wait for its response. Between each insert, it waits for the previous one to finish and for an entire network roundtrip.

Instead, we can insert all users at once with `insertMany`:

```kotlin
val usersToCreate = listOf(
	User("Bob"),
	User("Marcel"),
	/* … */
)

users.insertMany(usersToCreate)
```

Here, a single request is sent to the database, which can perform all inserts much quicker.

Similarly, other write operations have a variant that allows performing the same write on multiple documents: `updateMany` and `deleteMany`.

Sometimes, however, we want to perform very different writes, but we could still benefit from sending them all in a single request. In those situations, we can use `bulkWrite`:

```kotlin
users.bulkWrite {
	insertMany(usersToCreate)

	updateOne(
		filter = { User::name eq "Bob" }
	) {
		User::age set 65
	}

	updateMany {
		User::age inc 1
	}

	deleteOne {
		User::name eq "Janine"
	}
}
```

Note that this _isn't_ a transaction. The operations are performed in the same way they would be if calling their respective methods, the only difference is they are all sent together to the database in a single request to decrease network traffic and latency.
