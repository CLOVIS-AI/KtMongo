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

package opensavvy.ktmongo.dsl.aggregation.stages

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.options.common.SortOptionDsl
import opensavvy.ktmongo.dsl.path.Field
import opensavvy.ktmongo.dsl.path.Path
import opensavvy.ktmongo.dsl.query.common.AbstractCompoundExpression
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode
import opensavvy.ktmongo.dsl.tree.BsonNode

/**
 * Pipeline implementing the `$sort` stage.
 */
@KtMongoDsl
interface HasSort<Document : Any> : Pipeline<Document> {

	/**
	 * Specifies in which order elements should be sorted.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * collection.aggregate()
	 *     .sort {
	 *         ascending(User::age)
	 *     }
	 *     .limit(15)
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/sort/)
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun sort(
		block: SortOptionDsl<Document>.() -> Unit
	): Pipeline<Document> =
		withStage(SortStage<Document>(context).apply(block))
}

private class SortStage<Document : Any>(
	context: BsonContext,
) : AbstractCompoundExpression(context), SortOptionDsl<Document> {

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun ascending(field: Field<Document, *>) {
		accept(SortBsonNode(field.path, 1, context))
	}

	@OptIn(DangerousMongoApi::class, LowLevelApi::class)
	override fun descending(field: Field<Document, *>) {
		accept(SortBsonNode(field.path, -1, context))
	}

	@LowLevelApi
	override fun simplify(children: List<BsonNode>): AbstractBsonNode? =
		if (children.isNotEmpty()) this
		else null

	@LowLevelApi
	override fun write(writer: BsonFieldWriter, children: List<BsonNode>) = with(writer) {
		writeDocument("\$sort") {
			super.write(this, children)
		}
	}

	@LowLevelApi
	private class SortBsonNode(
		val path: Path,
		val value: Int,
		context: BsonContext,
	) : AbstractBsonNode(context) {

		override fun write(writer: BsonFieldWriter) = with(writer) {
			writeInt32(path.toString(), value)
		}
	}

}
