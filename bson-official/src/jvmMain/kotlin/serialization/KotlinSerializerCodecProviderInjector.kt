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

package opensavvy.ktmongo.bson.official.serialization

import kotlinx.serialization.Serializable
import opensavvy.ktmongo.bson.official.BsonArray
import opensavvy.ktmongo.bson.official.BsonDocument
import opensavvy.ktmongo.bson.official.BsonFactory
import opensavvy.ktmongo.bson.official.BsonValue
import opensavvy.ktmongo.bson.official.serialization.KotlinSerializerCodecProviderInjector.isOfficialKotlinSerializationInClasspath
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider

// A @Serializable class that no standard MongoDB codec provider handles except KotlinSerializerCodecProvider.
// Used as a probe to detect whether the user's registry contains one.
@Serializable
private class KtMongoSerializationProbe

/**
 * Detects whether a [CodecRegistry] contains a [KotlinSerializerCodecProvider], and if so, prepends a new
 * [KotlinSerializerCodecProvider] configured with [ktMongoSerializersModule] so that KtMongo BSON types
 * ([BsonDocument], [BsonArray], [BsonValue]) are handled correctly when used as polymorphic fields.
 *
 * The user's original provider is not removed; it remains in the chain and handles types that are not in
 * [ktMongoSerializersModule]. If the user configured a custom [org.bson.codecs.kotlinx.BsonConfiguration],
 * it will not be inherited by the injected provider. Since the provider only handles
 * [BsonDocument], [BsonArray], [BsonValue], it would not configure anything meaningful anyway.
 *
 * This object is intentionally isolated from [BsonFactory] so that the JVM only loads
 * [KotlinSerializerCodecProvider] (a `compileOnly` dependency) when this object is first accessed —
 * which only happens when [isOfficialKotlinSerializationInClasspath] is true.
 */
internal object KotlinSerializerCodecProviderInjector {

	fun injectIfPresent(registry: CodecRegistry): CodecRegistry =
		if (!isOfficialKotlinSerializationInClasspath || !hasKotlinSerializerCodecProvider(registry))
			registry
		else CodecRegistries.fromRegistries(
			CodecRegistries.fromProviders(KotlinSerializerCodecProvider(ktMongoSerializersModule)),
			registry,
		)

	private val isOfficialKotlinSerializationInClasspath: Boolean = try {
		Class.forName("org.bson.codecs.kotlinx.BsonEncoder")
		true
	} catch (_: ClassNotFoundException) {
		false
	}

	private fun hasKotlinSerializerCodecProvider(registry: CodecRegistry): Boolean = try {
		registry.get(KtMongoSerializationProbe::class.java)
		true
	} catch (_: CodecConfigurationException) {
		false
	}
}
