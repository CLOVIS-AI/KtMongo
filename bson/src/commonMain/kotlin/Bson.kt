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

package opensavvy.ktmongo.bson

/**
 * A BSON document.
 *
 * To create instances of this class, see [buildBsonDocument].
 */
expect class Bson {

	/**
	 * JSON representation of this [Bson] object, as a [String].
	 */
	override fun toString(): String
}

/**
 * A BSON array.
 *
 * To create instances of this class, see [buildBsonArray].
 */
expect class BsonArray {

	/**
	 * JSON representation of this [BsonArray] object, as a [String].
	 */
	override fun toString(): String
}
