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

import kotlinx.io.Buffer
import kotlinx.io.readTo
import opensavvy.ktmongo.bson.*
import opensavvy.ktmongo.bson.Bson
import opensavvy.ktmongo.bson.BsonArray
import opensavvy.ktmongo.bson.BsonContext
import opensavvy.ktmongo.dsl.LowLevelApi

class BsonContext : BsonContext {

	@LowLevelApi
	private inline fun buildArbitraryTopLevel(
		block: MultiplatformBsonFieldWriter.() -> Unit,
	): Bytes {
		val buffer = Buffer()
		val bsonWriter = RawBsonWriter(buffer)
		val fieldWriter = MultiplatformBsonFieldWriter(bsonWriter)

		bsonWriter.writeInt32(0) // Document size. 0 for now, will be overwritten later.
		fieldWriter.block()
		bsonWriter.writeUnsignedByte(0u)

		check(buffer.size <= Int.MAX_VALUE) { "A BSON document cannot be larger than 16MiB. Found ${buffer.size} bytes." }
		val size = buffer.size.toInt()
		val bytes = ByteArray(size)
		buffer.readTo(bytes)
		// 'buffer' is now empty

		// Overwrite the size at the very start of the document
		bsonWriter.writeInt32(size)
		buffer.readTo(bytes, 0, 4)

		return Bytes(bytes)
	}

	@LowLevelApi
	override fun buildDocument(block: BsonFieldWriter.() -> Unit): Bson =
		buildArbitraryTopLevel {
			block(this)
		}.let(::Bson)

	@LowLevelApi
	override fun readDocument(bytes: ByteArray): Bson =
		Bson(Bytes(bytes.copyOf()))

	@LowLevelApi
	override fun buildArray(block: BsonValueWriter.() -> Unit): BsonArray =
		buildArbitraryTopLevel {
			block(MultiplatformBsonArrayFieldWriter(this))
		}.let(::BsonArray)

	@LowLevelApi
	override fun readArray(bytes: ByteArray): BsonArray =
		BsonArray(Bytes(bytes.copyOf()))

}
