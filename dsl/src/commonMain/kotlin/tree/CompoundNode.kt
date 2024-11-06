/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.tree

import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * A [Node] that combines multiple other nodes into a single node.
 *
 * A compound node may have `0.n` children.
 * Children are added by calling the [accept] method.
 *
 * Accepted children must follow a few invariants. See [Node] for more information.
 *
 * There are no general-purpose way of accessing the children after they have been accepted.
 * Instead, this node should be considered as representing the children itself, as a single unit.
 * Subtypes may decide to provide such a feature, however.
 */
interface CompoundNode<N : Node> {

	/**
	 * Adds a new [Node] into the current node.
	 *
	 * This method is generally considered unsafe, as it allows inserting any kind of node into the current node.
	 * Since this library is about representing database requests, this method allows inserting any kind
	 * of operation, without necessarily checking any security or coherence invariants.
	 *
	 * Users should only interact with this method when they add a new type of node that doesn't exist in the library.
	 * For example, when adding an operator that is missing from the library.
	 * In these cases, we highly recommend users to contact the maintainers of KtMongo to ensure the created operator
	 * respects all invariants.
	 * If possible, upstreaming the operator would be of benefit to all users, and guarantees future bug fixes.
	 *
	 * In all other cases, it is expected that implementations of this interface provide methods for each added functionality
	 * that are responsible for checking invariants and are safe to call.
	 *
	 * For a more detailed explanation of the contract of this method, see [Node].
	 */
	@LowLevelApi
	@DangerousMongoApi
	@KtMongoDsl
	fun accept(node: N)

}
