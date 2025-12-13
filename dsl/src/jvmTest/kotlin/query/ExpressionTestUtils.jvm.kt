/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.query

import opensavvy.ktmongo.bson.official.JvmBsonFactory
import opensavvy.ktmongo.dsl.BsonContext
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.jsr310.LocalTimeCodec
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalAtomicApi::class, ExperimentalTime::class)
actual fun testContext(): BsonContext = BsonContext(
	bsonFactory = JvmBsonFactory(
		codecRegistry = CodecRegistries.fromCodecs(
			AtomicBooleanCodec(),
			AtomicIntegerCodec(),
			AtomicLongCodec(),
			BigDecimalCodec(),
			BinaryCodec(),
			BooleanCodec(),
			BsonArrayCodec(),
			BsonBinaryCodec(),
			BsonBooleanCodec(),
			BsonDateTimeCodec(),
			BsonDBPointerCodec(),
			BsonDecimal128Codec(),
			BsonDocumentCodec(),
			BsonDoubleCodec(),
			BsonInt32Codec(),
			BsonInt64Codec(),
			BsonJavaScriptCodec(),
			BsonMaxKeyCodec(),
			BsonMinKeyCodec(),
			BsonNullCodec(),
			BsonObjectIdCodec(),
			BsonRegularExpressionCodec(),
			BsonStringCodec(),
			BsonSymbolCodec(),
			BsonTimestampCodec(),
			BsonUndefinedCodec(),
			BsonValueCodec(),
			ByteArrayCodec(),
			ByteCodec(),
			CharacterCodec(),
			CodeCodec(),
			DateCodec(),
			Decimal128Codec(),
			DocumentCodec(),
			DoubleCodec(),
			FloatCodec(),
			InstantCodec(),
			IntegerCodec(),
			JsonObjectCodec(),
			LocalDateCodec(),
			LocalDateTimeCodec(),
			LocalTimeCodec(),
			LongCodec(),
			MaxKeyCodec(),
			MinKeyCodec(),
			ObjectIdCodec(),
			OverridableUuidRepresentationUuidCodec(),
			PatternCodec(),
			RawBsonDocumentCodec(),
			ShortCodec(),
			StringCodec(),
			SymbolCodec(),
			UuidCodec(),
		)
	)
)
