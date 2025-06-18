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

package opensavvy.ktmongo.official

import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.ReadConcern
import opensavvy.ktmongo.dsl.tree.BsonNode
import org.bson.conversions.Bson

@LowLevelApi
fun opensavvy.ktmongo.bson.Bson.toJava(): Bson = (this as opensavvy.ktmongo.bson.official.Bson).raw

@LowLevelApi
fun BsonNode.toJava(): Bson = this.toBson().toJava()

@LowLevelApi
fun ReadConcern?.toJava(): com.mongodb.ReadConcern = when (this) {
	ReadConcern.Local -> com.mongodb.ReadConcern.LOCAL
	ReadConcern.Available -> com.mongodb.ReadConcern.AVAILABLE
	ReadConcern.Majority -> com.mongodb.ReadConcern.MAJORITY
	ReadConcern.Linearizable -> com.mongodb.ReadConcern.LINEARIZABLE
	ReadConcern.Snapshot -> com.mongodb.ReadConcern.SNAPSHOT
	null -> com.mongodb.ReadConcern.DEFAULT
}
