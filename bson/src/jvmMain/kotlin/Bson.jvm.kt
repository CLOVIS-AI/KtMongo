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

import org.bson.BsonArray
import org.bson.BsonDocument

actual typealias Bson = BsonDocument

actual class BsonArray(val raw: BsonArray) {

	actual override fun toString(): String {
		// Yes, this is very ugly, and probably inefficient.
		// The Java library doesn't provide a way to serialize arrays to JSON.
		// https://www.mongodb.com/community/forums/t/how-to-convert-a-single-bsonvalue-such-as-bsonarray-to-json-in-the-java-bson-library

		val document = BsonDocument("a", raw).toJson()

		return document.substring(
			document.indexOf('['),
			document.lastIndexOf(']') + 1
		).trim()
	}
}
