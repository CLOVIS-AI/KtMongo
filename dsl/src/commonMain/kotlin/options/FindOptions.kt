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
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.ktmongo.dsl.options.common.Options
import opensavvy.ktmongo.dsl.options.common.OptionsHolder
import opensavvy.ktmongo.dsl.options.common.WithLimit
import opensavvy.ktmongo.dsl.options.common.WithSort

/**
 * The options for a `collection.find` operation.
 */
@OptIn(LowLevelApi::class)
class FindOptions<Document : Any>(context: BsonContext) :
	Options by OptionsHolder(context),
	WithLimit,
	WithSort<Document>
