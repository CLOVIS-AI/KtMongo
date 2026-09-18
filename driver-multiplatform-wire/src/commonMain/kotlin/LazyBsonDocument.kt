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

package opensavvy.ktmongo.multiplatform.wire

import kotlinx.io.Sink
import opensavvy.ktmongo.bson.BsonFieldWriteable
import opensavvy.ktmongo.bson.BsonFieldWriter
import opensavvy.ktmongo.bson.multiplatform.BsonDocument
import opensavvy.ktmongo.bson.multiplatform.BsonFactory
import opensavvy.ktmongo.dsl.DangerousMongoApi
import opensavvy.ktmongo.dsl.LowLevelApi

sealed class LazyBsonDocument : BsonFieldWriteable {

	abstract val factory: BsonFactory

	abstract fun toBson(): BsonDocument

	abstract fun writeTo(destination: Sink)

	final override fun toString(): String =
		toBson().toString()
}

private class ConcreteDocument(
	private val doc: BsonDocument,
) : LazyBsonDocument() {

	override val factory: BsonFactory
		get() = doc.factory

	override fun toBson(): BsonDocument =
		doc

	@DangerousMongoApi
	override fun writeTo(writer: BsonFieldWriter) = with(writer) {
		for (field in doc) {
			write(field.name) {
				pipe(field.value)
			}
		}
	}

	override fun writeTo(destination: Sink) {
		destination.write(doc.toByteArray())
	}
}

private class LazyDocument(
	override val factory: BsonFactory,
	private val builder: BsonFieldWriter.() -> Unit,
) : LazyBsonDocument() {

	override fun writeTo(writer: BsonFieldWriter) =
		builder(writer)

	@DangerousMongoApi
	override fun writeTo(destination: Sink) {
		factory.writeDocumentTo(destination, builder)
	}

	override fun toBson(): BsonDocument =
		factory.buildDocument { writeTo(this) }
}

fun LazyBsonDocument(
	doc: BsonDocument,
): LazyBsonDocument = ConcreteDocument(doc)

fun LazyBsonDocument(
	factory: BsonFactory,
	builder: BsonFieldWriter.() -> Unit,
): LazyBsonDocument = LazyDocument(factory, builder)
