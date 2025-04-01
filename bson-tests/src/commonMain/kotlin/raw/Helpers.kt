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

package opensavvy.ktmongo.bson.raw

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.PreparedDslMarker
import opensavvy.prepared.suite.SuiteDsl

@OptIn(ExperimentalStdlibApi::class, LowLevelApi::class)
infix fun Bson.shouldBeHex(expected: String) {
	withClue({ "Encoding BSON object: $this" }) {
		this.toByteArray().toHexString(HexFormat.UpperCase) shouldBe expected
	}
}

@OptIn(LowLevelApi::class, ExperimentalStdlibApi::class)
@PreparedDslMarker
fun SuiteDsl.testBson(
	context: Prepared<BsonContext>,
	name: String,
	expectedBinaryHex: String? = null,
	expectedJson: String? = null,
	builder: BsonFieldWriter.() -> Unit,
) = suite(name) {
	require(expectedBinaryHex != null || expectedJson != null) { "At least one of the 'expectedXXX' parameters should be specified" }

	if (expectedBinaryHex != null) {
		test("Write to binary") {
			withClue({ "Expected JSON: '$expectedJson'" }.takeIf { expectedJson != null }) {
				context().buildDocument {
					builder()
				} shouldBeHex expectedBinaryHex
			}
		}
	}

	if (expectedJson != null) {
		test("Write to JSON") {
			context().buildDocument {
				builder()
			}.toString() shouldBe expectedJson
		}
	}

	if (expectedBinaryHex != null && expectedJson != null) {
		test("Read to JSON") {
			withClue({ "Reading the document '$expectedBinaryHex' into JSON" }) {
				context().readDocument(expectedBinaryHex.hexToByteArray(HexFormat.UpperCase)).toString() shouldBe expectedJson
			}
		}
	}
}
