/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.aggregation

import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.KtMongoDsl
import opensavvy.ktmongo.dsl.aggregation.accumulators.ArithmeticValueAccumulators
import opensavvy.ktmongo.dsl.aggregation.accumulators.ValueAccumulators
import opensavvy.ktmongo.dsl.tree.AbstractCompoundBsonNode

/**
 * DSL to accumulate values into each other, available in the [`$group` stage][opensavvy.ktmongo.dsl.aggregation.stages.HasGroup.group].
 *
 * Accumulation operators are a specific type of [aggregation operators][AggregationOperators].
 *
 * ### Operators
 *
 * Arithmetic operators:
 * - [`$avg`][ArithmeticValueAccumulators.average]
 * - [`$sum`][ArithmeticValueAccumulators.sum]
 *
 * @see Value Representation of an aggregation value.
 * @see AggregationOperators Learn more about regular aggregation operators.
 */
@KtMongoDsl
interface AccumulationOperators<From : Any, Into : Any> : ValueAccumulators<From, Into>,
	AggregationOperators,
	ArithmeticValueAccumulators<From, Into>

internal class AccumulationOperatorsImpl<From : Any, Into : Any>(
	context: BsonContext,
) : AbstractCompoundBsonNode(context),
	AccumulationOperators<From, Into>
