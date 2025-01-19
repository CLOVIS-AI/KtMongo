# Filtered collections

Filtered collections are logical views that exist purely in the driver and are never sent to MongoDB.
Unlike [native MongoDB views](https://www.mongodb.com/docs/manual/core/views/) that are proper MongoDB objects and have a number of limitations (for example, they do not support write operations), KtMongo filtered collections are purely syntax sugar and are thus free to create and use, with fewer limitations.

Filtered collections allow us to write multiple requests that only impact a specific subset of a collection, without having to repeat the common filter each time.
The main use-case is to implement logical deletion.

## Logical deletion

We can use [delete operations](crud.md#delete) to remove documents from a collection. However, sometimes, we want to hide documents from users without truly deleting them. For example:

- We may want to store deleted documents to be able to debug complex situations later,
- This may be a "trash" feature where users can restore documents for a few days after deleting them,
- We may want to hide documents for a period of time and make them visible again later.

In all these situations, we want to hide documents and ensure no requests can impact them. The traditional approach is to have a shared BSON filter and remember to apply it to all operations. Using this approach, it is very easy to forget one request, creating hard to trace bugs. To alleviate this, KtMongo introduces filtered collections.

As an example, let's imagine a list of invoices. Users can trash invoices, but we cannot actually delete them because they may need to be inspected later.
We use the `filter` method to create a filtered collection containing only "live" invoices:
```kotlin
val allInvoices = database.getCollection<Invoice>("invoices").asKtMongo()
val liveInvoices = allInvoices.filter { Invoice::isLive ne false }
val trashedInvoices = allInvoices.filter { Invoice::isLive eq false }
```

Now, we can write any request we want using `liveInvoices` and we know that trashed invoices will not appear in results:
```kotlin
liveInvoices.find { Invoice::paid eq true }

liveInvoices.updateMany {
	Invoice::age inc 1
}

trashedInvoices.deleteMany {
	Invoice::creationDate lt (Clock.System.now() - 5.years)
}
```

!!! note "Implementation"
    `.filter {}` is implemented by combining the filter criteria with the command's own criteria using an `$and` operator.

## Bulk writes

When writing a complex [bulk write](bulk-writes.md), we can use the `filtered` method to apply a filter to some operations in the bulk write but not others:
```kotlin
liveInvoices.bulkWrite {
	insertOne(Invoice(/* … */))
	
	filtered({ Invoice::paid ne true }) {
		upsertOne(/* … */)
		updateMany(/* … */)
	}
}
```
