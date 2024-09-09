package fr.qsh.ktmongo.dsl.expr

import io.kotest.matchers.shouldBe
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.jsr310.LocalTimeCodec

fun testCodec(): CodecRegistry = CodecRegistries.fromCodecs(
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

infix fun String.shouldBeBson(expected: String) {
	this shouldBe expected
		.replace("\n", "")
		.replace("\t", "")
		.replace(",", ", ")
		.replace("  ", " ")
}
