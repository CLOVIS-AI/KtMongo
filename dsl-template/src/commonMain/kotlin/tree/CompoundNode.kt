/*
 * Copyright (c) 2024-2026, OpenSavvy and contributors.
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
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Value

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
// Not necessarily @KtMongoDsl, could be nodes of anything else
interface CompoundNode<N : Node> : Node {

	/**
	 * Adds a new [Node] into the current node.
	 *
	 * This method is considered unsafe as it allows inserting arbitrary nodes into the current node.
	 * Since KtMongo is a database driver, this method allows inserting any kind
	 * of operation without checking any security or coherence invariants.
	 *
	 * **If you are not careful, this method will create database injection risks.**
	 *
	 * Users should only interact with this method when they have a custom node that doesn't exist in the library,
	 * for example, when adding a missing operator.
	 * In these cases, we highly recommend users to [contact the maintainers of KtMongo](https://gitlab.com/opensavvy/ktmongo/-/work_items/new)
	 * to ensure the created operator respects all invariants.
	 * If possible, upstreaming the operator would be of benefit to all users and guarantees future bug fixes.
	 *
	 * In all other cases, it is expected that implementations of this interface provide methods for each added functionality
	 * that are responsible for checking invariants and are safe to call.
	 *
	 * For a more detailed explanation of the contract of this method, see [Node].
	 *
	 * ### Implementation notes
	 *
	 * - Implementations should reject any mutation if [Node.freeze] has been called.
	 * - If applicable, implementations should simplify the accepted node (see [BsonNode.simplify] and [Value.simplify]).
	 * - Implementations should call [Node.freeze] on [node] (or its simplification) before accepting it.
	 */
	@LowLevelApi
	@DangerousMongoApi
	fun accept(node: N)

}

/**
 * Adds any number of [nodes] into this one.
 *
 * To learn more about the behavior of this function and the security implications, see [accept][CompoundNode.accept].
 */
@LowLevelApi
@DangerousMongoApi
fun <N : Node> CompoundNode<N>.acceptAll(nodes: Iterable<N>) {
	for (child in nodes) {
		accept(child)
	}
}
