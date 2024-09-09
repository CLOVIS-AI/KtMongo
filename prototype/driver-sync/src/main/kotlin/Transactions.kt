package fr.qsh.ktmongo.sync

import com.mongodb.ClientSessionOptions
import com.mongodb.kotlin.client.ClientSession
import com.mongodb.kotlin.client.MongoClient

/**
 * See [transaction].
 */
interface TransactionScope {

	/**
	 * Commits any uncommitted work from the current [transaction].
	 */
	fun commit()

	/**
	 * Aborts any uncommitted work from the current [transaction].
	 */
	fun abort()
}

private class TransactionScopeImpl(
	val session: ClientSession
) : TransactionScope {

	override fun commit() {
		session.commitTransaction()
	}

	override fun abort() {
		session.abortTransaction()
	}
}

private val currentClientSession = ThreadLocal<ClientSession>()

/**
 * Manages a distributed transaction.
 *
 * ### Transactions shouldn't be used, most of the time
 *
 * In MongoDB, documents are always updated atomically.
 * This reduces the need for transactions, since all data needing to be updated at once is expected to be in a single
 * document.
 *
 * However, sometimes, distributed transactions are still necessary.
 * Note that MongoDB isn't optimized for heavy use of distributed transactions.
 *
 * ### Usage
 *
 * **This example purely demonstrates the syntax. It is not a valid situation to use distributed transactions.**
 *
 * ```kotlin
 * val client = MongoClient.create()
 * val database = client.getDatabase("test")
 *
 * val jedi = database.getCollection<Jedi>("jedi").asKtMongo()
 * val padawan = database.getCollection<Padawan>("padawan").asKtMongo()
 *
 * client.transaction {
 *     padawan.insertOne {
 *         Padawan::id set 1234
 *         Padawan::name set "Alexsandr"
 *     }
 *
 *     jedi.updateOne(
 *         filter = { Jedi::id eq 967 },
 *         update = { Jedi::padawans add 1234 }
 *     )
 * }
 * ```
 *
 * ### Behavior
 *
 * If the block terminates with an exception, the transaction is aborted.
 * If the block terminates normally, the transaction is committed.
 *
 * Alternatively, the [TransactionScope.commit] and [TransactionScope.abort] functions can be called
 * to manually commit or abort transactions.
 *
 * ### External resources
 *
 * - [Official documentation](https://www.mongodb.com/docs/manual/core/transactions/)
 */
fun <R> MongoClient.transaction(
	options: ClientSessionOptions = ClientSessionOptions.builder().build(),
	block: TransactionScope.() -> R
): R {
	val previousSession: ClientSession? = currentClientSession.get()
	val session = startSession(options)

	try {
		currentClientSession.set(session)
		val ret = block(TransactionScopeImpl(session))

		session.commitTransaction()
		return ret
	} catch (e: Throwable) {
		session.abortTransaction()
		throw e
	} finally {
		currentClientSession.set(previousSession)
		session.close()
	}
}

internal fun getCurrentSession(): ClientSession? =
	currentClientSession.get()
