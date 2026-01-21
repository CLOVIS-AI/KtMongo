/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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
 *
 * Because the reader is lazy, it is not thread-safe.
 * If you want to use this instance without external synchronization, call [eager].
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

	/**
	 * Scans this entire document recursively to find all the fields.
	 *
	 * By default, [Bson] lazily scans for fields.
	 * This is particularly beneficial if there is more data than you are interested in.
	 * However, this means the [reader] may discover fields as it is being used, which is not thread-safe.
	 *
	 * Instead, you can call this function to force a scan of the entire hierarchy.
	 * After this function returns, [reader] and all the values returned by it are thread-safe and immutable.
	 */
	@OptIn(LowLevelApi::class)
	fun eager() {
		reader.eager()
	}

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		reader().toString()

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		other is Bson && reader() == other.reader()

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		reader().hashCode()
}

/**
 * Pure Kotlin BSON array implementation.
 *
 * ### Implementation
 *
 * Instead of decoding the BSON array into subdocuments, this class stores the raw bytes in BSON representation.
 * Fields are decoded lazily when searched via the [reader].
 *
 * Because the reader is lazy, it is not thread-safe.
 * If you want to use this instance without external synchronization, call [eager].
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

	/**
	 * Scans this entire array recursively to find all the fields.
	 *
	 * By default, [BsonArray] lazily scans for items.
	 * This is particularly beneficial if there is more data than you are interested in.
	 * However, this means the [reader] may discover items as it is being used, which is not thread-safe.
	 *
	 * Instead, you can call this function to force a scan of the entire hierarchy.
	 * After this function returns, [reader] and all the values returned by it are thread-safe and immutable.
	 */
	@OptIn(LowLevelApi::class)
	fun eager() {
		reader.eager()
	}

	@OptIn(LowLevelApi::class)
	override fun toString(): String =
		reader().toString()

	@OptIn(LowLevelApi::class)
	override fun equals(other: Any?): Boolean =
		other is BsonArray && reader() == other.reader()

	@OptIn(LowLevelApi::class)
	override fun hashCode(): Int =
		reader().hashCode()
}
