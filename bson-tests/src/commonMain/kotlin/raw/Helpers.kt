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

import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.BsonFactory
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.PreparedDslMarker
import opensavvy.prepared.suite.SuiteDsl
import org.intellij.lang.annotations.Language
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class, LowLevelApi::class)
infix fun Bson.shouldBeHex(@Language("HEXDUMP") expected: String) {
	check(this.toByteArray().toHexString(HexFormat.UpperCase) == expected)
}

infix fun Bson.shouldBeJson(@Language("MongoDB-JSON") expected: String) {
	check(toString() == expected)
}

interface BsonDeclaration {

	companion object {
		@PreparedDslMarker
		fun document(writer: BsonFieldWriter.() -> Unit): BsonDeclaration =
			BsonBinaryDeclaration(writer)

		@PreparedDslMarker
		fun serialize(obj: Any, type: KType, klass: KClass<*>): BsonDeclaration =
			BsonSerializeDeclaration(obj, type, klass)

		@PreparedDslMarker
		inline fun <reified T : Any> serialize(obj: T): BsonDeclaration =
			serialize(obj, typeOf<T>(), T::class)

		@PreparedDslMarker
		fun hex(@Language("HEXDUMP") hex: String): BsonDeclaration =
			BsonHexadecimalRepresentation(hex)

		@PreparedDslMarker
		fun json(@Language("MongoDB-JSON") json: String): BsonDeclaration =
			BsonJsonRepresentation(json)

		@PreparedDslMarker
		fun verify(name: String, block: BsonDocumentReader.() -> Unit): BsonDeclaration =
			BsonAssertion(name, block)
	}
}

private class BsonBinaryDeclaration(
	val writer: BsonFieldWriter.() -> Unit,
) : BsonDeclaration {

	fun write(context: BsonFactory): Bson =
		context.buildDocument { writer() }
}

private class BsonSerializeDeclaration(
	val obj: Any,
	val type: KType,
	val klass: KClass<*>,
) : BsonDeclaration {

	@Suppress("UNCHECKED_CAST")
	fun write(context: BsonFactory): Bson =
		context.buildDocument(obj, type, klass as KClass<Any>)

	fun toAssertion() = BsonAssertion(obj.toString()) {
		check(this.read(type, klass) == obj)
	}

	override fun toString() = obj.toString()
}

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
	context: Prepared<BsonFactory>,
	name: String,
	vararg declarations: BsonDeclaration,
) = suite(name) {
	val writers = declarations.filterIsInstance<BsonBinaryDeclaration>().map { it::write } + declarations.filterIsInstance<BsonSerializeDeclaration>().map { it::write }
	val hexReprs = declarations.filterIsInstance<BsonHexadecimalRepresentation>().map { it.hex }
	val jsonReprs = declarations.filterIsInstance<BsonJsonRepresentation>().map { it.json }
	val verifications = declarations.filterIsInstance<BsonAssertion>() + declarations.filterIsInstance<BsonSerializeDeclaration>().map { it.toAssertion() }

	require(hexReprs.isNotEmpty() || jsonReprs.isNotEmpty()) { "At least one of the 'hex()' or 'json()' functions should be part of the declarations, otherwise we do not have expected values for this test" }

	for (writer in writers) {
		for (hex in hexReprs) {
			test("Write to binary: $hex") {
				writer(context()) shouldBeHex hex
			}
		}

		for (json in jsonReprs) {
			test("Write to JSON: $json") {
				writer(context()) shouldBeJson json
			}
		}

		for (verification in verifications) {
			test("Write and verify that $verification") {
				val document = writer(context())
				verification.assert(document.reader())
			}
		}
	}

	for (hex in hexReprs) {
		for (json in jsonReprs) {
			test("Read $hex outputs the JSON $json") {
				context().readDocument(hex.hexToByteArray(HexFormat.UpperCase)) shouldBeJson json
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
