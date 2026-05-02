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

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.*

internal class KotlinPrimitiveByteCodec : Codec<Byte> {
	override fun getEncoderClass(): Class<Byte> =
		Byte::class.java

	override fun encode(writer: BsonWriter?, value: Byte?, encoderContext: EncoderContext?) {
		ByteCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Byte =
		ByteCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveShortCodec : Codec<Short> {
	override fun getEncoderClass(): Class<Short> =
		Short::class.java

	override fun encode(writer: BsonWriter?, value: Short?, encoderContext: EncoderContext?) {
		ShortCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Short =
		ShortCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveIntCodec : Codec<Int> {
	override fun getEncoderClass(): Class<Int> =
		Int::class.java

	override fun encode(writer: BsonWriter?, value: Int?, encoderContext: EncoderContext?) {
		IntegerCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Int =
		IntegerCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveLongCodec : Codec<Long> {
	override fun getEncoderClass(): Class<Long> =
		Long::class.java

	override fun encode(writer: BsonWriter?, value: Long?, encoderContext: EncoderContext?) {
		LongCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Long =
		LongCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveFloatCodec : Codec<Float> {
	override fun getEncoderClass(): Class<Float> =
		Float::class.java

	override fun encode(writer: BsonWriter?, value: Float?, encoderContext: EncoderContext?) {
		FloatCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Float =
		FloatCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveDoubleCodec : Codec<Double> {
	override fun getEncoderClass(): Class<Double> =
		Double::class.java

	override fun encode(writer: BsonWriter?, value: Double?, encoderContext: EncoderContext?) {
		DoubleCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Double =
		DoubleCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveBooleanCodec : Codec<Boolean> {
	override fun getEncoderClass(): Class<Boolean> =
		Boolean::class.java

	override fun encode(writer: BsonWriter?, value: Boolean?, encoderContext: EncoderContext?) {
		BooleanCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Boolean =
		BooleanCodec().decode(reader, decoderContext)
}

internal class KotlinPrimitiveCharCodec : Codec<Char> {
	override fun getEncoderClass(): Class<Char> =
		Char::class.java

	override fun encode(writer: BsonWriter?, value: Char?, encoderContext: EncoderContext?) {
		CharacterCodec().encode(writer, value, encoderContext)
	}

	override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Char =
		CharacterCodec().decode(reader, decoderContext)
}
