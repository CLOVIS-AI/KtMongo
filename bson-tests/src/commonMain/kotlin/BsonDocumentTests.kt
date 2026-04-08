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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.assertions.matches

@Serializable
data class BsonDocumentUser(
	val a: String = "Bob",
	val b: Int,
)

fun SuiteDsl.verifyBsonDocuments(factory: Prepared<BsonFactory>) = suite("BSON documents") {

	test("Decode a simple document") {
		val document = factory().buildDocument {
			writeString("a", "Bob")
			writeInt32("b", 45)
		}

		check(document.decode<BsonDocumentUser>() == BsonDocumentUser("Bob", 45))
	}

	// TODO in https://gitlab.com/opensavvy/ktmongo/-/work_items/119
	//      Bugged in org.bson:bson-kotlin
	// test("Decode a single document with an optional field") {
	// 	val document = factory().buildDocument {
	// 		writeInt32("b", 45)
	// 	}
	//
	// 	check(document.decode<BsonDocumentUser>() == BsonDocumentUser("Bob", 45))
	// }

	test("Decode a single document with a missing mandatory field") {
		val document = factory().buildDocument {
			writeString("a", "Bob")
		}

		val exception = checkThrows<BsonDecodingException> {
			document.decode<BsonDocumentUser>()
		}

		check(exception.message matches "Could not decode opensavvy.ktmongo.bson.BsonDocumentUser.*\n\tfrom value \\{\"a\": \"Bob\"}.*\n*.*")
	}

}
