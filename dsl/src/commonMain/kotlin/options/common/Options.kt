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

package opensavvy.ktmongo.dsl.options.common

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.command.Count
import opensavvy.ktmongo.dsl.options.CountOptions
import opensavvy.ktmongo.dsl.query.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.tree.BsonNode
import opensavvy.ktmongo.dsl.tree.CompoundNode

/**
 * Additional parameters that are passed to MongoDB operations.
 *
 * Options are usually configured with the `options {}` DSL in an operation.
 * For example, if we want to know how many notifications a user has, but can only display "99" because of UI size
 * constraints, we can use the following request:
 * ```kotlin
 * notifications.count {
 *     Notification::ownedBy eq currentUser
 *
 *     options {
 *         limit(99)
 *     }
 * }
 * ```
 *
 * The order in which the filter and the options are declared is irrelevant; this request is strictly equivalent:
 * ```kotlin
 * notifications.count {
 *     options {
 *         limit(99)
 *     }
 *
 *     Notification::ownedBy eq currentUser
 * }
 * ```
 *
 * However, if the same option is specified multiple times, only the very last one applies:
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
 * ### Accessing the current value
 *
 * See [Options], [value] and [option].
 *
 * ### Implementing this interface
 *
 * Implementations of this interface must be careful to respect the contract of [BsonNode], in particular about
 * the [toString] representation.
 *
 * Option implementations must be immutable. If the user wants to change an option, they can specify it a second time
 * (which will override the previous one).
 *
 * @param Value The [value] stored by this option.
 * For example, [LimitOption] stores an integer.
 */
interface Option<out Value> : BsonNode {

	/**
	 * The value stored by this option, as specified by the user.
	 *
	 * Learn more about options: [Option].
	 *
	 * To access the value of a specific option, see [option].
	 */
	val value: Value
}

/**
 * Parent interface for all option containers.
 *
 * Option containers are types that declare a set of options. They are usually tied to a specific MongoDB operation
 * via a model.
 *
 * For example, for options related to the [Count] model, see [CountOptions].
 */
@KtMongoDsl
interface Options : BsonNode, CompoundNode<Option<*>> {

	/**
	 * The full list of options set on this container.
	 *
	 * Specific options are usually searched using the [option] extension.
	 */
	@LowLevelApi
	val allOptions: List<Option<*>>

	/**
	 * JSON representation of this option.
	 */
	override fun toString(): String // Specified explicitly to force implementation by the 'by' keyword
}

internal class OptionsHolder(context: BsonContext) : AbstractCompoundExpression(context), Options {
	@LowLevelApi
	@DangerousMongoApi
	override fun accept(node: Option<*>) {
		this.accept(node as BsonNode)
	}

	@OptIn(LowLevelApi::class)
	override val allOptions: List<Option<*>>
		get() = children.filterIsInstance<Option<*>>()
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
inline fun <reified O : Option<V>, V> Options.option(): V? = (allOptions.findLast { it is O } as O?)?.value
