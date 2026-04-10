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

@file:OptIn(LowLevelApi::class)

package opensavvy.ktmongo.bson.official

import com.mongodb.*
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider
import com.mongodb.client.model.mql.ExpressionCodecProvider
import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.BsonDecodingException
import opensavvy.ktmongo.bson.decode
import opensavvy.ktmongo.bson.verifyBsonFactory
import opensavvy.ktmongo.dsl.LowLevelApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.prepared
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.Jsr310CodecProvider
import org.bson.codecs.kotlin.ArrayCodecProvider
import org.bson.codecs.kotlin.DataClassCodecProvider
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider

/**
 * Default codec providers copied from [com.mongodb.MongoClientSettings.getDefaultCodecRegistry],
 * but with the [KotlinCodecProvider] removed.
 *
 * Should not be used directly. See [reflectionFactory] and [serializationFactory].
 */
private val defaultCodecProvidersWithoutKotlin = CodecRegistries.fromProviders(
	ValueCodecProvider(),
	BsonValueCodecProvider(),
	DBRefCodecProvider(),
	DBObjectCodecProvider(),
	DocumentCodecProvider(DocumentToDBRefTransformer()),
	CollectionCodecProvider(DocumentToDBRefTransformer()),
	IterableCodecProvider(DocumentToDBRefTransformer()),
	MapCodecProvider(DocumentToDBRefTransformer()),
	GeoJsonCodecProvider(),
	GridFSFileCodecProvider(),
	Jsr310CodecProvider(),
	JsonObjectCodecProvider(),
	BsonCodecProvider(),
	ExpressionCodecProvider(),
	Jep395RecordCodecProvider(),
	// KotlinCodecProvider(), // Do NOT include the KotlinCodecProvider! It uses classpath scanning to choose a serialization library, but we want to control it
	EnumCodecProvider()
)

/**
 * A BSON factory that uses reflection to serialize and deserialize documents.
 *
 * This factory is the default for users of the official library `org.bson:bson-kotlin`.
 */
val reflectionFactory by prepared {
	val codecRegistry = CodecRegistries.fromProviders(
		ArrayCodecProvider(),
		DataClassCodecProvider(),
		defaultCodecProvidersWithoutKotlin,
	)

	BsonFactory(codecRegistry)
}

/**
 * A BSON factory that uses KotlinX.Serialization to serialize and deserialize documents.
 *
 * This factory is the default for users of the official library `org.bson:bson-kotlinx`.
 */
val serializationFactory by prepared {
	val codecRegistry = CodecRegistries.fromProviders(
		KotlinSerializerCodecProvider(),
		defaultCodecProvidersWithoutKotlin,
	)

	BsonFactory(codecRegistry)
}

/**
 * This class is marked [Serializable] but isn't a data class: it can be serialized
 * by the KotlinX.Serialization factory, but not by the reflection factory.
 */
@Serializable
private class OnlySerializableWithKxS(
	val name: String,
	val age: Int,
)

/**
 * This class is not marked with [Serializable] but is a data class: it can be serialized
 * by the reflection factory, but not by the KotlinX.Serialization factory.
 */
internal data class OnlySerializableWithReflection(
	val name: String,
	val age: Int,
)

/**
 * This class is both marked [Serializable] and is a data class: it can be serialized
 * using both factories.
 */
@Serializable
internal data class SerializableWithBoth(
	val name: String,
	val age: Int,
)

private fun createTestDocument(factory: BsonFactory): BsonDocument =
	factory.buildDocument {
		writeString("name", "Bob")
		writeInt32("age", 42)
	}

@OptIn(ExperimentalStdlibApi::class)
val OfficialJvmBsonFactory by preparedSuite {
	suite("Reflection-based") {
		val testDocument by prepared {
			createTestDocument(reflectionFactory())
		}

		test("Cannot serialize a @Serializable class") {
			val exception = checkThrows<BsonDecodingException> {
				check(testDocument().decode<OnlySerializableWithKxS>().name == "Bob")
			}

			check(exception.message == "Could not find codec for type opensavvy.ktmongo.bson.official.OnlySerializableWithKxS (class opensavvy.ktmongo.bson.official.OnlySerializableWithKxS)\nIf you're using org.bson:bson-kotlin, are you sure your type is a non-private data class?\nIf you're using org.bson:bson-kotlinx, did you annotate your type with @Serializable and configured the KotlinX.Serialization plugin?")
		}

		test("Can serialize a data class") {
			check(testDocument().decode<OnlySerializableWithReflection>().name == "Bob")
		}

		test("Can serialize a @Serializable data class") {
			check(testDocument().decode<SerializableWithBoth>().name == "Bob")
		}

		verifyBsonFactory(reflectionFactory)
	}

	suite("KotlinX.Serialization-based") {
		val testDocument by prepared {
			createTestDocument(serializationFactory())
		}

		test("Cannot serialize a plain data class") {
			val exception = checkThrows<BsonDecodingException> {
				check(testDocument().decode<OnlySerializableWithReflection>().name == "Bob")
			}

			check(exception.message == "Could not find codec for type opensavvy.ktmongo.bson.official.OnlySerializableWithReflection (class opensavvy.ktmongo.bson.official.OnlySerializableWithReflection)\nIf you're using org.bson:bson-kotlin, are you sure your type is a non-private data class?\nIf you're using org.bson:bson-kotlinx, did you annotate your type with @Serializable and configured the KotlinX.Serialization plugin?")
		}

		test("Can serialize a @Serializable class") {
			check(testDocument().decode<OnlySerializableWithKxS>().name == "Bob")
		}

		test("Can serialize a @Serializable data class") {
			check(testDocument().decode<SerializableWithBoth>().name == "Bob")
		}

		verifyBsonFactory(serializationFactory)
	}
}
