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

package opensavvy.ktmongo.bson.official.types

import opensavvy.ktmongo.bson.types.ObjectId
import opensavvy.ktmongo.bson.types.ObjectIdGenerator
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.ObjectIdCodec
import kotlin.time.ExperimentalTime

// region Conversions

// Yes, this byte array is wasted memory and GC pressure.
// It is necessary because the Java driver doesn't provide a way to access the nonce
// more efficiently.

/**
 * Converts an ObjectId from the Java driver into a KtMongo ObjectId.
 */
@ExperimentalTime
fun ObjectId.toOfficial(): org.bson.types.ObjectId =
	org.bson.types.ObjectId(bytes)

/**
 * Converts an ObjectId from KtMongo into one from the Java driver.
 */
@ExperimentalTime
fun org.bson.types.ObjectId.toKtMongo(): ObjectId =
	ObjectId(toByteArray())

// endregion
// region Generator

private object JvmObjectIdGenerator : ObjectIdGenerator {
	@ExperimentalTime
	override fun newId(): ObjectId = org.bson.types.ObjectId().toKtMongo()
}

/**
 * An [ObjectIdGenerator] instance that uses the Java driver's [org.bson.types.ObjectId]'s algorithm.
 *
 * This generation algorithm is slightly different from [ObjectIdGenerator.Default].
 * Here are a few differences:
 *
 * |                                       | ObjectIdGenerator.Jvm        | ObjectIdGenerator.Default |
 * |---------------------------------------|------------------------------|----|
 * | Maximum number of ObjectId per second | ≈16 million                  | ≈1 billion billion |
 * | Random source                         | [java.security.SecureRandom] | [kotlin.random.Random] |
 * | Testability                           | None                         | Can inject a clock and a random source to deterministically generate tests |
 */
@Suppress("FunctionName", "GrazieInspection")
fun ObjectIdGenerator.Companion.Jvm(): ObjectIdGenerator =
	JvmObjectIdGenerator

// endregion
// region Codec

@OptIn(ExperimentalTime::class)
internal class KotlinObjectIdCodec : Codec<ObjectId> {
	private val objCodec = ObjectIdCodec()

	override fun encode(writer: BsonWriter?, value: ObjectId, encoderContext: EncoderContext?) {
		objCodec.encode(writer, value.toOfficial(), encoderContext)
	}

	override fun getEncoderClass(): Class<ObjectId> =
		ObjectId::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): ObjectId? =
		objCodec.decode(reader, decoderContext)?.toKtMongo()

}

// endregion
