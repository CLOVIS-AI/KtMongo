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

package opensavvy.ktmongo.utils.kmongo

import kotlinx.serialization.SerialName
import opensavvy.ktmongo.bson.PropertyNameStrategy
import opensavvy.ktmongo.dsl.LowLevelApi
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.property.KPropertyPath
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

class KMongoNameStrategy(
	private val default: PropertyNameStrategy = PropertyNameStrategy.Default,
) : PropertyNameStrategy {
	@LowLevelApi
	override fun nameOf(property: KProperty1<*, *>): String {
		require(property !is KPropertyPath) { "Attempted to generate a KtMongo Field from a KMongo KPropertyPath instance, which is not supported yet. Please avoid mixing KtMongo and KMongo property syntax (/).\nProperty: $property" }

		val bsonId = property.javaField?.annotations?.filterIsInstance<BsonId>()?.firstOrNull()
		val serialName = property.findAnnotation<SerialName>()

		if (serialName != null)
			return serialName.value

		if (bsonId != null)
			return "_id"

		return default.nameOf(property)
	}
}
