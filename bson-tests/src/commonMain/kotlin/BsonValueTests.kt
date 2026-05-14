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

@file:OptIn(LowLevelApi::class, ExperimentalBsonPathApi::class)

package opensavvy.ktmongo.bson

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl

@Serializable
data class BsonValueNested(
	val id: String,
	val nested: BsonValue,
)

fun SuiteDsl.verifyBsonValues(factory: Prepared<BsonFactory>) = suite("BSON values") {

	test("Encode and decode a nested BsonValue") {
		val nested = factory().buildDocument {
			writeString("v", "foo")
		}["v"]!!

		val wrapped = BsonValueNested("e", nested)

		check(wrapped.nested.decodeString() == "foo")

		val encoded = factory().buildDocument {
			writeSafe("d", wrapped)
		}

		check(encoded.selectFirst<String>("$.d.nested") == "foo")
		check(encoded["d"]?.decode<BsonValueNested>()?.nested == nested)
	}

}
