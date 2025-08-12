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

package opensavvy.ktmongo.bson.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class StringFallbackEncoder(override val serializersModule: SerializersModule) : AbstractEncoder() {
	var out = ""
	override fun encodeValue(value: Any) { out = value.toString() }
}

class StringFallbackDecoder(override val serializersModule: SerializersModule, val content: String): AbstractDecoder() {
	override fun decodeValue(): Any = throw IllegalArgumentException("Not supported")
	override fun decodeString(): String {
		return content
	}

	override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw IllegalArgumentException("Not supported")
}