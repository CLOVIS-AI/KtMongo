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

package opensavvy.ktmongo.dsl.path

import opensavvy.ktmongo.dsl.LowLevelApi
import kotlin.reflect.KProperty1

/**
 * Allows configuring how the DSL generates MongoDB paths from [KProperty1] instances.
 *
 * ### Introduction
 *
 * The KtMongo library uses type-safe [Field] instances to represent paths in MongoDB queries.
 * For example,
 * ```kotlin
 * class User(
 *     val profile: Profile,
 * )
 *
 * class Profile(
 *     val name: String,
 * )
 *
 * with(FieldDsl(PropertyNameStrategy.Default)) {
 *     User::profile / Profile::name
 * }
 * ```
 * which results in the path `profile.name`.
 *
 * To build this path, the [PropertyNameStrategy.Default] strategy uses the name of the fields as they are written in Kotlin code.
 *
 * ### Customizing
 *
 * If you use a serialization library that allows renaming fields, like Jackson or KotlinX.Serialization, you may find a mismatch
 * between how your documents are generated and how KtMongo queries try to access them.
 * ```kotlin
 * @Serializable
 * data class User(
 *     @SerialName("f")
 *     val aSuperLongField: String,
 * )
 *
 * with(FieldDsl(PropertyNameStrategy.Default)) {
 *     User::aSuperLongField
 * }
 * ```
 * which results in the path `aSuperLongField` **which is incorrect**: it should be `f`.
 * In this situation, your KtMongo queries will refer to paths that don't exist.
 *
 * It is not feasible for KtMongo to provide built-in support for all serialization libraries.
 * However, we do support:
 *
 * - The annotations `@BsonId` and `@SerialName` used by the KMongo library, with the modules `dev.opensavvy.ktmongo:driver-coroutines-kmongo` or `dev.opensavvy.ktmongo:driver-sync-kmongo`.
 *
 * You can also create your own strategy to support any other serialization library.
 * To do this, implement this interface.
 * All KtMongo entry-points should provide a way to override the default strategy, for example with the official drivers:
 * ```kotlin
 * val client = MongoClient.create()
 * val database = client.getDatabase("my-project")
 * val collection = database.getCollection<User>("users")
 *     .asKtMongo(
 *         nameStrategy = yourCustomNameStrategy,
 *     )
 * ```
 *
 * The different operators that build [Field] instances are in the [FieldDsl] interface.
 */
interface PropertyNameStrategy {

	/**
	 * Generates the name of a [property].
	 *
	 * This is used by the DSL to allow configuring how the notation `Foo::bar / Bar::baz` is
	 * converted into a MongoDB path.
	 *
	 * For example, an implementation could add support for the KMongo annotation `@BsonId` to rename
	 * the field `_id`.
	 */
	@LowLevelApi
	fun pathOf(property: KProperty1<*, *>): Path

	/**
	 * Default implementation of [PropertyNameStrategy], which always uses the property name exactly as it is written in Kotlin code.
	 */
	object Default : PropertyNameStrategy {

		@LowLevelApi
		override fun pathOf(property: KProperty1<*, *>): Path =
			Path(property.name)
	}
}
