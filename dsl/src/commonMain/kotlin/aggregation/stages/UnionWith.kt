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

import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.BsonContext
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.aggregation.Pipeline
import opensavvy.ktmongo.dsl.tree.AbstractBsonNode

/**
 * Pipeline implementing the `$unionWith` stage.
 */
@KtMongoDsl
interface HasUnionWith<Document : Any> : Pipeline<Document> {

	/**
	 * Combines two aggregations into a single result set.
	 *
	 * `$unionWith` outputs the combined result set (including duplicates) to the next stage.
	 * The order in which the combined result set documents are output is unspecified.
	 *
	 * ### Namespacing
	 *
	 * [other] must be a pipeline from the same namespace. It may be a pipeline from the same collection or another
	 * collection, as long as they are both part of the same namespace.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * interface Vehicle {
	 *     val brand: String,
	 * }
	 *
	 * class Car(
	 *     override val brand: String,
	 *     val enginePower: Int,
	 * ) : Vehicle
	 *
	 * class Bike(
	 *     override val brand: String,
	 *     val hasBasket: Boolean
	 * )
	 *
	 * val selectedCars = cars.aggregate()
	 *     .match { Car::enginePower gt 30 }
	 *     .project { include(Car::brand) }
	 *     .reinterpret<Vehicle>()
	 *
	 * val selectedVehicles = bikes.aggregate()
	 *     .project { include(Bike::brand) }
	 *     .reinterpret<Vehicle>()
	 *     .unionWith(selectedCars)
	 *     .limit(5)
	 *     .toList()
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation](https://www.mongodb.com/docs/manual/reference/operator/aggregation/unionWith/)
	 */
	@KtMongoDsl
	@OptIn(LowLevelApi::class, DangerousMongoApi::class)
	fun unionWith(other: HasUnionWithCompatibility<Document>): Pipeline<Document> =
		withStage(UnionWithStage(other, context))

}

/**
 * Pipeline that can be used as the second argument in a `$unionWith` stage.
 *
 * Instances of this interface should be immutable.
 */
@KtMongoDsl
interface HasUnionWithCompatibility<Document : Any> : Pipeline<Document> {

	/**
	 * Writes this pipeline into a `$unionWith` stage.
	 *
	 * This method is a low-level API for building custom `$unionWith` stages. Regular users should use [HasUnionWith.unionWith] instead.
	 *
	 * ### Implementation contract
	 *
	 * When another pipeline wants to embed the current pipeline into itself, it will call this method from within
	 * the `$unionWith` stage. This method should thus emit the body of the stage:
	 *
	 * ```json
	 * {
	 *     coll: "<collection>",
	 *     pipeline: [ <stage1>,… ]
	 * }
	 * ```
	 *
	 * ### External resources
	 *
	 * - [Official documentation of `$unionWith`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/unionWith/)
	 *
	 * @see HasUnionWith.unionWith The `$unionWith` stage.
	 */
	@LowLevelApi
	fun embedInUnionWith(writer: BsonFieldWriter)

}

@LowLevelApi
private class UnionWithStage(
	val other: HasUnionWithCompatibility<*>,
	context: BsonContext,
) : AbstractBsonNode(context) {

	override fun write(writer: BsonFieldWriter) = with(writer) {
		writeDocument("\$unionWith") {
			other.embedInUnionWith(this)
		}
	}

}
