package fr.qsh.ktmongo.sync

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.EstimatedDocumentCountOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.FindIterable
import fr.qsh.ktmongo.dsl.expr.FilterExpression
import fr.qsh.ktmongo.dsl.expr.UpdateExpression
import java.util.concurrent.TimeUnit

/**
 * Parent interface to all collection types provided by KtMongo.
 *
 * To obtain an instance of this interface, see [asKtMongo].
 */
sealed interface MongoCollection<Document : Any> {

	// region Find

	/**
	 * Finds all documents in this collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.find/)
	 */
	fun find(): FindIterable<Document>

	/**
	 * Finds all documents in this collection that satisfy [predicate].
	 *
	 * If multiple predicates are specified, and [and][FilterExpression.and] operator is implied.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.find {
	 *    User::name eq "foo"
	 *    User::age eq 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.find/)
	 *
	 * @see findOne When only one result is expected.
	 */
	fun find(predicate: FilterExpression<Document>.() -> Unit): FindIterable<Document>

	/**
	 * Finds a document in this collection that satisfies [predicate].
	 *
	 * If multiple predicates are specified, and [and][FilterExpression.and] operator is implied.
	 *
	 * This function doesn't check that there is exactly one value in the collection.
	 * It simply returns the first matching document it finds.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.findOne {
	 *     User::name eq "foo"
	 *     User::age eq 10
	 * }
	 * ```
	 *
	 * @see find When multiple results are expected.
	 */
	fun findOne(predicate: FilterExpression<Document>.() -> Unit): Document? =
		find(predicate).firstOrNull()

	// endregion
	// region Count

	/**
	 * Counts all documents in the collection.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.countDocuments/)
	 *
	 * @see countEstimated Faster alternative when the result doesn't need to be exact.
	 */
	fun count(
		options: CountOptions = CountOptions(),
	): Long

	/**
	 * Counts how many documents match [predicate] in the collection.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.countDocuments {
	 *     User::name eq "foo"
	 *     User::age eq 10
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.countDocuments/)
	 */
	fun count(
		options: CountOptions = CountOptions(),
		predicate: FilterExpression<Document>.() -> Unit,
	): Long

	/**
	 * Counts all documents in the collection.
	 *
	 * This function reads collection metadata instead of actually counting through all documents.
	 * This makes it much more performant (almost no CPU nor RAM usage), but the count may be slightly out of date.
	 *
	 * In particular, it may become inaccurate when:
	 * - there are orphaned documents in a shared cluster,
	 * - an unclean shutdown happened.
	 *
	 * Views do not possess the required metadata.
	 * When this function is called on a view (either a MongoDB view or a [filter] logical view), a regular [count] is executed instead.
	 *
	 * When this function is called from within a [transaction], a regular [count] is executed instead.
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/method/db.collection.estimatedDocumentCount/)
	 *
	 * @see count Perform the count for real.
	 */
	fun countEstimated(
		options: EstimatedDocumentCountOptions = EstimatedDocumentCountOptions(),
	): Long

	// endregion
	// region Update

	/**
	 * Updates all documents that match [filter] according to [update].
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.updateMany(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.updateMany {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 *
	 * @param filter Optional filter to select which documents are updated.
	 * If no filter is specified, all documents are updated.
	 * @see updateOne
	 */
	fun updateMany(
		options: UpdateOptions = UpdateOptions(),
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
	): UpdateResult

	/**
	 * Updates a single document that matches [filter] according to [update].
	 *
	 * If multiple documents match [filter], only the first one found is updated.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.updateOne(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.updateOne {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/update/)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateMany Update more than one document.
	 * @see findOneAndUpdate Also returns the result of the update.
	 */
	fun updateOne(
		options: UpdateOptions = UpdateOptions(),
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
	): UpdateResult

	/**
	 * Updates a single document that matches [filter] according to [update].
	 *
	 * If multiple documents match [filter], only the first one is updated.
	 *
	 * If no documents match [filter], a new one is created.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.upsertOne(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * If a document exists that has the `name` of "Patrick", its age is set to 15.
	 * If none exists, a document with `name` "Patrick" and `age` 15 is created.
	 *
	 * ### Using filtered collections
	 *
	 * The following code is equivalent:
	 * ```kotlin
	 * collection.filter {
	 *     User::name eq "Patrick"
	 * }.upsertOne {
	 *     User::age set 15
	 * }
	 * ```
	 *
	 * To learn more, see [filter][MongoCollection.filter].
	 *
	 * ### External resources
	 *
	 * - [The update operation]
	 * - [The behavior of upsert functions](https://www.mongodb.com/docs/manual/reference/method/db.collection.update/#insert-a-new-document-if-no-match-exists--upsert-)
	 *
	 * @see updateOne
	 */
	fun upsertOne(
		options: UpdateOptions = UpdateOptions(),
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
	) = updateOne(options.upsert(true), filter, update)

	/**
	 * Updates one element that matches [filter] according to [update] and returns it, atomically.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class User(
	 *     val name: String,
	 *     val age: Int,
	 * )
	 *
	 * collection.findOneAndUpdate(
	 *     filter = {
	 *         User::name eq "Patrick"
	 *     },
	 *     age = {
	 *         User::age set 15
	 *     },
	 * )
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/command/findAndModify/)
	 *
	 * @param filter Optional filter to select which document is updated.
	 * If no filter is specified, the first document found is updated.
	 * @see updateMany Update more than one document.
	 * @see updateOne Do not return the value.
	 */
	fun findOneAndUpdate(
		options: FindOneAndUpdateOptions = FindOneAndUpdateOptions(),
		filter: FilterExpression<Document>.() -> Unit = {},
		update: UpdateExpression<Document>.() -> Unit,
	): Document?

	// endregion

}

internal fun <Document : Any> MongoCollection<Document>.countForReal(
	options: EstimatedDocumentCountOptions,
	predicate: FilterExpression<Document>.() -> Unit = {},
) = count(
	options = CountOptions()
		.maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
		.comment(options.comment),
	predicate = predicate
)
