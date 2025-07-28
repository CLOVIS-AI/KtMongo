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

@file:OptIn(LowLevelApi::class, ExperimentalStdlibApi::class)

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

interface BsonDeclaration {

	companion object {
		fun document(writer: BsonFieldWriter.() -> Unit): BsonDeclaration =
			BsonBinaryDeclaration(writer)

		fun hex(/* language=hexdump */ hex: String): BsonDeclaration =
			BsonHexadecimalRepresentation(hex)

		fun json(/* language=mongodb-json */ json: String): BsonDeclaration =
			BsonJsonRepresentation(json)

		fun verify(name: String, block: BsonDocumentReader.() -> Unit): BsonDeclaration =
			BsonAssertion(name, block)
	}
}

private class BsonBinaryDeclaration(
	val writer: BsonFieldWriter.() -> Unit,
) : BsonDeclaration

private class BsonHexadecimalRepresentation(
	val hex: String,
) : BsonDeclaration {
	override fun toString() = hex
}

private class BsonJsonRepresentation(
	val json: String,
) : BsonDeclaration {
	override fun toString() = json
}

private class BsonAssertion(
	val name: String,
	val assert: BsonDocumentReader.() -> Unit,
) : BsonDeclaration {
	override fun toString() = "“$name”"
}

@PreparedDslMarker
fun SuiteDsl.testBson(
	context: Prepared<BsonContext>,
	name: String,
	vararg declarations: BsonDeclaration,
) = suite(name) {
	val writers = declarations.filterIsInstance<BsonBinaryDeclaration>().map { it.writer }
	val hexReprs = declarations.filterIsInstance<BsonHexadecimalRepresentation>().map { it.hex }
	val jsonReprs = declarations.filterIsInstance<BsonJsonRepresentation>().map { it.json }
	val verifications = declarations.filterIsInstance<BsonAssertion>()

	require(hexReprs.isNotEmpty() || jsonReprs.isNotEmpty()) { "At least one of the 'hex()' or 'json()' functions should be part of the declarations, otherwise we do not have expected values for this test" }

	for (writer in writers) {
		for (hex in hexReprs) {
			test("Write to binary: $hex") {
				context().buildDocument {
					writer(this)
				} shouldBeHex hex
			}
		}

		for (json in jsonReprs) {
			test("Write to JSON: $json") {
				context().buildDocument {
					writer(this)
				}.toString() shouldBe json
			}
		}

		for (verification in verifications) {
			test("Write and verify that $verification") {
				val document = context().buildDocument {
					writer(this)
				}
				verification.assert(document.reader())
			}
		}
	}

	for (hex in hexReprs) {
		for (json in jsonReprs) {
			test("Read $hex outputs the JSON $json") {
				context().readDocument(hex.hexToByteArray(HexFormat.UpperCase)).toString() shouldBe json
			}
		}

		for (verification in verifications) {
			test("Read $hex and verify that $verification") {
				val document = context().readDocument(hex.hexToByteArray(HexFormat.UpperCase))
				verification.assert(document.reader())
			}
		}
	}
}
