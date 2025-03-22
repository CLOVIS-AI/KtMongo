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

package opensavvy.ktmongo.bson

import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * An object that can be represented as fields in a [BSON document][Bson].
 */
interface BsonFieldWriteable {

	/**
	 * Writes this object to the provided [writer].
	 */
	@LowLevelApi
	fun writeTo(writer: BsonFieldWriter)
}

/**
 * An object that can be represented as an item in a [BSON array][BsonArray].
 */
interface BsonValueWriteable {

	/**
	 * Writes this object to the provided [writer].
	 */
	@LowLevelApi
	fun writeTo(writer: BsonValueWriter)
}
