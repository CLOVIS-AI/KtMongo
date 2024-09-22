/*
 * Copyright (c) 2024, OpenSavvy and contributors.
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

package opensavvy.ktmongo.dsl.expr

import opensavvy.ktmongo.bson.BsonContext
import org.bson.codecs.AtomicBooleanCodec
import org.bson.codecs.AtomicIntegerCodec
import org.bson.codecs.AtomicLongCodec
import org.bson.codecs.BigDecimalCodec
import org.bson.codecs.BinaryCodec
import org.bson.codecs.BooleanCodec
import org.bson.codecs.BsonArrayCodec
import org.bson.codecs.BsonBinaryCodec
import org.bson.codecs.BsonBooleanCodec
import org.bson.codecs.BsonDBPointerCodec
import org.bson.codecs.BsonDateTimeCodec
import org.bson.codecs.BsonDecimal128Codec
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.BsonDoubleCodec
import org.bson.codecs.BsonInt32Codec
import org.bson.codecs.BsonInt64Codec
import org.bson.codecs.BsonJavaScriptCodec
import org.bson.codecs.BsonMaxKeyCodec
import org.bson.codecs.BsonMinKeyCodec
import org.bson.codecs.BsonNullCodec
import org.bson.codecs.BsonObjectIdCodec
import org.bson.codecs.BsonRegularExpressionCodec
import org.bson.codecs.BsonStringCodec
import org.bson.codecs.BsonSymbolCodec
import org.bson.codecs.BsonTimestampCodec
import org.bson.codecs.BsonUndefinedCodec
import org.bson.codecs.BsonValueCodec
import org.bson.codecs.ByteArrayCodec
import org.bson.codecs.ByteCodec
import org.bson.codecs.CharacterCodec
import org.bson.codecs.CodeCodec
import org.bson.codecs.DateCodec
import org.bson.codecs.Decimal128Codec
import org.bson.codecs.DocumentCodec
import org.bson.codecs.DoubleCodec
import org.bson.codecs.FloatCodec
import org.bson.codecs.IntegerCodec
import org.bson.codecs.JsonObjectCodec
import org.bson.codecs.LongCodec
import org.bson.codecs.MaxKeyCodec
import org.bson.codecs.MinKeyCodec
import org.bson.codecs.ObjectIdCodec
import org.bson.codecs.OverridableUuidRepresentationUuidCodec
import org.bson.codecs.PatternCodec
import org.bson.codecs.RawBsonDocumentCodec
import org.bson.codecs.ShortCodec
import org.bson.codecs.StringCodec
import org.bson.codecs.SymbolCodec
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.jsr310.LocalTimeCodec

actual fun testContext(): BsonContext = BsonContext(
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
