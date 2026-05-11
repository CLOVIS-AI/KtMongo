/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

@file:OptIn(LowLevelApi::class, ExperimentalBsonDiffApi::class)

package opensavvy.ktmongo.bson.official

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.ExperimentalBsonDiffApi
import opensavvy.ktmongo.bson.decode
import opensavvy.ktmongo.bson.diff
import opensavvy.ktmongo.bson.encode
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@Serializable
data class NestedDocument(
	val data: BsonDocument,
)

@Serializable
data class NestedArray(
	val data: BsonArray,
)

@Serializable
data class NestedValues(
	val values: List<BsonValue>,
)

fun SuiteDsl.serializeIntoDocumentTypes(factory: Prepared<BsonFactory>) = suite("Nested BsonDocument & co") {

	test("BsonDocument") {
		val document = factory().buildDocument {
			writeDocument("data") {
				writeString("a", "1")
				writeInt32("b", 2)
			}
		}

		val decoded = document.decode<NestedDocument>()

		check(decoded.data["a"]?.decodeString() == "1")
		check(decoded.data["b"]?.decodeInt32() == 2)

		val encoded = factory().encode(decoded)

		check(document == encoded) { "Diff: ${document diff encoded}" }
	}

	test("BsonArray") {
		val document = factory().buildDocument {
			writeArray("data") {
				writeString("1")
				writeInt32(2)
			}
		}

		val decoded = document.decode<NestedArray>()

		check(decoded.data[0]?.decodeString() == "1")
		check(decoded.data[1]?.decodeInt32() == 2)

		val encoded = factory().encode(decoded)

		check(document == encoded) { "Diff: ${document diff encoded}" }
	}

	test("BsonValue") {
		val document = factory().buildDocument {
			writeArray("values") {
				writeString("1")
				writeInt32(2)
				writeDocument {
					writeString("a", "1")
					writeInt32("b", 2)
				}
				writeArray {
					writeBoolean(true)
					writeBoolean(false)
				}
			}
		}

		val decoded = document.decode<NestedValues>()

		check(decoded.values[0].decodeString() == "1")
		check(decoded.values[1].decodeInt32() == 2)
		check(decoded.values[2].decodeDocument()["a"]?.decodeString() == "1")
		check(decoded.values[2].decodeDocument()["b"]?.decodeInt32() == 2)
		check(decoded.values[3].decodeArray()[0]?.decodeBoolean() == true)
		check(decoded.values[3].decodeArray()[1]?.decodeBoolean() == false)

		val encoded = factory().encode(decoded)

		check(document == encoded) { "Diff: ${document diff encoded}" }
	}
}
