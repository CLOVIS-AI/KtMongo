/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.ktmongo.sync

import com.mongodb.client.MongoCollection
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

/**
 * Entry-point to convert objects from the official drivers to the KtMongo library.
 *
 * To learn more about the syntax of the different operators, see the top-level functions in
 * this package.
 */
object KtMongo {

	/**
	 * Converts a Java MongoDB collection into a KtMongo collection.
	 *
	 * ### Example (Java)
	 *
	 * ```java
	 * try (var client = MongoClients.create()) {
	 *     var javaCollection = client.getDatabase("my-db")
	 *         .getCollection("my-col", MyCollection.class);
	 *
	 *     var collection = KtMongo.from(javaCollection);
	 *
	 *     System.out.println(
	 *         collection.find().toList()
	 *     );
	 * }
	 * ```
	 */
	@JvmStatic
	fun <T : Any> from(driver: MongoCollection<T>): JvmMongoCollection<T> =
		from(com.mongodb.kotlin.client.MongoCollection(driver))

	/**
	 * Converts a Kotlin MongoDB collection into a KtMongo collection.
	 *
	 * ### Example (Kotlin)
	 *
	 * ```kotlin
	 * MongoClients.create().use { client ->
	 *     val kotlinCollection = it.getDatabase("my-db")
	 *         .getCollection("my-col", MyCollection::class.java)
	 *
	 *     val collection = KtMongo.from(kotlinCollection)
	 *
	 *     println(
	 *         collection.find().toList()
	 *     )
	 * }
	 * ```
	 */
	@JvmStatic
	fun <T : Any> from(driver: com.mongodb.kotlin.client.MongoCollection<T>): JvmMongoCollection<T> =
		driver.asKtMongo()

	private val typeOfNull: KType = kotlin.reflect.typeOf<Any?>()

	private fun <T : Any> typeOfKotlinClass(value: T?, type: KClass<T>): KType =
		object : KType {
			override val classifier: KClassifier
				get() = type

			private fun <T> Iterable<T>.superClass(): KClass<*> = this
				.asSequence()
				.filterNotNull()
				.map { it::class as KClass<*> }
				.reduceOrNull { a, b ->
					when {
						a == b -> a
						a.isSuperclassOf(b) -> a
						b.isSuperclassOf(a) -> b
						else -> {
							// The common parent is the KClass that appears in both lists but isn't a supertype of any
							// other listed KClass.
							val commonParent = (a.allSuperclasses.toSet() intersect b.allSuperclasses.toSet())
								.reduceOrNull { candidateA, candidateB ->
									when {
										candidateA.isSubclassOf(candidateB) -> candidateA
										candidateB.isSubclassOf(candidateA) -> candidateB
										else -> Any::class
									}
								}

							commonParent ?: Any::class
						}
					}
				}
				?: Any::class

			@Suppress("UNCHECKED_CAST")
			override val arguments: List<KTypeProjection> by lazy(mode = LazyThreadSafetyMode.PUBLICATION) {
				when (value) {
					is Collection<*> if value.isNotEmpty() -> listOf(
						KTypeProjection.invariant(typeOf(value.first(), value.superClass() as KClass<Any>))
					)

					is Map<*, *> if value.isNotEmpty() -> listOf(
						KTypeProjection.invariant(typeOf(value.keys.first(), value.keys.superClass() as KClass<Any>)),
						KTypeProjection.invariant(typeOf(value.values.first(), value.values.superClass() as KClass<Any>))
					)

					else -> emptyList()
				}
			}

			override val isMarkedNullable: Boolean
				get() = true // Java types are always declared nullable

			override val annotations: List<Annotation>
				get() = emptyList()

			override fun toString(): String = "TypeOf($type)"
		}

	private fun <T> typeOf(value: T, type: KClass<T & Any>): KType =
		if (value == null) typeOfNull
		else typeOfKotlinClass(value, type)

	/**
	 * Obtains a Kotlin [KType] instance for a Java [value].
	 *
	 * This method is a workaround required for now. In the future, when we move to stabilize the Java KtMongo driver,
	 * this will not be necessary anymore.
	 */
	@Suppress("UNCHECKED_CAST")
	@JvmStatic
	fun <T> typeOf(value: T): KType =
		if (value == null) typeOfNull
		else return typeOfKotlinClass(value, value::class as KClass<T & Any>)
}
