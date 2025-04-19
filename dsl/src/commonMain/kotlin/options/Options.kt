/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.options

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.Count
import opensavvy.ktmongo.dsl.command.CountOptions
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundBsonNode

/**
 * Additional parameters that are passed to MongoDB operations.
 *
 * Options are usually configured with the `options = {}` optional parameter in a command.
 * For example, if we want to know how many notifications a user has, but can only display "99" because of UI size
 * constraints, we can use the following command:
 * ```kotlin
 * notifications.count(
 *     options = {
 *         limit(99)
 *     }
 * ) {
 *     Notification::ownedBy eq currentUser
 * }
 * ```
 *
 * If the same option is specified multiple times, only the very last one applies:
 * ```kotlin
 * notifications.count {
 *     options {
 *         limit(99)
 *         limit(10)
 *     }
 * }
 * ```
 * will only count at most 10 elements.
 *
 * ### Accessing the current value of an option
 *
 * See [Options.allOptions] and [option].
 *
 * ### Implementing this interface
 *
 * Implementations of this interface must be careful to respect the contract of [BsonNode], in particular about
 * the [toString] representation.
 *
 * Option implementations must be immutable. If the user wants to change an option, they can specify it a second time
 * (which will override the previous one).
 */
interface Option : BsonNode

/**
 * Parent interface for all option containers.
 *
 * Option containers are types that declare a set of options. They are usually tied to a specific MongoDB [command].
 *
 * For example, for options related to the [Count] command, see [CountOptions].
 */
@KtMongoDsl
interface Options : CompoundBsonNode {

	/**
	 * The full list of options set on this container.
	 *
	 * Specific options are usually searched using the [option] extension.
	 */
	@LowLevelApi
	val allOptions: List<Option>

	/**
	 * JSON representation of this option.
	 */
	override fun toString(): String // Specified explicitly to force implementation by the 'by' keyword
}

internal class OptionsHolder(context: BsonContext) : AbstractCompoundBsonNode(context), Options {
	@OptIn(LowLevelApi::class)
	override val allOptions: List<Option>
		get() = children.filterIsInstance<Option>()
}

/**
 * Accesses the value of a given [Option].
 *
 * For example, if we have a helper function that sets some default options, and we want to know what maximum `limit` it
 * set, we can use:
 * ```kotlin
 * collection.count {
 *     // …
 *
 *     options {
 *         println(option<LimitOption>()) // Will print an integer
 *     }
 * }
 * ```
 */
@LowLevelApi
inline fun <reified O : Option> Options.option(): O? = (allOptions.findLast { it is O } as O?)
