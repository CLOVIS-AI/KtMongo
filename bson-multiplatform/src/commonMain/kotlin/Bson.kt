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

package opensavvy.ktmongo.bson.multiplatform

import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.BsonArrayReader
import opensavvy.ktmongo.bson.BsonDocumentReader
import opensavvy.ktmongo.bson.multiplatform.impl.read.MultiplatformArrayReader
import opensavvy.ktmongo.bson.multiplatform.impl.read.MultiplatformDocumentReader
import opensavvy.ktmongo.dsl.LowLevelApi

/**
 * Pure Kotlin BSON document implementation.
 *
 * ### Implementation
 *
 * Instead of decoding the BSON document into subdocuments, this class stores the raw bytes in BSON representation.
 * Fields are decoded lazily when searched via the [reader].
 */
class Bson internal constructor(
	private val factory: BsonFactory,
	private val data: Bytes,
) : Bson {

	@LowLevelApi
	override fun toByteArray(): ByteArray = data.toByteArray()

	@LowLevelApi
	private val reader by lazy(LazyThreadSafetyMode.PUBLICATION) {
		MultiplatformDocumentReader(factory, data)
	}

	@LowLevelApi
	override fun reader(): BsonDocumentReader = reader

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		reader().toString()
}

/**
 * Pure Kotlin BSON array implementation.
 *
 * ### Implementation
 *
 * Instead of decoding the BSON array into subdocuments, this class stores the raw bytes in BSON representation.
 * Fields are decoded lazily when searched via the [reader].
 */
class BsonArray internal constructor(
	private val factory: BsonFactory,
	private val data: Bytes,
) : BsonArray {

	@LowLevelApi
	override fun toByteArray(): ByteArray = data.toByteArray()

	@LowLevelApi
	private val reader by lazy(LazyThreadSafetyMode.PUBLICATION) {
		MultiplatformArrayReader(factory, data)
	}

	@LowLevelApi
	override fun reader(): BsonArrayReader = reader

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		reader().toString()
}
