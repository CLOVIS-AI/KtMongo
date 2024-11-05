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

import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * An element in an abstract tree.
 *
 * It is not expected that users of this library have to deal with this class directly.
 *
 * ### Trees
 *
 * Trees are expected to be built bottom-up: a node is always built before its parents.
 * Once a node has been [accepted][CompoundNode.accept] by a parent, it [freezes][freeze] (becomes forever immutable).
 * Before the node is accepted, however, it gets a chance to [simplify] itself.
 * The same node may be added to multiple parent nodes.
 *
 * This schemes ensures that trees are always as simplified as possible: we are always building a single node at a time,
 * and all its children are guaranteed to be immutable and fully simplified.
 *
 * There are two main categories of nodes:
 * - Nodes that represent some data by themselves,
 * - Nodes that group other nodes into a single larger node.
 *
 * The former category implements this interface, whereas the latter implements [CompoundNode].
 *
 * ### Implementing this interface
 *
 * See [AbstractNode].
 *
 * @param Self The type of node returned by the [simplify] methods.
 * In most cases, it should be an interface that implements [Node] and is implemented by the current subtype.
 */
interface Node<out Self : Node<Self>> {

	/**
	 * Makes this node immutable.
	 *
	 * After this method has been called, the expression can never be modified again.
	 * This ensures that nodes cannot change after they have been used within other nodes.
	 *
	 * To learn more about this process, see [Node].
	 */
	@LowLevelApi
	fun freeze()

	/**
	 * Returns a simplified (but equivalent) node to the current node.
	 *
	 * This function is always called before this node is added to a parent node;
	 * the result value is added in its stead after being [frozen][freeze].
	 * To learn more about this process, see [Node].
	 *
	 * The simplest default implementation is to return `this`.
	 *
	 * @return `null` when the current node was simplified into nothingness (i.e. it does nothing).
	 */
	@LowLevelApi
	fun simplify(): Self?

}

/**
 * Helper to implement [Node].
 *
 * This class takes care of handling [freezing][freeze].
 * Implementors should ensure to check the value of [frozen] before accepting any mutation.
 */
abstract class AbstractNode<out Self : Node<Self>> : Node<Self> {

	/**
	 * `true` if [freeze] has been called. Can never become `false` again.
	 *
	 * If this value is `true`, this node should reject any attempt to mutate it.
	 * It is the responsibility of the implementor to satisfy this invariant.
	 */
	protected var frozen: Boolean = false
		private set

	@LowLevelApi
	final override fun freeze() {
		frozen = true
	}

}
