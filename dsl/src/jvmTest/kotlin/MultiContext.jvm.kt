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

package opensavvy.ktmongo.dsl

import com.mongodb.*
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider
import com.mongodb.client.model.mql.ExpressionCodecProvider
import opensavvy.ktmongo.bson.BsonFactory
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
 * Should not be used directly. See [testFactories].
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

actual val testFactories: Map<String, () -> BsonFactory> = mapOf(
	"JVM Official, reflection-based" to {
		opensavvy.ktmongo.bson.official.BsonFactory(
			codecRegistry = CodecRegistries.fromProviders(
				ArrayCodecProvider(),
				DataClassCodecProvider(),
				defaultCodecProvidersWithoutKotlin,
			)
		)
	},

	"JVM Official, serialization-based" to {
		opensavvy.ktmongo.bson.official.BsonFactory(
			codecRegistry = CodecRegistries.fromProviders(
				KotlinSerializerCodecProvider(),
				defaultCodecProvidersWithoutKotlin,
			)
		)
	},

	"Multiplatform" to {
		opensavvy.ktmongo.bson.multiplatform.BsonFactory()
	},
)
