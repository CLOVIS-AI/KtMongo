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

package opensavvy.ktmongo.bson.official.types

import opensavvy.ktmongo.bson.types.BooleanVector
import opensavvy.ktmongo.bson.types.ByteVector
import opensavvy.ktmongo.bson.types.FloatVector
import opensavvy.ktmongo.bson.types.Vector
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

// region Conversions

/**
 * Converts a KtMongo [Vector] to an official [BsonBinary].
 */
@OptIn(LowLevelApi::class)
fun Vector.toBinary(): BsonBinary = BsonBinary(
	0x9,
	toBinaryData(),
)

fun FloatVector.toOfficial(): Float32BinaryVector =
	BinaryVector.floatVector(toArray())

@OptIn(LowLevelApi::class)
fun BooleanVector.toOfficial(): PackedBitBinaryVector =
	BinaryVector.packedBitVector(this.raw, this.padding)

fun ByteVector.toOfficial(): Int8BinaryVector =
	BinaryVector.int8Vector(toArray())

/**
 * Converts an official [BsonBinary] to a KtMongo [Vector].
 */
@OptIn(LowLevelApi::class)
fun BsonBinary.toKtMongoVector(): Vector =
	Vector.fromBinaryData(this.data)

fun Float32BinaryVector.toKtMongo(): FloatVector =
	FloatVector(data.asList())

@OptIn(LowLevelApi::class)
fun PackedBitBinaryVector.toKtMongo(): BooleanVector =
	BooleanVector(this.data, this.padding)

fun Int8BinaryVector.toKtMongo(): ByteVector =
	ByteVector(data.asList())

// endregion
// region Codecs

internal class KotlinVectorCodec : Codec<Vector> {

	override fun getEncoderClass(): Class<Vector> =
		Vector::class.java

	override fun encode(writer: BsonWriter, value: Vector, context: EncoderContext) {
		writer.writeBinaryData(value.toBinary())
	}

	override fun decode(reader: BsonReader, context: DecoderContext): Vector? {
		return reader.readBinaryData()?.toKtMongoVector()
	}
}

internal class KotlinFloatVectorCodec : Codec<FloatVector> {

	override fun getEncoderClass(): Class<FloatVector> =
		FloatVector::class.java

	override fun encode(writer: BsonWriter, value: FloatVector, context: EncoderContext) {
		writer.writeBinaryData(value.toBinary())
	}

	override fun decode(reader: BsonReader, context: DecoderContext): FloatVector? {
		val vector = reader.readBinaryData()?.toKtMongoVector()
			?: return null

		check(vector is FloatVector) { "Expected to decode a ${FloatVector::class}, but found a ${vector::class}: $vector" }

		return vector
	}
}

internal class KotlinBooleanVectorCodec : Codec<BooleanVector> {

	override fun getEncoderClass(): Class<BooleanVector> =
		BooleanVector::class.java

	override fun encode(writer: BsonWriter, value: BooleanVector, context: EncoderContext) {
		writer.writeBinaryData(value.toBinary())
	}

	override fun decode(reader: BsonReader, context: DecoderContext): BooleanVector? {
		val vector = reader.readBinaryData()?.toKtMongoVector()
			?: return null

		check(vector is BooleanVector) { "Expected to decode a ${BooleanVector::class}, but found a ${vector::class}: $vector" }

		return vector
	}
}

internal class KotlinByteVectorCodec : Codec<ByteVector> {

	override fun getEncoderClass(): Class<ByteVector> =
		ByteVector::class.java

	override fun encode(writer: BsonWriter, value: ByteVector, context: EncoderContext) {
		writer.writeBinaryData(value.toBinary())
	}

	override fun decode(reader: BsonReader, context: DecoderContext): ByteVector? {
		val vector = reader.readBinaryData()?.toKtMongoVector()
			?: return null

		check(vector is ByteVector) { "Expected to decode a ${ByteVector::class}, but found a ${vector::class}: $vector" }

		return vector
	}
}

// endregion
