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
import opensavvy.ktmongo.bson.BsonDocumentReader
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

@OptIn(LowLevelApi::class)
@PreparedDslMarker
interface BsonTestDsl {

	@PreparedDslMarker
	// @Language("json")
	var expectedJson: String?

	@PreparedDslMarker
	var expectedBinaryHex: String?

	@PreparedDslMarker
	fun document(block: BsonFieldWriter.() -> Unit)

	@PreparedDslMarker
	fun verify(name: String, block: BsonDocumentReader.() -> Unit)

}

@OptIn(LowLevelApi::class, ExperimentalStdlibApi::class)
@PreparedDslMarker
fun SuiteDsl.testBson(
	context: Prepared<BsonContext>,
	name: String,
	block: BsonTestDsl.() -> Unit,
) = suite(name) {
	var _expectedJson: String? = null
	var _expectedBinaryHex: String? = null
	var _documentWriter: (BsonFieldWriter.() -> Unit)? = null
	val _verifications = ArrayList<Pair<String, BsonDocumentReader.() -> Unit>>()

	block(
		object : BsonTestDsl {
			override var expectedJson: String?
				get() = _expectedJson
				set(value) {
					check(_expectedJson == null) { "Cannot specify multiple expected JSON representations in a single test block" }
					_expectedJson = value
				}

			override var expectedBinaryHex: String?
				get() = _expectedBinaryHex
				set(value) {
					check(_expectedBinaryHex == null) { "Cannot specify multiple expected binary representations in a single test block" }
					_expectedBinaryHex = value
				}

			override fun document(block: BsonFieldWriter.() -> Unit) {
				check(_documentWriter == null) { "Cannot specify multiple BSON builders in a single test block" }
				_documentWriter = block
			}

			override fun verify(name: String, block: BsonDocumentReader.() -> Unit) {
				_verifications += name to block
			}
		}
	)

	require(_expectedBinaryHex != null || _expectedJson != null) { "At least one of the 'expectedXXX' parameters should be specified" }

	if (_expectedBinaryHex != null && _documentWriter != null) {
		test("Write to binary") {
			withClue({ "Expected JSON: '$_expectedJson'" }.takeIf { _expectedJson != null }) {
				context().buildDocument {
					_documentWriter()
				} shouldBeHex _expectedBinaryHex
			}
		}
	}

	if (_expectedJson != null && _documentWriter != null) {
		test("Write to JSON") {
			context().buildDocument {
				_documentWriter()
			}.toString() shouldBe _expectedJson
		}
	}

	if (_expectedBinaryHex != null && _expectedJson != null) {
		test("Read to JSON") {
			withClue({ "Reading the document '$_expectedBinaryHex' into JSON" }) {
				context().readDocument(_expectedBinaryHex.hexToByteArray(HexFormat.UpperCase)).toString() shouldBe _expectedJson
			}
		}
	}

	if (_documentWriter != null) {
		for ((name, verification) in _verifications) {
			test("DSL → $name") {
				context().buildDocument {
					_documentWriter()
				}.read().verification()
			}
		}
	}

	if (_expectedBinaryHex != null) {
		for ((name, verification) in _verifications) {
			test("Binary → $name") {
				context().readDocument(_expectedBinaryHex.hexToByteArray(HexFormat.UpperCase))
					.read()
					.verification()
			}
		}
	}
}
